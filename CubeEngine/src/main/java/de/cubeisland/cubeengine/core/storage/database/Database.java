package de.cubeisland.cubeengine.core.storage.database;

import de.cubeisland.cubeengine.core.storage.Storage;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.QueryBuilder;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author Phillip Schichtel
 */
public interface Database
{
    public String getName();
    public String prepareName(String name);
    public String prepareFieldName(String name);
    public String prepareString(String name);
    public QueryBuilder getQueryBuilder();
    public PreparedStatement bindValues(PreparedStatement statement, Object... params) throws SQLException;
    public PreparedStatement createAndBindValues(String query, Object... params) throws SQLException;
    public boolean execute(String query, Object... params) throws SQLException;
    public PreparedStatement getStoredStatement(Class owner, String name);
    public void prepareAndStoreStatement(Class owner, String name, String statement) throws SQLException;
    public PreparedStatement prepareStatement(String statement) throws SQLException;
    public boolean preparedExecute(Class owner, String name, Object... params) throws SQLException;
    public ResultSet preparedQuery(Class owner, String name, Object... params) throws SQLException;
    public int preparedUpdate(Class owner, String name, Object... params) throws SQLException;
    public ResultSet query(String query, Object... params) throws SQLException;
    public void storePreparedStatement(Class owner, String name, PreparedStatement statement);
    public int update(String query, Object... params) throws SQLException;
    public int getLastInsertedId(Class owner, String name, Object... params) throws SQLException;
    public void startTransaction() throws SQLException;
    public void commmit() throws SQLException;
    public void rollback() throws SQLException;
    public void update(Storage manager);
}