/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.user;

import static net.sf.jsqlparser.test.TestUtils.assertDeparse;
import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Collectors;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StmtFeature;
import net.sf.jsqlparser.statement.create.user.PasswordLockOption.Kind;
import net.sf.jsqlparser.statement.create.user.PasswordLockOption.Setting;
import net.sf.jsqlparser.statement.role.CreateRole;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CreateUserTest {

    /** Parses with the dialect, checks both renderers, then reparses the rendered SQL. */
    static <T extends Statement> T roundTrip(Class<T> type, String sql, Dialect dialect)
            throws JSQLParserException {
        T statement = assertInstanceOf(type, assertSqlCanBeParsedAndDeparsed(sql, false,
                parser -> parser.withDialect(dialect)));
        Statement reparsed =
                CCJSqlParserUtil.parse(statement.toString(), parser -> parser.withDialect(dialect));
        assertInstanceOf(type, reparsed);
        assertEquals(sql, reparsed.toString());
        return statement;
    }

    private static List<String> names(List<UserAccount> accounts) {
        return accounts.stream().map(UserAccount::toString).collect(Collectors.toList());
    }

    @Test
    void parsesAccountAndPasswordAsStructuredValues() throws JSQLParserException {
        String sql = "CREATE USER IF NOT EXISTS 'replicator' IDENTIFIED BY 'replpass'";
        CreateUser createUser =
                assertInstanceOf(CreateUser.class, CCJSqlParserUtil.parse(sql));

        assertTrue(createUser.isIfNotExists());
        assertEquals(1, createUser.getAccounts().size());
        UserAccount account = createUser.getAccounts().get(0);
        assertEquals("replicator", account.getUser().getValue());
        assertNull(account.getHost());
        assertEquals(UserAuthentication.Mode.BY_PASSWORD, account.getAuthentication().getMode());
        assertEquals("replpass", account.getAuthentication().getCredential().getValue());
        assertSqlCanBeParsedAndDeparsed(sql);
    }

    @Test
    void parsesHostAndOptionalAuthentication() throws JSQLParserException {
        String sql = "CREATE USER IF NOT EXISTS 'snapshot'@'%'";
        CreateUser createUser =
                assertInstanceOf(CreateUser.class, assertSqlCanBeParsedAndDeparsed(sql));

        UserAccount account = createUser.getAccounts().get(0);
        assertEquals("snapshot", account.getUser().getValue());
        assertEquals("%", account.getHost().getValue());
        assertNull(account.getAuthentication());
    }

    @Test
    void parsesMultipleAccountsAndAuthenticationPlugin() throws JSQLParserException {
        String sql = "CREATE USER alice@localhost IDENTIFIED WITH caching_sha2_password "
                + "BY 'secret', 'service'@'10.%' IDENTIFIED BY RANDOM PASSWORD";
        CreateUser createUser =
                assertInstanceOf(CreateUser.class, assertSqlCanBeParsedAndDeparsed(sql));

        assertFalse(createUser.isIfNotExists());
        assertEquals(2, createUser.getAccounts().size());
        UserAuthentication pluginAuthentication =
                createUser.getAccounts().get(0).getAuthentication();
        assertEquals(UserAuthentication.Mode.WITH_PLUGIN_BY_PASSWORD,
                pluginAuthentication.getMode());
        assertEquals("caching_sha2_password", pluginAuthentication.getPlugin());
        assertEquals("secret", pluginAuthentication.getCredential().getValue());
        assertEquals(UserAuthentication.Mode.BY_RANDOM_PASSWORD,
                createUser.getAccounts().get(1).getAuthentication().getMode());
    }

    @Test
    void parsesSharedOptionsInClauseOrder() throws JSQLParserException {
        String sql = "CREATE USER IF NOT EXISTS 'app'@'10.0.0.%' IDENTIFIED WITH "
                + "caching_sha2_password BY 'secret' DEFAULT ROLE 'reader' REQUIRE SSL "
                + "WITH MAX_QUERIES_PER_HOUR 100 PASSWORD EXPIRE INTERVAL 90 DAY "
                + "FAILED_LOGIN_ATTEMPTS 3 PASSWORD_LOCK_TIME 2 ACCOUNT LOCK COMMENT 'app user'";
        CreateUser createUser = roundTrip(CreateUser.class, sql, Dialect.MYSQL);

        UserOptions options = createUser.getOptions();
        assertEquals(RoleSelection.Kind.ROLES, options.getDefaultRoles().getKind());
        assertEquals(List.of("'reader'"), names(options.getDefaultRoles().getRoles()));
        assertEquals(TlsOption.Kind.SSL, options.getTlsOptions().get(0).getKind());
        ResourceLimit limit = options.getResourceLimits().get(0);
        assertEquals(ResourceLimit.Kind.MAX_QUERIES_PER_HOUR, limit.getKind());
        assertEquals(100, limit.getValue());
        assertThat(options.getPasswordLockOptions()).extracting(PasswordLockOption::getKind)
                .containsExactly(Kind.PASSWORD_EXPIRE, Kind.FAILED_LOGIN_ATTEMPTS,
                        Kind.PASSWORD_LOCK_TIME, Kind.ACCOUNT_LOCK);
        assertThat(options.getPasswordLockOptions()).extracting(PasswordLockOption::getValue)
                .containsExactly(90L, 3L, 2L, null);
        assertEquals("app user", options.getComment().getValue());
        assertNull(options.getAttribute());
    }

    @Test
    void parsesPasswordSettingsAndRepeatedOptions() throws JSQLParserException {
        String sql = "CREATE USER app PASSWORD EXPIRE NEVER PASSWORD EXPIRE DEFAULT "
                + "PASSWORD EXPIRE PASSWORD HISTORY DEFAULT PASSWORD HISTORY 5 "
                + "PASSWORD REUSE INTERVAL DEFAULT PASSWORD REUSE INTERVAL 365 DAY "
                + "PASSWORD REQUIRE CURRENT PASSWORD REQUIRE CURRENT DEFAULT "
                + "PASSWORD REQUIRE CURRENT OPTIONAL PASSWORD_LOCK_TIME UNBOUNDED "
                + "ACCOUNT UNLOCK ACCOUNT LOCK";
        List<PasswordLockOption> options = roundTrip(CreateUser.class, sql, Dialect.MYSQL)
                .getOptions().getPasswordLockOptions();

        assertThat(options).extracting(PasswordLockOption::getKind).containsExactly(
                Kind.PASSWORD_EXPIRE, Kind.PASSWORD_EXPIRE, Kind.PASSWORD_EXPIRE,
                Kind.PASSWORD_HISTORY, Kind.PASSWORD_HISTORY, Kind.PASSWORD_REUSE_INTERVAL,
                Kind.PASSWORD_REUSE_INTERVAL, Kind.PASSWORD_REQUIRE_CURRENT,
                Kind.PASSWORD_REQUIRE_CURRENT, Kind.PASSWORD_REQUIRE_CURRENT,
                Kind.PASSWORD_LOCK_TIME, Kind.ACCOUNT_UNLOCK, Kind.ACCOUNT_LOCK);
        assertThat(options).extracting(PasswordLockOption::getSetting).containsExactly(
                Setting.NEVER, Setting.DEFAULT, null, Setting.DEFAULT, null, Setting.DEFAULT,
                null, null, Setting.DEFAULT, Setting.OPTIONAL, Setting.UNBOUNDED, null, null);
        assertEquals(5L, options.get(4).getValue());
        assertEquals(365L, options.get(6).getValue());
    }

    @Test
    void parsesTlsOptionsWithOptionalAnd() throws JSQLParserException {
        String sql = "CREATE USER 'app'@'%' REQUIRE CIPHER 'EDH-RSA-DES-CBC3-SHA' "
                + "AND ISSUER '/C=SE/O=MySQL' SUBJECT '/CN=app'";
        List<TlsOption> options =
                roundTrip(CreateUser.class, sql, Dialect.MYSQL).getOptions().getTlsOptions();

        assertThat(options).extracting(TlsOption::getKind).containsExactly(TlsOption.Kind.CIPHER,
                TlsOption.Kind.ISSUER, TlsOption.Kind.SUBJECT);
        assertThat(options).extracting(TlsOption::isAndKeyword).containsExactly(false, true,
                false);
        assertEquals("/CN=app", options.get(2).getValue().getValue());

        assertEquals(TlsOption.Kind.X509,
                roundTrip(CreateUser.class, "CREATE USER app REQUIRE X509", Dialect.MYSQL)
                        .getOptions().getTlsOptions().get(0).getKind());
        assertEquals(TlsOption.Kind.NONE,
                roundTrip(CreateUser.class, "CREATE USER app REQUIRE NONE", Dialect.MYSQL)
                        .getOptions().getTlsOptions().get(0).getKind());
    }

    @Test
    void parsesResourceLimitsAndAttribute() throws JSQLParserException {
        String sql = "CREATE USER app DEFAULT ROLE r1, 'r2'@'%' WITH MAX_QUERIES_PER_HOUR 10 "
                + "MAX_UPDATES_PER_HOUR 5 MAX_CONNECTIONS_PER_HOUR 2 MAX_USER_CONNECTIONS 1 "
                + "ATTRIBUTE '{\"team\": \"ops\"}'";
        UserOptions options = roundTrip(CreateUser.class, sql, Dialect.MYSQL).getOptions();

        assertEquals(List.of("r1", "'r2'@'%'"), names(options.getDefaultRoles().getRoles()));
        assertThat(options.getResourceLimits()).extracting(ResourceLimit::getKind)
                .containsExactly(ResourceLimit.Kind.MAX_QUERIES_PER_HOUR,
                        ResourceLimit.Kind.MAX_UPDATES_PER_HOUR,
                        ResourceLimit.Kind.MAX_CONNECTIONS_PER_HOUR,
                        ResourceLimit.Kind.MAX_USER_CONNECTIONS);
        assertEquals("{\"team\": \"ops\"}", options.getAttribute().getValue());
        assertNull(options.getComment());
    }

    @Test
    void parsesMariaDbAccountOptions() throws JSQLParserException {
        CreateUser createUser = roundTrip(CreateUser.class,
                "CREATE USER 'app'@'%' IDENTIFIED BY 'x' REQUIRE SSL WITH MAX_USER_CONNECTIONS 5 "
                        + "ACCOUNT LOCK PASSWORD EXPIRE INTERVAL 30 DAY",
                Dialect.MARIADB);

        assertEquals(2, createUser.getOptions().getPasswordLockOptions().size());
    }

    @Test
    void mySqlOptionsAfterAnUnquotedNameSelectCreateUser() throws JSQLParserException {
        for (String sql : List.of("CREATE USER app WITH MAX_USER_CONNECTIONS 1",
                "CREATE USER app PASSWORD EXPIRE", "CREATE USER app PASSWORD HISTORY 2",
                "CREATE USER app ACCOUNT LOCK", "CREATE USER app COMMENT 'c'")) {
            assertInstanceOf(CreateUser.class, CCJSqlParserUtil.parse(sql), sql);
        }
        // PostgreSQL role attributes keep the PostgreSQL CREATE USER alias
        assertInstanceOf(CreateRole.class, CCJSqlParserUtil.parse("CREATE USER app WITH LOGIN"));
        assertInstanceOf(CreateRole.class, CCJSqlParserUtil.parse("CREATE USER app PASSWORD 'x'"));
        assertInstanceOf(CreateRole.class, CCJSqlParserUtil.parse("CREATE USER app",
                parser -> parser.withDialect(Dialect.POSTGRESQL)));
    }

    @Test
    void omittedOptionsRenderNothing() throws JSQLParserException {
        CreateUser createUser = roundTrip(CreateUser.class, "CREATE USER app", Dialect.MYSQL);

        assertTrue(createUser.getOptions().isEmpty());
        createUser.setOptions(null);
        assertTrue(createUser.getOptions().isEmpty());
        assertEquals("CREATE USER app", createUser.toString());
    }

    @Test
    void buildsOptions() {
        CreateUser createUser = new CreateUser()
                .addAccounts(new UserAccount().withUser(new StringValue("app"))
                        .withHost(new StringValue("%")))
                .withOptions(new UserOptions()
                        .addTlsOptions(new TlsOption(TlsOption.Kind.SUBJECT,
                                new StringValue("/CN=app")))
                        .addResourceLimits(
                                new ResourceLimit(ResourceLimit.Kind.MAX_USER_CONNECTIONS, 3))
                        .addPasswordLockOptions(
                                new PasswordLockOption(Kind.PASSWORD_EXPIRE).withValue(30L),
                                new PasswordLockOption(Kind.ACCOUNT_LOCK))
                        .withComment(new StringValue("svc")));

        assertDeparse(createUser, "CREATE USER 'app'@'%' REQUIRE SUBJECT '/CN=app' "
                + "WITH MAX_USER_CONNECTIONS 3 PASSWORD EXPIRE INTERVAL 30 DAY ACCOUNT LOCK "
                + "COMMENT 'svc'");
        assertEquals("REQUIRE SUBJECT '/CN=app' WITH MAX_USER_CONNECTIONS 3 "
                + "PASSWORD EXPIRE INTERVAL 30 DAY ACCOUNT LOCK COMMENT 'svc'",
                createUser.getOptions().toString());
    }

    @Test
    void reportsSchemaChangeWithoutTables() throws JSQLParserException {
        Statement statement = CCJSqlParserUtil.parse("CREATE USER app REQUIRE SSL ACCOUNT LOCK");

        assertThat(statement.getFeatures().getCertain())
                .containsExactly(StmtFeature.MODIFIES_SCHEMA);
        assertThat(TablesNamesFinder.findTables(statement.toString())).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "CREATE USER app DEFAULT ROLE ALL",
            "CREATE USER app DEFAULT ROLE NONE",
            "CREATE USER app REQUIRE AND SSL",
            "CREATE USER app REQUIRE CIPHER",
            "CREATE USER app REQUIRE SSL 'x'",
            "CREATE USER app REQUIRE NONE AND SSL",
            "CREATE USER app PASSWORD EXPIRE INTERVAL 5 MONTH",
            "CREATE USER app PASSWORD HISTORY",
            "CREATE USER app COMMENT 'x' ATTRIBUTE '{}'",
            "CREATE USER app ACCOUNT LOCK REQUIRE SSL",
            "CREATE USER app IDENTIFIED BY 'x' REPLACE 'y'",
            "CREATE USER app IDENTIFIED BY 'x' RETAIN CURRENT PASSWORD",
            "CREATE USER app DISCARD OLD PASSWORD"
    })
    void rejectsOptionsOutsideTheGrammar(String sql) {
        assertThrows(JSQLParserException.class,
                () -> CCJSqlParserUtil.parse(sql, parser -> parser.withDialect(Dialect.MYSQL)));
    }

    @Test
    void optionWordsRemainIdentifiers() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT account, require, expire, reuse, optional, retain, "
                + "old, unlock, ssl, x509, cipher, issuer, subject, attribute, "
                + "failed_login_attempts, password_lock_time, max_user_connections FROM t");
    }
}
