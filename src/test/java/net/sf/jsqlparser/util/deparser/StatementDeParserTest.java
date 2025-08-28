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

import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.IfElseStatement;
import net.sf.jsqlparser.statement.SetStatement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.execute.Execute;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.ParenthesedSelect;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.TableStatement;
import net.sf.jsqlparser.statement.select.WithItem;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.update.UpdateSet;
import net.sf.jsqlparser.statement.upsert.Upsert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class StatementDeParserTest {

    @Mock
    private ExpressionDeParser expressionDeParser;

    @Mock
    private SelectDeParser selectDeParser;

    private StatementDeParser statementDeParser;

    private TableStatementDeParser tableStatementDeParser;

    @BeforeEach
    public void setUp() {
        tableStatementDeParser =
                new TableStatementDeParser(expressionDeParser, new StringBuilder());
        statementDeParser =
                new StatementDeParser(expressionDeParser, selectDeParser, new StringBuilder());
    }

    @Test
    @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
    public void shouldUseProvidedDeparsersWhenDeParsingDelete() {
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

        then(where).should().accept(expressionDeParser, null);
        then(orderByElement1Expression).should().accept(expressionDeParser, null);
        then(orderByElement2Expression).should().accept(expressionDeParser, null);
    }

    @Test
    @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
    public void shouldUseProvidedDeparsersWhenDeParsingInsert() {
        Insert insert = new Insert();
        Table table = new Table();
        List<UpdateSet> duplicateUpdateSets = new ArrayList<>();
        Column duplicateUpdateColumn1 = new Column();
        Expression duplicateUpdateExpression1 = mock(Expression.class);
        duplicateUpdateSets.add(new UpdateSet(duplicateUpdateColumn1, duplicateUpdateExpression1));

        Column duplicateUpdateColumn2 = new Column();
        Expression duplicateUpdateExpression2 = mock(Expression.class);
        duplicateUpdateSets.add(new UpdateSet(duplicateUpdateColumn2, duplicateUpdateExpression2));

        PlainSelect select = mock(PlainSelect.class);
        List<WithItem<?>> withItemsList = new ArrayList<WithItem<?>>();
        WithItem withItem1 = spy(new WithItem());
        WithItem withItem2 = spy(new WithItem());
        ParenthesedSelect withItem1SubSelect = mock(ParenthesedSelect.class);
        ParenthesedSelect withItem2SubSelect = mock(ParenthesedSelect.class);
        select.setWithItemsList(withItemsList);

        insert.setSelect(select);
        insert.setTable(table);
        insert.withDuplicateUpdateSets(duplicateUpdateSets);
        withItemsList.add(withItem1);
        withItemsList.add(withItem2);
        withItem1.setSelect(withItem1SubSelect);
        withItem2.setSelect(withItem2SubSelect);

        statementDeParser.visit(insert.withWithItemsList(withItemsList));

        then(withItem1).should().accept((SelectVisitor<?>) selectDeParser, null);
        then(withItem2).should().accept((SelectVisitor<?>) selectDeParser, null);
        then(select).should().accept((SelectVisitor<StringBuilder>) selectDeParser, null);
        then(duplicateUpdateExpression1).should().accept(expressionDeParser, null);
        then(duplicateUpdateExpression2).should().accept(expressionDeParser, null);
    }

    // @Test
    // @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
    // public void shouldUseProvidedDeParsersWhenDeParsingSelect() {
    // WithItem<?> withItem1 = spy(new WithItem<>());
    // withItem1.setSelect(mock(ParenthesedSelect.class));
    // WithItem<?> withItem2 = spy(new WithItem<>());
    // withItem2.setSelect(mock(ParenthesedSelect.class));
    //
    // List<WithItem<?>> withItemsList = new ArrayList<WithItem<?>>();
    // withItemsList.add(withItem1);
    // withItemsList.add(withItem2);
    //
    // PlainSelect plainSelect = mock(PlainSelect.class);
    // plainSelect.setWithItemsList(withItemsList);
    //
    // statementDeParser.visit(plainSelect);
    //
    // // then(withItem1).should().accept((SelectVisitor) selectDeParser);
    // // then(withItem2).should().accept((SelectVisitor) selectDeParser);
    // then(plainSelect).should().accept((SelectVisitor<StringBuilder>) selectDeParser, null);
    // }

    @Test
    @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
    public void shouldUseProvidedDeParsersWhenDeParsingUpdateNotUsingSelect() {
        Update update = new Update();
        Expression where = mock(Expression.class);
        List<OrderByElement> orderByElements = new ArrayList<OrderByElement>();
        Column column1 = new Column();
        Column column2 = new Column();
        Expression expression1 = mock(Expression.class);
        Expression expression2 = mock(Expression.class);
        OrderByElement orderByElement1 = new OrderByElement();
        OrderByElement orderByElement2 = new OrderByElement();
        Expression orderByElement1Expression = mock(Expression.class);
        Expression orderByElement2Expression = mock(Expression.class);

        update.setWhere(where);
        update.setOrderByElements(orderByElements);

        update.addUpdateSet(column1, expression1);
        update.addUpdateSet(column2, expression2);

        orderByElements.add(orderByElement1);
        orderByElements.add(orderByElement2);
        orderByElement1.setExpression(orderByElement1Expression);
        orderByElement2.setExpression(orderByElement2Expression);

        statementDeParser.visit(update);

        then(expressionDeParser).should().visit(column1, null);
        then(expressionDeParser).should().visit(column2, null);
        then(expression1).should().accept(expressionDeParser, null);
        then(expression2).should().accept(expressionDeParser, null);
        then(where).should().accept(expressionDeParser, null);
        then(orderByElement1Expression).should().accept(expressionDeParser, null);
        then(orderByElement2Expression).should().accept(expressionDeParser, null);
    }

    @Test
    @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
    public void shouldUseProvidedDeParsersWhenDeParsingUpdateUsingSelect() {
        Update update = new Update();
        List<Column> columns = new ArrayList<Column>();
        Expression where = mock(Expression.class);
        List<OrderByElement> orderByElements = new ArrayList<OrderByElement>();
        Column column1 = new Column();
        Column column2 = new Column();
        OrderByElement orderByElement1 = new OrderByElement();
        OrderByElement orderByElement2 = new OrderByElement();
        Expression orderByElement1Expression = mock(Expression.class);
        Expression orderByElement2Expression = mock(Expression.class);

        update.setWhere(where);
        update.setOrderByElements(orderByElements);

        UpdateSet updateSet = new UpdateSet();
        updateSet.add(column1);
        updateSet.add(column2);

        update.addUpdateSet(updateSet);

        orderByElements.add(orderByElement1);
        orderByElements.add(orderByElement2);
        orderByElement1.setExpression(orderByElement1Expression);
        orderByElement2.setExpression(orderByElement2Expression);

        statementDeParser.visit(update);

        then(expressionDeParser).should().visit(column1, null);
        then(expressionDeParser).should().visit(column2, null);
        then(where).should().accept(expressionDeParser, null);
        then(orderByElement1Expression).should().accept(expressionDeParser, null);
        then(orderByElement2Expression).should().accept(expressionDeParser, null);
    }

    @Test
    @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
    public void shouldUseProvidedDeParserWhenDeParsingExecute() {
        Execute execute = new Execute();
        ExpressionList<Expression> expressions = new ExpressionList<>();
        Expression expression1 = mock(Expression.class);
        Expression expression2 = mock(Expression.class);

        execute.setExprList(expressions);
        expressions.add(expression1);
        expressions.add(expression2);

        statementDeParser.visit(execute);

        then(expression1).should().accept(expressionDeParser, null);
        then(expression2).should().accept(expressionDeParser, null);
    }

    @Test
    @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
    public void shouldUseProvidedDeParserWhenDeParsingSetStatement() {
        String name = "name";
        Expression expression = mock(Expression.class);
        ExpressionList<Expression> expressions = new ExpressionList<>();
        expressions.add(expression);

        SetStatement setStatement = new SetStatement(name, expressions);

        statementDeParser.visit(setStatement);

        then(expression).should().accept(expressionDeParser, null);
    }

    // private Matcher<ReplaceDeParser> replaceDeParserWithDeParsers(final
    // Matcher<ExpressionDeParser> expressionDeParserMatcher, final Matcher<SelectDeParser>
    // selectDeParserMatcher) {
    // Description description = new StringDescription();
    // description.appendText("replace de-parser with expression de-parser ");
    // expressionDeParserMatcher.describeTo(description);
    // description.appendText(" and select de-parser ");
    // selectDeParserMatcher.describeTo(description);
    // return new CustomTypeSafeMatcher<ReplaceDeParser>(description.toString()) {
    // @Override
    // public boolean matchesSafely(ReplaceDeParser item) {
    // return expressionDeParserMatcher.matches(item.getExpressionVisitor()) &&
    // selectDeParserMatcher.matches(item.getSelectVisitor());
    // }
    // };
    // }
    @Test
    @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
    public void shouldUseProvidedDeparsersWhenDeParsingUpsertWithExpressionList() {
        Upsert upsert = new Upsert();
        Table table = new Table();
        List<Column> duplicateUpdateColumns = new ArrayList<Column>();
        List<Expression> duplicateUpdateExpressionList = new ArrayList<Expression>();
        Column duplicateUpdateColumn1 = new Column();
        Column duplicateUpdateColumn2 = new Column();
        Expression duplicateUpdateExpression1 = mock(Expression.class);
        Expression duplicateUpdateExpression2 = mock(Expression.class);
        PlainSelect select = mock(PlainSelect.class);
        List<WithItem<?>> withItemsList = new ArrayList<WithItem<?>>();
        WithItem withItem1 = spy(new WithItem());
        WithItem withItem2 = spy(new WithItem());
        ParenthesedSelect withItem1SubSelect = mock(ParenthesedSelect.class);
        ParenthesedSelect withItem2SubSelect = mock(ParenthesedSelect.class);
        select.setWithItemsList(withItemsList);

        upsert.setSelect(select);
        upsert.setTable(table);
        upsert.setDuplicateUpdateSets(
                Arrays.asList(
                        new UpdateSet(duplicateUpdateColumn1, duplicateUpdateExpression1),
                        new UpdateSet(duplicateUpdateColumn2, duplicateUpdateExpression2)));
        withItemsList.add(withItem1);
        withItemsList.add(withItem2);
        withItem1.setSelect(withItem1SubSelect);
        withItem2.setSelect(withItem2SubSelect);

        statementDeParser.visit(upsert);

        then(select).should().accept((SelectVisitor<StringBuilder>) selectDeParser, null);
        then(duplicateUpdateExpression1).should().accept(expressionDeParser, null);
        then(duplicateUpdateExpression1).should().accept(expressionDeParser, null);
    }

    @Test
    @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
    public void shouldUseProvidedDeparsersWhenDeParsingIfThenStatement()
            throws JSQLParserException {
        String sqlStr = "IF OBJECT_ID('tOrigin', 'U') IS NOT NULL DROP TABLE tOrigin1";
        IfElseStatement ifElseStatement = (IfElseStatement) CCJSqlParserUtil.parse(sqlStr);
        statementDeParser.deParse(ifElseStatement);
    }

    @Test
    public void testIssue1500AllColumns() throws JSQLParserException {
        String sqlStr = "select count(*) from some_table";
        PlainSelect selectBody = (PlainSelect) CCJSqlParserUtil.parse(sqlStr);
        selectBody.accept((SelectVisitor<StringBuilder>) new SelectDeParser(), null);
    }

    @Test
    public void testIssue1836() throws JSQLParserException {
        String sqlStr = "TABLE columns ORDER BY column_name LIMIT 10 OFFSET 10;";
        TableStatement tableStatement = (TableStatement) CCJSqlParserUtil.parse(sqlStr);
        tableStatement.accept(tableStatementDeParser, null);
    }

    @Test
    public void testIssue1500AllTableColumns() throws JSQLParserException {
        String sqlStr = "select count(a.*) from some_table a";
        PlainSelect selectBody = (PlainSelect) CCJSqlParserUtil.parse(sqlStr);
        selectBody.accept((SelectVisitor<StringBuilder>) new SelectDeParser(), null);
    }

    @Test
    public void testIssue1608DeparseValueList() throws JSQLParserException {
        String providedSql =
                "INSERT INTO example (num, name, address, tel) VALUES (1, 'name', 'test ', '1234-1234')";
        String expectedSql = "INSERT INTO example (num, name, address, tel) VALUES (?, ?, ?, ?)";

        net.sf.jsqlparser.statement.Statement statement = CCJSqlParserUtil.parse(providedSql);
        StringBuilder builder = new StringBuilder();
        ExpressionDeParser expressionDeParser = new ExpressionDeParser() {
            @Override
            public <K> StringBuilder visit(StringValue stringValue, K parameters) {
                builder.append("?");
                return null;
            }

            @Override
            public <K> StringBuilder visit(LongValue longValue, K parameters) {
                builder.append("?");
                return null;
            }
        };

        SelectDeParser selectDeParser = new SelectDeParser(expressionDeParser, builder);
        expressionDeParser.setSelectVisitor(selectDeParser);
        expressionDeParser.setBuilder(builder);

        StatementDeParser statementDeParser =
                new StatementDeParser(expressionDeParser, selectDeParser, builder);
        statement.accept(statementDeParser);

        Assertions.assertEquals(expectedSql, builder.toString());
    }
}
