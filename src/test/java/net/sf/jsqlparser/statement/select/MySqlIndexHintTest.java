/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

import static net.sf.jsqlparser.test.TestUtils.assertDeparse;
import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.MySQLIndexHint;
import net.sf.jsqlparser.expression.MySQLIndexHint.Scope;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.validation.ValidationTestAsserts;
import net.sf.jsqlparser.util.validation.feature.MySqlVersion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class MySqlIndexHintTest {

    private static final Consumer<CCJSqlParser> MYSQL = parser -> parser.withDialect(Dialect.MYSQL);

    private static PlainSelect parse(String sql) throws JSQLParserException {
        // exact output: single spaces, and the same text from toString() and the deparser
        assertSqlCanBeParsedAndDeparsed(sql, false);
        return (PlainSelect) assertSqlCanBeParsedAndDeparsed(sql, false, MYSQL);
    }

    private static void assertHint(MySQLIndexHint hint, String action, String qualifier,
            Scope scope, String... names) {
        assertEquals(action, hint.getAction());
        assertEquals(qualifier, hint.getIndexQualifier());
        assertEquals(scope, hint.getScope());
        assertEquals(Arrays.asList(names), hint.getIndexNames());
    }

    @Test
    void ignoreIndexForOrderBy() throws JSQLParserException {
        PlainSelect select = parse("SELECT a FROM t IGNORE INDEX FOR ORDER BY (i1) ORDER BY a");

        Table table = (Table) select.getFromItem();
        assertEquals(1, table.getIndexHints().size());
        assertHint(table.getIndexHint(), "IGNORE", "INDEX", Scope.ORDER_BY, "i1");
        assertEquals(1, select.getOrderByElements().size());
    }

    @Test
    void forceIndexForJoin() throws JSQLParserException {
        PlainSelect select = parse("SELECT a FROM t FORCE INDEX FOR JOIN (i1) JOIN s ON t.a = s.a");

        assertHint(((Table) select.getFromItem()).getIndexHint(), "FORCE", "INDEX", Scope.JOIN,
                "i1");
        assertEquals("s", ((Table) select.getJoins().get(0).getFromItem()).getName());
        assertNull(((Table) select.getJoins().get(0).getFromItem()).getIndexHints());
    }

    @Test
    void useKeyForGroupBy() throws JSQLParserException {
        PlainSelect select = parse("SELECT a FROM t USE KEY FOR GROUP BY (i1,i2) GROUP BY a");

        assertHint(((Table) select.getFromItem()).getIndexHint(), "USE", "KEY", Scope.GROUP_BY,
                "i1", "i2");
    }

    @Test
    void ignoreKeyIsAHintNotAnAlias() throws JSQLParserException {
        // IGNORE before KEY used to be read as an alias of t
        PlainSelect select = parse("SELECT a FROM t IGNORE KEY (i1)");
        Table table = (Table) select.getFromItem();
        assertNull(table.getAlias());
        assertHint(table.getIndexHint(), "IGNORE", "KEY", null, "i1");

        Update update = (Update) assertSqlCanBeParsedAndDeparsed(
                "UPDATE t IGNORE KEY FOR ORDER BY (i1) SET a = 1 ORDER BY a LIMIT 1", false, MYSQL);
        assertHint(update.getTable().getIndexHint(), "IGNORE", "KEY", Scope.ORDER_BY, "i1");
    }

    @Test
    void emptyUseIndexList() throws JSQLParserException {
        PlainSelect select = parse("SELECT a FROM t USE INDEX ()");

        assertHint(((Table) select.getFromItem()).getIndexHint(), "USE", "INDEX", null);
        assertTrue(((Table) select.getFromItem()).getIndexHint().getIndexNames().isEmpty());
    }

    @Test
    void severalHintsKeepTheirOrder() throws JSQLParserException {
        PlainSelect select = parse("SELECT a FROM t AS x USE INDEX (i1) IGNORE INDEX FOR ORDER BY"
                + " (i2) FORCE KEY FOR JOIN (i3,PRIMARY) WHERE a = 1");

        Table table = (Table) select.getFromItem();
        assertEquals("x", table.getAlias().getName());
        List<MySQLIndexHint> hints = table.getIndexHints();
        assertEquals(3, hints.size());
        assertHint(hints.get(0), "USE", "INDEX", null, "i1");
        assertHint(hints.get(1), "IGNORE", "INDEX", Scope.ORDER_BY, "i2");
        assertHint(hints.get(2), "FORCE", "KEY", Scope.JOIN, "i3", "PRIMARY");
        assertSame(hints.get(0), table.getIndexHint());
    }

    @Test
    void hintsOnJoinedTables() throws JSQLParserException {
        PlainSelect select = parse("SELECT a FROM t1 USE INDEX (i1) JOIN t2 IGNORE INDEX (i2)"
                + " USE INDEX FOR JOIN (i3) ON t1.a = t2.a");

        Table joined = (Table) select.getJoins().get(0).getFromItem();
        assertEquals(2, joined.getIndexHints().size());
        assertHint(joined.getIndexHints().get(1), "USE", "INDEX", Scope.JOIN, "i3");
    }

    @Test
    void updateTargetWithSeveralHints() throws JSQLParserException {
        String sql = "UPDATE t USE INDEX (i1) IGNORE INDEX FOR ORDER BY (i2) SET a = 1";
        Update update = (Update) assertSqlCanBeParsedAndDeparsed(sql, false, MYSQL);

        assertEquals(2, update.getTable().getIndexHints().size());
        assertHint(update.getTable().getIndexHints().get(1), "IGNORE", "INDEX", Scope.ORDER_BY,
                "i2");
    }

    @Test
    void lockingClauseAfterHintsKeepsItsMeaning() throws JSQLParserException {
        PlainSelect select = parse("SELECT a FROM t USE INDEX (i1) FOR UPDATE");

        assertEquals(ForMode.UPDATE, select.getForMode());
        assertNull(((Table) select.getFromItem()).getIndexHint().getScope());

        PlainSelect plain = parse("SELECT a FROM t FOR UPDATE");
        assertEquals(ForMode.UPDATE, plain.getForMode());
        assertNull(((Table) plain.getFromItem()).getIndexHints());
    }

    @Test
    void otherDialectsParseTheSameHints() throws JSQLParserException {
        String sql = "SELECT a FROM t IGNORE INDEX FOR ORDER BY (i1) ORDER BY a";
        PlainSelect select = (PlainSelect) assertSqlCanBeParsedAndDeparsed(sql, false,
                parser -> parser.withDialect(Dialect.POSTGRESQL));

        assertEquals(Scope.ORDER_BY, ((Table) select.getFromItem()).getIndexHint().getScope());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "SELECT a FROM t IGNORE INDEX ()",
            "SELECT a FROM t FORCE KEY ()",
            "SELECT a FROM t IGNORE KEY ()",
            "SELECT a FROM t USE INDEX",
            "SELECT a FROM t USE INDEX FOR (i1)",
            "SELECT a FROM t USE INDEX FOR UPDATE (i1)",
            "SELECT a FROM t USE INDEX FOR ORDER (i1)",
            "SELECT a FROM t USE INDEX (i1,)"
    })
    void rejectsIncompleteHints(String sql) {
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql, MYSQL));
    }

    @Test
    void statementBoundary() throws JSQLParserException {
        Statements statements = CCJSqlParserUtil.parseStatements(
                "SELECT a FROM t USE INDEX FOR JOIN (i1); SELECT 1", MYSQL);

        assertEquals(2, statements.size());
        assertEquals("SELECT a FROM t USE INDEX FOR JOIN (i1)", statements.get(0).toString());
    }

    @Test
    void tablesNamesFinderIgnoresIndexNames() throws JSQLParserException {
        assertEquals(new HashSet<>(Arrays.asList("t", "s")), TablesNamesFinder.findTables(
                "SELECT a FROM t FORCE INDEX FOR JOIN (i1) JOIN s USE INDEX () ON t.a = s.a"));
    }

    @Test
    void validatesForMySql() throws JSQLParserException {
        ValidationTestAsserts.validateNoErrors(
                "SELECT a FROM t USE INDEX (i1) IGNORE INDEX FOR ORDER BY (i2) ORDER BY a", 1,
                MySqlVersion.V8_0);
    }

    @Test
    void editHintsOnTheModel() throws JSQLParserException {
        Table table = new Table("t");
        MySQLIndexHint legacy = new MySQLIndexHint("USE", "INDEX", Arrays.asList("i1"));
        assertNull(legacy.getScope());

        table.setHint(legacy);
        assertEquals("t USE INDEX (i1)", table.toString());

        table.addIndexHint(new MySQLIndexHint("IGNORE", "KEY", Scope.GROUP_BY,
                Arrays.asList("i2", "i3")));
        assertEquals("t USE INDEX (i1) IGNORE KEY FOR GROUP BY (i2,i3)", table.toString());
        assertEquals(" USE INDEX (i1) IGNORE KEY FOR GROUP BY (i2,i3)",
                table.appendIndexHintsTo(new StringBuilder()).toString());

        PlainSelect select = new PlainSelect().addSelectItems(new AllColumns()).withFromItem(table);
        assertEquals("SELECT * FROM t USE INDEX (i1) IGNORE KEY FOR GROUP BY (i2,i3)",
                select.toString());
        assertDeparse(select,
                "SELECT * FROM t USE INDEX (i1) IGNORE KEY FOR GROUP BY (i2,i3)");
        assertSqlCanBeParsedAndDeparsed(select.toString(), false, MYSQL);

        // setHint replaces every hint; null removes them
        table.setHint(new MySQLIndexHint("FORCE", "INDEX", Scope.ORDER_BY,
                Collections.singletonList("i4")));
        assertEquals("t FORCE INDEX FOR ORDER BY (i4)", table.toString());
        table.setHint(null);
        assertNull(table.getIndexHint());
        assertNull(table.getIndexHints());
        assertEquals("t", table.toString());

        table.setIndexHints(Collections.singletonList(
                new MySQLIndexHint("USE", "INDEX", Scope.JOIN, Collections.emptyList())));
        assertEquals("t USE INDEX FOR JOIN ()", table.toString());
        assertInstanceOf(MySQLIndexHint.class, table.getIndexHint());
        assertEquals("ORDER BY", Scope.ORDER_BY.getKeywords());
    }
}
