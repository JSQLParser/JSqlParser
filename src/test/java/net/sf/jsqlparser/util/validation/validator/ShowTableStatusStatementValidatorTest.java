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
import net.sf.jsqlparser.util.validation.feature.MariaDbVersion;
import net.sf.jsqlparser.util.validation.feature.MySqlVersion;
import net.sf.jsqlparser.util.validation.feature.PostgresqlVersion;
import org.junit.jupiter.api.Test;

public class ShowTableStatusStatementValidatorTest extends ValidationTestAsserts {

    @Test
    public void testAllowedForMySqlAndMariaDb() throws JSQLParserException {
        for (String sql : new String[] {"SHOW TABLE STATUS", "SHOW TABLE STATUS LIKE 'a%'",
                "SHOW TABLE STATUS IN db WHERE Comment <> ''"}) {
            validateNoErrors(sql, 1, MySqlVersion.V8_0, MariaDbVersion.V10_5_4,
                    DatabaseType.MYSQL, DatabaseType.MARIADB);
        }
    }

    @Test
    public void testNotAllowedForPostgresql() throws JSQLParserException {
        validate("SHOW TABLE STATUS", 1, 1, PostgresqlVersion.V14);
    }

    @Test
    public void testFeatureNotAllowed() throws JSQLParserException {
        validateNotAllowed("SHOW TABLE STATUS FROM db", 1, 1, FeaturesAllowed.DML,
                Feature.showTableStatus);
    }
}
