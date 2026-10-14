/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2020 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.validation.validator;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.util.validation.ValidationTestAsserts;
import net.sf.jsqlparser.util.validation.feature.DatabaseType;
import net.sf.jsqlparser.util.validation.feature.FeaturesAllowed;
import net.sf.jsqlparser.util.validation.feature.MySqlVersion;
import net.sf.jsqlparser.util.validation.feature.PostgresqlVersion;
import org.junit.jupiter.api.Test;

public class DropValidatorTest extends ValidationTestAsserts {

    @Test
    public void testValidationDrop() throws JSQLParserException {
        String sql = "DROP TABLE tab1; DROP TABLE tab2;";
        validateNoErrors(sql, 2, DatabaseType.DATABASES);
    }

    @Test
    public void testValidationDropNotAllowed() throws JSQLParserException {
        String sql = "DROP VIEW myview";
        validateNotAllowed(sql, 1, 1, FeaturesAllowed.DML, Feature.drop, Feature.dropView);
    }

    @Test
    public void testValidationDropIfExists() throws JSQLParserException {
        String sql = "DROP TABLE IF EXISTS tab2;";
        validateNoErrors(sql, 1, DatabaseType.MYSQL, DatabaseType.SQLSERVER, DatabaseType.MARIADB,
                DatabaseType.POSTGRESQL, DatabaseType.H2);
    }

    @Test
    public void testValidationDropIndexIfExists() throws JSQLParserException {
        String sql = "DROP INDEX IF EXISTS idx_tab2_id;";
        validateNoErrors(sql, 1, DatabaseType.SQLSERVER, DatabaseType.MARIADB,
                DatabaseType.POSTGRESQL, DatabaseType.H2);
    }

    @Test
    public void testValidationDropViewIfExists() throws JSQLParserException {
        String sql = "DROP VIEW IF EXISTS myview;";
        validateNoErrors(sql, 1, DatabaseType.MYSQL, DatabaseType.SQLSERVER, DatabaseType.MARIADB,
                DatabaseType.POSTGRESQL, DatabaseType.H2);
    }

    @Test
    public void testValidationDropSchemaIfExists() throws JSQLParserException {
        String sql = "DROP SCHEMA IF EXISTS myschema;";
        validateNoErrors(sql, 1, DatabaseType.MYSQL, DatabaseType.SQLSERVER, DatabaseType.MARIADB,
                DatabaseType.POSTGRESQL, DatabaseType.H2);
    }

    @Test
    public void testValidationDropSequenceIfExists() throws JSQLParserException {
        String sql = "DROP SEQUENCE IF EXISTS mysequence;";
        validateNoErrors(sql, 1, DatabaseType.SQLSERVER, DatabaseType.MARIADB,
                DatabaseType.POSTGRESQL, DatabaseType.H2);
        validateNotSupported(sql, 1, 1, DatabaseType.MYSQL, Feature.dropSequence);
    }

    @Test
    public void testValidationDropRoutinesAndAccounts() throws JSQLParserException {
        validateNoErrors("DROP FUNCTION IF EXISTS f(int), g(text) CASCADE", 1,
                DatabaseType.POSTGRESQL);
        validateNoErrors("DROP OWNED BY old_role CASCADE", 1, DatabaseType.POSTGRESQL);
        validateNoErrors("DROP USER IF EXISTS 'app'@'%', u2", 1, DatabaseType.MYSQL,
                DatabaseType.MARIADB);
    }

    @Test
    public void testValidationReassignOwned() throws JSQLParserException {
        String sql = "REASSIGN OWNED BY old_role TO new_role";
        validateNoErrors(sql, 1, PostgresqlVersion.V14, DatabaseType.POSTGRESQL);
        validateNotSupported(sql, 1, 1, MySqlVersion.V8_0, Feature.reassignOwned);
        validateNotAllowed(sql, 1, 1, FeaturesAllowed.DDL, Feature.reassignOwned);
    }

}
