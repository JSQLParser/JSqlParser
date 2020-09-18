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
import org.junit.Before;
import org.junit.Test;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.util.validation.ValidationTestAsserts;
import net.sf.jsqlparser.util.validation.feature.DatabaseType;
import net.sf.jsqlparser.util.validation.metadata.AbstractDatabaseMetaDataCapability.NamesLookup;

public class DatabaseMetaDataValidationTest extends ValidationTestAsserts {

    private Connection connection;

    @Before
    public void setupDatabase() throws SQLException {
        connection = DriverManager.getConnection("jdbc:h2:mem:");
        connection.prepareStatement("create table mytable (id bigint, description varchar(100), active boolean);")
        .execute();
    }

    @Test
    public void testValidationAlterTable() throws JSQLParserException, SQLException {
        String sql = "ALTER TABLE mytable ADD price numeric(10,5) not null";
        JdbcDatabaseMetaDataCapability meta = new JdbcDatabaseMetaDataCapability(connection, NamesLookup.UPPERCASE);
        validateNoErrors(sql, 1, DatabaseType.H2, meta); // no errors
        connection.prepareStatement(sql).execute();
        validateMetadata(sql, 1, 1, meta.clearCache(), false, "mytable.price"); // column exists
    }



}
