/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cubeengine.module.stats;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.cubeisland.engine.converter.ConversionException;
import de.cubeisland.engine.converter.ConverterManager;
import de.cubeisland.engine.module.core.module.Module;
import de.cubeisland.engine.module.core.storage.database.Database;
import org.cubeengine.module.stats.annotations.Configured;
import org.cubeengine.module.stats.annotations.Scheduled;
import org.cubeengine.module.stats.configuration.DynamicSection;
import org.cubeengine.module.stats.stat.Stat;
import org.cubeengine.module.stats.storage.StatsModel;
import org.cubeengine.module.stats.storage.TableStats;
import org.cubeengine.module.stats.storage.TableStatsData;
import org.cubeengine.service.task.TaskManager;
import de.cubeisland.engine.logscribe.Log;
import de.cubeisland.engine.reflect.annotations.Comment;
import de.cubeisland.engine.reflect.annotations.Name;
import org.jooq.DSLContext;

public class StatsManager
{
    private final Stats module;
    private final DSLContext dsl;
    private final Log log;
    private final ConverterManager converterManager;

    private final ObjectMapper jsonMapper;
    private final Map<Class<? extends Stat>, Stat> stats;
    private final Set<Class<? extends Stat>> registeredStats;
    private final Map<Class<? extends Stat>, Set<Integer>> tasks;

    private boolean started = false;

    public StatsManager(Stats stats, ConverterManager converterManager)
    {
        this.module = stats;
        this.log = stats.getLog();
        this.converterManager = converterManager;

        final Database database = stats.getCore().getDB();
        this.dsl = database.getDSL();
        database.registerTable(TableStats.class);
        database.registerTable(TableStatsData.class);

        this.jsonMapper = new ObjectMapper();
        this.stats = new HashMap<>();
        this.registeredStats = new HashSet<>();
        this.tasks = new HashMap<>();

        for (StatsModel stat : dsl.selectFrom(TableStats.TABLE_STATS).fetch())
        {
            final String className = stat.getValue(TableStats.TABLE_STATS.STAT);
            try
            {
                final Class statClass = Class.forName(className);
                if (Stat.class.isAssignableFrom(statClass))
                {
                    registeredStats.add((Class<? extends Stat>)statClass);
                }
                else
                {
                    stats.getLog().warn("The statistic class {} is not compatible anymore.", className);
                }
            }
            catch (ClassNotFoundException e)
            {
                stats.getLog().warn(e, "The previously existing statistic class {} could not be loaded.", className);
            }
        }

        this.started = true;
    }

    /**
     * Register a statistic.
     * This will make this StatsManager handle the statistic's database connection
     * and register it as a listener.
     *
     * @param statType The stat class to register
     */
    public void register(Class< ? extends Stat> statType)
    {
        if (!this.started)
        {
            throw new IllegalStateException("StatsManager not started!");
        }

        try
        {
            // Get the Constructor, and construct a new instance of the Stat.
            Constructor<? extends Stat> constructor = statType.getConstructor();
            Stat stat = constructor.newInstance();
            Method init = statType.getMethod("init", this.getClass(), Module.class);
            init.invoke(stat, this, this.module);

            // Get or register the stat in the database
            if (!registeredStats.contains(statType))
            {
                synchronized (this.dsl)
                {
                    StatsModel model = this.dsl.newRecord(TableStats.TABLE_STATS).newStatsModel(statType.getName());
                    model.insert();
                    registeredStats.add(statType);
                }
            }
            this.stats.put(statType, stat);

            // Load configured fields

            for (Field field : statType.getFields())
            {
                if (!field.isAnnotationPresent(Configured.class))
                {
                    return;
                }

                String name = field.getName();
                String[] comment = {};
                if (field.isAnnotationPresent(Name.class))
                {
                    Name nameAnnotation = field.getAnnotation(Name.class);
                    name = nameAnnotation.value();
                }
                if (field.isAnnotationPresent(Comment.class))
                {
                    Comment commentAnnotation = field.getAnnotation(Comment.class);
                    comment = commentAnnotation.value();
                }

                if (!module.getConfig().statConfigs.containsKey(statType.getSimpleName()))
                {
                    module.getConfig().statConfigs.put(statType.getSimpleName(), new DynamicSection(converterManager));
                }
                DynamicSection section = module.getConfig().statConfigs.get(statType.getSimpleName());

                if (section.hasKey(name))
                {
                    section.getNode(name).setComments(comment);
                    field.set(stat, section.get(name));
                }
                else
                {
                    section.put(name, field.get(stat), comment);
                }
            }

            // Activate hook in the stat
            stat.activate();

            // Register Schedulers
            for (Method method : stat.getClass().getMethods())
            {
                if (!method.isAnnotationPresent(Scheduled.class))
                {
                    continue;
                }
                Scheduled annotation = method.getAnnotation(Scheduled.class);

                long interval;
                if (annotation.periodFinal())
                {
                    interval = annotation.interval();
                }
                else
                {
                    if (!module.getConfig().statConfigs.containsKey(statType.getSimpleName()))
                    {
                        module.getConfig().statConfigs.put(statType.getSimpleName(), new DynamicSection(converterManager));
                    }
                    DynamicSection section = module.getConfig().statConfigs.get(statType.getSimpleName());
                    if (!section.hasKey("tasks"))
                    {
                        section.put("tasks", new DynamicSection(converterManager), "Intervals for the tasks this statistic schedules");
                    }
                    else
                    {
                        section.getNode("tasks").setComments(new String[]{"Intervals for the tasks this statistic schedules"});
                    }
                    DynamicSection tasks = (DynamicSection)section.get("tasks", DynamicSection.class);
                    if (!tasks.hasKey(annotation.name()))
                    {
                        tasks.put(annotation.name(), annotation.interval(), annotation.comment());
                    }
                    else
                    {
                        tasks.getNode(annotation.name()).setComments(annotation.comment());
                    }
                    interval = (Long)tasks.get(annotation.name(), Long.class);
                }

                if (!this.tasks.containsKey(statType))
                {
                    this.tasks.put(statType, new HashSet<Integer>());
                }
                Set<Integer> tasks = this.tasks.get(statType);
                Runnable wrapper = new ScheduledMethod(this.module.getLog(), stat, method);
                TaskManager taskManager = module.getCore().getTaskManager();
                if (annotation.async())
                {
                    tasks.add(taskManager.runAsynchronousTimer(module, wrapper, 1, interval));
                }
                else
                {
                    tasks.add(taskManager.runTimer(module, wrapper, 1, interval));
                }

                this.module.getLog().debug("Scheduled method {} at interval {}", annotation.name(), interval);
            }
        }
        catch (ReflectiveOperationException | ConversionException ex)
        {
            this.module.getLog().error(ex, "An error occurred while registering statistic");
        }
        this.module.getLog().debug("Registered statistic {}.", statType.getSimpleName());
    }

    public void disableStat(Class<? extends Stat> stat)
    {
        if (!this.started)
        {
            throw new IllegalStateException("StatsManager not started!");
        }
        if (this.tasks.containsKey(stat))
        {
            for (Integer id : tasks.get(stat))
            {
                this.module.getCore().getTaskManager().cancelTask(this.module, id);
            }
        }
        this.stats.get(stat).deactivate();
    }

    /**
     * Save an object to the database
     *
     * The object must be able to be parsed through ObjectMapper.writeValue()
     *
     * @param stat The stat that owns this data
     * @param object The data to save
     */
    public void save(Stat stat, Object object)
    {
        if (!this.started)
        {
            throw new IllegalStateException("StatsManager not started!");
        }
        synchronized (this.dsl)
        {
            try
            {
                String data = this.jsonMapper.writeValueAsString(object);
                int result = this.dsl.insertInto(TableStatsData.TABLE_STATSDATA)
                    .set(TableStatsData.TABLE_STATSDATA.STAT, dsl.select(TableStats.TABLE_STATS.KEY)
                           .from(TableStats.TABLE_STATS)
                           .where(TableStats.TABLE_STATS.STAT.eq(stat.getClass().getName())).getQuery())
                    .set(TableStatsData.TABLE_STATSDATA.DATA, data).execute();
                if (result != 1)
                {
                    log.warn("Failed to insert a row in the data table for statistics!");
                    log.debug("Result int from jOOQ was {}", result);
                }
            }
            catch (JsonProcessingException ex)
            {
                log.warn(ex, "An error occurred while parsing an object to JSON.");
            }
        }
    }

    public void shutdown()
    {
        for (Class<? extends Stat> stat : this.registeredStats)
        {
            this.disableStat(stat);
        }
        this.started = false;
    }

    private static final class ScheduledMethod implements Runnable
    {
        private final Log logger;
        private final Object object;
        private final Method method;

        ScheduledMethod(Log logger, Object object, Method method)
        {
            this.logger = logger;
            this.object = object;
            this.method = method;
        }

        public void run()
        {
            try
            {
                method.invoke(object);
            }
            catch (IllegalAccessException | InvocationTargetException ex)
            {
                logger.warn(ex, "An error occurred while invoking a scheduled method");
            }
        }
    }
}
