package net.sf.jsqlparser.util.validation.validator;

import org.junit.Test;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.util.validation.ValidationTestAsserts;
import net.sf.jsqlparser.util.validation.feature.DatabaseType;
import net.sf.jsqlparser.util.validation.feature.FeaturesAllowed;

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

}
