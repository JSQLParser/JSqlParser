/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import static net.sf.jsqlparser.test.TestUtils.assertDeparse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Collectors;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.create.user.RoleSelection;
import net.sf.jsqlparser.statement.create.user.RoleSelection.Kind;
import net.sf.jsqlparser.statement.create.user.UserAccount;
import net.sf.jsqlparser.test.TestUtils;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

/** MySQL SET ROLE and SET DEFAULT ROLE; PostgreSQL SET ROLE stays a {@link SetStatement}. */
class SetRoleStatementTest {

    private static <T extends Statement> T parse(Class<T> type, String sql, Dialect dialect)
            throws JSQLParserException {
        T statement = assertInstanceOf(type, TestUtils.assertSqlCanBeParsedAndDeparsed(sql, false,
                parser -> parser.withDialect(dialect)));
        assertEquals(sql, assertInstanceOf(type,
                CCJSqlParserUtil.parse(statement.toString(), parser -> parser.withDialect(dialect)))
                .toString());
        return statement;
    }

    private static List<String> names(RoleSelection roles) {
        return roles.getRoles().stream().map(UserAccount::toString).collect(Collectors.toList());
    }

    @ParameterizedTest
    @EnumSource(value = Dialect.class, names = {"MYSQL", "MARIADB"})
    void typesEverySetRoleFormInMySqlDialects(Dialect dialect) throws JSQLParserException {
        assertEquals(Kind.DEFAULT,
                parse(SetRoleStatement.class, "SET ROLE DEFAULT", dialect).getRoles().getKind());
        assertEquals(Kind.NONE,
                parse(SetRoleStatement.class, "SET ROLE NONE", dialect).getRoles().getKind());
        assertEquals(Kind.ALL,
                parse(SetRoleStatement.class, "SET ROLE ALL", dialect).getRoles().getKind());

        RoleSelection except =
                parse(SetRoleStatement.class, "SET ROLE ALL EXCEPT 'writer', 'admin'@'%'", dialect)
                        .getRoles();
        assertEquals(Kind.ALL_EXCEPT, except.getKind());
        assertEquals(List.of("'writer'", "'admin'@'%'"), names(except));

        RoleSelection roles =
                parse(SetRoleStatement.class, "SET ROLE 'reader', app_role", dialect).getRoles();
        assertEquals(Kind.ROLES, roles.getKind());
        assertEquals(List.of("'reader'", "app_role"), names(roles));
    }

    @Test
    void keepsPostgreSqlSetRole() throws JSQLParserException {
        for (Dialect dialect : new Dialect[] {null, Dialect.POSTGRESQL}) {
            for (String sql : List.of("SET ROLE app_admin", "SET ROLE 'reader'", "SET ROLE NONE",
                    "SET ROLE DEFAULT", "SET ROLE ALL", "SET LOCAL ROLE 'app'",
                    "SET SESSION ROLE app")) {
                assertInstanceOf(SetStatement.class, CCJSqlParserUtil.parse(sql, parser -> {
                    if (dialect != null) {
                        parser.withDialect(dialect);
                    }
                }), sql);
            }
        }
        // variable assignments and scoped forms are not MySQL SET ROLE
        for (String sql : List.of("SET role = 'x'", "SET LOCAL ROLE 'app'")) {
            assertInstanceOf(SetStatement.class,
                    CCJSqlParserUtil.parse(sql, parser -> parser.withDialect(Dialect.MYSQL)), sql);
        }
    }

    @Test
    void typesMySqlOnlyFormsWithoutDialect() throws JSQLParserException {
        for (Dialect dialect : new Dialect[] {Dialect.ANSI_SQL, Dialect.POSTGRESQL}) {
            assertEquals(Kind.ALL_EXCEPT,
                    parse(SetRoleStatement.class, "SET ROLE ALL EXCEPT 'writer'", dialect)
                            .getRoles().getKind());
        }
        for (String sql : List.of("SET ROLE r1, 'r2'@'%'", "SET ROLE 'r'@localhost",
                "SET ROLE r@'%'", "SET ROLE `r`@`%`")) {
            assertInstanceOf(SetRoleStatement.class,
                    TestUtils.assertSqlCanBeParsedAndDeparsed(sql, false), sql);
        }
        // an unquoted r@h is one identifier, as before
        assertInstanceOf(SetStatement.class, CCJSqlParserUtil.parse("SET ROLE r@h"));
    }

    @Test
    void setsDefaultRoles() throws JSQLParserException {
        SetDefaultRoleStatement single = parse(SetDefaultRoleStatement.class,
                "SET DEFAULT ROLE 'reader' TO 'app'@'%'", Dialect.MYSQL);
        assertEquals(List.of("'reader'"), names(single.getRoles()));
        assertEquals("app", single.getAccounts().get(0).getUser().getValue());
        assertFalse(single.isForKeyword());

        SetDefaultRoleStatement all = parse(SetDefaultRoleStatement.class,
                "SET DEFAULT ROLE ALL TO 'app'@'%', 'u2'@'%'", Dialect.MYSQL);
        assertEquals(Kind.ALL, all.getRoles().getKind());
        assertEquals(2, all.getAccounts().size());

        assertEquals(Kind.NONE, parse(SetDefaultRoleStatement.class,
                "SET DEFAULT ROLE NONE TO app", Dialect.MYSQL).getRoles().getKind());
        // available without a dialect, as no generic SET form starts with SET DEFAULT ROLE
        assertInstanceOf(SetDefaultRoleStatement.class, TestUtils
                .assertSqlCanBeParsedAndDeparsed("SET DEFAULT ROLE r1, r2 TO app", false));
    }

    @Test
    void setsMariaDbDefaultRole() throws JSQLParserException {
        SetDefaultRoleStatement forUser = parse(SetDefaultRoleStatement.class,
                "SET DEFAULT ROLE reader FOR 'app'@'localhost'", Dialect.MARIADB);
        assertTrue(forUser.isForKeyword());
        assertEquals(1, forUser.getAccounts().size());

        SetDefaultRoleStatement current =
                parse(SetDefaultRoleStatement.class, "SET DEFAULT ROLE NONE", Dialect.MARIADB);
        assertTrue(current.getAccounts().isEmpty());
    }

    @Test
    void buildsAndDeparses() {
        UserAccount reader = new UserAccount().withUser(new StringValue("reader"));
        assertDeparse(new SetRoleStatement(new RoleSelection(Kind.ALL_EXCEPT).addRoles(reader)),
                "SET ROLE ALL EXCEPT 'reader'");
        assertDeparse(new SetDefaultRoleStatement().withRoles(new RoleSelection(Kind.ROLES)
                .addRoles(reader)).addAccounts(new UserAccount().withUser(new StringValue("app"))
                        .withHost(new StringValue("%"))),
                "SET DEFAULT ROLE 'reader' TO 'app'@'%'");
    }

    @Test
    void reportsStatementFeatures() throws JSQLParserException {
        Statement setRole =
                CCJSqlParserUtil.parse("SET ROLE ALL", parser -> parser.withDialect(Dialect.MYSQL));
        assertThat(setRole.getFeatures().getCertain())
                .containsExactly(StmtFeature.MODIFIES_SESSION);

        Statement setDefaultRole = CCJSqlParserUtil.parse("SET DEFAULT ROLE ALL TO app");
        assertThat(setDefaultRole.getFeatures().getCertain())
                .containsExactly(StmtFeature.MODIFIES_SCHEMA);
        assertThat(TablesNamesFinder.findTables(setDefaultRole.toString())).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"SET ROLE ALL EXCEPT", "SET ROLE 'r' IDENTIFIED BY 'x'",
            "SET DEFAULT ROLE DEFAULT TO app", "SET DEFAULT ROLE ALL EXCEPT r TO app",
            "SET DEFAULT ROLE TO app", "SET DEFAULT ROLE ALL TO"})
    void rejectsSyntaxOutsideTheStatementDefinition(String sql) {
        assertThrows(JSQLParserException.class,
                () -> CCJSqlParserUtil.parse(sql, parser -> parser.withDialect(Dialect.MYSQL)));
    }
}
