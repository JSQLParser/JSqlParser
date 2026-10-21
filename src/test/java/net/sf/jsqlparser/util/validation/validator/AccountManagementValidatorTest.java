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

import java.util.Map;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.util.validation.ValidationTestAsserts;
import net.sf.jsqlparser.util.validation.feature.FeaturesAllowed;
import net.sf.jsqlparser.util.validation.feature.MariaDbVersion;
import net.sf.jsqlparser.util.validation.feature.MySqlVersion;
import net.sf.jsqlparser.util.validation.feature.PostgresqlVersion;
import org.junit.jupiter.api.Test;

public class AccountManagementValidatorTest extends ValidationTestAsserts {

    private static final Map<String, Feature> STATEMENTS = Map.of(
            "ALTER USER 'app'@'%' IDENTIFIED BY 'x' ACCOUNT LOCK", Feature.alterUser,
            "RENAME USER 'a'@'%' TO 'b'@'%'", Feature.renameUser,
            "SET ROLE ALL EXCEPT 'writer'", Feature.setRole,
            "SET DEFAULT ROLE ALL TO 'app'@'%'", Feature.setDefaultRole,
            "SET PASSWORD FOR 'app'@'%' = 'x'", Feature.setPassword,
            "CREATE ROLE IF NOT EXISTS 'reader'", Feature.createRole);

    @Test
    public void testAllowedForMySqlAndMariaDb() throws JSQLParserException {
        for (String sql : STATEMENTS.keySet()) {
            validateNoErrors(sql, 1, MySqlVersion.V8_0, MariaDbVersion.V10_5_4);
        }
        validateNoErrors("CREATE USER 'app'@'%' REQUIRE SSL PASSWORD EXPIRE NEVER", 1,
                MySqlVersion.V8_0, MariaDbVersion.V10_5_4);
    }

    @Test
    public void testMySqlAccountStatementsNotAllowedForPostgresql() throws JSQLParserException {
        validate("ALTER USER 'app'@'%' ACCOUNT LOCK", 1, 1, PostgresqlVersion.V14);
        validate("SET DEFAULT ROLE ALL TO app", 1, 1, PostgresqlVersion.V14);
    }

    @Test
    public void testFeatureNotAllowed() throws JSQLParserException {
        for (Map.Entry<String, Feature> entry : STATEMENTS.entrySet()) {
            validateNotAllowed(entry.getKey(), 1, 1, FeaturesAllowed.DDL, entry.getValue());
        }
    }
}
