package net.sf.jsqlparser.util.deparser;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import net.sf.jsqlparser.expression.AnalyticExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.KeepExpression;
import net.sf.jsqlparser.expression.WindowElement;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.SelectVisitor;

public class ExpressionDeParserTest {
    private ExpressionDeParser expressionDeParser;

    @Mock
    private SelectVisitor selectVisitor;

    private StringBuilder buffer;

    @Before
    public void setUp() {
        buffer = new StringBuilder();
        expressionDeParser = new ExpressionDeParser(selectVisitor, buffer);
    }

    @Test
    public void shouldDeParseSimplestAnalyticExpression() {
        AnalyticExpression analyticExpression = new AnalyticExpression();
        analyticExpression.setName("name");
        expressionDeParser.visit(analyticExpression);
        assertEquals("name() OVER ()", buffer.toString());
    }

    @Test
    public void shouldDeParseAnalyticExpressionWithExpression() {
        AnalyticExpression analyticExpression = new AnalyticExpression();
        Expression expression = mock(Expression.class);

        analyticExpression.setName("name");
        analyticExpression.setExpression(expression);

        given(expression.toString()).willReturn("expression");

        expressionDeParser.visit(analyticExpression);

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

        given(expression.toString()).willReturn("expression");
        given(offset.toString()).willReturn("offset");

        expressionDeParser.visit(analyticExpression);

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

        given(expression.toString()).willReturn("expression");
        given(offset.toString()).willReturn("offset");
        given(defaultValue.toString()).willReturn("default value");

        expressionDeParser.visit(analyticExpression);

        assertEquals("name(expression, offset, default value) OVER ()", buffer.toString());
    }

    @Test
    public void shouldDeParseAnalyticExpressionWithAllColumns() {
        AnalyticExpression analyticExpression = new AnalyticExpression();

        analyticExpression.setName("name");
        analyticExpression.setAllColumns(true);

        expressionDeParser.visit(analyticExpression);

        assertEquals("name(*) OVER ()", buffer.toString());
    }

    @Test
    public void shouldDeParseComplexAnalyticExpressionWithKeep() {
        AnalyticExpression analyticExpression = new AnalyticExpression();
        KeepExpression keep = mock(KeepExpression.class);

        analyticExpression.setName("name");
        analyticExpression.setKeep(keep);

        given(keep.toString()).willReturn("keep");

        expressionDeParser.visit(analyticExpression);

        assertEquals("name() keep OVER ()", buffer.toString());
    }

    @Test
    public void shouldDeParseComplexAnalyticExpressionWithPartitionExpressionList() {
        AnalyticExpression analyticExpression = new AnalyticExpression();
        ExpressionList partitionExpressionList = new ExpressionList();
        List<Expression> partitionExpressions = new ArrayList<Expression>();
        Expression partitionExpression1 = mock(Expression.class);
        Expression partitionExpression2 = mock(Expression.class);

        analyticExpression.setName("name");
        analyticExpression.setPartitionExpressionList(partitionExpressionList);
        partitionExpressionList.setExpressions(partitionExpressions);
        partitionExpressions.add(partitionExpression1);
        partitionExpressions.add(partitionExpression2);

        given(partitionExpression1.toString()).willReturn("partition expression 1");
        given(partitionExpression2.toString()).willReturn("partition expression 2");

        expressionDeParser.visit(analyticExpression);

        assertEquals("name() OVER (PARTITION BY partition expression 1, partition expression 2 )", buffer.toString());
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

        given(orderByElement1.toString()).willReturn("order by element 1");
        given(orderByElement2.toString()).willReturn("order by element 2");

        expressionDeParser.visit(analyticExpression);

        assertEquals("name() OVER (ORDER BY order by element 1, order by element 2)", buffer.toString());
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

        given(orderByElement1.toString()).willReturn("order by element 1");
        given(orderByElement2.toString()).willReturn("order by element 2");
        given(windowElement.toString()).willReturn("window element");

        expressionDeParser.visit(analyticExpression);

        assertEquals("name() OVER (ORDER BY order by element 1, order by element 2 window element)", buffer.toString());
    }
}
