package de.cubeisland.cubeengine.core.storage.database;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines the tableName and/or engine/defaultCharset for a single-integer-key
 * model.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SingleKeyEntity
{
    public String tableName();

    public String engine() default "InnoDB";

    public String charset() default "utf8";

    /**
     * only use with unsigned int!
     */
    public boolean autoIncrement();

    public String primaryKey();

    public Index[] indices() default {};
}
