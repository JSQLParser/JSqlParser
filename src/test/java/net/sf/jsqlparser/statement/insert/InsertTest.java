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

import static net.sf.jsqlparser.test.TestUtils.assertDeparse;
import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import java.io.StringReader;
import java.util.Arrays;

import org.junit.jupiter.api.Test;






import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.MultiExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import static net.sf.jsqlparser.test.TestUtils.assertOracleHintExists;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Disabled;

public class InsertTest {

    private CCJSqlParserManager parserManager = new CCJSqlParserManager();

    @Test
    public void testRegularInsert() throws JSQLParserException {
        String statement = "INSERT INTO mytable (col1, col2, col3) VALUES (?, 'sadfsd', 234)";
        Insert insert = (Insert) parserManager.parse(new StringReader(statement));
        assertEquals("mytable", insert.getTable().getName());
        assertEquals(3, insert.getColumns().size());
        assertEquals("col1", insert.getColumns().get(0).getColumnName());
        assertEquals("col2", insert.getColumns().get(1).getColumnName());
        assertEquals("col3", insert.getColumns().get(2).getColumnName());
        assertEquals(3, ((ExpressionList) insert.getItemsList()).getExpressions().size());
        assertTrue(((ExpressionList) insert.getItemsList()).getExpressions().get(0) instanceof JdbcParameter);
        assertEquals("sadfsd",
                ((StringValue) ((ExpressionList) insert.getItemsList()).getExpressions().get(1)).
                getValue());
        assertEquals(234, ((LongValue) ((ExpressionList) insert.getItemsList()).getExpressions().
                get(2)).getValue());
        assertEquals(statement, "" + insert);

        assertDeparse(new Insert().withTable(new Table("mytable"))
                .addColumns(Arrays.asList(new Column("col1"), new Column("col2"), new Column("col3")))
                .withItemsList(new ExpressionList(new JdbcParameter(), new StringValue("sadfsd"),
                        new LongValue().withValue(234))),
                statement);

        statement = "INSERT INTO myschema.mytable VALUES (?, ?, 2.3)";
        insert = (Insert) parserManager.parse(new StringReader(statement));
        assertEquals("myschema.mytable", insert.getTable().getFullyQualifiedName());
        assertEquals(3, insert.getItemsList(ExpressionList.class).getExpressions().size());
        assertTrue(((ExpressionList) insert.getItemsList()).getExpressions().get(0) instanceof JdbcParameter);
        assertEquals(2.3, ((DoubleValue) insert.getItemsList(ExpressionList.class).getExpressions()
                .get(2)).getValue(), 0.0);
        assertEquals(statement, "" + insert);



    }

    @Test
    public void testInsertWithKeywordValue() throws JSQLParserException {
        String statement = "INSERT INTO mytable (col1) VALUE ('val1')";
        Insert insert = (Insert) parserManager.parse(new StringReader(statement));
        assertEquals("mytable", insert.getTable().getName());
        assertEquals(1, insert.getColumns().size());
        assertEquals("col1", insert.getColumns().get(0).getColumnName());
        assertEquals("val1",
                ((StringValue) ((ExpressionList) insert.getItemsList()).getExpressions().get(0)).
                getValue());
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
        assertNull(insert.getItemsList());
        assertNotNull(insert.getSelect());
        assertEquals("mytable2",
                ((Table) ((PlainSelect) insert.getSelect().getSelectBody()).getFromItem()).getName());

        // toString uses brakets
        String statementToString = "INSERT INTO mytable (col1, col2, col3) SELECT * FROM mytable2";
        assertEquals(statementToString, "" + insert);

        assertDeparse(new Insert().withUseValues(false).withUseSelectBrackets(false).withTable(new Table("mytable"))
                .addColumns(new Column("col1"), new Column("col2"), new Column("col3"))
                .withSelect(new Select().withSelectBody(
                        new PlainSelect().addSelectItems(new AllColumns()).withFromItem(new Table("mytable2")))),
                statement);
    }

    @Test
    public void testInsertFromSet() throws JSQLParserException {
        String statement = "INSERT INTO mytable SET col1 = 12, col2 = name1 * name2";
        Insert insert = (Insert) parserManager.parse(new StringReader(statement));
        assertEquals("mytable", insert.getTable().getName());
        assertEquals(2, insert.getSetColumns().size());
        assertEquals("col1", insert.getSetColumns().get(0).getColumnName());
        assertEquals("col2", insert.getSetColumns().get(1).getColumnName());
        assertEquals(2, insert.getSetExpressionList().size());
        assertEquals("12", insert.getSetExpressionList().get(0).toString());
        assertEquals("name1 * name2", insert.getSetExpressionList().get(1).toString());
        assertEquals(statement, "" + insert);
    }

    @Test
    public void testInsertValuesWithDuplicateElimination() throws JSQLParserException {
        String statement = "INSERT INTO TEST (ID, COUNTER) VALUES (123, 0) "
                + "ON DUPLICATE KEY UPDATE COUNTER = COUNTER + 1";
        Insert insert = (Insert) parserManager.parse(new StringReader(statement));
        assertEquals("TEST", insert.getTable().getName());
        assertEquals(2, insert.getColumns().size());
        assertTrue(insert.isUseValues());
        assertEquals("ID", insert.getColumns().get(0).getColumnName());
        assertEquals("COUNTER", insert.getColumns().get(1).getColumnName());
        assertEquals(2, ((ExpressionList) insert.getItemsList()).getExpressions().size());
        assertEquals(123, ((LongValue) ((ExpressionList) insert.getItemsList()).getExpressions().
                get(0)).getValue());
        assertEquals(0, ((LongValue) ((ExpressionList) insert.getItemsList()).getExpressions().
                get(1)).getValue());
        assertEquals(1, insert.getDuplicateUpdateColumns().size());
        assertEquals("COUNTER", insert.getDuplicateUpdateColumns().get(0).getColumnName());
        assertEquals(1, insert.getDuplicateUpdateExpressionList().size());
        assertEquals("COUNTER + 1", insert.getDuplicateUpdateExpressionList().get(0).toString());
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
        assertEquals(1, insert.getSetColumns().size());
        assertEquals("col1", insert.getSetColumns().get(0).getColumnName());
        assertEquals(1, insert.getSetExpressionList().size());
        assertEquals("122", insert.getSetExpressionList().get(0).toString());
        assertEquals(2, insert.getDuplicateUpdateColumns().size());
        assertEquals("col2", insert.getDuplicateUpdateColumns().get(0).getColumnName());
        assertEquals("col3", insert.getDuplicateUpdateColumns().get(1).getColumnName());
        assertEquals(2, insert.getDuplicateUpdateExpressionList().size());
        assertEquals("col2 + 1", insert.getDuplicateUpdateExpressionList().get(0).toString());
        assertEquals("'saint'", insert.getDuplicateUpdateExpressionList().get(1).toString());
        assertEquals(statement, "" + insert);
    }

    @Test
    public void testInsertMultiRowValue() throws JSQLParserException {
        String statement = "INSERT INTO mytable (col1, col2) VALUES (a, b), (d, e)";
        assertSqlCanBeParsedAndDeparsed(statement);
        assertDeparse(new Insert().withTable(new Table("mytable"))
                .withColumns(Arrays.asList(new Column("col1"), new Column("col2")))
                .withItemsList(new MultiExpressionList().addExpressionLists(
                        new ExpressionList().addExpressions(Arrays.asList(new Column("a"), new Column("b"))),
                        new ExpressionList(new Column("d"), new Column("e")))),
                statement);
    }

    @Test
    public void testInsertMultiRowValueDifferent() throws JSQLParserException {
        try {
            assertSqlCanBeParsedAndDeparsed("INSERT INTO mytable (col1, col2) VALUES (a, b), (d, e, c)");
        } catch (Exception e) {
            return;
        }

        fail("should not work");
    }

    @Test
    @Disabled
    public void testOracleInsertMultiRowValue() throws JSQLParserException {
        String sqlStr
                = "INSERT ALL\n"
                + "  INTO suppliers (supplier_id, supplier_name) VALUES (1000, 'IBM')\n"
                + "  INTO suppliers (supplier_id, supplier_name) VALUES (2000, 'Microsoft')\n"
                + "  INTO suppliers (supplier_id, supplier_name) VALUES (3000, 'Google')\n"
                + "SELECT * FROM dual;";
        assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    public void testSimpleInsert() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("INSERT INTO example (num, name, address, tel) VALUES (1, 'name', 'test ', '1234-1234')");
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
        assertSqlCanBeParsedAndDeparsed("INSERT INTO mytable (mycolumn) VALUES ('1') RETURNING id AS a1, id2 AS a2");
    }

    @Test
    public void testInsertSelect() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("INSERT INTO mytable (mycolumn) SELECT mycolumn FROM mytable");
        assertSqlCanBeParsedAndDeparsed("INSERT INTO mytable (mycolumn) (SELECT mycolumn FROM mytable)");
    }

    @Test
    public void testInsertWithSelect() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("INSERT INTO mytable (mycolumn) WITH a AS (SELECT mycolumn FROM mytable) SELECT mycolumn FROM a");
        assertSqlCanBeParsedAndDeparsed("INSERT INTO mytable (mycolumn) (WITH a AS (SELECT mycolumn FROM mytable) SELECT mycolumn FROM a)");
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
        assertSqlCanBeParsedAndDeparsed("INSERT INTO Users0 (UserId, Key, Value) VALUES (51311, 'T_211', 18) ON DUPLICATE KEY UPDATE Value = 18");
    }

    @Test
    public void testModifierIgnore() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("INSERT IGNORE INTO `AoQiSurvey_FlashVersion_Single` VALUES (302215163, 'WIN 16,0,0,235')");
    }

    @Test
    public void testModifierPriority1() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("INSERT DELAYED INTO kvPair (value, key) VALUES (?, ?)");
    }

    @Test
    public void testModifierPriority2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("INSERT LOW_PRIORITY INTO kvPair (value, key) VALUES (?, ?)");
    }

    @Test
    public void testModifierPriority3() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("INSERT HIGH_PRIORITY INTO kvPair (value, key) VALUES (?, ?)");
    }

    @Test
    public void testIssue223() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("INSERT INTO user VALUES (2001, '\\'Clark\\'', 'Kent')");
    }

    @Test
    public void testKeywordPrecisionIssue363() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("INSERT INTO test (user_id, precision) VALUES (1, '111')");
    }

    @Test
    public void testWithDeparsingIssue406() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("insert into mytab3 (a,b,c) select a,b,c from mytab where exists(with t as (select * from mytab2) select * from t)", true);
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
        assertSqlCanBeParsedAndDeparsed("INSERT INTO account t (name, addr, phone) SELECT * FROM user");
    }

    @Test
    public void testInsertKeyWordEnableIssue592() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("INSERT INTO T_USER (ID, EMAIL_VALIDATE, ENABLE, PASSWORD) VALUES (?, ?, ?, ?)");
    }

    @Test
    public void testInsertKeyWordIntervalIssue682() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("INSERT INTO BILLING_TASKS (TIMEOUT, INTERVAL, RETRY_UPON_FAILURE, END_DATE, MAX_RETRY_COUNT, CONTINUOUS, NAME, LAST_RUN, START_TIME, NEXT_RUN, ID, UNIQUE_NAME, INTERVAL_TYPE) VALUES (?, ?, ?, ?, ?, ?, ?, NULL, ?, ?, ?, ?, ?)");
    }

    @Test
    public void testWithAtFront() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("WITH foo AS ( SELECT attr FROM bar ) INSERT INTO lalelu (attr) SELECT attr FROM foo", true);
    }

    @Test
    public void testNextVal() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("INSERT INTO tracker (monitor_id, user_id, module_name, item_id, item_summary, team_id, date_modified, action, visible, id) VALUES (?, ?, ?, ?, ?, ?, to_date(?, 'YYYY-MM-DD HH24:MI:SS'), ?, ?, NEXTVAL FOR TRACKER_ID_SEQ)");
    }

    @Test
    public void testNextValueFor() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("INSERT INTO tracker (monitor_id, user_id, module_name, item_id, item_summary, team_id, date_modified, action, visible, id) VALUES (?, ?, ?, ?, ?, ?, to_date(?, 'YYYY-MM-DD HH24:MI:SS'), ?, ?, NEXT VALUE FOR TRACKER_ID_SEQ)");
    }

    @Test
    public void testNextValIssue773() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("INSERT INTO tableA (ID, c1, c2) SELECT hibernate_sequence.nextval, c1, c2 FROM tableB");
    }

    @Test
    public void testBackslashEscapingIssue827() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("INSERT INTO my_table (my_column_1, my_column_2) VALUES ('my_value_1\\\\', 'my_value_2')");
    }

    @Test
    public void testDisableKeywordIssue945() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("INSERT INTO SOMESCHEMA.TEST (DISABLE, TESTCOLUMN) VALUES (1, 1)");
    }
    
    @Test
    public void testWithListIssue282() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("WITH myctl AS (SELECT a, b FROM mytable) INSERT INTO mytable SELECT a, b FROM myctl");
    }
    
    @Test
    public void testOracleHint() throws JSQLParserException {
        assertOracleHintExists("INSERT /*+ SOMEHINT */ INTO mytable VALUES (1, 2, 3)", true, "SOMEHINT");
       
       //@todo: add a testcase supposed to not finding a misplaced hint
    }

  @Test
  public void testInsertTableArrays4() throws JSQLParserException {
    assertSqlCanBeParsedAndDeparsed(
        "INSERT INTO sal_emp\n"
            + "    VALUES ('Carol',\n"
            + "    ARRAY[20000, 25000, 25000, 25000],\n"
            + "    ARRAY[['breakfast', 'consulting'], ['meeting', 'lunch']])",
        true);
    }
}
