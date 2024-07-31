/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.insert;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.ExistsExpression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.ParenthesedExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.update.UpdateSet;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.io.StringReader;
import java.util.List;

import static junit.framework.Assert.assertNull;
import static net.sf.jsqlparser.test.TestUtils.assertDeparse;
import static net.sf.jsqlparser.test.TestUtils.assertOracleHintExists;
import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static net.sf.jsqlparser.test.TestUtils.assertStatementCanBeDeparsedAs;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InsertTest {

    private final CCJSqlParserManager parserManager = new CCJSqlParserManager();

    @Test
    public void testRegularInsert() throws JSQLParserException {
        String statement = "INSERT INTO mytable (col1, col2, col3) VALUES (?, 'sadfsd', 234)";
        Insert insert = (Insert) assertSqlCanBeParsedAndDeparsed(statement, true);

        assertEquals("mytable", insert.getTable().getName());
        assertEquals(3, insert.getColumns().size());
        assertEquals("col1", insert.getColumns().get(0).getColumnName());
        assertEquals("col2", insert.getColumns().get(1).getColumnName());
        assertEquals("col3", insert.getColumns().get(2).getColumnName());

        Values values = insert.getValues();
        assertEquals(3, values.getExpressions().size());
        assertTrue(values.getExpressions().get(0) instanceof JdbcParameter);
        assertEquals("sadfsd", ((StringValue) values.getExpressions().get(1)).getValue());
        assertEquals(234, ((LongValue) values.getExpressions().get(2)).getValue());
        assertEquals(statement, insert.toString());

        ExpressionList expressionList = new ParenthesedExpressionList(new JdbcParameter(),
                new StringValue("sadfsd"), new LongValue().withValue(234));

        Select select = new Values().withExpressions(expressionList);

        Insert insert2 = new Insert().withTable(new Table("mytable"))
                .withColumns(
                        new ExpressionList<>(new Column("col1"), new Column("col2"),
                                new Column("col3")))
                .withSelect(select);

        assertDeparse(insert2, statement);

        statement = "INSERT INTO myschema.mytable VALUES (?, ?, 2.3)";
        insert = (Insert) parserManager.parse(new StringReader(statement));
        assertEquals("myschema.mytable", insert.getTable().getFullyQualifiedName());
        assertEquals(3, insert.getValues().getExpressions().size());
        assertTrue(insert.getValues().getExpressions().get(0) instanceof JdbcParameter);
        assertEquals(2.3,
                ((DoubleValue) insert.getValues().getExpressions().get(2))
                        .getValue(),
                0.0);
        assertEquals(statement, "" + insert);

    }

    @Test
    public void testInsertWithKeywordValue() throws JSQLParserException {
        String statement = "INSERT INTO mytable (col1) VALUE ('val1')";
        Insert insert = (Insert) parserManager.parse(new StringReader(statement));
        assertEquals("mytable", insert.getTable().getName());
        assertEquals(1, insert.getColumns().size());
        assertEquals("col1", insert.getColumns().get(0).getColumnName());
        assertEquals("'val1'",
                (insert.getValues().getExpressions().get(0)).toString());
        assertEquals("INSERT INTO mytable (col1) VALUES ('val1')", insert.toString());

    }

    @Test
    public void testInsertFromSelect() throws JSQLParserException {
        String statement = "INSERT INTO mytable (col1, col2, col3) SELECT * FROM mytable2";
        Insert insert = (Insert) parserManager.parse(new StringReader(statement));
        assertEquals("mytable", insert.getTable().getName());
        assertEquals(3, insert.getColumns().size());
        assertEquals("col1", insert.getColumns().get(0).getColumnName());
        assertEquals("col2", insert.getColumns().get(1).getColumnName());
        assertEquals("col3", insert.getColumns().get(2).getColumnName());

        // throw a NPE since its a PlainSelect statement
        assertThrows(Exception.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                insert.getValues();
            }
        });

        assertNotNull(insert.getSelect());
        assertEquals("mytable2",
                ((Table) insert.getPlainSelect().getFromItem()).getName());

        // toString uses brackets
        String statementToString = "INSERT INTO mytable (col1, col2, col3) SELECT * FROM mytable2";
        assertEquals(statementToString, "" + insert);

        assertDeparse(new Insert().withTable(new Table("mytable"))
                .addColumns(new Column("col1"), new Column("col2"), new Column("col3"))
                .withSelect(new PlainSelect()
                        .addSelectItems(new AllColumns()).withFromItem(new Table("mytable2"))),
                statement);
    }

    @Test
    public void testInsertFromSet() throws JSQLParserException {
        String statement = "INSERT INTO mytable SET col1 = 12, col2 = name1 * name2";
        Insert insert = (Insert) parserManager.parse(new StringReader(statement));
        assertEquals("mytable", insert.getTable().getName());
        assertEquals(2, insert.getSetUpdateSets().size());
        assertEquals("col1", insert.getSetUpdateSets().get(0).getColumns().get(0).getColumnName());
        assertEquals("col2", insert.getSetUpdateSets().get(1).getColumns().get(0).getColumnName());
        assertEquals("12", insert.getSetUpdateSets().get(0).getValues().get(0).toString());
        assertEquals("name1 * name2",
                insert.getSetUpdateSets().get(1).getValues().get(0).toString());
        assertEquals(statement, "" + insert);
    }

    @Test
    public void testInsertValuesWithDuplicateElimination() throws JSQLParserException {
        String statement = "INSERT INTO TEST (ID, COUNTER) VALUES (123, 0) "
                + "ON DUPLICATE KEY UPDATE COUNTER = COUNTER + 1";
        Insert insert = (Insert) parserManager.parse(new StringReader(statement));
        assertEquals("TEST", insert.getTable().getName());
        assertEquals(2, insert.getColumns().size());
        assertEquals("ID", insert.getColumns().get(0).getColumnName());
        assertEquals("COUNTER", insert.getColumns().get(1).getColumnName());
        assertEquals(2, insert.getValues().getExpressions().size());
        assertEquals(123,
                ((LongValue) insert.getValues().getExpressions().get(0))
                        .getValue());
        assertEquals(0,
                ((LongValue) insert.getValues().getExpressions().get(1))
                        .getValue());
        assertEquals(1, insert.getDuplicateUpdateSets().size());
        assertEquals("COUNTER",
                insert.getDuplicateUpdateSets().get(0).getColumns().get(0).getColumnName());
        assertEquals("COUNTER + 1",
                insert.getDuplicateUpdateSets().get(0).getValues().get(0).toString());
        assertFalse(insert.isUseSelectBrackets());
        assertTrue(insert.isUseDuplicate());
        assertEquals(statement, "" + insert);
    }

    @Test
    public void testInsertFromSetWithDuplicateElimination() throws JSQLParserException {
        String statement = "INSERT INTO mytable SET col1 = 122 "
                + "ON DUPLICATE KEY UPDATE col2 = col2 + 1, col3 = 'saint'";
        Insert insert = (Insert) parserManager.parse(new StringReader(statement));
        assertEquals("mytable", insert.getTable().getName());
        assertEquals(1, insert.getSetUpdateSets().size());
        assertEquals("col1", insert.getSetUpdateSets().get(0).getColumns().get(0).getColumnName());
        assertEquals("122", insert.getSetUpdateSets().get(0).getValues().get(0).toString());
        assertEquals(2, insert.getDuplicateUpdateSets().size());
        assertEquals("col2",
                insert.getDuplicateUpdateSets().get(0).getColumns().get(0).getColumnName());
        assertEquals("col3",
                insert.getDuplicateUpdateSets().get(1).getColumns().get(0).getColumnName());
        assertEquals("col2 + 1",
                insert.getDuplicateUpdateSets().get(0).getValues().get(0).toString());
        assertEquals("'saint'",
                insert.getDuplicateUpdateSets().get(1).getValues().get(0).toString());
        assertEquals(statement, "" + insert);
    }

    @Test
    public void testInsertMultiRowValue() throws JSQLParserException {
        String statement = "INSERT INTO mytable (col1, col2) VALUES (a, b), (d, e)";
        assertSqlCanBeParsedAndDeparsed(statement);

        ExpressionList<Expression> multiExpressionList = new ExpressionList<>()
                .addExpression(
                        new ParenthesedExpressionList<Expression>(new Column("a"), new Column("b")))
                .addExpression(
                        new ParenthesedExpressionList<Expression>(new Column("d"),
                                new Column("e")));

        Select select = new Values().withExpressions(multiExpressionList);

        Insert insert = new Insert().withTable(new Table("mytable"))
                .withColumns(new ExpressionList<>(new Column("col1"), new Column("col2")))
                .withSelect(select);

        assertDeparse(insert, statement);
    }

    @Test
    @Disabled
    // @todo: Clarify, if and why this test is supposed to fail and if it is the Parser's job to
    // decide
    // What if col1 and col2 are Array Columns?
    public void testInsertMultiRowValueDifferent() throws JSQLParserException {
        assertThrowsExactly(JSQLParserException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                CCJSqlParserUtil.parse("INSERT INTO mytable (col1, col2) VALUES (a, b), (d, e, c)");
            }
        });
    }

    @Test
    @Disabled
    public void testOracleInsertMultiRowValue() throws JSQLParserException {
        String sqlStr = "INSERT ALL\n"
                + "  INTO suppliers (supplier_id, supplier_name) VALUES (1000, 'IBM')\n"
                + "  INTO suppliers (supplier_id, supplier_name) VALUES (2000, 'Microsoft')\n"
                + "  INTO suppliers (supplier_id, supplier_name) VALUES (3000, 'Google')\n"
                + "SELECT * FROM dual;";
        assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    public void testSimpleInsert() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "INSERT INTO example (num, name, address, tel) VALUES (1, 'name', 'test ', '1234-1234')");
    }

    @Test
    public void testInsertWithReturning() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("INSERT INTO mytable (mycolumn) VALUES ('1') RETURNING id");
    }

    @Test
    public void testInsertWithReturning2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("INSERT INTO mytable (mycolumn) VALUES ('1') RETURNING *");
    }

    @Test
    public void testInsertWithReturning3() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "INSERT INTO mytable (mycolumn) VALUES ('1') RETURNING id AS a1, id2 AS a2");
    }

    @Test
    public void testInsertSelect() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "INSERT INTO mytable (mycolumn) SELECT mycolumn FROM mytable");
        assertSqlCanBeParsedAndDeparsed(
                "INSERT INTO mytable (mycolumn) (SELECT mycolumn FROM mytable)");
    }

    @Test
    public void testInsertWithSelect() throws JSQLParserException {
        String sqlStr1 = "INSERT INTO mytable (mycolumn) WITH a AS (SELECT mycolumn FROM mytable) SELECT mycolumn FROM a";
        Insert insert1 = (Insert) assertSqlCanBeParsedAndDeparsed(sqlStr1, true);
        List<WithItem> insertWithItems1 = insert1.getWithItemsList();
        List<WithItem> selectWithItems1 = insert1.getSelect().getWithItemsList();
        assertEquals("mytable", insert1.getTable().getFullyQualifiedName());
        assertNull(insertWithItems1);
        assertEquals(1, selectWithItems1.size());
        assertEquals("SELECT mycolumn FROM mytable", selectWithItems1.get(0).getSelect().getPlainSelect().toString());
        assertEquals(" a", selectWithItems1.get(0).getAlias().toString());

        String sqlStr2 = "INSERT INTO mytable (mycolumn) (WITH a AS (SELECT mycolumn FROM mytable) SELECT mycolumn FROM a)";
        Insert insert2 = (Insert) assertSqlCanBeParsedAndDeparsed(sqlStr2, true);
        List<WithItem> insertWithItems2 = insert2.getWithItemsList();
        assertEquals("mytable", insert2.getTable().getFullyQualifiedName());
        assertNull(insertWithItems2);
        ParenthesedSelect select = (ParenthesedSelect) insert2.getSelect();
        List<WithItem> selectWithItems2 = select.getSelect().getWithItemsList();
        assertEquals(1, selectWithItems2.size());
        assertEquals("SELECT mycolumn FROM mytable", selectWithItems2.get(0).getSelect().getPlainSelect().toString());
        assertEquals(" a", selectWithItems2.get(0).getAlias().toString());
    }

    @Test
    public void testInsertWithKeywords() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("INSERT INTO kvPair (value, key) VALUES (?, ?)");
    }

    @Test
    public void testHexValues() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("INSERT INTO TABLE2 VALUES ('1', \"DSDD\", x'EFBFBDC7AB')");
    }

    @Test
    public void testHexValues2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("INSERT INTO TABLE2 VALUES ('1', \"DSDD\", 0xEFBFBDC7AB)");
    }

    @Test
    public void testHexValues3() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("INSERT INTO TABLE2 VALUES ('1', \"DSDD\", 0xabcde)");
    }

    @Test
    public void testDuplicateKey() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "INSERT INTO Users0 (UserId, Key, Value) VALUES (51311, 'T_211', 18) ON DUPLICATE KEY UPDATE Value = 18");
    }

    @Test
    public void testModifierIgnore() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "INSERT IGNORE INTO `AoQiSurvey_FlashVersion_Single` VALUES (302215163, 'WIN 16,0,0,235')");
    }

    @Test
    public void testModifierPriority1() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("INSERT DELAYED INTO kvPair (value, key) VALUES (?, ?)");
    }

    @Test
    public void testModifierPriority2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "INSERT LOW_PRIORITY INTO kvPair (value, key) VALUES (?, ?)");
    }

    @Test
    public void testModifierPriority3() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "INSERT HIGH_PRIORITY INTO kvPair (value, key) VALUES (?, ?)");
    }

    @Test
    public void testIssue223() throws JSQLParserException {
        String sqlStr = "INSERT INTO user VALUES (2001, '\\'Clark\\'', 'Kent')";
        assertSqlCanBeParsedAndDeparsed(sqlStr, true,
                parser -> parser.withBackslashEscapeCharacter(true));
    }

    @Test
    public void testKeywordPrecisionIssue363() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("INSERT INTO test (user_id, precision) VALUES (1, '111')");
    }

    @Test
    public void testWithDeparsingIssue406() throws JSQLParserException {
        String sqlStr = "insert into mytab3 (a,b,c) select a,b,c from mytab where exists(with t as (select * from mytab2) select * from t)";
        Insert insert = (Insert) assertSqlCanBeParsedAndDeparsed(sqlStr, true);
        List<WithItem> insertWithItems = insert.getWithItemsList();
        List<WithItem> selectWithItems = insert.getSelect().getWithItemsList();
        assertEquals("mytab3", insert.getTable().getFullyQualifiedName());
        assertNull(insertWithItems);
        assertNull(selectWithItems);
        ExistsExpression exists = (ExistsExpression) insert.getPlainSelect().getWhere();
        assertEquals("(WITH t AS (SELECT * FROM mytab2) SELECT * FROM t)", exists.getRightExpression().toString());
    }

    @Test
    public void testInsertSetInDeparsing() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("INSERT INTO mytable SET col1 = 12, col2 = name1 * name2");
    }

    @Test
    public void testInsertValuesWithDuplicateEliminationInDeparsing() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("INSERT INTO TEST (ID, COUNTER) VALUES (123, 0) "
                + "ON DUPLICATE KEY UPDATE COUNTER = COUNTER + 1");
    }

    @Test
    public void testInsertSetWithDuplicateEliminationInDeparsing() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("INSERT INTO mytable SET col1 = 122 "
                + "ON DUPLICATE KEY UPDATE col2 = col2 + 1, col3 = 'saint'");
    }

    @Test
    public void testInsertTableWithAliasIssue526() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "INSERT INTO account t (name, addr, phone) SELECT * FROM user");
    }

    @Test
    public void testInsertKeyWordEnableIssue592() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "INSERT INTO T_USER (ID, EMAIL_VALIDATE, ENABLE, PASSWORD) VALUES (?, ?, ?, ?)");
    }

    @Test
    public void testInsertKeyWordIntervalIssue682() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "INSERT INTO BILLING_TASKS (TIMEOUT, INTERVAL, RETRY_UPON_FAILURE, END_DATE, MAX_RETRY_COUNT, CONTINUOUS, NAME, LAST_RUN, START_TIME, NEXT_RUN, ID, UNIQUE_NAME, INTERVAL_TYPE) VALUES (?, ?, ?, ?, ?, ?, ?, NULL, ?, ?, ?, ?, ?)");
    }

    @Test
    public void testWithAtFront() throws JSQLParserException {
        String sqlStr = "WITH foo AS ( SELECT attr FROM bar ) INSERT INTO lalelu (attr) SELECT attr FROM foo";
        Insert insert = (Insert) assertSqlCanBeParsedAndDeparsed(sqlStr, true);
        List<WithItem> insertWithItems = insert.getWithItemsList();
        assertEquals("lalelu", insert.getTable().getFullyQualifiedName());
        assertEquals(1, insertWithItems.size());
        assertEquals("SELECT attr FROM bar", insertWithItems.get(0).getSelect().getPlainSelect().toString());
        assertEquals(" foo", insertWithItems.get(0).getAlias().toString());
        assertEquals("SELECT attr FROM foo", insert.getSelect().toString());
        assertEquals("foo", insert.getSelect().getPlainSelect().getFromItem().toString());
        assertEquals("[attr]", insert.getSelect().getPlainSelect().getSelectItems().toString());
    }

    @Test
    public void testNextVal() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "INSERT INTO tracker (monitor_id, user_id, module_name, item_id, item_summary, team_id, date_modified, action, visible, id) VALUES (?, ?, ?, ?, ?, ?, to_date(?, 'YYYY-MM-DD HH24:MI:SS'), ?, ?, NEXTVAL FOR TRACKER_ID_SEQ)");
    }

    @Test
    public void testNextValueFor() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "INSERT INTO tracker (monitor_id, user_id, module_name, item_id, item_summary, team_id, date_modified, action, visible, id) VALUES (?, ?, ?, ?, ?, ?, to_date(?, 'YYYY-MM-DD HH24:MI:SS'), ?, ?, NEXT VALUE FOR TRACKER_ID_SEQ)");
    }

    @Test
    public void testNextValIssue773() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "INSERT INTO tableA (ID, c1, c2) SELECT hibernate_sequence.nextval, c1, c2 FROM tableB");
    }

    @Test
    public void testBackslashEscapingIssue827() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "INSERT INTO my_table (my_column_1, my_column_2) VALUES ('my_value_1\\\\', 'my_value_2')");
    }

    @Test
    public void testDisableKeywordIssue945() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "INSERT INTO SOMESCHEMA.TEST (DISABLE, TESTCOLUMN) VALUES (1, 1)");
    }

    @Test
    public void testWithListIssue282() throws JSQLParserException {
        String sqlStr = "WITH myctl AS (SELECT a, b FROM mytable) INSERT INTO mytable SELECT a, b FROM myctl";
        Insert insert = (Insert) assertSqlCanBeParsedAndDeparsed(sqlStr, true);
        List<WithItem> insertWithItems = insert.getWithItemsList();
        assertEquals("mytable", insert.getTable().getFullyQualifiedName());
        assertEquals(1, insertWithItems.size());
        assertEquals("SELECT a, b FROM mytable", insertWithItems.get(0).getSelect().getPlainSelect().toString());
        assertEquals(" myctl", insertWithItems.get(0).getAlias().toString());
        assertEquals("SELECT a, b FROM myctl", insert.getSelect().toString());
        assertEquals("myctl", insert.getSelect().getPlainSelect().getFromItem().toString());
        assertEquals("[a, b]", insert.getSelect().getPlainSelect().getSelectItems().toString());
    }

    @Test
    public void testOracleHint() throws JSQLParserException {
        assertOracleHintExists("INSERT /*+ SOMEHINT */ INTO mytable VALUES (1, 2, 3)", true,
                "SOMEHINT");

        // @todo: add a testcase supposed to not finding a misplaced hint
    }

    @Test
    public void testInsertTableArrays4() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("INSERT INTO sal_emp\n" + "    VALUES ('Carol',\n"
                + "    ARRAY[20000, 25000, 25000, 25000],\n"
                + "    ARRAY[['breakfast', 'consulting'], ['meeting', 'lunch']])", true);
    }

    @Test
    public void testKeywordDefaultIssue1470() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "INSERT INTO mytable (col1, col2, col3) VALUES (?, 'sadfsd', default)");
    }

    @Test
    public void testInsertUnionSelectIssue1491() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("insert into table1 (tf1,tf2,tf2)\n"
                + "select sf1,sf2,sf3 from s1\n" + "union\n" + "select rf1,rf2,rf2 from r1\n",
                true);

        assertSqlCanBeParsedAndDeparsed("insert into table1 (tf1,tf2,tf2)\n"
                + "( select sf1,sf2,sf3 from s1\n" + "union\n" + "select rf1,rf2,rf2 from r1\n)",
                true);

        assertSqlCanBeParsedAndDeparsed("insert into table1 (tf1,tf2,tf2)\n"
                + "(select sf1,sf2,sf3 from s1)" + "union " + "(select rf1,rf2,rf2 from r1)", true);

        assertSqlCanBeParsedAndDeparsed("insert into table1 (tf1,tf2,tf2)\n"
                + "((select sf1,sf2,sf3 from s1)" + "union " + "(select rf1,rf2,rf2 from r1))",
                true);
    }

    @Test
    public void testWithSelectFromDual() throws JSQLParserException {
        String sqlStr = "(with a as (select * from dual) select * from a)";
        ParenthesedSelect parenthesedSelect = (ParenthesedSelect) assertSqlCanBeParsedAndDeparsed(sqlStr, true);
        List<WithItem> withItems = parenthesedSelect.getSelect().getWithItemsList();
        assertEquals(1, withItems.size());
        assertEquals("SELECT * FROM dual", withItems.get(0).getSelect().getPlainSelect().toString());
        assertEquals(" a", withItems.get(0).getAlias().toString());
        assertEquals("a", parenthesedSelect.getPlainSelect().getFromItem().toString());
        assertEquals("[*]", parenthesedSelect.getPlainSelect().getSelectItems().toString());
    }

    @Test
    public void testInsertOutputClause() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "INSERT INTO dbo.EmployeeSales (LastName, FirstName, CurrentSales)  \n"
                        + "  OUTPUT INSERTED.EmployeeID,\n" + "         INSERTED.LastName,   \n"
                        + "         INSERTED.FirstName,   \n" + "         INSERTED.CurrentSales,\n"
                        + "         INSERTED.ProjectedSales\n" + "  INTO @MyTableVar  \n"
                        + "    SELECT c.LastName, c.FirstName, sp.SalesYTD  \n"
                        + "    FROM Sales.SalesPerson AS sp  \n"
                        + "    INNER JOIN Person.Person AS c  \n"
                        + "        ON sp.BusinessEntityID = c.BusinessEntityID  \n"
                        + "    WHERE sp.BusinessEntityID LIKE '2%'  \n"
                        + "    ORDER BY c.LastName, c.FirstName",
                true);
    }

    // Samples taken from: https://www.postgresql.org/docs/current/sql-insert.html
    @Test
    public void testInsertOnConflictIssue1551() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("INSERT INTO distributors (did, dname)\n"
                + "    VALUES (5, 'Gizmo Transglobal'), (6, 'Associated Computing, Inc')\n"
                + "    ON CONFLICT (did) DO UPDATE SET dname = EXCLUDED.dname\n", true);
        assertSqlCanBeParsedAndDeparsed(
                "INSERT INTO distributors (did, dname) VALUES (7, 'Redline GmbH')\n"
                        + "    ON CONFLICT (did) DO NOTHING",
                true);

        assertSqlCanBeParsedAndDeparsed(
                "-- Don't update existing distributors based in a certain ZIP code\n"
                        + "INSERT INTO distributors AS d (did, dname) VALUES (8, 'Anvil Distribution')\n"
                        + "    ON CONFLICT (did) DO UPDATE\n"
                        + "    SET dname = EXCLUDED.dname || ' (formerly ' || d.dname || ')'\n"
                        + "    WHERE d.zipcode <> '21201'",
                true);

        assertSqlCanBeParsedAndDeparsed(
                "-- Name a constraint directly in the statement (uses associated\n"
                        + "-- index to arbitrate taking the DO NOTHING action)\n"
                        + "INSERT INTO distributors (did, dname) VALUES (9, 'Antwerp Design')\n"
                        + "    ON CONFLICT ON CONSTRAINT distributors_pkey DO NOTHING",
                true);

        assertSqlCanBeParsedAndDeparsed(
                "-- This statement could infer a partial unique index on \"did\"\n"
                        + "-- with a predicate of \"WHERE is_active\", but it could also\n"
                        + "-- just use a regular unique constraint on \"did\"\n"
                        + "INSERT INTO distributors (did, dname) VALUES (10, 'Conrad International')\n"
                        + "    ON CONFLICT (did) WHERE is_active DO NOTHING",
                true);
    }

    @Test
    public void insertOnConflictObjectsTest() throws JSQLParserException {
        String sqlStr = "WITH a ( a, b , c ) \n" + "AS (SELECT  1 , 2 , 3 )\n"
                + "insert into test\n" + "select * from a";
        Insert insert = (Insert) CCJSqlParserUtil.parse(sqlStr);
        List<WithItem> withItems = insert.getWithItemsList();
        assertEquals("test", insert.getTable().getFullyQualifiedName());
        assertEquals(1, withItems.size());
        assertEquals("[1, 2, 3]", withItems.get(0).getSelect().getPlainSelect().getSelectItems().toString());
        assertEquals(" a", withItems.get(0).getAlias().toString());

        Expression whereExpression = CCJSqlParserUtil.parseExpression("a=1", false);
        Expression valueExpression = CCJSqlParserUtil.parseExpression("b/2", false);

        InsertConflictTarget conflictTarget = new InsertConflictTarget("a", null, null, null);
        insert.setConflictTarget(conflictTarget);

        InsertConflictAction conflictAction =
                new InsertConflictAction(ConflictActionType.DO_NOTHING);
        insert.setConflictAction(conflictAction);

        assertStatementCanBeDeparsedAs(insert,
                sqlStr + " ON CONFLICT " + conflictTarget + conflictAction, true);

        conflictTarget = new InsertConflictTarget((String) null, null, null, "testConstraint");
        conflictTarget = conflictTarget.withWhereExpression(whereExpression);
        assertNotNull(conflictTarget.withConstraintName("a").getConstraintName());
        conflictTarget.setIndexExpression(valueExpression);
        assertNotNull(conflictTarget.getIndexExpression());
        assertNotNull(conflictTarget.withIndexColumnName("b").getIndexColumnName());

        assertTrue(conflictTarget.withIndexExpression(valueExpression).getIndexColumnNames()
                .isEmpty());
        assertNotNull(conflictTarget.withWhereExpression(whereExpression).getWhereExpression());

        conflictAction = new InsertConflictAction(ConflictActionType.DO_UPDATE);
        conflictAction.addUpdateSet(new Column().withColumnName("a"), valueExpression);

        UpdateSet updateSet = new UpdateSet();
        updateSet.add(new Column().withColumnName("b"));
        updateSet.add(valueExpression);
        conflictAction = conflictAction.addUpdateSet(updateSet);

        assertNotNull(conflictAction.withWhereExpression(whereExpression).getWhereExpression());
        assertEquals(ConflictActionType.DO_UPDATE, conflictAction.getConflictActionType());

        insert = insert.withConflictTarget(conflictTarget).withConflictAction(conflictAction);

        assertStatementCanBeDeparsedAs(insert,
                sqlStr + " ON CONFLICT " + conflictTarget + conflictAction, true);

    }

    @Test
    void testMultiColumnConflictTargetIssue1749() throws JSQLParserException {
        String sqlStr =
                "INSERT INTO re_rule_mapping ( id, created_time, last_modified_time, rule_item_id, department_id, scene, operation )\n"
                        + "            VALUES\n"
                        + "                ( '1', now( ), now( ), '1', '11', 'test', 'stop7' ),\n"
                        + "                ( '2', now( ), now( ), '2', '22', 'test2', 'stop8' ) ON CONFLICT ( rule_item_id, department_id, scene ) \n"
                        + "            DO UPDATE\n"
                        + "            SET operation = excluded.operation";
        assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testMultiColumnConflictTargetIssue955() throws JSQLParserException {
        String sqlStr =
                "INSERT INTO tableName (id,xxx0,xxx1,xxx2,is_deleted,create_time,update_time) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?) "
                        + "on conflict(xxx0, xxx1) do update set xxx1=?, update_time=?";
        assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    public void testDefaultValues() throws JSQLParserException {
        String statement = "INSERT INTO mytable DEFAULT VALUES";
        //assertSqlCanBeParsedAndDeparsed(statement);
        Insert insert = (Insert) parserManager.parse(new StringReader(statement));
        assertEquals("mytable", insert.getTable().getFullyQualifiedName());
        assertEquals("INSERT INTO MYTABLE DEFAULT VALUES", insert.toString().toUpperCase());
        assertTrue(insert.isOnlyDefaultValues());
        assertDeparse(new Insert()
                .withTable(new Table("mytable"))
                .withOnlyDefaultValues(true), statement);
    }

    @Test
    public void testDefaultValuesWithAlias() throws JSQLParserException {
        String statement = "INSERT INTO mytable x DEFAULT VALUES";
        Insert insert = (Insert) assertSqlCanBeParsedAndDeparsed(statement);
        assertEquals("mytable", insert.getTable().getFullyQualifiedName());
        assertEquals("INSERT INTO MYTABLE X DEFAULT VALUES", insert.toString().toUpperCase());
        assertEquals("x", insert.getTable().getAlias().getName());
        assertTrue(insert.isOnlyDefaultValues());
        assertDeparse(new Insert()
                .withTable(new Table("mytable")
                        .withAlias(new Alias("x").withUseAs(false)))
                .withOnlyDefaultValues(true), statement);
    }

    @Test
    public void testDefaultValuesWithAliasAndAs() throws JSQLParserException {
        String statement = "INSERT INTO mytable AS x DEFAULT VALUES";
        Insert insert = (Insert) assertSqlCanBeParsedAndDeparsed(statement);
        assertEquals("mytable", insert.getTable().getFullyQualifiedName());
        assertEquals("INSERT INTO MYTABLE AS X DEFAULT VALUES", insert.toString().toUpperCase());
        assertEquals("x", insert.getTable().getAlias().getName());
        assertTrue(insert.isOnlyDefaultValues());
        assertDeparse(new Insert()
                .withTable(new Table("mytable")
                        .withAlias(new Alias("x").withUseAs(true)))
                .withOnlyDefaultValues(true), statement);
    }

    @Test
    public void throwsParseWhenDefaultKeyowrdUsedAsAlias() {
        String statement = "INSERT INTO mytable default DEFAULT VALUES";
        assertThrows(JSQLParserException.class,
                () -> parserManager.parse(new StringReader(statement)));
    }

}
