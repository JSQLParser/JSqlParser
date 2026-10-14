/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.grant;

import static net.sf.jsqlparser.test.TestUtils.assertEqualsObjectTree;
import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.Collections;
import java.util.function.Consumer;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** The grantee model shared by MySQL and PostgreSQL, and the string-based legacy API. */
class GranteeModelTest {

    private static final Consumer<CCJSqlParser> POSTGRESQL =
            parser -> parser.withDialect(Dialect.POSTGRESQL);

    private static Statement parse(String sql) throws JSQLParserException {
        Statement statement = assertSqlCanBeParsedAndDeparsed(sql, false, POSTGRESQL);
        assertEquals(sql, CCJSqlParserUtil.parse(statement.toString(), POSTGRESQL).toString());
        return statement;
    }

    @Test
    void typesPostgreSqlRoleSpecifications() throws JSQLParserException {
        Grant grant = (Grant) parse("GRANT SELECT ON users TO app, GROUP staff, PUBLIC, "
                + "CURRENT_USER, CURRENT_ROLE, SESSION_USER, \"Mixed Case\" GRANTED BY CURRENT_ROLE");

        assertThat(grant.getClause().getGranteeItems()).extracting(Grantee::getKind)
                .containsExactly(Grantee.Kind.ACCOUNT, Grantee.Kind.GROUP, Grantee.Kind.PUBLIC,
                        Grantee.Kind.CURRENT_USER, Grantee.Kind.CURRENT_ROLE,
                        Grantee.Kind.SESSION_USER, Grantee.Kind.ACCOUNT);
        Grantee group = grant.getClause().getGranteeItems().get(1);
        assertEquals("staff", group.getAccount().getUser().getValue());
        assertNull(group.getAccount().getHost());
        Grantee quoted = grant.getClause().getGranteeItems().get(6);
        assertEquals("Mixed Case", quoted.getAccount().getUser().getValue());
        assertEquals("\"", quoted.getAccount().getUser().getQuoteStr());
        assertEquals(Grantee.Kind.CURRENT_ROLE, grant.getClause().getGrantor().getKind());

        assertEquals(Arrays.asList("app", "GROUP staff", "PUBLIC", "CURRENT_USER", "CURRENT_ROLE",
                "SESSION_USER", "\"Mixed Case\""), grant.getUsers());
        assertEquals("CURRENT_ROLE", grant.getClause().getGrantedBy());
        assertThat(TablesNamesFinder.findTables(grant.toString())).containsExactly("users");
    }

    @Test
    void typesMembershipOptionList() throws JSQLParserException {
        Grant grant = (Grant) parse(
                "GRANT app_read TO alice WITH INHERIT TRUE, SET FALSE, ADMIN OPTION GRANTED BY postgres");

        assertNull(grant.getTarget());
        assertThat(grant.getOptions()).extracting(GrantOption::getKind).containsExactly(
                GrantOption.Kind.INHERIT, GrantOption.Kind.SET, GrantOption.Kind.ADMIN);
        assertThat(grant.getOptions()).extracting(GrantOption::getValue).containsExactly(
                GrantOption.Value.TRUE, GrantOption.Value.FALSE, GrantOption.Value.OPTION);
        assertSame(grant.getOptions().get(0), grant.getOption());
        assertEquals("postgres", grant.getClause().getGrantor().getAccount().getUser().getValue());

        grant.setOption(new GrantOption(GrantOption.Kind.ADMIN));
        assertEquals("GRANT app_read TO alice WITH ADMIN OPTION GRANTED BY postgres",
                grant.toString());
        grant.setOption(null);
        assertEquals("GRANT app_read TO alice GRANTED BY postgres", grant.toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "GRANT app_read TO alice WITH INHERIT TRUE, INHERIT FALSE",
            "GRANT app_read TO alice WITH ADMIN OPTION, GRANT OPTION",
            "GRANT SELECT ON users TO alice WITH GRANT OPTION, INHERIT TRUE",
            "GRANT SELECT ON users TO alice GRANTED BY PUBLIC",
            "GRANT SELECT ON users TO alice GRANTED BY GROUP staff",
            "GRANT SELECT ON users TO GROUP"
    })
    void rejectsInvalidRoleSpecifications(String sql) {
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql, POSTGRESQL));
    }

    @Test
    void legacyGranteeStringsWriteThroughTypedState() throws JSQLParserException {
        Grant grant = (Grant) CCJSqlParserUtil.parse("GRANT SELECT ON t TO 'app'@'%', app2",
                parser -> parser.withDialect(Dialect.MYSQL));
        Grantee typed = grant.getClause().getGranteeItems().get(0);

        grant.getUsers().add("PUBLIC");
        grant.getUsers().set(1, "GROUP staff");
        grant.addUsers("CURRENT_USER()");
        assertSame(typed, grant.getClause().getGranteeItems().get(0));
        assertThat(grant.getClause().getGranteeItems()).extracting(Grantee::getKind)
                .containsExactly(Grantee.Kind.ACCOUNT, Grantee.Kind.GROUP, Grantee.Kind.PUBLIC,
                        Grantee.Kind.CURRENT_USER);
        assertEquals("GRANT SELECT ON t TO 'app'@'%', GROUP staff, PUBLIC, CURRENT_USER()",
                grant.toString());

        grant.getUsers().remove(0);
        grant.getClause().setGrantedBy("SESSION_USER");
        assertEquals(Grantee.Kind.SESSION_USER, grant.getClause().getGrantor().getKind());
        assertEquals("GRANT SELECT ON t TO GROUP staff, PUBLIC, CURRENT_USER() "
                + "GRANTED BY SESSION_USER", grant.toString());

        grant.setUsers(Collections.singletonList("reader"));
        assertEquals(Grantee.Kind.ACCOUNT, grant.getClause().getGranteeItems().get(0).getKind());
        grant.setUsers(null);
        assertNull(grant.getUsers());
        assertNull(new Grant().getUsers());
    }

    @Test
    void legacyBuildersMatchParsedStatements() throws JSQLParserException {
        Grant parsed =
                (Grant) CCJSqlParserUtil.parse("GRANT SELECT, CREATE VIEW ON t1 TO u, PUBLIC");
        Grant built = new Grant().addPrivileges("SELECT", "CREATE VIEW").withObjectName("t1")
                .addUsers("u", "PUBLIC");
        assertEqualsObjectTree(parsed, built);
        assertEquals(Privilege.Kind.CREATE_VIEW, built.getPrivilegeItems().get(1).getKind());

        Grant role = (Grant) CCJSqlParserUtil.parse("GRANT r1 TO u GRANTED BY CURRENT_USER");
        Grant builtRole = new Grant().withRole("r1").addUsers("u");
        builtRole.getClause().setGrantedBy("CURRENT_USER");
        assertEqualsObjectTree(role, builtRole);
        assertNull(builtRole.getTarget());

        builtRole.getRoles().add("'r2'@'%'");
        assertEquals("GRANT r1, 'r2'@'%' TO u GRANTED BY CURRENT_USER", builtRole.toString());
        assertEquals("r1", builtRole.getRole());
    }

    @Test
    void legacyObjectNameReplacesWildcard() throws JSQLParserException {
        Grant grant = (Grant) CCJSqlParserUtil.parse("GRANT SELECT ON *.* TO app");
        assertEquals("*.*", grant.getObjectName());

        grant.setObjectName(Arrays.asList("db1", "t1"));
        assertNull(grant.getTarget().getWildcard());
        assertEquals("GRANT SELECT ON db1.t1 TO app", grant.toString());

        Grant role = (Grant) CCJSqlParserUtil.parse("GRANT reader TO app");
        assertNull(role.getObjectName());
        role.setPrivileges(Collections.singletonList("SELECT"));
        role.setObjectName("t1");
        assertEquals("GRANT SELECT ON t1 TO app", role.toString());
    }

    @Test
    void legacyPrivilegeNames() {
        assertEquals(Privilege.Kind.CREATE_TEMPORARY_TABLES,
                new Privilege("CREATE TEMPORARY TABLES").getKind());
        assertEquals(Privilege.Kind.ALTER_SYSTEM, new Privilege("ALTER SYSTEM").getKind());
        Privilege unknown = new Privilege("DYNAMIC");
        assertNull(unknown.getKind());
        assertEquals("DYNAMIC", unknown.toString());
        Privilege text = new Privilege("select (a)");
        assertNull(text.getKind());
        assertEquals("select (a)", text.getName());
        assertFalse(new Privilege(Privilege.Kind.SELECT).isUsePrivileges());
    }

    @Test
    void grantedByAndRolesAcceptLegacyText() {
        PrivilegeClause clause = new PrivilegeClause();
        clause.setGrantedBy("GROUP admins");
        assertEquals(Grantee.Kind.GROUP, clause.getGrantor().getKind());
        clause.setGrantedBy(null);
        assertNull(clause.getGrantor());

        clause.setRoles(Arrays.asList("a", "b"));
        clause.getRoles().set(0, "c");
        assertEquals(Arrays.asList("c", "b"), clause.getRoles());
        clause.setRoles(null);
        assertEquals(Collections.emptyList(), clause.getRoleItems());
    }
}
