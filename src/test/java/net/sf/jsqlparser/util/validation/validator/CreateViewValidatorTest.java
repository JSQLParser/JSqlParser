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

public class CreateViewValidatorTest extends ValidationTestAsserts {

    @Test
    public void testValidateCreateView() throws JSQLParserException {
        for (String sql : Arrays.asList("CREATE VIEW myview AS SELECT * FROM mytab",
                "CREATE VIEW myview AS (SELECT * FROM mytab)")) {
            validateNoErrors(sql, 1, DatabaseType.DATABASES);
        }
    }

    @Test
    public void testValidateCreateViewNotAllowed() throws JSQLParserException {
        for (String sql : Arrays.asList("CREATE VIEW myview AS SELECT * FROM mytab",
                "CREATE VIEW myview AS (SELECT * FROM mytab)")) {
            validateNotAllowed(sql, 1, 1, FeaturesAllowed.DML, Feature.createView);
        }
    }

    @Test
    public void testValidateCreateViewMaterialized() throws JSQLParserException {
        validateNoErrors("CREATE MATERIALIZED VIEW myview AS SELECT * FROM mytab", 1, DatabaseType.ORACLE);
    }

    @Test
    public void testValidateCreateOrReplaceView() throws JSQLParserException {
        validateNoErrors("CREATE OR REPLACE VIEW myview AS SELECT * FROM mytab", 1, DatabaseType.ORACLE,
                DatabaseType.POSTGRESQL, DatabaseType.MYSQL, DatabaseType.MARIADB, DatabaseType.H2);
    }

    @Test
    public void testValidateCreateForceView() throws JSQLParserException {
        validateNoErrors("CREATE FORCE VIEW myview AS SELECT * FROM mytab", 1, DatabaseType.ORACLE, DatabaseType.H2);
    }

    @Test
    public void testValidateCreateTemporaryView() throws JSQLParserException {
        for (String sql : Arrays.asList("CREATE TEMPORARY VIEW myview AS SELECT * FROM mytab",
                "CREATE TEMP VIEW myview AS SELECT * FROM mytab")) {
            validateNoErrors(sql, 1, DatabaseType.POSTGRESQL);
        }
    }

    @Test
    public void testValidateCreateViewWith() throws JSQLParserException {
        for (String sql : Arrays.asList(
                "CREATE VIEW foo(\"BAR\") AS WITH temp AS (SELECT temp_bar FROM foobar) SELECT bar FROM temp")) {
            validateNoErrors(sql, 1, DatabaseType.DATABASES);
        }
    }
}
