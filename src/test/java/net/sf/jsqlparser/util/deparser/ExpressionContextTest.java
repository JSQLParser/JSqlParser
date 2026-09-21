/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.deparser;

import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.List;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.relational.ParenthesedExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.Limit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ExpressionContextTest {
    @ParameterizedTest
    @ValueSource(strings = {"7 + 8 * 9", "COALESCE(7, ABS(8 + 9))",
            "CASE WHEN 7 < 8 AND 9 <> 10 THEN 11 ELSE 12 END",
            "(7, 8, 9)", "7 IN (8, 9)", "7 BETWEEN 8 AND 9",
            "7 / 8 - 9 % 10", "7 || 8", "7 & 8 | 9 ^ 10",
            "sum(7) FILTER (WHERE 8 > 9) OVER (PARTITION BY 10 ORDER BY 11)",
            "array_agg(7 ORDER BY 8 LIMIT 9)"})
    void preservesCallerContextThroughNestedExpressions(String sql) throws Exception {
        Object context = new Object();
        List<Long> seen = new ArrayList<>();
        StringBuilder output = new StringBuilder();
        ExpressionDeParser visitor = visitor(output, seen, context);
        Expression expression = CCJSqlParserUtil.parseExpression(sql);
        expression.accept(visitor, context);
        assertFalse(seen.isEmpty());
        assertEquals(expression.toString(), output.toString());
        assertEquals(output.toString(),
                CCJSqlParserUtil.parseExpression(output.toString()).toString());
    }

    @Test
    void helpersRetainLegacyNullContextAndForwardExplicitContext() {
        Object context = new Object();
        List<Long> seen = new ArrayList<>();
        StringBuilder out = new StringBuilder();
        ParenthesedExpressionList<LongValue> values =
                new ParenthesedExpressionList<>(new LongValue(7), new LongValue(8));
        new ExpressionListDeParser<>(visitor(out, seen, context), out).deParse(values, context);
        assertEquals(List.of(7L, 8L), seen);
        out.setLength(0);
        seen.clear();
        new ExpressionListDeParser<>(visitor(out, seen, null), out).deParse(values);
        assertEquals("(7, 8)", out.toString());
        Limit limit = new Limit().withOffset(new LongValue(2)).withRowCount(new LongValue(3));
        out.setLength(0);
        seen.clear();
        new LimitDeparser(visitor(out, seen, context), out).deParse(limit, context);
        assertEquals(List.of(2L, 3L), seen);
        out.setLength(0);
        new LimitDeparser(visitor(out, new ArrayList<>(), null), out).deParse(limit);
        assertEquals(" LIMIT 2, 3", out.toString());
    }

    @Test
    void legacyExpressionEntryPointStillUsesNullContext() throws Exception {
        StringBuilder out = new StringBuilder();
        List<Long> seen = new ArrayList<>();
        CCJSqlParserUtil.parseExpression("COALESCE(7 + 8, 9)")
                .accept(visitor(out, seen, null));
        assertEquals(List.of(7L, 8L, 9L), seen);
    }

    private static ExpressionDeParser visitor(StringBuilder out, List<Long> seen, Object expected) {
        ExpressionDeParser visitor = new ExpressionDeParser() {
            @Override
            public <S> StringBuilder visit(LongValue value, S context) {
                assertSame(expected, context);
                seen.add(value.getValue());
                return getBuilder().append(value.getValue());
            }
        };
        visitor.setBuilder(out);
        return visitor;
    }
}
