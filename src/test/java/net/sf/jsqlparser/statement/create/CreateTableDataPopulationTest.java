/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.view.CreateView;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CreateTableDataPopulationTest {
    @ParameterizedTest
    @ValueSource(strings = {"SELECT id FROM source", "TABLE source", "VALUES (1), (2)",
            "(SELECT id FROM source)", "WITH c AS (SELECT 1 AS id) SELECT id FROM c",
            "SELECT id FROM source UNION ALL SELECT id FROM other ORDER BY id LIMIT 4"})
    void preservesDataChoiceForQuerySources(String query) throws JSQLParserException {
        for (String suffix : new String[] {"", " WITH DATA", " WITH NO DATA"}) {
            CreateTable table = parse("CREATE TABLE t AS " + query + suffix);
            assertEquals(suffix.isEmpty() ? null : !suffix.contains("NO"), table.getWithData());
            roundTrip(table);
        }
    }

    @Test
    void sharesOutputAndKeepsCustomQueryVisitor() throws JSQLParserException {
        CreateTable table =
                parse("CREATE TEMP TABLE t (id) ON COMMIT DROP AS SELECT 7 WITH NO DATA");
        table.setWithData(true);
        assertTrue(table.toString().endsWith("WITH DATA"));
        ExpressionDeParser expression = new ExpressionDeParser() {
            @Override
            public <S> StringBuilder visit(LongValue value, S context) {
                return getBuilder().append(value.getValue() + 100);
            }
        };
        StringBuilder out = new StringBuilder();
        table.accept(new StatementDeParser(expression, new SelectDeParser(), out));
        assertTrue(out.toString().endsWith("SELECT 107 WITH DATA"));
        assertTrue(table.toString().contains("SELECT 7"));
        table.setWithData(null);
        assertFalse(table.toString().contains("WITH DATA"));
        roundTrip(table);
    }

    @Test
    void retainsMaterializedViewTailAndStatementBoundaries() throws JSQLParserException {
        CreateView view = (CreateView) CCJSqlParserUtil.parse(
                "CREATE MATERIALIZED VIEW v AS SELECT 1 WITH NO DATA");
        assertEquals(Boolean.FALSE, view.getWithData());
        assertEquals(2, CCJSqlParserUtil.parseStatements(
                "CREATE TABLE t AS SELECT 1 WITH NO DATA; SELECT 2").size());
        for (String sql : List.of("CREATE TABLE t AS SELECT 1 WITH NO",
                "CREATE TABLE t AS SELECT 1 WITH DATA WITH NO DATA",
                "SELECT 1 WITH NO DATA")) {
            assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql));
        }
    }

    private static CreateTable parse(String sql) throws JSQLParserException {
        return (CreateTable) CCJSqlParserUtil.parse(sql, p -> p.withDialect(Dialect.POSTGRESQL));
    }

    private static void roundTrip(CreateTable table) throws JSQLParserException {
        StringBuilder out = new StringBuilder();
        table.accept(new StatementDeParser(out));
        assertEquals(table.toString(), out.toString());
        assertEquals(table.getWithData(), parse(out.toString()).getWithData());
        assertEquals(out.toString(), parse(out.toString()).toString());
    }
}
