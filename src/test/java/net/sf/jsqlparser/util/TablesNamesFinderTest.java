/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.OracleHint;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.DescribeStatement;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.comment.Comment;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.merge.Merge;
import net.sf.jsqlparser.statement.replace.Replace;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.simpleparsing.CCJSqlParserManagerTest;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.upsert.Upsert;
import net.sf.jsqlparser.test.TestException;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


public class TablesNamesFinderTest {

    private static CCJSqlParserManager pm = new CCJSqlParserManager();

    @Test
    public void testRUBiSTableList() throws Exception {
        runTestOnResource("/RUBiS-select-requests.txt");
    }

    @Test
    public void testMoreComplexExamples() throws Exception {
        runTestOnResource("complex-select-requests.txt");
    }

    @Test
    public void testComplexMergeExamples() throws Exception {
        runTestOnResource("complex-merge-requests.txt");
    }

    private void runTestOnResource(String resPath) throws Exception {
        BufferedReader in = new BufferedReader(
                new InputStreamReader(TablesNamesFinderTest.class.getResourceAsStream(resPath)));
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();

        try {
            int numSt = 1;
            while (true) {
                String line = getLine(in);
                if (line == null) {
                    break;
                }

                if (line.length() == 0) {
                    continue;
                }

                if (!"#begin".equals(line)) {
                    break;
                }
                line = getLine(in);
                StringBuilder buf = new StringBuilder(line);
                while (true) {
                    line = getLine(in);
                    if ("#end".equals(line)) {
                        break;
                    }
                    buf.append("\n");
                    buf.append(line);
                }

                String query = buf.toString();
                if (!getLine(in).equals("true")) {
                    continue;
                }

                String cols = getLine(in);
                String tables = getLine(in);
                String whereCols = getLine(in);
                String type = getLine(in);
                try {
                    Statement statement = pm.parse(new StringReader(query));

                    String[] tablesArray = tables.split("\\s+");

                    List<String> tableListRetr = tablesNamesFinder.getTableList(statement);
                    assertEquals("stm num:" + numSt, tablesArray.length, tableListRetr.size());

                    for (int i = 0; i < tablesArray.length; i++) {
                        assertTrue("stm num:" + numSt, tableListRetr.contains(tablesArray[i]));
                    }
                } catch (Exception e) {
                    throw new TestException("error at stm num: " + numSt + " in file " + resPath, e);
                }
                numSt++;
            }
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    @Test
    public void testGetTableList() throws Exception {

        String sql = "SELECT * FROM MY_TABLE1, MY_TABLE2, (SELECT * FROM MY_TABLE3) LEFT OUTER JOIN MY_TABLE4 "
                + " WHERE ID = (SELECT MAX(ID) FROM MY_TABLE5) AND ID2 IN (SELECT * FROM MY_TABLE6)";
        net.sf.jsqlparser.statement.Statement statement = pm.parse(new StringReader(sql));

        // now you should use a class that implements StatementVisitor to decide what to
        // do
        // based on the kind of the statement, that is SELECT or INSERT etc. but here we
        // are only
        // interested in SELECTS
        if (statement instanceof Select) {
            Select selectStatement = (Select) statement;
            TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
            List<String> tableList = tablesNamesFinder.getTableList(selectStatement);
            assertEquals(6, tableList.size());
            int i = 1;
            for (Iterator iter = tableList.iterator(); iter.hasNext(); i++) {
                String tableName = (String) iter.next();
                assertEquals("MY_TABLE" + i, tableName);
            }
        }

    }

    @Test
    public void testGetTableListWithAlias() throws Exception {
        String sql = "SELECT * FROM MY_TABLE1 as ALIAS_TABLE1";
        net.sf.jsqlparser.statement.Statement statement = pm.parse(new StringReader(sql));

        Select selectStatement = (Select) statement;
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        List<String> tableList = tablesNamesFinder.getTableList(selectStatement);
        assertEquals(1, tableList.size());
        assertEquals("MY_TABLE1", (String) tableList.get(0));
    }

    @Test
    public void testGetTableListWithStmt() throws Exception {
        String sql = "WITH TESTSTMT as (SELECT * FROM MY_TABLE1 as ALIAS_TABLE1) SELECT * FROM TESTSTMT";
        net.sf.jsqlparser.statement.Statement statement = pm.parse(new StringReader(sql));

        Select selectStatement = (Select) statement;
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        List<String> tableList = tablesNamesFinder.getTableList(selectStatement);
        assertEquals(1, tableList.size());
        assertEquals("MY_TABLE1", (String) tableList.get(0));
    }

    @Test
    public void testGetTableListWithLateral() throws Exception {
        String sql = "SELECT * FROM MY_TABLE1, LATERAL(select a from MY_TABLE2) as AL";
        net.sf.jsqlparser.statement.Statement statement = pm.parse(new StringReader(sql));

        Select selectStatement = (Select) statement;
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        List<String> tableList = tablesNamesFinder.getTableList(selectStatement);
        assertEquals(2, tableList.size());
        assertTrue(tableList.contains("MY_TABLE1"));
        assertTrue(tableList.contains("MY_TABLE2"));
    }

    @Test
    public void testGetTableListFromDelete() throws Exception {
        String sql = "DELETE FROM MY_TABLE1 as AL WHERE a = (SELECT a from MY_TABLE2)";
        net.sf.jsqlparser.statement.Statement statement = pm.parse(new StringReader(sql));

        Delete deleteStatement = (Delete) statement;
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        List<String> tableList = tablesNamesFinder.getTableList(deleteStatement);
        assertEquals(2, tableList.size());
        assertTrue(tableList.contains("MY_TABLE1"));
        assertTrue(tableList.contains("MY_TABLE2"));
    }

    @Test
    public void testGetTableListFromDelete2() throws Exception {
        String sql = "DELETE FROM MY_TABLE1";
        net.sf.jsqlparser.statement.Statement statement = pm.parse(new StringReader(sql));

        Delete deleteStatement = (Delete) statement;
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        List<String> tableList = tablesNamesFinder.getTableList(deleteStatement);
        assertEquals(1, tableList.size());
        assertTrue(tableList.contains("MY_TABLE1"));
    }

    @Test
    public void testGetTableListFromTruncate() throws Exception {
        String sql = "TRUNCATE TABLE MY_TABLE1";
        List<String> tables = new TablesNamesFinder().getTableList(pm.parse(new StringReader(sql)));
        assertEquals(1, tables.size());
        assertTrue(tables.contains("MY_TABLE1"));
    }

    @Test
    public void testGetTableListFromDeleteWithJoin() throws Exception {
        String sql = "DELETE t1, t2 FROM MY_TABLE1 t1 JOIN MY_TABLE2 t2 ON t1.id = t2.id";
        net.sf.jsqlparser.statement.Statement statement = pm.parse(new StringReader(sql));

        Delete deleteStatement = (Delete) statement;
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        List<String> tableList = tablesNamesFinder.getTableList(deleteStatement);
        assertEquals(2, tableList.size());
        assertTrue(tableList.contains("MY_TABLE1"));
        assertTrue(tableList.contains("MY_TABLE2"));
    }

    @Test
    public void testGetTableListFromInsert() throws Exception {
        String sql = "INSERT INTO MY_TABLE1 (a) VALUES ((SELECT a from MY_TABLE2 WHERE a = 1))";
        net.sf.jsqlparser.statement.Statement statement = pm.parse(new StringReader(sql));

        Insert insertStatement = (Insert) statement;
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        List<String> tableList = tablesNamesFinder.getTableList(insertStatement);
        assertEquals(2, tableList.size());
        assertTrue(tableList.contains("MY_TABLE1"));
        assertTrue(tableList.contains("MY_TABLE2"));
    }

    @Test
    public void testGetTableListFromInsertValues() throws Exception {
        String sql = "INSERT INTO MY_TABLE1 (a) VALUES (5)";
        net.sf.jsqlparser.statement.Statement statement = pm.parse(new StringReader(sql));

        Insert insertStatement = (Insert) statement;
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        List<String> tableList = tablesNamesFinder.getTableList(insertStatement);
        assertEquals(1, tableList.size());
        assertTrue(tableList.contains("MY_TABLE1"));
    }

    @Test
    public void testGetTableListFromReplace() throws Exception {
        String sql = "REPLACE INTO MY_TABLE1 (a) VALUES ((SELECT a from MY_TABLE2 WHERE a = 1))";
        net.sf.jsqlparser.statement.Statement statement = pm.parse(new StringReader(sql));

        Replace replaceStatement = (Replace) statement;
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        List<String> tableList = tablesNamesFinder.getTableList(replaceStatement);
        assertEquals(2, tableList.size());
        assertTrue(tableList.contains("MY_TABLE1"));
        assertTrue(tableList.contains("MY_TABLE2"));
    }

    @Test
    public void testGetTableListFromUpdate() throws Exception {
        String sql = "UPDATE MY_TABLE1 SET a = (SELECT a from MY_TABLE2 WHERE a = 1)";
        net.sf.jsqlparser.statement.Statement statement = pm.parse(new StringReader(sql));

        Update updateStatement = (Update) statement;
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        List<String> tableList = tablesNamesFinder.getTableList(updateStatement);
        assertEquals(2, tableList.size());
        assertTrue(tableList.contains("MY_TABLE1"));
        assertTrue(tableList.contains("MY_TABLE2"));
    }

    @Test
    public void testGetTableListFromUpdate2() throws Exception {
        String sql = "UPDATE MY_TABLE1 SET a = 5 WHERE 0 < (SELECT COUNT(b) FROM MY_TABLE3)";
        net.sf.jsqlparser.statement.Statement statement = pm.parse(new StringReader(sql));

        Update updateStatement = (Update) statement;
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        List<String> tableList = tablesNamesFinder.getTableList(updateStatement);
        assertEquals(2, tableList.size());
        assertTrue(tableList.contains("MY_TABLE1"));
        assertTrue(tableList.contains("MY_TABLE3"));
    }

    @Test
    public void testGetTableListFromUpdate3() throws Exception {
        String sql = "UPDATE MY_TABLE1 SET a = 5 FROM MY_TABLE1 INNER JOIN MY_TABLE2 on MY_TABLE1.C = MY_TABLE2.D WHERE 0 < (SELECT COUNT(b) FROM MY_TABLE3)";
        net.sf.jsqlparser.statement.Statement statement = pm.parse(new StringReader(sql));

        Update updateStatement = (Update) statement;
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        List<String> tableList = tablesNamesFinder.getTableList(updateStatement);
        assertEquals(3, tableList.size());
        assertTrue(tableList.contains("MY_TABLE1"));
        assertTrue(tableList.contains("MY_TABLE2"));
        assertTrue(tableList.contains("MY_TABLE3"));
    }

    @Test
    public void testCmplxSelectProblem() throws Exception {
        String sql = "SELECT cid, (SELECT name FROM tbl0 WHERE tbl0.id = cid) AS name, original_id AS bc_id FROM tbl WHERE crid = ? AND user_id is null START WITH ID = (SELECT original_id FROM tbl2 WHERE USER_ID = ?) CONNECT BY prior parent_id = id AND rownum = 1";
        net.sf.jsqlparser.statement.Statement statement = pm.parse(new StringReader(sql));

        Select selectStatement = (Select) statement;
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        List<String> tableList = tablesNamesFinder.getTableList(selectStatement);
        assertEquals(3, tableList.size());
        assertTrue(tableList.contains("tbl0"));
        assertTrue(tableList.contains("tbl"));
        assertTrue(tableList.contains("tbl2"));
    }

    @Test
    public void testInsertSelect() throws Exception {
        String sql = "INSERT INTO mytable (mycolumn) SELECT mycolumn FROM mytable2";
        net.sf.jsqlparser.statement.Statement statement = pm.parse(new StringReader(sql));

        Insert insertStatement = (Insert) statement;
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        List<String> tableList = tablesNamesFinder.getTableList(insertStatement);
        assertEquals(2, tableList.size());
        assertTrue(tableList.contains("mytable"));
        assertTrue(tableList.contains("mytable2"));
    }

    @Test
    public void testCreateSelect() throws Exception {
        String sql = "CREATE TABLE mytable AS SELECT mycolumn FROM mytable2";
        net.sf.jsqlparser.statement.Statement statement = pm.parse(new StringReader(sql));

        CreateTable createTable = (CreateTable) statement;
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        List<String> tableList = tablesNamesFinder.getTableList(createTable);
        assertEquals(2, tableList.size());
        assertTrue(tableList.contains("mytable"));
        assertTrue(tableList.contains("mytable2"));
    }

    @Test
    public void testInsertSubSelect() throws JSQLParserException {
        String sql = "INSERT INTO Customers (CustomerName, Country) SELECT SupplierName, Country FROM Suppliers WHERE Country='Germany'";
        Insert insert = (Insert) pm.parse(new StringReader(sql));
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        List<String> tableList = tablesNamesFinder.getTableList(insert);
        assertEquals(2, tableList.size());
        assertTrue(tableList.contains("Customers"));
        assertTrue(tableList.contains("Suppliers"));
    }

    @Test
    public void testExpr() throws JSQLParserException {
        String sql = "mycol in (select col2 from mytable)";
        Expression expr = (Expression) CCJSqlParserUtil.parseCondExpression(sql);
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        List<String> tableList = tablesNamesFinder.getTableList(expr);
        assertEquals(1, tableList.size());
        assertTrue(tableList.contains("mytable"));
    }

    private String getLine(BufferedReader in) throws Exception {
        return CCJSqlParserManagerTest.getLine(in);
    }

    @Test
    public void testOracleHint() throws JSQLParserException {
        String sql = "select --+ HINT\ncol2 from mytable";
        Select select = (Select) CCJSqlParserUtil.parse(sql);
        final OracleHint[] holder = new OracleHint[1];
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder() {

            @Override
            public void visit(OracleHint hint) {
                super.visit(hint);
                holder[0] = hint;
            }

        };
        tablesNamesFinder.getTableList(select);
        assertNull(holder[0]);
    }

    @Test
    public void testGetTableListIssue194() throws Exception {
        String sql = "SELECT 1";
        net.sf.jsqlparser.statement.Statement statement = pm.parse(new StringReader(sql));

        Select selectStatement = (Select) statement;
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        List<String> tableList = tablesNamesFinder.getTableList(selectStatement);
        assertEquals(0, tableList.size());
    }

    @Test
    public void testGetTableListIssue284() throws Exception {
        String sql = "SELECT NVL( (SELECT 1 FROM DUAL), 1) AS A FROM TEST1";
        Select selectStatement = (Select) CCJSqlParserUtil.parse(sql);
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        List<String> tableList = tablesNamesFinder.getTableList(selectStatement);
        assertEquals(2, tableList.size());
        assertTrue(tableList.contains("DUAL"));
        assertTrue(tableList.contains("TEST1"));
    }

    @Test
    public void testUpdateGetTableListIssue295() throws JSQLParserException {
        Update statement = (Update) CCJSqlParserUtil.parse(
                "UPDATE component SET col = 0 WHERE (component_id,ver_num) IN (SELECT component_id,ver_num FROM component_temp)");
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        List<String> tableList = tablesNamesFinder.getTableList(statement);
        assertEquals(2, tableList.size());
        assertTrue(tableList.contains("component"));
        assertTrue(tableList.contains("component_temp"));
    }

    @Test
    public void testGetTableListForMerge() throws Exception {
        String sql = "MERGE INTO employees e  USING hr_records h  ON (e.id = h.emp_id) WHEN MATCHED THEN  UPDATE SET e.address = h.address  WHEN NOT MATCHED THEN    INSERT (id, address) VALUES (h.emp_id, h.address);";
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();

        List<String> tableList = tablesNamesFinder.getTableList((Merge) CCJSqlParserUtil.parse(sql));
        assertEquals(2, tableList.size());
        assertEquals("employees", (String) tableList.get(0));
        assertEquals("hr_records", (String) tableList.get(1));
    }

    @Test
    public void testGetTableListForMergeUsingQuery() throws Exception {
        String sql = "MERGE INTO employees e USING (SELECT * FROM hr_records WHERE start_date > ADD_MONTHS(SYSDATE, -1)) h  ON (e.id = h.emp_id)  WHEN MATCHED THEN  UPDATE SET e.address = h.address WHEN NOT MATCHED THEN INSERT (id, address) VALUES (h.emp_id, h.address)";
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        List<String> tableList = tablesNamesFinder.getTableList((Merge) CCJSqlParserUtil.parse(sql));
        assertEquals(2, tableList.size());
        assertEquals("employees", (String) tableList.get(0));
        assertEquals("hr_records", (String) tableList.get(1));
    }

    @Test
    public void testUpsertValues() throws Exception {
        String sql = "UPSERT INTO MY_TABLE1 (a) VALUES (5)";
        net.sf.jsqlparser.statement.Statement statement = pm.parse(new StringReader(sql));

        Upsert insertStatement = (Upsert) statement;
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        List<String> tableList = tablesNamesFinder.getTableList(insertStatement);
        assertEquals(1, tableList.size());
        assertTrue(tableList.contains("MY_TABLE1"));
    }

    @Test
    public void testUpsertSelect() throws Exception {
        String sql = "UPSERT INTO mytable (mycolumn) SELECT mycolumn FROM mytable2";
        net.sf.jsqlparser.statement.Statement statement = pm.parse(new StringReader(sql));

        Upsert insertStatement = (Upsert) statement;
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        List<String> tableList = tablesNamesFinder.getTableList(insertStatement);
        assertEquals(2, tableList.size());
        assertTrue(tableList.contains("mytable"));
        assertTrue(tableList.contains("mytable2"));
    }

    @Test
    public void testCaseWhenSubSelect() throws JSQLParserException {
        String sql = "select case (select count(*) from mytable2) when 1 then 0 else -1 end";
        Statement stmt = CCJSqlParserUtil.parse(sql);
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        List<String> tableList = tablesNamesFinder.getTableList(stmt);
        assertEquals(1, tableList.size());
        assertTrue(tableList.contains("mytable2"));
    }

    @Test
    public void testCaseWhenSubSelect2() throws JSQLParserException {
        String sql = "select case when (select count(*) from mytable2) = 1 then 0 else -1 end";
        Statement stmt = CCJSqlParserUtil.parse(sql);
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        List<String> tableList = tablesNamesFinder.getTableList(stmt);
        assertEquals(1, tableList.size());
        assertTrue(tableList.contains("mytable2"));
    }

    @Test
    public void testCaseWhenSubSelect3() throws JSQLParserException {
        String sql = "select case when 1 = 2 then 0 else (select count(*) from mytable2) end";
        Statement stmt = CCJSqlParserUtil.parse(sql);
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        List<String> tableList = tablesNamesFinder.getTableList(stmt);
        assertEquals(1, tableList.size());
        assertTrue(tableList.contains("mytable2"));
    }

    @Test
    public void testExpressionIssue515() throws JSQLParserException {
        TablesNamesFinder finder = new TablesNamesFinder();
        List<String> tableList = finder.getTableList(CCJSqlParserUtil.parseCondExpression("SOME_TABLE.COLUMN = 'A'"));
        assertEquals(1, tableList.size());
        assertTrue(tableList.contains("SOME_TABLE"));
    }

    @Test
    public void testSelectHavingSubquery() throws Exception {
        String sql = "SELECT * FROM TABLE1 GROUP BY COL1 HAVING SUM(COL2) > (SELECT COUNT(*) FROM TABLE2)";
        net.sf.jsqlparser.statement.Statement statement = pm.parse(new StringReader(sql));

        Select selectStmt = (Select) statement;
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        List<String> tableList = tablesNamesFinder.getTableList(selectStmt);
        assertEquals(2, tableList.size());
        assertTrue(tableList.contains("TABLE1"));
        assertTrue(tableList.contains("TABLE2"));
    }

    @Test
    public void testMySQLValueListExpression() throws JSQLParserException {
        String sql = "SELECT * FROM TABLE1 WHERE (a, b) = (c, d)";
        TablesNamesFinder finder = new TablesNamesFinder();
        List<String> tableList = finder.getTableList(CCJSqlParserUtil.parse(sql));
        assertEquals(1, tableList.size());
        assertTrue(tableList.contains("TABLE1"));
    }

    @Test
    public void testSkippedSchemaIssue600() throws JSQLParserException {
        String sql = "delete from schema.table where id = 1";
        TablesNamesFinder finder = new TablesNamesFinder();
        List<String> tableList = finder.getTableList(CCJSqlParserUtil.parse(sql));
        assertEquals(1, tableList.size());
        assertTrue(tableList.contains("schema.table"));
    }

    @Test
    public void testCommentTable() throws JSQLParserException {
        String sql = "comment on table schema.table is 'comment1'";
        TablesNamesFinder finder = new TablesNamesFinder();
        List<String> tableList = finder.getTableList(CCJSqlParserUtil.parse(sql));
        assertEquals(1, tableList.size());
        assertTrue(tableList.contains("schema.table"));
    }

    @Test
    public void testCommentColumn() throws JSQLParserException {
        String sql = "comment on column schema.table.column1 is 'comment1'";
        TablesNamesFinder finder = new TablesNamesFinder();
        List<String> tableList = finder.getTableList(CCJSqlParserUtil.parse(sql));
        assertEquals(1, tableList.size());
        assertTrue(tableList.contains("schema.table"));
    }

    @Test
    public void testCommentColumn2() throws JSQLParserException {
        Comment comment = new Comment();
        comment.setColumn(new Column());
        TablesNamesFinder finder = new TablesNamesFinder();
        List<String> tableList = finder.getTableList(comment);
        assertEquals(0, tableList.size());
    }

    @Test
    public void testDescribe() throws JSQLParserException {
        DescribeStatement describe = new DescribeStatement(new Table("foo", "product"));
        TablesNamesFinder finder = new TablesNamesFinder();
        List<String> tableList = finder.getTableList(describe);
        assertEquals(1, tableList.size());
        assertEquals("foo.product", tableList.get(0));
    }

    @Test
    public void testBetween() throws JSQLParserException {
        String sql = "mycol BETWEEN (select col2 from mytable) AND (select col3 from mytable2)";
        Expression expr = (Expression) CCJSqlParserUtil.parseCondExpression(sql);
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        List<String> tableList = tablesNamesFinder.getTableList(expr);
        assertEquals(2, tableList.size());
        assertTrue(tableList.contains("mytable"));
        assertTrue(tableList.contains("mytable2"));

    }

    @Test
    public void testRemoteLink() throws JSQLParserException {
        String sql = "select * from table1@remote";
        Statement stmt = CCJSqlParserUtil.parse(sql);
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        List<String> tableList = tablesNamesFinder.getTableList(stmt);
        assertEquals(1, tableList.size());
        assertTrue(tableList.contains("table1@remote"));
    }

    @Test
    public void testCreateSequence_throwsException() throws JSQLParserException {
        String sql = "CREATE SEQUENCE my_seq";
        Statement stmt = CCJSqlParserUtil.parse(sql);
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        assertThatThrownBy(() -> tablesNamesFinder.getTableList(stmt)).isInstanceOf(UnsupportedOperationException.class)
                .hasMessage("Finding tables from CreateSequence is not supported");
    }

    @Test
    public void testAlterSequence_throwsException() throws JSQLParserException {
        String sql = "ALTER SEQUENCE my_seq";
        Statement stmt = CCJSqlParserUtil.parse(sql);
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        assertThatThrownBy(() -> tablesNamesFinder.getTableList(stmt)).isInstanceOf(UnsupportedOperationException.class)
                .hasMessage("Finding tables from AlterSequence is not supported");
    }
    
    @Test
    public void testNPEIssue1009() throws JSQLParserException {
        Statement stmt = CCJSqlParserUtil.parse(" SELECT * FROM (SELECT * FROM biz_fund_info WHERE tenant_code = ? AND ((ta_code, manager_code) IN ((?, ?)) OR department_type IN (?)))");
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();    
        
        assertThat(tablesNamesFinder.getTableList(stmt)).containsExactly("biz_fund_info");
    }
}
