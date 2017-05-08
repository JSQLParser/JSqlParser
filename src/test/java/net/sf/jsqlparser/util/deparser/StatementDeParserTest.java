package net.sf.jsqlparser.util.deparser;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.WithItem;

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

    @Test
    public void shouldUseProvidedExpressionDeparserWhenDeParsingInsert() throws JSQLParserException {
        Insert insert = new Insert();
        Table table = new Table();
        List<Column> duplicateUpdateColumns = new ArrayList<Column>();
        List<Expression> duplicateUpdateExpressionList = new ArrayList<Expression>();
        Column duplicateUpdateColumn1 = new Column();
        Column duplicateUpdateColumn2 = new Column();
        Expression duplicateUpdateExpression1 = mock(Expression.class);
        Expression duplicateUpdateExpression2 = mock(Expression.class);
        Select select = new Select();
        List<WithItem> withItemsList = new ArrayList<WithItem>();
        WithItem withItem1 = spy(new WithItem());
        WithItem withItem2 = spy(new WithItem());
        SelectBody withItem1SelectBody = mock(SelectBody.class);
        SelectBody withItem2SelectBody = mock(SelectBody.class);
        SelectBody selectBody = mock(SelectBody.class);

        insert.setSelect(select);
        insert.setTable(table);
        insert.setUseDuplicate(true);
        insert.setDuplicateUpdateColumns(duplicateUpdateColumns);
        insert.setDuplicateUpdateExpressionList(duplicateUpdateExpressionList);
        duplicateUpdateColumns.add(duplicateUpdateColumn1);
        duplicateUpdateColumns.add(duplicateUpdateColumn2);
        duplicateUpdateExpressionList.add(duplicateUpdateExpression1);
        duplicateUpdateExpressionList.add(duplicateUpdateExpression2);
        insert.setDuplicateUpdateExpressionList(duplicateUpdateExpressionList);
        select.setWithItemsList(withItemsList);
        select.setSelectBody(selectBody);
        withItemsList.add(withItem1);
        withItemsList.add(withItem2);
        withItem1.setSelectBody(withItem1SelectBody);
        withItem2.setSelectBody(withItem2SelectBody);

        statementDeParser.visit(insert);

        then(withItem1).should().accept(argThat(isSelectDeParserWithExpressionDeParser(expressionDeParser)));
        then(withItem2).should().accept(argThat(isSelectDeParserWithExpressionDeParser(expressionDeParser)));
        then(selectBody).should().accept(argThat(isSelectDeParserWithExpressionDeParser(expressionDeParser)));
        then(duplicateUpdateExpression1).should().accept(expressionDeParser);
        then(duplicateUpdateExpression1).should().accept(expressionDeParser);
    }

    private ArgumentMatcher<SelectDeParser> isSelectDeParserWithExpressionDeParser(final ExpressionDeParser expressionDeParser) {
        return new ArgumentMatcher<SelectDeParser>() {
            @Override
            public boolean matches(SelectDeParser argument) {
                return argument.getExpressionVisitor().equals(expressionDeParser);
            }
        };
    }
}
