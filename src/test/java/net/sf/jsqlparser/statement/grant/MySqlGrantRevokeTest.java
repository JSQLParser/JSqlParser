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

import static net.sf.jsqlparser.test.TestUtils.assertDeparse;
import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static net.sf.jsqlparser.util.validation.ValidationTestAsserts.validateNoErrors;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
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
import net.sf.jsqlparser.statement.create.user.RoleSelection;
import net.sf.jsqlparser.statement.create.user.UserAccount;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.validation.feature.DatabaseType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class MySqlGrantRevokeTest {

    private static final Consumer<CCJSqlParser> MYSQL = parser -> parser.withDialect(Dialect.MYSQL);

    /** Parses with the MySQL dialect, checks toString and the deparser, and reparses. */
    private static Statement parse(String sql) throws JSQLParserException {
        Statement statement = assertSqlCanBeParsedAndDeparsed(sql, false, MYSQL);
        assertEquals(sql, CCJSqlParserUtil.parse(statement.toString(), MYSQL).toString());
        return statement;
    }

    private static Grant grant(String sql) throws JSQLParserException {
        return (Grant) parse(sql);
    }

    private static Revoke revoke(String sql) throws JSQLParserException {
        return (Revoke) parse(sql);
    }

    /** The value and quote of the user and host, for example {@code app(')@%(')}. */
    private static String account(UserAccount account) {
        StringValue host = account.getHost();
        return account.getUser().getValue() + "(" + account.getUser().getQuoteStr() + ")"
                + (host == null ? "" : "@" + host.getValue() + "(" + host.getQuoteStr() + ")");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "GRANT SELECT ON db1.* TO 'app'@'%'",
            "GRANT SELECT, INSERT, UPDATE, DELETE ON `db1`.`t1` TO 'app'@'%' WITH GRANT OPTION",
            "GRANT ALL PRIVILEGES ON *.* TO 'admin'@'localhost' WITH GRANT OPTION",
            "GRANT SELECT (id, name), UPDATE (name) ON db1.t1 TO 'app'@'%'",
            "GRANT EXECUTE ON PROCEDURE db1.p1 TO 'app'@'%'",
            "GRANT EXECUTE ON FUNCTION db1.f1 TO app@'%'",
            "GRANT CREATE, ALTER, DROP, INDEX, REFERENCES, CREATE VIEW, SHOW VIEW, TRIGGER, EVENT, "
                    + "CREATE ROUTINE, ALTER ROUTINE, CREATE TEMPORARY TABLES, LOCK TABLES "
                    + "ON db1.* TO 'dev'@'%'",
            "GRANT SELECT, RELOAD, SHOW DATABASES, REPLICATION SLAVE, REPLICATION CLIENT "
                    + "ON *.* TO 'cdc'@'%'",
            "GRANT PROCESS, SUPER, FILE, SHUTDOWN, CREATE USER, CREATE TABLESPACE, CREATE ROLE, "
                    + "DROP ROLE, USAGE, GRANT OPTION ON *.* TO 'admin'@'%'",
            "GRANT BACKUP_ADMIN, replication_slave_admin ON *.* TO 'admin'@'%'",
            "GRANT 'reader', 'writer'@'%' TO 'app'@'%' WITH ADMIN OPTION",
            "GRANT reader TO app",
            "GRANT PROXY ON 'a'@'%' TO 'b'@'%', CURRENT_USER WITH GRANT OPTION",
            "GRANT SELECT ON TABLE db1.* TO `app`@`%`",
            "GRANT ALL ON * TO app@localhost",
            "GRANT SELECT ON `my-db`.* TO 'app'@localhost, CURRENT_USER()",
            "GRANT SELECT ON db1.* TO 'app'@'%' AS 'admin'@'%' WITH ROLE 'r1', r2@'%'",
            "GRANT SELECT ON db1.t1 TO app WITH GRANT OPTION AS CURRENT_USER WITH ROLE DEFAULT",
            "REVOKE INSERT ON db1.* FROM 'app'@'%', app2",
            "REVOKE ALL PRIVILEGES, GRANT OPTION FROM 'app'@'%'",
            "REVOKE ALL, GRANT OPTION FROM app",
            "REVOKE GRANT OPTION ON *.* FROM app",
            "REVOKE 'reader', writer FROM 'app'@'%'",
            "REVOKE IF EXISTS SELECT ON db1.t1 FROM 'app'@'%' IGNORE UNKNOWN USER",
            "REVOKE IF EXISTS PROXY ON 'a'@'%' FROM 'b'@'%'",
            "REVOKE REPLICATION SLAVE, REPLICATION CLIENT ON *.* FROM 'repl'@'%'"
    })
    void roundTripsMySqlForms(String sql) throws JSQLParserException {
        Statement statement = parse(sql);

        Statement withoutDialect = assertSqlCanBeParsedAndDeparsed(sql, false);
        assertEquals(statement.toString(), withoutDialect.toString());
        assertThat(statement.getFeatures().getCertain())
                .containsExactly(StmtFeature.MODIFIES_SCHEMA);
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "*          | DEFAULT_DATABASE | []    | *",
            "*.*        | GLOBAL           | []    | *.*",
            "db1.*      | DATABASE         | [db1] | db1.*",
            "`my-db`.*  | DATABASE         | [`my-db`] | `my-db`.*"
    })
    void typesWildcardPrivilegeLevels(String level, PrivilegeTarget.Wildcard wildcard,
            String names, String objectName) throws JSQLParserException {
        Grant grant = grant("GRANT SELECT ON " + level + " TO app");
        PrivilegeTarget target = grant.getTarget();

        assertEquals(wildcard, target.getWildcard());
        assertEquals(PrivilegeTarget.Kind.TABLE, target.getKind());
        assertFalse(target.isExplicitKind());
        assertEquals(names, target.getNames().stream().map(name -> name.get(0))
                .collect(Collectors.toList()).toString());
        assertEquals(objectName, grant.getObjectName());
        assertThat(TablesNamesFinder.findTables(grant.toString())).isEmpty();
    }

    @Test
    void namedPrivilegeLevelsRemainTables() throws JSQLParserException {
        Grant qualified = grant("GRANT SELECT ON TABLE db1.t1 TO app");
        assertNull(qualified.getTarget().getWildcard());
        assertTrue(qualified.getTarget().isExplicitKind());
        assertEquals(Arrays.asList("db1", "t1"), qualified.getTarget().getNames().get(0));
        assertThat(TablesNamesFinder.findTables(qualified.toString())).containsExactly("db1.t1");

        Grant routine = grant("GRANT EXECUTE ON PROCEDURE db1.p1 TO app");
        assertEquals(PrivilegeTarget.Kind.PROCEDURE, routine.getTarget().getKind());
        assertEquals("db1.p1", routine.getTarget().getRoutines().get(0).getName());
    }

    @Test
    void typesStaticAndDynamicPrivileges() throws JSQLParserException {
        Grant grant = grant("GRANT CREATE TEMPORARY TABLES, LOCK TABLES, SHOW DATABASES, "
                + "REPLICATION SLAVE, REPLICATION CLIENT, ALTER ROUTINE, CREATE VIEW, INDEX, "
                + "SHUTDOWN, GRANT OPTION, backup_admin ON *.* TO app");

        assertThat(grant.getPrivilegeItems()).extracting(Privilege::getKind).containsExactly(
                Privilege.Kind.CREATE_TEMPORARY_TABLES, Privilege.Kind.LOCK_TABLES,
                Privilege.Kind.SHOW_DATABASES, Privilege.Kind.REPLICATION_SLAVE,
                Privilege.Kind.REPLICATION_CLIENT, Privilege.Kind.ALTER_ROUTINE,
                Privilege.Kind.CREATE_VIEW, Privilege.Kind.INDEX, Privilege.Kind.SHUTDOWN,
                Privilege.Kind.GRANT_OPTION, Privilege.Kind.DYNAMIC);
        Privilege dynamic = grant.getPrivilegeItems().get(10);
        assertEquals("backup_admin", dynamic.getDynamicName());
        assertEquals("backup_admin", dynamic.getName());
        assertEquals("CREATE TEMPORARY TABLES", grant.getPrivilegeItems().get(0).getName());
        assertEquals("CREATE TEMPORARY TABLES", grant.getPrivileges().get(0));
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', quoteCharacter = '"', value = {
            "'app'@'%'       | app(')@%(')",
            "app@'10.0.0.%'  | app()@10.0.0.%(')",
            "app@localhost   | app()@localhost()",
            "'app'@localhost | app(')@localhost()",
            "`app`@`%`       | app(`)@%(`)",
            "'app'           | app(')",
            "app             | app()"
    })
    void typesAccountGrantees(String account, String expected) throws JSQLParserException {
        Grant grant = grant("GRANT SELECT ON db1.t1 TO " + account);
        Grantee grantee = grant.getClause().getGranteeItems().get(0);

        assertEquals(Grantee.Kind.ACCOUNT, grantee.getKind());
        assertEquals(expected, account(grantee.getAccount()));
        assertEquals(Collections.singletonList(account), grant.getUsers());
    }

    @Test
    void typesCurrentUser() throws JSQLParserException {
        Grant grant = grant("GRANT SELECT ON db1.* TO CURRENT_USER, CURRENT_USER()");
        List<Grantee> grantees = grant.getClause().getGranteeItems();

        assertThat(grantees).extracting(Grantee::getKind)
                .containsExactly(Grantee.Kind.CURRENT_USER, Grantee.Kind.CURRENT_USER);
        assertFalse(grantees.get(0).isCurrentUserParentheses());
        assertTrue(grantees.get(1).isCurrentUserParentheses());
        assertNull(grantees.get(0).getAccount());
        assertEquals(Arrays.asList("CURRENT_USER", "CURRENT_USER()"), grant.getUsers());
    }

    @Test
    void roleGrantsHaveTypedRolesAndNoTarget() throws JSQLParserException {
        Grant grant = grant("GRANT 'reader', 'writer'@'%' TO 'app'@'%' WITH ADMIN OPTION");

        assertNull(grant.getTarget());
        assertNull(grant.getPrivilegeItems());
        assertNull(grant.getObjectName());
        assertThat(grant.getClause().getRoleItems()).extracting(MySqlGrantRevokeTest::account)
                .containsExactly("reader(')", "writer(')@%(')");
        assertEquals(Arrays.asList("'reader'", "'writer'@'%'"), grant.getRoles());
        assertEquals("'reader'", grant.getRole());
        assertEquals(GrantOption.Kind.ADMIN, grant.getOption().getKind());
        assertThat(TablesNamesFinder.findTables(grant.toString())).isEmpty();

        Revoke revoke = revoke("REVOKE 'reader' FROM 'app'@'%'");
        assertNull(revoke.getClause().getTarget());
        assertEquals("reader", revoke.getClause().getRoleItems().get(0).getUser().getValue());
    }

    @Test
    void proxyTargetsAnAccount() throws JSQLParserException {
        Grant grant = grant("GRANT PROXY ON 'a'@'%' TO 'b'@'%' WITH GRANT OPTION");

        assertThat(grant.getPrivilegeItems()).extracting(Privilege::getKind)
                .containsExactly(Privilege.Kind.PROXY);
        PrivilegeTarget target = grant.getTarget();
        assertEquals(PrivilegeTarget.Kind.ACCOUNT, target.getKind());
        assertEquals("a(')@%(')", account(target.getAccount().getAccount()));
        assertTrue(target.getNames().isEmpty());
        assertNull(grant.getObjectName());
        assertThat(TablesNamesFinder.findTables(grant.toString())).isEmpty();

        Revoke revoke = revoke("REVOKE PROXY ON CURRENT_USER FROM 'b'@'%'");
        assertEquals(Grantee.Kind.CURRENT_USER,
                revoke.getClause().getTarget().getAccount().getKind());
    }

    @Test
    void revokesAllPrivilegesAndGrantOptionWithoutTarget() throws JSQLParserException {
        Revoke revoke = revoke("REVOKE ALL PRIVILEGES, GRANT OPTION FROM 'app'@'%', app2");
        PrivilegeClause clause = revoke.getClause();

        assertThat(clause.getPrivileges()).extracting(Privilege::getKind)
                .containsExactly(Privilege.Kind.ALL, Privilege.Kind.GRANT_OPTION);
        assertTrue(clause.getPrivileges().get(0).isUsePrivileges());
        assertNull(clause.getTarget());
        assertEquals(Arrays.asList("'app'@'%'", "app2"), clause.getGrantees());
        assertThat(TablesNamesFinder.findTables(revoke.toString())).isEmpty();
    }

    @Test
    void revokeIfExistsIgnoreUnknownUser() throws JSQLParserException {
        Revoke revoke =
                revoke("REVOKE IF EXISTS SELECT ON db1.t1 FROM 'app'@'%' IGNORE UNKNOWN USER");
        assertTrue(revoke.isIfExists());
        assertTrue(revoke.isIgnoreUnknownUser());
        assertThat(TablesNamesFinder.findTables(revoke.toString())).containsExactly("db1.t1");

        revoke.setIfExists(false);
        revoke.setIgnoreUnknownUser(false);
        assertEquals("REVOKE SELECT ON db1.t1 FROM 'app'@'%'", revoke.toString());
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', quoteCharacter = '"', value = {
            "DEFAULT                | DEFAULT    | \"\"",
            "NONE                   | NONE       | \"\"",
            "ALL                    | ALL        | \"\"",
            "ALL EXCEPT 'r1', r2@'%' | ALL_EXCEPT | 'r1', r2@'%'",
            "'r1', r2               | ROLES      | 'r1', r2"
    })
    void typesAsUserWithRole(String selection, RoleSelection.Kind kind, String roles)
            throws JSQLParserException {
        Grant grant = grant("GRANT SELECT ON db1.* TO app AS 'admin'@'%' WITH ROLE " + selection);

        assertEquals(Grantee.Kind.ACCOUNT, grant.getAsUser().getKind());
        assertEquals("admin(')@%(')", account(grant.getAsUser().getAccount()));
        assertEquals(kind, grant.getWithRole().getKind());
        assertEquals(roles, grant.getWithRole().getRoles().stream().map(Object::toString)
                .collect(Collectors.joining(", ")));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "GRANT SELECT ON db1.t1.* TO app",
            "GRANT SELECT ON db1.*, db2.* TO app",
            "GRANT USAGE ON SCHEMA * TO app",
            "GRANT EXECUTE ON PROCEDURE db1.* TO app",
            "GRANT PROXY ON db1.t1 TO app",
            "GRANT PROXY, SELECT ON t1 TO app",
            "GRANT PROXY ON PUBLIC TO app",
            "GRANT PROXY ON 'a'@'%' TO app AS admin",
            "GRANT reader TO app AS admin",
            "GRANT SELECT ON t1 TO app AS PUBLIC",
            "GRANT SELECT ON t1 TO app WITH ROLE r1",
            "GRANT SELECT ON t1 TO app WITH GRANT OPTION, GRANT OPTION",
            "GRANT SELECT ON t1 TO 'app'@",
            "GRANT SELECT ON t1 TO app@",
            "GRANT SELECT ON t1 TO PUBLIC()",
            "GRANT BACKUP_ADMIN PRIVILEGES ON *.* TO app",
            "GRANT ALL PRIVILEGES, GRANT OPTION TO app",
            "REVOKE SELECT ON t1 FROM app IGNORE UNKNOWN",
            "ALTER DEFAULT PRIVILEGES GRANT SELECT ON *.* TO app",
            "ALTER DEFAULT PRIVILEGES REVOKE IF EXISTS SELECT ON TABLES FROM app",
            "ALTER DEFAULT PRIVILEGES REVOKE SELECT ON TABLES FROM app IGNORE UNKNOWN USER",
            "ALTER DEFAULT PRIVILEGES GRANT PROXY ON a TO app",
            "ALTER DEFAULT PRIVILEGES REVOKE ALL, GRANT OPTION FROM app"
    })
    void rejectsInvalidForms(String sql) {
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql, MYSQL));
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql));
    }

    @Test
    void buildsTypedGrant() {
        PrivilegeTarget target = new PrivilegeTarget();
        target.setWildcard(PrivilegeTarget.Wildcard.DATABASE);
        target.getNames().add(Collections.singletonList("db1"));
        UserAccount app = new UserAccount().withUser(new StringValue("app"))
                .withHost(new StringValue("%"));
        Grant grant = new Grant();
        grant.setPrivilegeItems(Arrays.asList(new Privilege(Privilege.Kind.SHOW_VIEW),
                Privilege.dynamic("BACKUP_ADMIN")));
        grant.setTarget(target);
        grant.getClause().setGranteeItems(Arrays.asList(new Grantee(app),
                new Grantee(Grantee.Kind.CURRENT_USER).withCurrentUserParentheses(true)));
        grant.setOption(new GrantOption(GrantOption.Kind.GRANT));
        grant.setAsUser(new Grantee(new UserAccount().withUser(new StringValue("admin"))));
        grant.setWithRole(new RoleSelection(RoleSelection.Kind.ALL_EXCEPT)
                .withRoles(Collections.singletonList(
                        new UserAccount().withUser(new StringValue("r1")))));

        assertDeparse(grant, "GRANT SHOW VIEW, BACKUP_ADMIN ON db1.* TO 'app'@'%', "
                + "CURRENT_USER() WITH GRANT OPTION AS 'admin' WITH ROLE ALL EXCEPT 'r1'");

        Revoke revoke = new Revoke();
        revoke.setIfExists(true);
        revoke.getClause().setPrivileges(Collections.singletonList(
                new Privilege(Privilege.Kind.PROXY)));
        PrivilegeTarget proxied = new PrivilegeTarget();
        proxied.setKind(PrivilegeTarget.Kind.ACCOUNT);
        proxied.setAccount(new Grantee(app));
        revoke.getClause().setTarget(proxied);
        revoke.getClause().setGranteeItems(
                Collections.singletonList(new Grantee(Grantee.Kind.CURRENT_USER)));
        revoke.setIgnoreUnknownUser(true);
        assertDeparse(revoke,
                "REVOKE IF EXISTS PROXY ON 'app'@'%' FROM CURRENT_USER IGNORE UNKNOWN USER");
    }

    @Test
    void validatesWithMySqlAndMariaDbCapabilities() {
        for (String sql : Arrays.asList("GRANT SELECT ON db1.* TO 'app'@'%'",
                "GRANT 'reader' TO 'app'@'%'",
                "REVOKE ALL PRIVILEGES, GRANT OPTION FROM 'app'@'%'",
                "REVOKE SELECT ON *.* FROM app")) {
            validateNoErrors(sql, 1, DatabaseType.MYSQL, DatabaseType.MARIADB);
        }
    }
}
