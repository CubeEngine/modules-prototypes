package de.cubeisland.cubeengine.core.storage.database.querybuilder;

/**
 *
 * @author Phillip Schichtel
 */
public interface UpdateBuilder extends ConditionalBuilder<UpdateBuilder>
{
    public UpdateBuilder tables(String... tables);
    public UpdateBuilder cols(String... cols);
}