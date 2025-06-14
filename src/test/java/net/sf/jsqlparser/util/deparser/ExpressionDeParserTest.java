/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.deparser;

import net.sf.jsqlparser.expression.AnalyticExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.KeepExpression;
import net.sf.jsqlparser.expression.WindowElement;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.will;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class ExpressionDeParserTest {

    private ExpressionDeParser expressionDeParser;

    @Mock
    private SelectVisitor<StringBuilder> selectVisitor;

    private StringBuilder buffer;

    @Mock
    private OrderByDeParser orderByDeParser;

    @BeforeEach
    public void setUp() {
        buffer = new StringBuilder();
        expressionDeParser = new ExpressionDeParser(selectVisitor, buffer, orderByDeParser);
    }

    @Test
    public void shouldDeParseSimplestAnalyticExpression() {
        AnalyticExpression analyticExpression = new AnalyticExpression();
        analyticExpression.setName("name");
        expressionDeParser.visit(analyticExpression, null);
        assertEquals("name() OVER ()", buffer.toString());
    }

    @Test
    public void shouldDeParseAnalyticExpressionWithExpression() {
        AnalyticExpression analyticExpression = new AnalyticExpression();
        Expression expression = mock(Expression.class);

        analyticExpression.setName("name");
        analyticExpression.setExpression(expression);

        will(appendToBuffer("expression")).given(expression).accept(expressionDeParser, null);

        expressionDeParser.visit(analyticExpression, null);

        assertEquals("name(expression) OVER ()", buffer.toString());
    }

    @Test
    public void shouldDeParseAnalyticExpressionWithOffset() {
        AnalyticExpression analyticExpression = new AnalyticExpression();
        Expression expression = mock(Expression.class);
        Expression offset = mock(Expression.class);

        analyticExpression.setName("name");
        analyticExpression.setExpression(expression);
        analyticExpression.setOffset(offset);

        will(appendToBuffer("expression")).given(expression).accept(expressionDeParser, null);
        will(appendToBuffer("offset")).given(offset).accept(expressionDeParser, null);

        expressionDeParser.visit(analyticExpression, null);

        assertEquals("name(expression, offset) OVER ()", buffer.toString());
    }

    @Test
    public void shouldDeParseAnalyticExpressionWithDefaultValue() {
        AnalyticExpression analyticExpression = new AnalyticExpression();
        Expression expression = mock(Expression.class);
        Expression offset = mock(Expression.class);
        Expression defaultValue = mock(Expression.class);

        analyticExpression.setName("name");
        analyticExpression.setExpression(expression);
        analyticExpression.setOffset(offset);
        analyticExpression.setDefaultValue(defaultValue);

        will(appendToBuffer("expression")).given(expression).accept(expressionDeParser, null);
        will(appendToBuffer("offset")).given(offset).accept(expressionDeParser, null);
        will(appendToBuffer("default value")).given(defaultValue).accept(expressionDeParser, null);

        expressionDeParser.visit(analyticExpression, null);

        assertEquals("name(expression, offset, default value) OVER ()", buffer.toString());
    }

    @Test
    public void shouldDeParseAnalyticExpressionWithAllColumns() {
        AnalyticExpression analyticExpression = new AnalyticExpression();

        analyticExpression.setName("name");
        analyticExpression.setAllColumns(true);

        expressionDeParser.visit(analyticExpression, null);

        assertEquals("name(*) OVER ()", buffer.toString());
    }

    @Test
    public void shouldDeParseComplexAnalyticExpressionWithKeep() {
        AnalyticExpression analyticExpression = new AnalyticExpression();
        KeepExpression keep = mock(KeepExpression.class);

        analyticExpression.setName("name");
        analyticExpression.setKeep(keep);

        will(appendToBuffer("keep")).given(keep).accept(expressionDeParser, null);

        expressionDeParser.visit(analyticExpression, null);

        assertEquals("name() keep OVER ()", buffer.toString());
    }

    @Test
    public void shouldDeParseComplexAnalyticExpressionWithPartitionExpressionList() {
        AnalyticExpression analyticExpression = new AnalyticExpression();
        ExpressionList<Expression> partitionExpressionList = new ExpressionList<>();
        Expression partitionExpression1 = mock(Expression.class);
        Expression partitionExpression2 = mock(Expression.class);

        partitionExpressionList.add(partitionExpression1);
        partitionExpressionList.add(partitionExpression2);

        analyticExpression.setName("name");
        analyticExpression.setPartitionExpressionList(partitionExpressionList);
        will(appendToBuffer("partition expression 1")).given(partitionExpression1)
                .accept(expressionDeParser, null);
        will(appendToBuffer("partition expression 2")).given(partitionExpression2)
                .accept(expressionDeParser, null);

        expressionDeParser.visit(analyticExpression, null);

        assertEquals("name() OVER (PARTITION BY partition expression 1, partition expression 2 )",
                buffer.toString());
    }

    @Test
    public void shouldDeParseAnalyticExpressionWithOrderByElements() {
        AnalyticExpression analyticExpression = new AnalyticExpression();
        List<OrderByElement> orderByElements = new ArrayList<OrderByElement>();
        OrderByElement orderByElement1 = mock(OrderByElement.class);
        OrderByElement orderByElement2 = mock(OrderByElement.class);

        analyticExpression.setName("name");
        analyticExpression.setOrderByElements(orderByElements);
        orderByElements.add(orderByElement1);
        orderByElements.add(orderByElement2);

        will(appendToBuffer("order by element 1")).given(orderByDeParser)
                .deParseElement(orderByElement1);
        will(appendToBuffer("order by element 2")).given(orderByDeParser)
                .deParseElement(orderByElement2);

        expressionDeParser.visit(analyticExpression, null);

        assertEquals("name() OVER (ORDER BY order by element 1, order by element 2)",
                buffer.toString());
    }

    @Test
    public void shouldDeParseAnalyticExpressionWithWindowElement() {
        AnalyticExpression analyticExpression = new AnalyticExpression();
        List<OrderByElement> orderByElements = new ArrayList<OrderByElement>();
        OrderByElement orderByElement1 = mock(OrderByElement.class);
        OrderByElement orderByElement2 = mock(OrderByElement.class);
        WindowElement windowElement = mock(WindowElement.class);

        analyticExpression.setName("name");
        analyticExpression.setOrderByElements(orderByElements);
        analyticExpression.setWindowElement(windowElement);
        orderByElements.add(orderByElement1);
        orderByElements.add(orderByElement2);

        will(appendToBuffer("order by element 1")).given(orderByDeParser)
                .deParseElement(orderByElement1);
        will(appendToBuffer("order by element 2")).given(orderByDeParser)
                .deParseElement(orderByElement2);
        given(windowElement.toString()).willReturn("window element");

        expressionDeParser.visit(analyticExpression, null);

        assertEquals("name() OVER (ORDER BY order by element 1, order by element 2 window element)",
                buffer.toString());
    }

    private Answer<Void> appendToBuffer(final String string) {
        return new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                buffer.append(string);
                return null;
            }
        };
    }
}
