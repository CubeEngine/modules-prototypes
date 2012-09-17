package de.cubeisland.cubeengine.core.storage.database.mysql;

import de.cubeisland.cubeengine.core.storage.database.querybuilder.QueryBuilder;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.UpdateBuilder;
import de.cubeisland.cubeengine.core.util.Validate;

/**
 *
 * @author Anselm Brehme
 */
public class MySQLUpdateBuilder extends MySQLConditionalBuilder<UpdateBuilder> implements UpdateBuilder
{
    private boolean hasCols;

    protected MySQLUpdateBuilder(MySQLQueryBuilder parent)
    {
        super(parent);
    }

    @Override
    public MySQLUpdateBuilder tables(String... tables)
    {
        Validate.notEmpty(tables, "No tables specified!");

        this.hasCols = false;
        this.query = new StringBuilder("UPDATE ");
        this.query.append(this.database.prepareName(tables[0]));
        for (int i = 1; i < tables.length; ++i)
        {
            this.query.append(',').append(this.database.prepareName(tables[i]));
        }
        return this;
    }

    @Override
    public MySQLUpdateBuilder cols(String... cols)
    {
        Validate.notEmpty(cols, "No cols specified!");

        this.query.append(" SET ").append(this.database.prepareFieldName(cols[0])).append("=? ");
        for (int i = 1; i < cols.length; ++i)
        {
            this.query.append(',').append(this.database.prepareFieldName(cols[i])).append("=? ");
        }

        this.hasCols = true;

        return this;
    }

    @Override
    public QueryBuilder end()
    {
        if (!this.hasCols)
        {
            throw new IllegalStateException("No cols where specified!");
        }
        return super.end();
    }
}