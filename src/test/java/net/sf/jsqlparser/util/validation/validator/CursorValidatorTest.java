/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.validation.validator;

import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.util.validation.ValidationTestAsserts;
import net.sf.jsqlparser.util.validation.feature.DatabaseType;
import net.sf.jsqlparser.util.validation.feature.FeaturesAllowed;
import net.sf.jsqlparser.util.validation.feature.MySqlVersion;
import net.sf.jsqlparser.util.validation.feature.PostgresqlVersion;
import net.sf.jsqlparser.util.validation.feature.SqlServerVersion;
import org.junit.jupiter.api.Test;

public class CursorValidatorTest extends ValidationTestAsserts {

    @Test
    public void allowedForPostgresql() {
        for (String sql : new String[] {"DECLARE c SCROLL CURSOR WITH HOLD FOR SELECT a FROM t",
                "DECLARE c CURSOR FOR VALUES (1)", "FETCH FORWARD 5 FROM c", "MOVE LAST IN c",
                "CLOSE c", "CLOSE ALL"}) {
            validateNoErrors(sql, 1, PostgresqlVersion.V10, PostgresqlVersion.V14,
                    DatabaseType.POSTGRESQL);
        }
    }

    @Test
    public void notSupportedByMySqlAndSqlServer() {
        validateNotSupported("DECLARE c CURSOR FOR SELECT 1", 1, 1, MySqlVersion.V8_0,
                Feature.declareCursor);
        validateNotSupported("FETCH NEXT FROM c", 1, 1, MySqlVersion.V8_0, Feature.fetchCursor);
        validateNotSupported("MOVE c", 1, 1, SqlServerVersion.V2019, Feature.fetchCursor);
        validateNotSupported("CLOSE ALL", 1, 1, MySqlVersion.V8_0, Feature.closeCursor);
    }

    @Test
    public void featureNotAllowed() {
        validateNotAllowed("DECLARE c CURSOR FOR SELECT a FROM t", 1, 1, FeaturesAllowed.SELECT,
                Feature.declareCursor);
        validateNotAllowed("FETCH ALL c", 1, 1, FeaturesAllowed.SELECT, Feature.fetchCursor);
        validateNotAllowed("MOVE c", 1, 1, FeaturesAllowed.DML, Feature.fetchCursor);
        validateNotAllowed("CLOSE c", 1, 1, FeaturesAllowed.DDL, Feature.closeCursor);
    }

    @Test
    public void validatesTheQueryOfTheCursor() {
        // the query is validated like a SELECT statement
        validateNotAllowed("DECLARE c CURSOR FOR SELECT a FROM t", 1, 1,
                new FeaturesAllowed(Feature.declareCursor), Feature.select);
    }
}
