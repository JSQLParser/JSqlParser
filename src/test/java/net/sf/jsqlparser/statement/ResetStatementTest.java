/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2021 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import net.sf.jsqlparser.JSQLParserException;
import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.sf.jsqlparser.parser.ASTNodeAccess;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.Node;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class ResetStatementTest {
    private static final Consumer<CCJSqlParser> MYSQL = parser -> parser.withDialect(Dialect.MYSQL);
    private static final Consumer<CCJSqlParser> NO_DIALECT = parser -> {
    };

    @Test
    public void tesResetTZ() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("RESET Time Zone");
    }

    @Test
    public void tesResetAll() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("RESET ALL");
    }

    @Test
    public void testObject() {
        ResetStatement resetStatement = new ResetStatement();
        assertNotNull(resetStatement.getName());

        resetStatement.add("something");
        resetStatement.setName("somethingElse");
        assertEquals("somethingElse", resetStatement.getName());
    }

    @ParameterizedTest
    @ValueSource(strings = {"RESET PERSIST", "RESET PERSIST max_connections",
            "RESET PERSIST IF EXISTS max_connections", "RESET PERSIST `max_connections`",
            "RESET PERSIST a.b", "RESET PERSIST `a`.`b`", "RESET PERSIST DEFAULT.x",
            "RESET PERSIST IF EXISTS validate_password.length",
            "RESET PERSIST IF EXISTS DEFAULT.x"})
    public void testResetPersistInMySql(String sql) throws JSQLParserException {
        ResetStatement reset = (ResetStatement) assertSqlCanBeParsedAndDeparsed(sql, false, MYSQL);
        assertTrue(reset.isPersist());
        assertEquals(sql, deparse(reset));
    }

    @Test
    public void testResetPersistState() throws JSQLParserException {
        ResetStatement all = (ResetStatement) CCJSqlParserUtil.parse("reset persist", MYSQL);
        assertTrue(all.isPersist());
        assertFalse(all.isIfExists());
        assertNull(all.getName());
        assertEquals("RESET PERSIST", all.toString());

        ResetStatement one = (ResetStatement) CCJSqlParserUtil
                .parse("RESET PERSIST IF EXISTS max_connections", MYSQL);
        assertTrue(one.isPersist());
        assertTrue(one.isIfExists());
        assertEquals("max_connections", one.getName());

        // a component variable keeps its prefix in the name
        ResetStatement component = (ResetStatement) CCJSqlParserUtil
                .parse("RESET PERSIST IF EXISTS `validate_password`.length", MYSQL);
        assertEquals("`validate_password`.length", component.getName());
        assertEquals("DEFAULT.x",
                ((ResetStatement) CCJSqlParserUtil.parse("RESET PERSIST DEFAULT.x", MYSQL))
                        .getName());
    }

    @ParameterizedTest
    @ValueSource(strings = {"RESET PERSIST max_connections",
            "RESET PERSIST IF EXISTS max_connections", "RESET PERSIST validate_password.length",
            "RESET PERSIST DEFAULT.x"})
    public void testResetPersistWithANameWithoutDialect(String sql) throws JSQLParserException {
        ResetStatement reset =
                (ResetStatement) assertSqlCanBeParsedAndDeparsed(sql, false, NO_DIALECT);
        assertTrue(reset.isPersist());
        assertEquals(sql.substring(sql.lastIndexOf(' ') + 1), reset.getName());
    }

    @Test
    public void testBareResetPersistKeepsItsReadingOutsideMySql() throws JSQLParserException {
        // PostgreSQL resets a parameter named persist; only MySQL reads the PERSIST keyword alone
        for (Consumer<CCJSqlParser> dialect : List.of(NO_DIALECT,
                (Consumer<CCJSqlParser>) parser -> parser.withDialect(Dialect.POSTGRESQL),
                (Consumer<CCJSqlParser>) parser -> parser.withDialect(Dialect.MARIADB))) {
            ResetStatement reset =
                    (ResetStatement) assertSqlCanBeParsedAndDeparsed("RESET persist", false,
                            dialect);
            assertFalse(reset.isPersist());
            assertEquals("persist", reset.getName());
        }
        ResetStatement script = (ResetStatement) CCJSqlParserUtil
                .parseStatements("RESET persist; SELECT 1", NO_DIALECT).get(0);
        assertFalse(script.isPersist());
    }

    @ParameterizedTest
    @ValueSource(strings = {"RESET PERSIST max_connections",
            "RESET PERSIST IF EXISTS max_connections", "RESET PERSIST a.b"})
    public void testOtherDialectsKeepRejectingResetPersistOfAName(String sql) {
        for (Dialect dialect : List.of(Dialect.POSTGRESQL, Dialect.MARIADB, Dialect.ORACLE)) {
            assertThrows(JSQLParserException.class,
                    () -> CCJSqlParserUtil.parse(sql, parser -> parser.withDialect(dialect)));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"RESET PERSIST IF EXISTS", "RESET PERSIST IF max_connections",
            "RESET PERSIST a.b.c", "RESET PERSIST a.", "RESET PERSIST DEFAULT",
            "RESET PERSIST a b", "RESET PERSIST 'max_connections'",
            "RESET PERSIST max_connections, wait_timeout"})
    public void testRejectsResetPersistFormsMySqlRejects(String sql) {
        for (Consumer<CCJSqlParser> dialect : List.of(NO_DIALECT, MYSQL)) {
            assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql, dialect),
                    sql);
        }
    }

    @Test
    public void testResetPersistEndsTheStatement() throws JSQLParserException {
        List<String> statements = CCJSqlParserUtil
                .parseStatements("RESET PERSIST; RESET PERSIST IF EXISTS x; SELECT 1", MYSQL)
                .stream().map(Object::toString).collect(Collectors.toList());
        assertEquals(List.of("RESET PERSIST", "RESET PERSIST IF EXISTS x", "SELECT 1"),
                statements);
    }

    @Test
    public void testFindsNoTables() throws JSQLParserException {
        for (String sql : new String[] {"RESET ALL", "RESET work_mem", "RESET PERSIST x"}) {
            assertThat(new TablesNamesFinder<>().getTables(CCJSqlParserUtil.parse(sql)))
                    .isEmpty();
        }
    }

    @Test
    public void testNodeSpansTheStatement() throws JSQLParserException {
        String sql = "RESET PERSIST IF EXISTS max_connections";
        Node node = ((ASTNodeAccess) CCJSqlParserUtil.parse(sql, MYSQL)).getASTNode();
        assertEquals(sql, sql.substring(node.jjtGetFirstToken().absoluteBegin - 1,
                node.jjtGetLastToken().absoluteEnd - 1));
    }

    @Test
    public void testBuildsResetPersistInCode() {
        ResetStatement reset = new ResetStatement().withPersist(true);
        assertEquals("RESET PERSIST", reset.toString());
        reset.withIfExists(true).setName("x");
        assertEquals("RESET PERSIST IF EXISTS x", deparse(reset));
        reset.setPersist(false);
        assertEquals("RESET x", reset.toString());
    }

    private static String deparse(ResetStatement reset) {
        StringBuilder builder = new StringBuilder();
        reset.accept(new StatementDeParser(builder), null);
        return builder.toString();
    }
}
