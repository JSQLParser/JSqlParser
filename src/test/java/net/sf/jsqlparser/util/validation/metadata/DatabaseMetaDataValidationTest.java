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
import org.junit.Before;
import org.junit.Test;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.util.validation.ValidationTestAsserts;
import net.sf.jsqlparser.util.validation.feature.DatabaseType;

public class DatabaseMetaDataValidationTest extends ValidationTestAsserts {

    private Connection connection;
    private String databaseName;

    @Before
    public void setupDatabase() throws SQLException {
        databaseName = "testdb_" + Math.abs(UUID.randomUUID().hashCode());
        connection = DriverManager.getConnection("jdbc:h2:mem:" + databaseName);
        connection.prepareStatement("CREATE TABLE mytable (id bigint, description varchar(100), active boolean);")
        .execute();
        connection.prepareStatement("CREATE VIEW myview AS SELECT * FROM mytable").execute();
    }

    @Test
    public void testValidationAlterTable() throws JSQLParserException, SQLException {
        String sql = "ALTER TABLE mytable ADD price numeric(10,5) not null";
        JdbcDatabaseMetaDataCapability meta = new JdbcDatabaseMetaDataCapability(connection, NamesLookup.UPPERCASE);
        validateNoErrors(sql, 1, DatabaseType.H2, meta); // no errors
        connection.prepareStatement(sql).execute();
        validateMetadata(sql, 1, 1, meta.clearCache(), false, "mytable.price"); // column exists
    }

    @Test
    public void testValidationAlterTableAlterColumn() throws JSQLParserException, SQLException {
        String sql = "ALTER TABLE mytable ALTER COLUMN description SET NOT NULL";
        JdbcDatabaseMetaDataCapability meta = new JdbcDatabaseMetaDataCapability(connection, NamesLookup.UPPERCASE);
        validateNoErrors(sql, 1, DatabaseType.H2, meta); // no errors
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

}
