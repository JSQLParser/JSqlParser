/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.drop;

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.stream.Collectors;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.CascadeBehavior;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.StmtFeature;
import net.sf.jsqlparser.statement.create.user.UserAccount;
import net.sf.jsqlparser.statement.role.ReassignOwned;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class DropRoleTest {

    @ParameterizedTest
    @ValueSource(strings = {"DROP USER IF EXISTS 'app'@'%', 'u2'@'localhost'", "DROP USER app",
            "DROP USER 'app'@'%'", "DROP USER app@localhost, `u`@`h`",
            "DROP USER IF EXISTS app@'%', 'app'@localhost",
            "DROP ROLE IF EXISTS 'reader'",
            "DROP ROLE 'r'@'h', r2"})
    void mySqlAccounts(String sql) throws JSQLParserException {
        for (Statement statement : roundTrip(sql, Dialect.MYSQL)) {
            Drop drop = (Drop) statement;
            assertEquals(sql, drop.toString());
            assertTrue(drop.getObjectType().hasAccountTargets());
            assertTrue(new TablesNamesFinder().getTables(drop).isEmpty());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"DROP USER IF EXISTS alice", "DROP USER \"Alice\", bob",
            "DROP GROUP staff", "DROP ROLE IF EXISTS app_read, app_rw",
            "DROP OWNED BY old_role", "DROP OWNED BY old_role, \"New\" CASCADE",
            "DROP OWNED BY CURRENT_USER RESTRICT"})
    void postgreSqlRoles(String sql) throws JSQLParserException {
        for (Statement statement : roundTrip(sql, Dialect.POSTGRESQL)) {
            assertEquals(sql, statement.toString());
        }
    }

    @Test
    void accountParts() throws JSQLParserException {
        Drop drop = parse("DROP USER IF EXISTS 'app'@'%', u2", Dialect.MYSQL);
        assertEquals(Drop.ObjectType.USER, drop.getObjectType());
        assertTrue(drop.isIfExists());
        UserAccount first = drop.getAccounts().get(0);
        assertEquals("app", first.getUser().getValue());
        assertEquals("%", first.getHost().getValue());
        assertNull(drop.getAccounts().get(1).getHost());

        first.setHost(new StringValue("localhost"));
        assertEquals("DROP USER IF EXISTS 'app'@'localhost', u2", drop.toString());
        assertEquals(List.of("'app'@'localhost'", "u2"), names(drop));
        assertThrows(UnsupportedOperationException.class,
                () -> drop.getAccounts().add(new UserAccount()));

        Drop role = parse("DROP ROLE \"Admin\"", Dialect.POSTGRESQL);
        assertEquals(Drop.ObjectType.ROLE, role.getObjectType());
        StringValue admin = role.getAccounts().get(0).getUser();
        assertEquals("Admin", admin.getValue());
        assertEquals("\"", admin.getQuoteStr());
        assertNull(role.getAccounts().get(0).getHost());
        assertEquals("\"Admin\"", role.getName().getName());
        assertEquals("DROP ROLE \"Admin\"", role.toString());
    }

    @Test
    void dropOwnedKeepsRolesOutOfTheName() throws JSQLParserException {
        Drop drop = parse("DROP OWNED BY old_role, other CASCADE", Dialect.POSTGRESQL);
        assertEquals(Drop.ObjectType.OWNED, drop.getObjectType());
        assertEquals(List.of("old_role", "other"), drop.getAccounts().stream()
                .map(Object::toString).collect(Collectors.toList()));
        assertEquals(List.of("old_role", "other"), names(drop));
        assertEquals(CascadeBehavior.CASCADE, drop.getBehavior());
        assertEquals(List.of("CASCADE"), drop.getParameters());

        drop.setName(new Table("renamed"));
        assertEquals("DROP OWNED BY renamed CASCADE", drop.toString());

        assertEquals("DROP OWNED BY r", new Drop().withObjectType(Drop.ObjectType.OWNED)
                .addAccounts(new UserAccount().withUser(new StringValue("r").setQuoteStr("")))
                .toString());
    }

    @Test
    void reassignOwned() throws JSQLParserException {
        ReassignOwned statement = (ReassignOwned) CCJSqlParserUtil.parse(
                "REASSIGN OWNED BY old_role, \"Old\" TO new_role",
                parser -> parser.withDialect(Dialect.POSTGRESQL));
        assertEquals(List.of("old_role", "\"Old\""), statement.getRoles().stream()
                .map(Object::toString).collect(Collectors.toList()));
        assertEquals("new_role", statement.getNewRole().toString());
        assertEquals("Old", statement.getRoles().get(1).getUser().getValue());
        assertEquals("\"", statement.getRoles().get(1).getUser().getQuoteStr());

        statement.setNewRole(new UserAccount().withUser(new StringValue("CURRENT_USER")
                .setQuoteStr("")));
        assertEquals("REASSIGN OWNED BY old_role, \"Old\" TO CURRENT_USER", statement.toString());
        assertThrows(UnsupportedOperationException.class,
                () -> statement.getRoles().add(new UserAccount()));

        ReassignOwned built = new ReassignOwned().withRoles(statement.getRoles())
                .withNewRole(statement.getNewRole());
        assertEquals(statement.toString(), built.toString());

        assertTrue(new TablesNamesFinder().getTables((Statement) statement).isEmpty());
        assertNull(statement.accept(new StatementVisitorAdapter<Void>(), null));
        assertThat(statement.getFeatures().getCertain())
                .containsExactly(StmtFeature.MODIFIES_SCHEMA);
        assertThat(parse("DROP OWNED BY r", Dialect.POSTGRESQL).getFeatures().getCertain())
                .contains(StmtFeature.MODIFIES_SCHEMA, StmtFeature.MODIFIES_DATA);
    }

    @Test
    void ownershipStatementsInScripts() throws JSQLParserException {
        List<Statement> statements = CCJSqlParserUtil.parseStatements(
                "REASSIGN OWNED BY a TO b; DROP OWNED BY a; DROP ROLE a",
                parser -> parser.withDialect(Dialect.POSTGRESQL));
        assertEquals(3, statements.size());
        assertInstanceOf(ReassignOwned.class, statements.get(0));
        assertEquals("reassign", ((Drop) parse("DROP TABLE reassign", Dialect.POSTGRESQL))
                .getName().getName());
        assertEquals("reassign", CCJSqlParserUtil.parse("SELECT reassign FROM owned")
                .toString().split(" ")[1]);
    }

    @Test
    void rejectsMalformedRoleStatements() {
        for (String sql : List.of("DROP OWNED IF EXISTS BY r", "DROP OWNED BY", "DROP USER",
                "DROP USER 'app'@", "REASSIGN OWNED BY a", "REASSIGN OWNED a TO b",
                "REASSIGN OWNED BY TO b", "DROP ROLE a.b")) {
            assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql,
                    parser -> parser.withDialect(Dialect.POSTGRESQL)), sql);
        }
    }

    private static List<String> names(Drop drop) {
        return drop.getNames().stream().map(Table::getFullyQualifiedName)
                .collect(Collectors.toList());
    }

    private static Drop parse(String sql, Dialect dialect) throws JSQLParserException {
        return (Drop) CCJSqlParserUtil.parse(sql, parser -> parser.withDialect(dialect));
    }

    private static List<Statement> roundTrip(String sql, Dialect dialect)
            throws JSQLParserException {
        Statement statement = assertSqlCanBeParsedAndDeparsed(sql, true,
                parser -> parser.withDialect(dialect));
        StringBuilder builder = new StringBuilder();
        statement.accept(new StatementDeParser(builder), null);
        assertEquals(statement.toString(), builder.toString());
        return List.of(statement, CCJSqlParserUtil.parse(statement.toString(),
                parser -> parser.withDialect(dialect)));
    }
}
