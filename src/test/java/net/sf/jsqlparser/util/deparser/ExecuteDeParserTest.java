package net.sf.jsqlparser.util.deparser;

import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.statement.execute.Execute;

public class ExecuteDeParserTest {
    private ExecuteDeParser executeDeParser;

    @Mock
    private ExpressionVisitor expressionVisitor;

    private StringBuilder buffer;

    @Before
    public void setUp() {
        buffer = new StringBuilder();
        executeDeParser = new ExecuteDeParser(expressionVisitor, buffer);
    }

    @Test
    public void shouldDeParseExecute() {
        Execute execute = new Execute();
        String name = "name";
        ExpressionList exprList = new ExpressionList();
        List<Expression> expressions = new ArrayList<Expression>();
        Expression expression1 = mock(Expression.class);
        Expression expression2 = mock(Expression.class);

        execute.setName(name);
        execute.setExprList(exprList);
        exprList.setExpressions(expressions);
        expressions.add(expression1);
        expressions.add(expression2);

        executeDeParser.deParse(execute);

        String actual = buffer.toString();
        assertTrue(actual.matches("EXECUTE " + name + " .*?, .*"));
    }

    @Test
    public void shouldUseProvidedExpressionVisitorWhenDeParsingExecute() {
        Execute execute = new Execute();
        String name = "name";
        ExpressionList exprList = new ExpressionList();
        List<Expression> expressions = new ArrayList<Expression>();
        Expression expression1 = mock(Expression.class);
        Expression expression2 = mock(Expression.class);

        execute.setName(name);
        execute.setExprList(exprList);
        exprList.setExpressions(expressions);
        expressions.add(expression1);
        expressions.add(expression2);

        executeDeParser.deParse(execute);

        then(expression1).should().accept(expressionVisitor);
        then(expression2).should().accept(expressionVisitor);
    }
}
