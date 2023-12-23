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

import java.util.Arrays;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.util.validation.ValidationTestAsserts;
import net.sf.jsqlparser.util.validation.feature.DatabaseType;
import net.sf.jsqlparser.util.validation.feature.FeaturesAllowed;
import org.junit.jupiter.api.Test;

public class StatementValidatorTest extends ValidationTestAsserts {

    @Test
    public void testValidateCreateSchema() throws JSQLParserException {
        for (String sql : Arrays.asList("CREATE SCHEMA my_schema",
                "CREATE SCHEMA myschema AUTHORIZATION myauth")) {
            validateNoErrors(sql, 1, DatabaseType.DATABASES);
        }
    }

    @Test
    public void testValidateCreateSchemaNotAllowed() throws JSQLParserException {
        for (String sql : Arrays.asList("CREATE SCHEMA my_schema",
                "CREATE SCHEMA myschema AUTHORIZATION myauth")) {
            validateNotAllowed(sql, 1, 1, FeaturesAllowed.DML, Feature.createSchema);
        }
    }

    @Test
    public void testValidateDescNoErrors() throws JSQLParserException {
        for (String sql : Arrays.asList("DESC table_name", "EXPLAIN table_name")) {
            validateNoErrors(sql, 1, DatabaseType.MYSQL);
        }
    }

    @Test
    public void testValidateTruncate() throws JSQLParserException {
        validateNoErrors("TRUNCATE TABLE my_table", 1, DatabaseType.DATABASES);
    }

    @Test
    public void testValidateCommit() throws JSQLParserException {
        validateNoErrors("COMMIT", 1, DatabaseType.DATABASES);
    }

    @Test
    public void testValidateBlock() throws JSQLParserException {
        validateNoErrors("BEGIN UPDATE tab SET val = 1 WHERE col = 2; END;", 1, DatabaseType.ORACLE,
                DatabaseType.SQLSERVER);
    }

    @Test
    public void testValidateComment() throws JSQLParserException {
        for (String sql : Arrays.asList("COMMENT ON VIEW myschema.myView IS 'myComment'",
                "COMMENT ON COLUMN myTable.myColumn is 'Some comment'",
                "COMMENT ON TABLE table1 IS 'comment1'")) {
            validateNoErrors(sql, 1, DatabaseType.H2, DatabaseType.ORACLE, DatabaseType.POSTGRESQL);
        }
    }

}
