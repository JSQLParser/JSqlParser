/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2020 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.validation.metadata;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.UUID;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.util.validation.ValidationTestAsserts;
import net.sf.jsqlparser.util.validation.feature.DatabaseType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DatabaseMetaDataValidationTest extends ValidationTestAsserts {

    private Connection connection;
    private String databaseName;

    @BeforeEach
    public void setupDatabase() throws SQLException {
        databaseName = "testdb_" + Math.abs(UUID.randomUUID().hashCode());
        connection = DriverManager.getConnection("jdbc:h2:mem:" + databaseName);
        connection
                .prepareStatement(
                        "CREATE TABLE mytable (id bigint, ref bigint, description varchar(100), active boolean);")
                .execute();
        connection.prepareStatement("CREATE TABLE mysecondtable (id bigint, description varchar(100), active boolean);")
                .execute();
        connection.prepareStatement("CREATE VIEW myview AS SELECT * FROM mytable").execute();
    }

    @Test
    public void testValidationAlterTable() throws JSQLParserException, SQLException {
        String sql = "ALTER TABLE mytable ADD price numeric(10,5) not null";
        JdbcDatabaseMetaDataCapability meta = new JdbcDatabaseMetaDataCapability(connection, NamesLookup.UPPERCASE);
        validateNoErrors(sql, 1, DatabaseType.H2, meta); // no errors
        connection.prepareStatement(sql).execute();
        validateMetadata(sql, 1, 1, meta.clearCache(), false, "price"); // column exists
    }

    @Test
    public void testValidationAlterTableAlterColumn() throws JSQLParserException, SQLException {
        String sql = "ALTER TABLE mytable ALTER COLUMN description SET NOT NULL";
        JdbcDatabaseMetaDataCapability meta = new JdbcDatabaseMetaDataCapability(connection, NamesLookup.UPPERCASE);
        validateNoErrors(sql, 1, DatabaseType.H2, meta); // no errors
    }

    @Test
    public void testValidationMetadataInsert() throws JSQLParserException, SQLException {
        String sql = "INSERT INTO mytable (id, description, active) VALUES (1, 'test', 1)";
        JdbcDatabaseMetaDataCapability meta = new JdbcDatabaseMetaDataCapability(connection, NamesLookup.UPPERCASE);
        validateNoErrors(sql, 1, DatabaseType.H2, meta); // no errors
    }

    @Test
    public void testValidationMetadataSelectWithColumnsAndAlias() throws JSQLParserException, SQLException {
        String sql = "SELECT * FROM mytable t JOIN mysecondtable t2 WHERE t.ref = t2.id AND t.id = ?";
        JdbcDatabaseMetaDataCapability meta = new JdbcDatabaseMetaDataCapability(connection, NamesLookup.UPPERCASE);
        validateNoErrors(sql, 1, DatabaseType.H2, meta); // no errors
    }

    @Test
    public void testValidationMetadataUpdate() throws JSQLParserException, SQLException {
        String sql = "UPDATE mytable t SET t.ref = 2 WHERE t.id = 1";
        JdbcDatabaseMetaDataCapability meta = new JdbcDatabaseMetaDataCapability(connection, NamesLookup.UPPERCASE);
        validateNoErrors(sql, 1, DatabaseType.H2, meta); // no errors
    }

    @Test
    public void testValidationMetadataDelete() throws JSQLParserException, SQLException {
        String sql = "DELETE FROM mytable t WHERE t.id = 1 and ref = 2";
        JdbcDatabaseMetaDataCapability meta = new JdbcDatabaseMetaDataCapability(connection, NamesLookup.UPPERCASE);
        validateNoErrors(sql, 1, DatabaseType.H2, meta); // no errors
    }

    @Test
    public void testValidationMetadataDeleteError() throws JSQLParserException, SQLException {
        String sql = "DELETE FROM mytable t WHERE t.id = 1 and x.ref = 2";
        JdbcDatabaseMetaDataCapability meta = new JdbcDatabaseMetaDataCapability(connection, NamesLookup.UPPERCASE);
        validateMetadata(sql, 1, 1, meta, true, "x.ref");
    }

    @Test
    public void testValidationMetadataSelectWithColumns() throws JSQLParserException, SQLException {
        String sql = "SELECT * FROM mytable JOIN mysecondtable WHERE mytable.ref = mysecondtable.id AND mysecondtable.id = ?";
        JdbcDatabaseMetaDataCapability meta = new JdbcDatabaseMetaDataCapability(connection, NamesLookup.UPPERCASE);
        validateNoErrors(sql, 1, DatabaseType.H2, meta); // no errors
    }

    @Test
    public void testValidationMetadataSelectWithoutColumns() throws JSQLParserException, SQLException {
        String sql = String.format("SELECT * FROM %s.public.mytable", databaseName);
        JdbcDatabaseMetaDataCapability meta = new JdbcDatabaseMetaDataCapability(connection, NamesLookup.UPPERCASE);
        validateNoErrors(sql, 1, DatabaseType.H2, meta);
        sql = String.format("SELECT * FROM public.mytable", databaseName);
        validateNoErrors(sql, 1, DatabaseType.H2, meta.clearCache());
        sql = String.format("SELECT public.mytable.id FROM mytable", databaseName);
        validateNoErrors(sql, 1, DatabaseType.H2, meta.clearCache());
    }

    @Test
    public void testValidationDropView3Parts() throws JSQLParserException, SQLException {
        String sql = String.format("DROP VIEW %s.public.myview", databaseName);
        JdbcDatabaseMetaDataCapability meta = new JdbcDatabaseMetaDataCapability(connection, NamesLookup.UPPERCASE,
                false);
        validateNoErrors(sql, 1, DatabaseType.H2, meta); // no errors
    }

    @Test
    public void testValidationDropView2Parts() throws JSQLParserException, SQLException {
        String sql = "DROP VIEW public.myview";
        JdbcDatabaseMetaDataCapability meta = new JdbcDatabaseMetaDataCapability(connection, NamesLookup.UPPERCASE,
                false);
        validateNoErrors(sql, 1, DatabaseType.H2, meta); // no errors
    }

    @Test
    public void testValidationDropViewDoesNotExist() throws JSQLParserException, SQLException {
        String sql = "DROP VIEW public.anotherView";
        JdbcDatabaseMetaDataCapability meta = new JdbcDatabaseMetaDataCapability(connection, NamesLookup.UPPERCASE,
                false);
        // view does not exist
        validateMetadata(sql, 1, 1, meta, true, String.format("public.anotherView", databaseName));
    }

    @Test
    public void testValidationMetadataSelectWithColumnsAndAlias2() throws JSQLParserException, SQLException {
        String sql = "select my.id from mytable as my";
        JdbcDatabaseMetaDataCapability meta = new JdbcDatabaseMetaDataCapability(connection, NamesLookup.UPPERCASE);
        validateNoErrors(sql, 1, DatabaseType.H2, meta); // no errors
    }
}
