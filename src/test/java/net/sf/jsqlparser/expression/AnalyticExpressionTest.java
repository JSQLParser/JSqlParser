package net.sf.jsqlparser.expression;

import static org.mockito.BDDMockito.then;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import net.sf.jsqlparser.util.deparser.ExpressionDeParser;

@RunWith(MockitoJUnitRunner.class)
public class AnalyticExpressionTest {
    private AnalyticExpression analyticExpression;

    @Mock
    private ExpressionDeParser expressionDeParser;

    @Before
    public void setUp() {
        analyticExpression = new AnalyticExpression(expressionDeParser);
    }

    @Test
    public void shouldDeParseOnToString() {
        analyticExpression.toString();
        then(expressionDeParser).should().visit(analyticExpression);
    }
}
