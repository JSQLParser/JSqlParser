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

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.util.validation.ValidationTestAsserts;
import net.sf.jsqlparser.util.validation.feature.DatabaseType;
import net.sf.jsqlparser.util.validation.feature.FeaturesAllowed;
import net.sf.jsqlparser.util.validation.feature.MySqlVersion;
import net.sf.jsqlparser.util.validation.feature.PostgresqlVersion;
import org.junit.jupiter.api.Test;

public class PostgreSqlSessionStatementValidatorTest extends ValidationTestAsserts {

    private static final String[] STATEMENTS = {"DISCARD ALL", "DISCARD TEMP", "LISTEN ch",
            "UNLISTEN *", "SHOW TIME ZONE", "SHOW TRANSACTION ISOLATION LEVEL",
            "SHOW SESSION AUTHORIZATION", "SHOW search_path", "RESET SESSION AUTHORIZATION",
            "COPY t TO STDOUT CSV HEADER FORCE QUOTE *", "COPY BINARY t FROM STDIN"};

    @Test
    public void testAllowedForPostgresql() throws JSQLParserException {
        for (String sql : STATEMENTS) {
            validateNoErrors(sql, 1, PostgresqlVersion.V10, PostgresqlVersion.V14,
                    DatabaseType.POSTGRESQL);
        }
    }

    @Test
    public void testSessionStatementsNotAllowedForMySql() throws JSQLParserException {
        validate("DISCARD PLANS", 1, 1, MySqlVersion.V8_0);
        validate("LISTEN ch", 1, 1, MySqlVersion.V8_0);
        validate("UNLISTEN ch", 1, 1, MySqlVersion.V8_0);
    }

    @Test
    public void testFeatureNotAllowed() throws JSQLParserException {
        validateNotAllowed("DISCARD SEQUENCES", 1, 1, FeaturesAllowed.SELECT, Feature.discard);
        validateNotAllowed("LISTEN ch", 1, 1, FeaturesAllowed.SELECT, Feature.listen);
        validateNotAllowed("UNLISTEN *", 1, 1, FeaturesAllowed.SELECT, Feature.listen);
        validateNotAllowed("SHOW TIME ZONE", 1, 1, FeaturesAllowed.SELECT, Feature.show);
        validateNotAllowed("COPY t TO STDOUT CSV", 1, 1, FeaturesAllowed.SELECT, Feature.copy);
    }
}
