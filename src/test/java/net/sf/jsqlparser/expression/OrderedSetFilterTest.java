/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class OrderedSetFilterTest {
    @ParameterizedTest
    @ValueSource(strings = {
            "percentile_cont(0.5) WITHIN GROUP (ORDER BY score) FILTER (WHERE active)",
            "percentile_disc(0.5) WITHIN GROUP (ORDER BY score DESC NULLS LAST) "
                    + "FILTER (WHERE active AND score > 0)",
            "mode() WITHIN GROUP (ORDER BY score) FILTER (WHERE active)",
            "rank(5) WITHIN GROUP (ORDER BY score) FILTER (WHERE NOT active)",
            "sum(score) FILTER (WHERE active)",
            "sum(score) FILTER (WHERE active) OVER (PARTITION BY team)",
            "percentile_cont(0.5) WITHIN GROUP (ORDER BY score)"
    })
    void preservesAggregateClauses(String expression) throws JSQLParserException {
        String sql = "SELECT " + expression + " FROM measurements";
        PlainSelect select = parse(sql);
        StringBuilder output = new StringBuilder();
        select.accept(new StatementDeParser(output), null);
        assertEquals(sql, select.toString().replace(" )", ")"));
        assertEquals(sql, output.toString().replace(" )", ")"));
        assertEquals(select.toString(), parse(output.toString()).toString());
        assertEquals(select.toString(), CCJSqlParserUtil.parse(sql).toString());
    }

    @Test
    void visitsBothOrderingAndFilterExpressions() throws JSQLParserException {
        PlainSelect select = parse("SELECT rank(5) WITHIN GROUP (ORDER BY score + 1) "
                + "FILTER (WHERE score > 2) FROM measurements");
        AnalyticExpression aggregate = assertInstanceOf(AnalyticExpression.class,
                select.getSelectItem(0).getExpression());
        assertEquals(AnalyticType.WITHIN_GROUP, aggregate.getType());
        assertEquals("score > 2", aggregate.getFilterExpression().toString());
        StringBuilder output = new StringBuilder();
        ExpressionDeParser expressions = new ExpressionDeParser() {
            @Override
            public <S> StringBuilder visit(LongValue value, S context) {
                return getBuilder().append(value.getValue() + 10);
            }
        };
        select.accept(new StatementDeParser(expressions, new SelectDeParser(), output), null);
        String expected = "SELECT rank(15) WITHIN GROUP (ORDER BY score + 11) "
                + "FILTER (WHERE score > 12) FROM measurements";
        assertEquals(expected, output.toString());
        assertEquals(expected, parse(output.toString()).toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "percentile_cont(0.5) FILTER (WHERE active) WITHIN GROUP (ORDER BY score)",
            "percentile_cont(0.5) WITHIN GROUP (ORDER BY score) FILTER (active)",
            "percentile_cont(0.5) WITHIN GROUP (ORDER BY score) FILTER (WHERE)",
            "percentile_cont(0.5) WITHIN GROUP (ORDER BY score) "
                    + "FILTER (WHERE active) FILTER (WHERE active)",
            "sum(score) OVER (PARTITION BY team) FILTER (WHERE active)"
    })
    void rejectsInvalidPostgresFilterPlacement(String expression) {
        assertThrows(JSQLParserException.class,
                () -> parse("SELECT " + expression + " FROM measurements"));
    }

    private static PlainSelect parse(String sql) throws JSQLParserException {
        return (PlainSelect) CCJSqlParserUtil.parse(sql, p -> p.withDialect(Dialect.POSTGRESQL));
    }
}
