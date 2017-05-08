package net.sf.jsqlparser.util.deparser;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasToString;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.parser.JSqlParser;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.OrderByElement;

@RunWith(MockitoJUnitRunner.class)
public class StatementDeParserTest {
    @Mock
    private ExpressionDeParser expressionDeParser;

    private StringBuilder buffer;

    private StatementDeParser statementDeParser;

    @Before
    public void setUp() {
        buffer = new StringBuilder();
        statementDeParser = new StatementDeParser(expressionDeParser, buffer);
    }

    @Test
    public void shouldUseProvidedExpressionDeparserWhenDeParsingDelete() {
        Delete delete = new Delete();
        Table table = new Table();
        Expression where = mock(Expression.class);
        List<OrderByElement> orderByElements = new ArrayList<OrderByElement>();
        OrderByElement orderByElement1 = new OrderByElement();
        OrderByElement orderByElement2 = new OrderByElement();
        Expression orderByElement1Expression = mock(Expression.class);
        Expression orderByElement2Expression = mock(Expression.class);

        delete.setTable(table);
        delete.setWhere(where);
        delete.setOrderByElements(orderByElements);
        orderByElements.add(orderByElement1);
        orderByElements.add(orderByElement2);
        orderByElement1.setExpression(orderByElement1Expression);
        orderByElement2.setExpression(orderByElement2Expression);

        statementDeParser.visit(delete);

        then(where).should().accept(expressionDeParser);
        then(orderByElement1Expression).should().accept(expressionDeParser);
        then(orderByElement2Expression).should().accept(expressionDeParser);
    }

    private JSqlParser jSqlParser = new CCJSqlParserManager();

    @Test
    public void shouldUseProvidedExpressionDeparserWhenDeParsingInsert() throws JSQLParserException {
        Insert insert = (Insert) jSqlParser.parse(new StringReader("INSERT INTO mytable (col1) VALUES (123), (456) ON DUPLICATE KEY UPDATE col3 = 789")); 

        statementDeParser.visit(insert);

        then(expressionDeParser).should().visit((LongValue) argThat(hasToString(equalTo("123"))));
        then(expressionDeParser).should().visit((LongValue) argThat(hasToString(equalTo("456"))));
        then(expressionDeParser).should().visit((LongValue) argThat(hasToString(equalTo("789"))));
    }
}
