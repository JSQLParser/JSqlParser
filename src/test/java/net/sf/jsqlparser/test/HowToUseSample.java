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
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.ParseException;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.AllColumns;
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

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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

        SelectItem<?> selectItem =
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
        ExpressionVisitorAdapter<Void> expressionVisitorAdapter =
                new ExpressionVisitorAdapter<Void>() {
                    public <K> Void visit(EqualsTo equalsTo, K context) {
                        equalsTo.getLeftExpression().accept(this, context);
                        equalsTo.getRightExpression().accept(this, context);
                        return null;
                    }

                    public <K> Void visit(Column column, K context) {
                        System.out.println("Found a Column " + column.getColumnName());
                        return null;
                    }
                };

        // Define a Select Visitor reacting on a Plain Select invoking the Expression Visitor on the
        // Where Clause
        SelectVisitorAdapter<Void> selectVisitorAdapter = new SelectVisitorAdapter<Void>() {
            @Override
            public <K> Void visit(PlainSelect plainSelect, K context) {
                return plainSelect.getWhere().accept(expressionVisitorAdapter, context);
            }
        };

        // Define a Statement Visitor for dispatching the Statements
        StatementVisitorAdapter<Void> statementVisitor = new StatementVisitorAdapter<Void>() {
            public <K> Void visit(Select select, K context) {
                return select.accept(selectVisitorAdapter, context);
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
        ParenthesedFromItem fromItem = (ParenthesedFromItem) select.getFromItem();
        Values values = (Values) fromItem.getFromItem();
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

    @Test
    void migrationTest10() throws JSQLParserException {
        String sqlStr = "INSERT INTO target SELECT * FROM source";

        PlainSelect select = new PlainSelect()
                .addSelectItem(new AllColumns())
                .withFromItem(new Table("source"));

        Insert insert = new Insert()
                .withTable(new Table("target"))
                .withSelect(select);

        TestUtils.assertStatementCanBeDeparsedAs(insert, sqlStr);
    }

    @Test
    void migrationTest11() throws JSQLParserException {
        String sqlStr = "INSERT INTO target VALUES (1, 2, 3)";

        Values values = new Values()
                .addExpressions(
                        new LongValue(1), new LongValue(2), new LongValue(3));

        Insert insert = new Insert()
                .withTable(new Table("target"))
                .withSelect(values);

        TestUtils.assertStatementCanBeDeparsedAs(insert, sqlStr);
    }

    @Test
    void testComplexParsingOnly() throws Exception {
        String sqlStr = "SELECT  e.id\n" +
                "        , e.code\n" +
                "        , e.review_type\n" +
                "        , e.review_object\n" +
                "        , e.review_first_datetime AS reviewfirsttime\n" +
                "        , e.review_latest_datetime AS reviewnewtime\n" +
                "        , e.risk_event\n" +
                "        , e.risk_detail\n" +
                "        , e.risk_grade\n" +
                "        , e.risk_status\n" +
                "        , If( e.deal_type IS NULL\n" +
                "            OR e.deal_type = '', '--', e.deal_type ) AS dealtype\n" +
                "        , e.deal_result\n" +
                "        , If( e.deal_remark IS NULL\n" +
                "            OR e.deal_remark = '', '--', e.deal_remark ) AS dealremark\n" +
                "        , e.is_deleted\n" +
                "        , e.review_object_id\n" +
                "        , e.archive_id\n" +
                "        , e.feature AS featurename\n" +
                "        , Ifnull( ( SELECT real_name\n" +
                "                    FROM bladex.blade_user\n" +
                "                    WHERE id = e.review_first_user ), ( SELECT DISTINCT\n" +
                "                                                            real_name\n" +
                "                                                        FROM app_sys.asys_uniapp_rn_auth\n"
                +
                "                                                        WHERE uniapp_user_id = e.review_first_user\n"
                +
                "                                                            AND is_disable = 0 ) ) AS reviewfirstuser\n"
                +
                "        , Ifnull( ( SELECT real_name\n" +
                "                    FROM bladex.blade_user\n" +
                "                    WHERE id = e.review_latest_user ), (    SELECT DISTINCT\n" +
                "                                                                real_name\n" +
                "                                                            FROM app_sys.asys_uniapp_rn_auth\n"
                +
                "                                                            WHERE uniapp_user_id = e.review_latest_user\n"
                +
                "                                                                AND is_disable = 0 ) ) AS reviewnewuser\n"
                +
                "        , If( ( SELECT real_name\n" +
                "                FROM bladex.blade_user\n" +
                "                WHERE id = e.deal_user ) IS NOT NULL\n" +
                "            AND e.deal_user != - 9999, (    SELECT real_name\n" +
                "                                            FROM bladex.blade_user\n" +
                "                                            WHERE id = e.deal_user ), '--' ) AS dealuser\n"
                +
                "        , CASE\n" +
                "                WHEN 'COMPANY'\n" +
                "                    THEN Concat( (  SELECT ar.customer_name\n" +
                "                                    FROM mtp_cs.mtp_rsk_cust_archive ar\n" +
                "                                    WHERE ar.is_deleted = 0\n" +
                "                                        AND ar.id = e.archive_id ), If( (   SELECT alias\n"
                +
                "                                                                            FROM web_crm.wcrm_customer\n"
                +
                "                                                                            WHERE id = e.customer_id ) = ''\n"
                +
                "                OR (    SELECT alias\n" +
                "                        FROM web_crm.wcrm_customer\n" +
                "                        WHERE id = e.customer_id ) IS NULL, ' ', Concat( '（', ( SELECT alias\n"
                +
                "                                                                                FROM web_crm.wcrm_customer\n"
                +
                "                                                                                WHERE id = e.customer_id ), '）' ) ) )\n"
                +
                "                WHEN 'EMPLOYEE'\n" +
                "                    THEN (  SELECT Concat( auth.real_name, ' ', auth.phone )\n" +
                "                            FROM app_sys.asys_uniapp_rn_auth auth\n" +
                "                            WHERE auth.is_disable = 0\n" +
                "                                AND auth.uniapp_user_id = e.uniapp_user_id )\n" +
                "                WHEN 'DEAL'\n" +
                "                    THEN (  SELECT DISTINCT\n" +
                "                                Concat( batch.code, '-', detail.line_seq\n" +
                "                                        , ' ', Ifnull( (    SELECT DISTINCT\n" +
                "                                                                auth.real_name\n" +
                "                                                            FROM app_sys.asys_uniapp_rn_auth auth\n"
                +
                "                                                            WHERE auth.uniapp_user_id = e.uniapp_user_id\n"
                +
                "                                                                AND auth.is_disable = 0 ), ' ' ) )\n"
                +
                "                            FROM web_pym.wpym_payment_batch_detail detail\n" +
                "                                LEFT JOIN web_pym.wpym_payment_batch batch\n" +
                "                                    ON detail.payment_batch_id = batch.id\n" +
                "                            WHERE detail.id = e.review_object_id )\n" +
                "                WHEN 'TASK'\n" +
                "                    THEN (  SELECT code\n" +
                "                            FROM web_tm.wtm_task task\n" +
                "                            WHERE e.review_object_id = task.id )\n" +
                "                ELSE NULL\n" +
                "            END AS reviewobjectname\n" +
                "        , CASE\n" +
                "                WHEN 4\n" +
                "                    THEN 'HIGH_LEVEL'\n" +
                "                WHEN 3\n" +
                "                    THEN 'MEDIUM_LEVEL'\n" +
                "                WHEN 2\n" +
                "                    THEN 'LOW_LEVEL'\n" +
                "                ELSE 'HEALTHY'\n" +
                "            END AS risklevel\n" +
                "FROM mtp_cs.mtp_rsk_event e\n" +
                "WHERE e.is_deleted = 0\n" +
                "ORDER BY e.review_latest_datetime DESC\n" +
                "LIMIT 30\n" +
                ";";

        long startMillis = System.currentTimeMillis();
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        for (int i = 1; i < 1; i++) {
            final CCJSqlParser parser = new CCJSqlParser(sqlStr)
                    .withSquareBracketQuotation(false)
                    .withAllowComplexParsing(true)
                    .withBackslashEscapeCharacter(false);
            Future<Statements> future = executorService.submit(new Callable<Statements>() {
                @Override
                public Statements call() throws ParseException {
                    return parser.Statements();
                }
            });
            try {
                future.get(6000, TimeUnit.MILLISECONDS);
                long endMillis = System.currentTimeMillis();

                System.out.println("Time to parse [ms]: " + (endMillis - startMillis) / i);
            } catch (TimeoutException | InterruptedException ex2) {
                parser.interrupted = true;
                future.cancel(true);
                throw new JSQLParserException("Failed to within reasonable time ", ex2);
            } catch (ExecutionException e) {
                if (e.getCause() instanceof ParseException) {
                    ParseException parseException = (ParseException) e.getCause();
                    net.sf.jsqlparser.parser.Token token = parseException.currentToken.next;
                    throw new JSQLParserException(
                            "Failed to parse statement at Token " + token.image);
                }
            }
        }
        executorService.shutdown();
    }
}
