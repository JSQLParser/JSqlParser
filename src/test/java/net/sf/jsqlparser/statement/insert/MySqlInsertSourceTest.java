/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.insert;

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.function.Consumer;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.operators.relational.ParenthesedExpressionList;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.Node;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.TableStatement;
import net.sf.jsqlparser.statement.select.Values;
import net.sf.jsqlparser.statement.upsert.Upsert;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.validation.ValidationTestAsserts;
import net.sf.jsqlparser.util.validation.feature.MySqlVersion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class MySqlInsertSourceTest {

    private static final Consumer<CCJSqlParser> MYSQL = parser -> parser.withDialect(Dialect.MYSQL);

    /** Parses exactly with and without the MySQL dialect and returns the MySQL result. */
    private static Statement parse(String sql) throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(sql, false);
        return assertSqlCanBeParsedAndDeparsed(sql, false, MYSQL);
    }

    @Test
    void valuesRowConstructors() throws JSQLParserException {
        Insert insert = (Insert) parse("INSERT INTO t VALUES ROW(1, 2), ROW(3, 4)");

        assertEquals("t", insert.getTable().getName());
        assertNull(insert.getTable().getAlias());
        assertNull(insert.getColumns());
        Values values = assertInstanceOf(Values.class, insert.getSelect());
        assertEquals(2, values.getExpressions().size());
        Function row = assertInstanceOf(Function.class, values.getExpressions().get(0));
        assertEquals("ROW", row.getName());
        assertEquals(2, row.getParameters().size());

        // the same model as the query VALUES ROW(1, 2), ROW(3, 4)
        Values query = (Values) CCJSqlParserUtil.parse("VALUES ROW(1, 2), ROW(3, 4)", MYSQL);
        assertEquals(query.toString(), values.toString());
    }

    @Test
    void valuesRowWithColumnsAndDuplicateKeyUpdate() throws JSQLParserException {
        Insert insert = (Insert) parse("INSERT INTO t (a, b) VALUES ROW(1, 2), ROW(3, 4)"
                + " ON DUPLICATE KEY UPDATE b = 5");

        assertEquals(2, insert.getColumns().size());
        assertInstanceOf(Values.class, insert.getSelect());
        assertEquals(1, insert.getDuplicateUpdateSets().size());
    }

    @Test
    void tableStatementSource() throws JSQLParserException {
        Insert insert = (Insert) parse("INSERT INTO t TABLE s");

        assertNull(insert.getTable().getAlias());
        TableStatement source = assertInstanceOf(TableStatement.class, insert.getSelect());
        assertEquals("s", source.getTable().getName());
    }

    @Test
    void tableStatementSourceWithColumnsAndClauses() throws JSQLParserException {
        Insert insert = (Insert) parse("INSERT INTO t (a, b) TABLE db.s ORDER BY a LIMIT 2");

        assertEquals(2, insert.getColumns().size());
        TableStatement source = assertInstanceOf(TableStatement.class, insert.getSelect());
        assertEquals("db.s", source.getTable().getFullyQualifiedName());
        assertNotNull(source.getLimit());
    }

    @Test
    void tableStillNamesAnAliasBeforeOtherSources() throws JSQLParserException {
        Insert values = (Insert) assertSqlCanBeParsedAndDeparsed("INSERT INTO t table VALUES (1)");
        assertEquals("table", values.getTable().getAlias().getName());

        Insert columns =
                (Insert) assertSqlCanBeParsedAndDeparsed("INSERT INTO t table (a) VALUES (1)");
        assertEquals("table", columns.getTable().getAlias().getName());

        Insert explicit =
                (Insert) assertSqlCanBeParsedAndDeparsed("INSERT INTO t AS table SELECT 1");
        assertEquals("table", explicit.getTable().getAlias().getName());
    }

    @Test
    void emptyColumnListAndRow() throws JSQLParserException {
        Insert insert = (Insert) parse("INSERT INTO t () VALUES ()");

        assertNotNull(insert.getColumns());
        assertTrue(insert.getColumns().isEmpty());
        Values values = assertInstanceOf(Values.class, insert.getSelect());
        assertInstanceOf(ParenthesedExpressionList.class, values.getExpressions());
        assertTrue(values.getExpressions().isEmpty());

        // without a column list the columns stay null
        Insert noColumns = (Insert) parse("INSERT INTO t VALUES ()");
        assertNull(noColumns.getColumns());
    }

    @Test
    void emptyColumnListWithOtherSources() throws JSQLParserException {
        parse("INSERT INTO t () VALUES (), ()");
        parse("INSERT INTO t () TABLE s");
        parse("INSERT IGNORE INTO t PARTITION (p0) () VALUES ()");
    }

    @Test
    void replaceSharesTheSources() throws JSQLParserException {
        String sql = "REPLACE INTO t () VALUES ()";
        Upsert empty = (Upsert) parse(sql);
        assertTrue(empty.getColumns().isEmpty());
        assertInstanceOf(ParenthesedExpressionList.class, empty.getColumns());
        // the list prints its parentheses, so its node spans them
        Node node = empty.getColumns().getASTNode();
        assertEquals("()", sql.substring(node.jjtGetFirstToken().absoluteBegin - 1,
                node.jjtGetLastToken().absoluteEnd - 1));

        Upsert rows = (Upsert) parse("REPLACE INTO t VALUES ROW(1, 2), ROW(3, 4)");
        assertInstanceOf(Values.class, rows.getSelect());

        Upsert table = (Upsert) parse("REPLACE INTO t (a, b) TABLE s");
        assertInstanceOf(TableStatement.class, table.getSelect());
    }

    @Test
    void otherDialectsParseTheSameSources() throws JSQLParserException {
        Consumer<CCJSqlParser> postgres = parser -> parser.withDialect(Dialect.POSTGRESQL);
        Insert table = (Insert) assertSqlCanBeParsedAndDeparsed("INSERT INTO t TABLE s", false,
                postgres);
        assertInstanceOf(TableStatement.class, table.getSelect());

        // VALUES before something other than ROW( is still read as before
        assertInstanceOf(PlainSelect.class, assertSqlCanBeParsedAndDeparsed(
                "SELECT values.a FROM t values WHERE a = 1", false, postgres));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "INSERT INTO t (,) VALUES ()",
            "INSERT INTO t () VALUES",
            "INSERT INTO t TABLE",
            "INSERT INTO t VALUES ROW",
            "INSERT INTO t VALUES ROW(1, 2) ROW(3, 4)",
            // a row constructor list starts with VALUES only
            "INSERT INTO t VALUE ROW(1, 2)"
    })
    void rejectsIncompleteForms(String sql) {
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql, MYSQL));
    }

    @Test
    void statementBoundaries() throws JSQLParserException {
        Statements statements = CCJSqlParserUtil.parseStatements(
                "INSERT INTO t TABLE s; INSERT INTO t () VALUES (); "
                        + "INSERT INTO t VALUES ROW(1, 2); SELECT 1",
                MYSQL);

        assertEquals(4, statements.size());
        assertEquals("INSERT INTO t TABLE s", statements.get(0).toString());
        assertEquals("INSERT INTO t VALUES ROW(1, 2)", statements.get(2).toString());
    }

    @Test
    void tablesNamesFinderSeesTheSources() throws JSQLParserException {
        assertEquals(new HashSet<>(Arrays.asList("t", "s")),
                TablesNamesFinder.findTables("INSERT INTO t TABLE s"));
        assertEquals(new HashSet<>(Arrays.asList("t", "s")), TablesNamesFinder.findTables(
                "INSERT INTO t VALUES ROW((SELECT max(a) FROM s), 2)"));
        assertEquals(new HashSet<>(Arrays.asList("t")),
                TablesNamesFinder.findTables("INSERT INTO t () VALUES ()"));
    }

    @Test
    void validatesForMySql() throws JSQLParserException {
        for (String sql : new String[] {"INSERT INTO t VALUES ROW(1, 2), ROW(3, 4)",
                "INSERT INTO t TABLE s", "INSERT INTO t () VALUES ()"}) {
            ValidationTestAsserts.validateNoErrors(sql, 1, MySqlVersion.V8_0);
        }
    }
}
