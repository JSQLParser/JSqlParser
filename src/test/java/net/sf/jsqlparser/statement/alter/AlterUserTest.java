/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.alter;

import static net.sf.jsqlparser.test.TestUtils.assertDeparse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StmtFeature;
import net.sf.jsqlparser.statement.create.user.PasswordLockOption;
import net.sf.jsqlparser.statement.create.user.RoleSelection;
import net.sf.jsqlparser.statement.create.user.TlsOption;
import net.sf.jsqlparser.statement.create.user.UserAccount;
import net.sf.jsqlparser.statement.create.user.UserAuthentication;
import net.sf.jsqlparser.statement.create.user.UserAuthentication.Mode;
import net.sf.jsqlparser.statement.create.user.UserOptions;
import net.sf.jsqlparser.statement.role.AlterRole;
import net.sf.jsqlparser.test.TestUtils;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class AlterUserTest {

    private static AlterUser parse(String sql, Dialect dialect) throws JSQLParserException {
        AlterUser alterUser = assertInstanceOf(AlterUser.class,
                TestUtils.assertSqlCanBeParsedAndDeparsed(sql, false,
                        parser -> parser.withDialect(dialect)));
        Statement reparsed = CCJSqlParserUtil.parse(alterUser.toString(),
                parser -> parser.withDialect(dialect));
        assertEquals(sql, assertInstanceOf(AlterUser.class, reparsed).toString());
        return alterUser;
    }

    private static AlterUser parse(String sql) throws JSQLParserException {
        return parse(sql, Dialect.MYSQL);
    }

    private static List<String> names(List<UserAccount> accounts) {
        return accounts.stream().map(UserAccount::toString).collect(Collectors.toList());
    }

    @Test
    void retainsCurrentPassword() throws JSQLParserException {
        AlterUser alterUser =
                parse("ALTER USER 'app'@'%' IDENTIFIED BY 'newsecret' RETAIN CURRENT PASSWORD");

        UserAccount account = alterUser.getAccounts().get(0);
        assertEquals("app", account.getUser().getValue());
        assertEquals("%", account.getHost().getValue());
        UserAuthentication authentication = account.getAuthentication();
        assertEquals(Mode.BY_PASSWORD, authentication.getMode());
        assertEquals("newsecret", authentication.getCredential().getValue());
        assertTrue(authentication.isRetainCurrentPassword());
        assertNull(authentication.getCurrentCredential());
        assertTrue(alterUser.getOptions().isEmpty());
        assertFalse(alterUser.isIfExists());
        assertFalse(alterUser.isUserFunction());
    }

    @Test
    void replacesCurrentPassword() throws JSQLParserException {
        UserAuthentication authentication = parse("ALTER USER 'app'@'%' IDENTIFIED WITH "
                + "caching_sha2_password BY 'x' REPLACE 'old' RETAIN CURRENT PASSWORD")
                .getAccounts().get(0).getAuthentication();

        assertEquals(Mode.WITH_PLUGIN_BY_PASSWORD, authentication.getMode());
        assertEquals("caching_sha2_password", authentication.getPlugin());
        assertEquals("old", authentication.getCurrentCredential().getValue());
        assertTrue(authentication.isRetainCurrentPassword());
    }

    @Test
    void namesTheCurrentUserWithParentheses() throws JSQLParserException {
        AlterUser alterUser = parse("ALTER USER CURRENT_USER() IDENTIFIED BY 'x' "
                + "RETAIN CURRENT PASSWORD");
        UserAccount account = alterUser.getAccounts().get(0);

        assertEquals("CURRENT_USER", account.getUser().getValue());
        assertTrue(account.isCurrentUserParentheses());
        assertFalse(alterUser.isUserFunction());
        assertFalse(parse("ALTER USER CURRENT_USER IDENTIFIED BY 'x'").getAccounts().get(0)
                .isCurrentUserParentheses());
        assertEquals("CURRENT_USER()", new UserAccount()
                .withUser(new StringValue("CURRENT_USER").setQuoteStr(""))
                .withCurrentUserParentheses(true).toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"ALTER USER 'CURRENT_USER'() IDENTIFIED BY 'x'",
            "ALTER USER app() IDENTIFIED BY 'x'",
            "ALTER USER CURRENT_USER()@localhost ACCOUNT LOCK"})
    void takesParenthesesOnlyAfterAnUnquotedCurrentUser(String sql) {
        assertThrows(JSQLParserException.class,
                () -> CCJSqlParserUtil.parse(sql, parser -> parser.withDialect(Dialect.MYSQL)));
    }

    @Test
    void altersTheCurrentUserWithUserFunction() throws JSQLParserException {
        AlterUser alterUser = parse("ALTER USER USER() IDENTIFIED BY 'x'");

        assertTrue(alterUser.isUserFunction());
        assertEquals("USER()", alterUser.getAccounts().get(0).getUser().getValue());
        assertEquals(Mode.BY_PASSWORD,
                alterUser.getAccounts().get(0).getAuthentication().getMode());

        AlterUser random = parse("ALTER USER IF EXISTS USER() IDENTIFIED BY RANDOM PASSWORD "
                + "REPLACE 'old' RETAIN CURRENT PASSWORD");
        assertTrue(random.isIfExists());
        assertTrue(random.isUserFunction());

        assertEquals(Mode.DISCARD_OLD_PASSWORD,
                parse("ALTER USER USER() DISCARD OLD PASSWORD").getAccounts().get(0)
                        .getAuthentication().getMode());
    }

    @Test
    void sharesOptionsWithCreateUser() throws JSQLParserException {
        AlterUser alterUser =
                parse("ALTER USER IF EXISTS 'app'@'%' REQUIRE NONE WITH MAX_USER_CONNECTIONS 10");

        assertTrue(alterUser.isIfExists());
        UserOptions options = alterUser.getOptions();
        assertEquals(TlsOption.Kind.NONE, options.getTlsOptions().get(0).getKind());
        assertEquals(10, options.getResourceLimits().get(0).getValue());

        assertEquals(PasswordLockOption.Kind.PASSWORD_EXPIRE,
                parse("ALTER USER 'app'@'%' PASSWORD EXPIRE").getOptions()
                        .getPasswordLockOptions().get(0).getKind());
        assertEquals(PasswordLockOption.Kind.ACCOUNT_UNLOCK,
                parse("ALTER USER 'app'@'%' ACCOUNT UNLOCK").getOptions()
                        .getPasswordLockOptions().get(0).getKind());
    }

    @Test
    void altersSeveralAccounts() throws JSQLParserException {
        AlterUser alterUser = parse("ALTER USER a IDENTIFIED BY 'x', 'b'@'%' DISCARD OLD PASSWORD, "
                + "c ACCOUNT LOCK COMMENT 'locked'");

        assertEquals(List.of("a IDENTIFIED BY 'x'", "'b'@'%' DISCARD OLD PASSWORD", "c"),
                names(alterUser.getAccounts()));
        assertEquals(Mode.DISCARD_OLD_PASSWORD,
                alterUser.getAccounts().get(1).getAuthentication().getMode());
        assertEquals("locked", alterUser.getOptions().getComment().getValue());
    }

    @Test
    void setsDefaultRoles() throws JSQLParserException {
        RoleSelection all = parse("ALTER USER 'app'@'%' DEFAULT ROLE ALL").getOptions()
                .getDefaultRoles();
        assertEquals(RoleSelection.Kind.ALL, all.getKind());
        assertTrue(all.getRoles().isEmpty());

        assertEquals(RoleSelection.Kind.NONE,
                parse("ALTER USER app DEFAULT ROLE NONE").getOptions().getDefaultRoles()
                        .getKind());

        RoleSelection roles = parse("ALTER USER app DEFAULT ROLE r1, 'r2'@'%'").getOptions()
                .getDefaultRoles();
        assertEquals(RoleSelection.Kind.ROLES, roles.getKind());
        assertEquals(List.of("r1", "'r2'@'%'"), names(roles.getRoles()));
    }

    @Test
    void parsesUnquotedAccountNames() throws JSQLParserException {
        assertEquals("app", parse("ALTER USER app IDENTIFIED BY 'x'").getAccounts().get(0)
                .getUser().getValue());
        UserAccount account =
                parse("ALTER USER app@localhost IDENTIFIED BY 'x'").getAccounts().get(0);
        assertEquals("app", account.getUser().getValue());
        assertEquals("localhost", account.getHost().getValue());
        assertTrue(parse("ALTER USER app").getOptions().isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "ALTER USER \"app\"@\"%\" IDENTIFIED BY 'x' ACCOUNT LOCK",
            "ALTER USER `app`@'%', app@'%', 'app'@localhost ACCOUNT LOCK",
            "RENAME USER \"a\"@\"%\" TO `b`@localhost",
            "SET PASSWORD FOR \"app\"@\"%\" = 'x'",
            "SET DEFAULT ROLE `r`@'%' TO \"app\"@\"%\"",
            "CREATE ROLE \"reader\", `writer`@'%'"
    })
    void keepsQuotedAccountNamesInEveryMode(String sql) throws JSQLParserException {
        // without a dialect "app" is a quoted identifier, with MySQL a double-quoted string
        for (Dialect dialect : new Dialect[] {null, Dialect.MYSQL}) {
            Consumer<CCJSqlParser> configuration = parser -> {
                if (dialect != null) {
                    parser.withDialect(dialect);
                }
            };
            Statement statement =
                    TestUtils.assertSqlCanBeParsedAndDeparsed(sql, false, configuration);
            assertEquals(sql,
                    CCJSqlParserUtil.parse(statement.toString(), configuration).toString());
        }
    }

    @Test
    void parsesInDefaultModeAndMariaDb() throws JSQLParserException {
        String sql = "ALTER USER IF EXISTS 'app'@'%' IDENTIFIED BY 'x' REQUIRE SSL "
                + "WITH MAX_QUERIES_PER_HOUR 10 PASSWORD EXPIRE NEVER ACCOUNT UNLOCK";
        parse(sql, Dialect.MARIADB);
        assertInstanceOf(AlterUser.class, TestUtils.assertSqlCanBeParsedAndDeparsed(sql, false));
    }

    @Test
    void keepsPostgreSqlAlterUser() throws JSQLParserException {
        for (String sql : List.of("ALTER USER app WITH LOGIN", "ALTER USER app PASSWORD 'x'",
                "ALTER USER app WITH PASSWORD NULL", "ALTER USER app VALID UNTIL '2030-01-01'",
                "ALTER USER app CONNECTION LIMIT 5", "ALTER USER app RENAME TO app2",
                "ALTER USER app SET search_path TO app, public", "ALTER USER ALL RESET ALL",
                "ALTER USER CURRENT_USER WITH PASSWORD NULL",
                "ALTER USER SESSION_USER SET enable_seqscan TO on")) {
            assertInstanceOf(AlterRole.class, CCJSqlParserUtil.parse(sql), sql);
            assertInstanceOf(AlterRole.class, CCJSqlParserUtil.parse(sql,
                    parser -> parser.withDialect(Dialect.POSTGRESQL)), sql);
        }
    }

    @Test
    void buildsAndDeparses() {
        AlterUser alterUser = new AlterUser().withIfExists(true)
                .addAccounts(new UserAccount().withUser(new StringValue("app"))
                        .withAuthentication(new UserAuthentication().withMode(Mode.BY_PASSWORD)
                                .withCredential(new StringValue("new"))
                                .withCurrentCredential(new StringValue("old"))))
                .withOptions(new UserOptions().addPasswordLockOptions(
                        new PasswordLockOption(PasswordLockOption.Kind.ACCOUNT_LOCK)));

        assertDeparse(alterUser,
                "ALTER USER IF EXISTS 'app' IDENTIFIED BY 'new' REPLACE 'old' ACCOUNT LOCK");

        alterUser.withUserFunction(
                new UserAuthentication().withMode(Mode.DISCARD_OLD_PASSWORD));
        assertTrue(alterUser.isUserFunction());
        assertEquals("ALTER USER IF EXISTS USER() DISCARD OLD PASSWORD ACCOUNT LOCK",
                alterUser.toString());
    }

    @Test
    void reportsSchemaChangeWithoutTables() throws JSQLParserException {
        Statement statement = CCJSqlParserUtil.parse("ALTER USER 'app'@'%' ACCOUNT LOCK");

        assertThat(statement.getFeatures().getCertain())
                .containsExactly(StmtFeature.MODIFIES_SCHEMA);
        assertThat(TablesNamesFinder.findTables(statement.toString())).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "ALTER USER a, b DEFAULT ROLE r",
            "ALTER USER app IDENTIFIED BY 'x' DEFAULT ROLE r",
            "ALTER USER app DEFAULT ROLE DEFAULT",
            "ALTER USER app DEFAULT ROLE ALL EXCEPT r",
            "ALTER USER app IDENTIFIED WITH auth_socket REPLACE 'x'",
            "ALTER USER app IDENTIFIED WITH auth_socket AS 'h' RETAIN CURRENT PASSWORD",
            "ALTER USER app IDENTIFIED BY 'x' REPLACE",
            "ALTER USER USER() IDENTIFIED WITH auth_socket BY 'x'",
            "ALTER USER USER() ACCOUNT LOCK",
            "ALTER USER USER()",
            "ALTER USER app DISCARD OLD PASSWORD RETAIN CURRENT PASSWORD",
            "ALTER USER app ACCOUNT LOCK REQUIRE SSL"
    })
    void rejectsSyntaxOutsideTheStatementDefinition(String sql) {
        assertThrows(JSQLParserException.class,
                () -> CCJSqlParserUtil.parse(sql, parser -> parser.withDialect(Dialect.MYSQL)));
    }
}
