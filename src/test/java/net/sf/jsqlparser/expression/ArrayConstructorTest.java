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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.operators.relational.ParenthesedExpressionList;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ArrayConstructorTest {
    @ParameterizedTest
    @ValueSource(strings = {"ARRAY['a', (1 + 0)::text]", "ARRAY[(1 + 2), 3]",
            "ARRAY[1, (2 + 3), 4]", "ARRAY[1, ((2 * 3) + 4)]",
            "ARRAY[(SELECT 1), 2]", "ARRAY[NULL, COALESCE(NULL, (2 + 3))]",
            "ARRAY[CASE WHEN 1 < 2 THEN (3 + 4) ELSE 5 END, 6]",
            "ARRAY[ARRAY[(1 + 2), 3], ARRAY[4, (5 + 6)]]",
            "ARRAY[[(1 + 2), 3], [4, (5 + 6)]]", "ARRAY[]::integer[]",
            "ARRAY[ROW(1, 2), ROW(3, 4)]", "ARRAY[(1 < 2), (3 > 4)]"})
    void parsesGeneralExpressionsAtEveryArrayPosition(String expression) throws Exception {
        for (boolean complex : List.of(false, true)) {
            Statement statement = CCJSqlParserUtil.parse("SELECT " + expression,
                    p -> p.withDialect(Dialect.POSTGRESQL).withAllowComplexParsing(complex));
            roundTrip(statement);
        }
    }

    @Test
    void retainsCastAndParenthesesInTheElementAst() throws Exception {
        PlainSelect select = (PlainSelect) parse("SELECT ARRAY['a', (1 + 0)::text]");
        ArrayConstructor array = assertInstanceOf(ArrayConstructor.class,
                select.getSelectItem(0).getExpression());
        assertEquals(2, array.getExpressions().size());
        CastExpression cast = assertInstanceOf(CastExpression.class, array.getExpressions().get(1));
        assertInstanceOf(ParenthesedExpressionList.class, cast.getLeftExpression());
        cast.setLeftExpression(new LongValue(42));
        assertEquals("SELECT ARRAY['a', 42::text]", select.toString());
        roundTrip(select);
    }

    @Test
    void visitsElementsAndFindsTablesInScalarSubqueries() throws Exception {
        Statement statement = parse("SELECT ARRAY[1, (SELECT value FROM hidden), (2 + 3)]");
        assertThat(new TablesNamesFinder().getTables(statement)).containsExactly("hidden");
        List<Long> seen = new ArrayList<>();
        ExpressionDeParser expressions = new ExpressionDeParser() {
            @Override
            public <S> StringBuilder visit(LongValue value, S context) {
                seen.add(value.getValue());
                return getBuilder().append(value.getValue() + 10);
            }
        };
        StringBuilder output = new StringBuilder();
        statement.accept(new StatementDeParser(expressions, new SelectDeParser(), output), null);
        assertEquals(List.of(1L, 2L, 3L), seen);
        assertEquals("SELECT ARRAY[11, (SELECT value FROM hidden), (12 + 13)]", output.toString());
        roundTrip(parse(output.toString()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"SELECT ARRAY[1,]", "SELECT ARRAY[,1]", "SELECT ARRAY[(1 +)]",
            "SELECT ARRAY[1, (2 + 3]", "SELECT ARRAY[[1,2],]"})
    void rejectsIncompleteElements(String sql) {
        assertThrows(JSQLParserException.class, () -> parse(sql));
    }

    @ParameterizedTest
    @ValueSource(strings = {"SELECT [1, (2 + 3)]", "SELECT [(1 + 2), 3]",
            "SELECT ARRAY<INT64>[1, (2 + 3)]",
            "SELECT ARRAY[1:3]", "SELECT a[(1 + 2)] FROM t",
            "SELECT a.b[1:2].c FROM t", "SELECT ARRAY[ARRAY[], ARRAY[]]"})
    void preservesExistingArrayAndSubscriptForms(String sql) throws Exception {
        Statement statement = CCJSqlParserUtil.parse(sql);
        StringBuilder output = new StringBuilder();
        statement.accept(new StatementDeParser(output), null);
        assertEquals(statement.toString(), output.toString());
        assertEquals(output.toString(), CCJSqlParserUtil.parse(output.toString()).toString());
    }

    private static Statement parse(String sql) throws JSQLParserException {
        return CCJSqlParserUtil.parse(sql, p -> p.withDialect(Dialect.POSTGRESQL));
    }

    private static void roundTrip(Statement statement) throws Exception {
        StringBuilder output = new StringBuilder();
        statement.accept(new StatementDeParser(output), null);
        assertEquals(statement.toString(), output.toString());
        assertEquals(output.toString(), parse(output.toString()).toString());
    }
}
