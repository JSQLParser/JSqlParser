/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2022 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.test;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.ParenthesedExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.ParenthesedFromItem;
import net.sf.jsqlparser.statement.select.ParenthesedSelect;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SelectVisitorAdapter;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.select.Values;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.update.UpdateSet;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings("PMD")
public class HowToUseSample {
    //@formatter:off
    /*
     SQL Text
      └─Statements: net.sf.jsqlparser.statement.select.Select
          ├─selectItems -> Collection<SelectItem>
          │  └─LongValue: 1
          ├─Table: dual
          └─where: net.sf.jsqlparser.expression.operators.relational.EqualsTo
             ├─Column: a
             └─Column: b
     */
    //@formatter:on

    @Test
    void writeSQL() {
        String expectedSQLStr = "SELECT 1 FROM dual t WHERE a = b";

        // Step 1: generate the Java Object Hierarchy for
        Table table = new Table().withName("dual").withAlias(new Alias("t", false));

        Column columnA = new Column().withColumnName("a");
        Column columnB = new Column().withColumnName("b");
        Expression whereExpression =
                new EqualsTo().withLeftExpression(columnA).withRightExpression(columnB);

        PlainSelect select = new PlainSelect().addSelectItem(new LongValue(1))
                .withFromItem(table).withWhere(whereExpression);

        // Step 2a: Print into a SQL Statement
        Assertions.assertEquals(expectedSQLStr, select.toString());

        // Step 2b: De-Parse into a SQL Statement
        StringBuilder builder = new StringBuilder();
        StatementDeParser deParser = new StatementDeParser(builder);
        deParser.visit(select);

        Assertions.assertEquals(expectedSQLStr, builder.toString());
    }

    @Test
    public void howToParseStatementDeprecated() throws JSQLParserException {
        String sqlStr = "select 1 from dual where a=b";

        Statement statement = CCJSqlParserUtil.parse(sqlStr);
        if (statement instanceof Select) {
            Select select = (Select) statement;
            PlainSelect plainSelect = (PlainSelect) select.getSelectBody();

            SelectItem selectItem =
                    (SelectItem) plainSelect.getSelectItems().get(0);

            Table table = (Table) plainSelect.getFromItem();

            EqualsTo equalsTo = (EqualsTo) plainSelect.getWhere();
            Column a = (Column) equalsTo.getLeftExpression();
            Column b = (Column) equalsTo.getRightExpression();
        }
    }

    @Test
    public void howToParseStatement() throws JSQLParserException {
        String sqlStr = "select 1 from dual where a=b";

        PlainSelect select = (PlainSelect) CCJSqlParserUtil.parse(sqlStr);

        SelectItem selectItem =
                select.getSelectItems().get(0);
        Assertions.assertEquals(
                new LongValue(1), selectItem.getExpression());

        Table table = (Table) select.getFromItem();
        Assertions.assertEquals("dual", table.getName());

        EqualsTo equalsTo = (EqualsTo) select.getWhere();
        Column a = (Column) equalsTo.getLeftExpression();
        Column b = (Column) equalsTo.getRightExpression();
        Assertions.assertEquals("a", a.getColumnName());
        Assertions.assertEquals("b", b.getColumnName());
    }

    @Test
    public void howToUseVisitors() throws JSQLParserException {

        // Define an Expression Visitor reacting on any Expression
        // Overwrite the visit() methods for each Expression Class
        ExpressionVisitorAdapter expressionVisitorAdapter = new ExpressionVisitorAdapter() {
            public void visit(EqualsTo equalsTo) {
                equalsTo.getLeftExpression().accept(this);
                equalsTo.getRightExpression().accept(this);
            }

            public void visit(Column column) {
                System.out.println("Found a Column " + column.getColumnName());
            }
        };

        // Define a Select Visitor reacting on a Plain Select invoking the Expression Visitor on the
        // Where Clause
        SelectVisitorAdapter selectVisitorAdapter = new SelectVisitorAdapter() {
            @Override
            public void visit(PlainSelect plainSelect) {
                plainSelect.getWhere().accept(expressionVisitorAdapter);
            }
        };

        // Define a Statement Visitor for dispatching the Statements
        StatementVisitorAdapter statementVisitor = new StatementVisitorAdapter() {
            public void visit(Select select) {
                select.accept(selectVisitorAdapter);
            }
        };

        String sqlStr = "select 1 from dual where a=b";
        Statement stmt = CCJSqlParserUtil.parse(sqlStr);

        // Invoke the Statement Visitor
        stmt.accept(statementVisitor);
    }

    @Test
    public void howToUseFeatures() throws JSQLParserException {

        String sqlStr = "select 1 from [sample_table] where [a]=[b]";

        // T-SQL Square Bracket Quotation
        Statement stmt =
                CCJSqlParserUtil.parse(sqlStr, parser -> parser.withSquareBracketQuotation(true));

        // Set Parser Timeout to 6000 ms
        Statement stmt1 = CCJSqlParserUtil.parse(sqlStr,
                parser -> parser.withSquareBracketQuotation(true).withTimeOut(6000));

        // Allow Complex Parsing (which allows nested Expressions, but is much slower)
        Statement stmt2 = CCJSqlParserUtil.parse(sqlStr, parser -> parser
                .withSquareBracketQuotation(true).withAllowComplexParsing(true).withTimeOut(6000));
    }

    @Test
    public void showBracketHandling() throws JSQLParserException {
        String sqlStr = " ( (values(1,2), (3,4)) UNION (values((1,2), (3,4))) )";
        Statement statement = CCJSqlParserUtil.parse(sqlStr);
        final String reflectionString = TestUtils.toReflectionString(statement);

        System.out.println(reflectionString);
    }

    @Test
    void migrationTest1() throws JSQLParserException {
        String sqlStr = "VALUES ( 1, 2, 3 )";

        Values values = (Values) CCJSqlParserUtil.parse(sqlStr);
        Assertions.assertEquals(3, values.getExpressions().size());
    }

    @Test
    void migrationTest2() throws JSQLParserException {
        String sqlStr = "SELECT *\n" +
                "        FROM ( VALUES 1, 2, 3 )";

        PlainSelect select = (PlainSelect) CCJSqlParserUtil.parse(sqlStr);
        ParenthesedSelect subSelect = (ParenthesedSelect) select.getFromItem();
        Values values = (Values) subSelect.getSelect();
        Assertions.assertEquals(3, values.getExpressions().size());
    }

    @Test
    void migrationTest3() throws JSQLParserException {
        String sqlStr = "UPDATE test\n" +
                "        SET (   a\n" +
                "                , b\n" +
                "                , c ) = ( VALUES 1, 2, 3 )";

        Update update = (Update) CCJSqlParserUtil.parse(sqlStr);
        UpdateSet updateSet = update.getUpdateSets().get(0);
        ParenthesedSelect subSelect = (ParenthesedSelect) updateSet.getValues().get(0);
        Values values = (Values) subSelect.getSelect();
        Assertions.assertEquals(3, values.getExpressions().size());
    }

    @Test
    void migrationTest4() throws JSQLParserException {
        String sqlStr = "INSERT INTO test\n" +
                "        VALUES ( 1, 2, 3 )\n" +
                "        ;";

        Insert insert = (Insert) CCJSqlParserUtil.parse(sqlStr);
        Values values = (Values) insert.getSelect();
        Assertions.assertEquals(3, values.getExpressions().size());
    }

    @Test
    void migrationTest5() throws JSQLParserException {
        String sqlStr = "SELECT Function( a, b, c )\n" +
                "        FROM dual\n" +
                "        GROUP BY    a\n" +
                "                    , b\n" +
                "                    , c";

        PlainSelect select = (PlainSelect) CCJSqlParserUtil.parse(sqlStr);
        Function function = (Function) select.getSelectItem(0).getExpression();
        Assertions.assertEquals(3, function.getParameters().size());

        ExpressionList<?> groupByExpressions = select.getGroupBy().getGroupByExpressionList();
        Assertions.assertEquals(3, groupByExpressions.size());
    }

    @Test
    void migrationTest6() throws JSQLParserException {
        String sqlStr = "(\n" +
                "    SELECT *\n" +
                "    FROM (  SELECT 1 )\n" +
                "    UNION ALL\n" +
                "    SELECT *\n" +
                "    FROM ( VALUES 1, 2, 3 )\n" +
                "    UNION ALL\n" +
                "    VALUES ( 1, 2, 3 ) )";

        ParenthesedSelect parenthesedSelect = (ParenthesedSelect) CCJSqlParserUtil.parse(sqlStr);
        SetOperationList setOperationList = parenthesedSelect.getSetOperationList();

        PlainSelect select1 = (PlainSelect) setOperationList.getSelect(0);
        PlainSelect subSelect1 = ((ParenthesedSelect) select1.getFromItem()).getPlainSelect();
        Assertions.assertEquals(1L,
                subSelect1.getSelectItem(0).getExpression(LongValue.class).getValue());

        Values values = (Values) setOperationList.getSelect(2);
        Assertions.assertEquals(3, values.getExpressions().size());
    }

    @Test
    void migrationTest7() throws JSQLParserException {
        String sqlStr = "SELECT *\n" +
                "FROM a\n" +
                "  INNER JOIN (  b\n" +
                "                  LEFT JOIN c\n" +
                "                    ON b.d = c.d )\n" +
                "    ON a.e = b.e";

        PlainSelect select = (PlainSelect) CCJSqlParserUtil.parse(sqlStr);
        Table aTable = (Table) select.getFromItem();

        ParenthesedFromItem fromItem = (ParenthesedFromItem) select.getJoin(0).getFromItem();
        Table bTable = (Table) fromItem.getFromItem();

        Join join = fromItem.getJoin(0);
        Table cTable = (Table) join.getFromItem();

        Assertions.assertEquals("c", cTable.getName());
    }

    @Test
    void migrationTest8() throws JSQLParserException {
        String sqlStr = "SELECT ( ( 1, 2, 3 ), ( 4, 5, 6 ), ( 7, 8, 9 ) )";

        PlainSelect select = (PlainSelect) CCJSqlParserUtil.parse(sqlStr);
        ParenthesedExpressionList<?> expressionList =
                (ParenthesedExpressionList<?>) select.getSelectItem(0).getExpression();

        ParenthesedExpressionList<?> expressionList1 =
                (ParenthesedExpressionList<?>) expressionList.get(0);
        Assertions.assertEquals(3, expressionList1.size());
    }

    @Test
    void migrationTest9() throws JSQLParserException {
        String sqlStr = "UPDATE a\n" +
                "SET (   a\n" +
                "        , b\n" +
                "        , c ) = (   1\n" +
                "                    , 2\n" +
                "                    , 3 )\n" +
                "    , d = 4";

        Update update = (Update) CCJSqlParserUtil.parse(sqlStr);
        UpdateSet updateSet1 = update.getUpdateSet(0);
        Assertions.assertEquals(3, updateSet1.getColumns().size());
        Assertions.assertEquals(3, updateSet1.getValues().size());

        UpdateSet updateSet2 = update.getUpdateSet(1);
        Assertions.assertEquals("d", updateSet2.getColumn(0).getColumnName());
        Assertions.assertEquals(4L, ((LongValue) updateSet2.getValue(0)).getValue());
    }
}
