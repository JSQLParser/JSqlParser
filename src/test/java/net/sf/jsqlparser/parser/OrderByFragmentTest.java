/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.List;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.OrderByDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class OrderByFragmentTest {
    @ParameterizedTest
    @ValueSource(strings = {"a DESC NULLS LAST, b ASC", "COALESCE(a, 0) DESC, b + ? ASC",
            "(1 + a) / (1 + b) DESC", "a COLLATE \"C\" ASC", "a WITH FILL FROM 1 TO 5"})
    void usesTheSameStructuredElementsAsAnOrderByClause(String fragment) throws Exception {
        List<OrderByElement> elements = CCJSqlParserUtil.parseOrderByElements(fragment);
        PlainSelect select =
                (PlainSelect) CCJSqlParserUtil.parse("SELECT * FROM t ORDER BY " + fragment);
        assertEquals(select.getOrderByElements().toString(), elements.toString());
        StringBuilder output = new StringBuilder();
        new OrderByDeParser(new ExpressionDeParser(null, output), output).deParse(elements);
        assertEquals(elements.toString(),
                CCJSqlParserUtil
                        .parseOrderByElements(output.toString().substring(" ORDER BY ".length()))
                        .toString());
    }

    @Test
    void exposesSortFlagsAndSupportsCustomExpressionRendering() throws Exception {
        List<OrderByElement> elements =
                CCJSqlParserUtil.parseOrderByElements("1 DESC NULLS LAST, 2 ASC");
        assertEquals(OrderByElement.NullOrdering.NULLS_LAST, elements.get(0).getNullOrdering());
        assertTrue(elements.get(1).isAscDescPresent());
        StringBuilder output = new StringBuilder();
        ExpressionDeParser expressions = new ExpressionDeParser(null, output) {
            @Override
            public <S> StringBuilder visit(LongValue value, S context) {
                return getBuilder().append(value.getValue() + 100);
            }
        };
        new OrderByDeParser(expressions, output).deParse(elements);
        assertEquals(" ORDER BY 101 DESC NULLS LAST, 102 ASC", output.toString());
    }

    @Test
    void appliesConfigurationAndStartsEachFragmentWithFreshParameterState() throws Exception {
        assertEquals("[my column] DESC", CCJSqlParserUtil.parseOrderByElements("[my column] DESC",
                parser -> parser.withSquareBracketQuotation(true)).get(0).toString());
        for (int i = 0; i < 2; i++) {
            OrderByElement element = CCJSqlParserUtil.parseOrderByElements("? DESC").get(0);
            assertEquals(1, ((JdbcParameter) element.getExpression()).getIndex());
        }
        assertTrue(CCJSqlParserUtil.parseOrderByElements(null).isEmpty());
        assertTrue(CCJSqlParserUtil.parseOrderByElements("").isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"a DESC LIMIT 1", "a DESC,", "a DESC; SELECT 1", "ORDER BY a", "a +"})
    void rejectsTrailingInputAndIncompleteElements(String fragment) {
        assertThrows(JSQLParserException.class,
                () -> CCJSqlParserUtil.parseOrderByElements(fragment));
    }
}
