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

import jdk.nashorn.internal.ir.annotations.Ignore;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.OracleHint;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.DescribeStatement;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.comment.Comment;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.simpleparsing.CCJSqlParserManagerTest;
import net.sf.jsqlparser.test.TestException;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TablesNamesFinderTest {

    private static final CCJSqlParserManager PARSER_MANAGER = new CCJSqlParserManager();

    @Ignore
    public void testRUBiSTableList() throws Exception {
        runTestOnResource("/RUBiS-select-requests.txt");
    }

    @Ignore
    public void testMoreComplexExamples() throws Exception {
        runTestOnResource("complex-select-requests.txt");
    }

    @Ignore
    public void testComplexMergeExamples() throws Exception {
        runTestOnResource("complex-merge-requests.txt");
    }

    private void runTestOnResource(String resPath) throws Exception {

        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(TablesNamesFinderTest.class.getResourceAsStream(resPath)))) {
            TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
            int numSt = 1;
            while (true) {
                String line = getLine(in);
                if (line == null) {
                    break;
                }

                if (line.isEmpty()) {
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

                String tables = getLine(in);
                try {
                    Statement statement = PARSER_MANAGER.parse(new StringReader(query));

                    String[] tablesArray = tables.split("\\s+");

                    List<String> tableListRetr = tablesNamesFinder.getTableList(statement);
                    assertEquals(tablesArray.length, tableListRetr.size(), "stm num:" + numSt);

                    for (String element : tablesArray) {
                        assertTrue(tableListRetr.contains(element), "stm num:" + numSt);
                    }
                } catch (Exception e) {
                    throw new TestException("error at stm num: " + numSt + " in file " + resPath,
                            e);
                }
                numSt++;
            }
        }
    }

    @Test
    public void testGetTables() throws Exception {
        String sqlStr =
                "SELECT * FROM MY_TABLE1, MY_TABLE2, (SELECT * FROM MY_TABLE3) LEFT OUTER JOIN MY_TABLE4 "
                        + " WHERE ID = (SELECT MAX(ID) FROM MY_TABLE5) AND ID2 IN (SELECT * FROM MY_TABLE6)";
        assertThat(TablesNamesFinder.findTables(sqlStr)).containsExactlyInAnyOrder("MY_TABLE1",
                "MY_TABLE2", "MY_TABLE3", "MY_TABLE4", "MY_TABLE5", "MY_TABLE6");
    }

    @Test
    public void testGetTablesWithAlias() throws Exception {
        String sqlStr = "SELECT * FROM MY_TABLE1 as ALIAS_TABLE1";
        assertThat(TablesNamesFinder.findTables(sqlStr)).containsExactlyInAnyOrder("MY_TABLE1");
    }

    @Test
    public void testGetTablesWithXor() throws Exception {
        String sqlStr = "SELECT * FROM MY_TABLE1 WHERE true XOR false";
        assertThat(TablesNamesFinder.findTables(sqlStr)).containsExactlyInAnyOrder("MY_TABLE1");
    }

    @Test
    public void testGetTablesWithStmt() throws Exception {
        String sqlStr =
                "WITH TESTSTMT as (SELECT * FROM MY_TABLE1 as ALIAS_TABLE1) SELECT * FROM TESTSTMT";
        assertThat(TablesNamesFinder.findTables(sqlStr)).containsExactlyInAnyOrder("MY_TABLE1");
    }

    @Test
    public void testGetTablesWithLateral() throws Exception {
        String sqlStr = "SELECT * FROM MY_TABLE1, LATERAL(select a from MY_TABLE2) as AL";
        assertThat(TablesNamesFinder.findTables(sqlStr)).containsExactlyInAnyOrder("MY_TABLE1",
                "MY_TABLE2");
    }

    @Test
    public void testGetTablesFromDelete() throws Exception {
        String sqlStr = "DELETE FROM MY_TABLE1 as AL WHERE a = (SELECT a from MY_TABLE2)";
        assertThat(TablesNamesFinder.findTables(sqlStr)).containsExactlyInAnyOrder("MY_TABLE1",
                "MY_TABLE2");
    }

    @Test
    public void testGetTablesFromDelete2() throws Exception {
        String sqlStr = "DELETE FROM MY_TABLE1";
        assertThat(TablesNamesFinder.findTables(sqlStr)).containsExactlyInAnyOrder("MY_TABLE1");
    }

    @Test
    public void testGetTablesFromTruncate() throws Exception {
        String sqlStr = "TRUNCATE TABLE MY_TABLE1";
        assertThat(TablesNamesFinder.findTables(sqlStr)).containsExactlyInAnyOrder("MY_TABLE1");
    }

    @Test
    public void testGetTablesFromDeleteWithJoin() throws Exception {
        String sqlStr = "DELETE t1, t2 FROM MY_TABLE1 t1 JOIN MY_TABLE2 t2 ON t1.id = t2.id";
        assertThat(TablesNamesFinder.findTables(sqlStr)).containsExactlyInAnyOrder("MY_TABLE1",
                "MY_TABLE2");
    }

    @Test
    public void testGetTablesFromInsert() throws Exception {
        String sqlStr = "INSERT INTO MY_TABLE1 (a) VALUES ((SELECT a from MY_TABLE2 WHERE a = 1))";
        assertThat(TablesNamesFinder.findTables(sqlStr)).containsExactlyInAnyOrder("MY_TABLE1",
                "MY_TABLE2");
    }

    @Test
    public void testGetTablesFromInsertValues() throws Exception {
        String sqlStr = "INSERT INTO MY_TABLE1 (a) VALUES (5)";
        assertThat(TablesNamesFinder.findTables(sqlStr)).containsExactlyInAnyOrder("MY_TABLE1");
    }

    @Test
    public void testGetTablesFromReplace() throws Exception {
        String sqlStr = "REPLACE INTO MY_TABLE1 (a) VALUES ((SELECT a from MY_TABLE2 WHERE a = 1))";
        assertThat(TablesNamesFinder.findTables(sqlStr)).containsExactlyInAnyOrder("MY_TABLE1",
                "MY_TABLE2");
    }

    @Test
    public void testGetTablesFromUpdate() throws Exception {
        String sqlStr = "UPDATE MY_TABLE1 SET a = (SELECT a from MY_TABLE2 WHERE a = 1)";
        assertThat(TablesNamesFinder.findTables(sqlStr)).containsExactlyInAnyOrder("MY_TABLE1",
                "MY_TABLE2");
    }

    @Test
    public void testGetTablesFromUpdate2() throws Exception {
        String sqlStr = "UPDATE MY_TABLE1 SET a = 5 WHERE 0 < (SELECT COUNT(b) FROM MY_TABLE3)";
        assertThat(TablesNamesFinder.findTables(sqlStr)).containsExactlyInAnyOrder("MY_TABLE1",
                "MY_TABLE3");
    }

    @Test
    public void testGetTablesFromUpdate3() throws Exception {
        String sqlStr =
                "UPDATE MY_TABLE1 SET a = 5 FROM MY_TABLE1 INNER JOIN MY_TABLE2 on MY_TABLE1.C = MY_TABLE2.D WHERE 0 < (SELECT COUNT(b) FROM MY_TABLE3)";
        assertThat(TablesNamesFinder.findTables(sqlStr)).containsExactlyInAnyOrder("MY_TABLE1",
                "MY_TABLE2", "MY_TABLE3");
    }

    @Test
    public void testCmplxSelectProblem() throws Exception {
        String sqlStr =
                "SELECT cid, (SELECT name FROM tbl0 WHERE tbl0.id = cid) AS name, original_id AS bc_id FROM tbl WHERE crid = ? AND user_id is null START WITH ID = (SELECT original_id FROM tbl2 WHERE USER_ID = ?) CONNECT BY prior parent_id = id AND rownum = 1";
        assertThat(TablesNamesFinder.findTables(sqlStr)).containsExactlyInAnyOrder("tbl0", "tbl",
                "tbl2");
    }

    @Test
    public void testInsertSelect() throws Exception {
        String sqlStr = "INSERT INTO mytable (mycolumn) SELECT mycolumn FROM mytable2";
        assertThat(TablesNamesFinder.findTables(sqlStr)).containsExactlyInAnyOrder("mytable",
                "mytable2");
    }

    @Test
    public void testCreateSelect() throws Exception {
        String sqlStr = "CREATE TABLE mytable AS SELECT mycolumn FROM mytable2";
        assertThat(TablesNamesFinder.findTables(sqlStr)).containsExactlyInAnyOrder("mytable",
                "mytable2");
    }

    @Test
    public void testInsertSubSelect() throws JSQLParserException {
        String sqlStr =
                "INSERT INTO Customers (CustomerName, Country) SELECT SupplierName, Country FROM Suppliers WHERE Country='Germany'";
        assertThat(TablesNamesFinder.findTables(sqlStr)).containsExactlyInAnyOrder("Customers",
                "Suppliers");
    }

    @Test
    public void testExpr() throws JSQLParserException {
        String exprStr = "mycol in (select col2 from mytable)";
        assertThat(TablesNamesFinder.findTablesInExpression(exprStr))
                .containsExactlyInAnyOrder("mytable");
    }

    private String getLine(BufferedReader in) throws Exception {
        return CCJSqlParserManagerTest.getLine(in);
    }

    @Test
    public void testOracleHint() throws JSQLParserException {
        String sql = "select --+ HINT\ncol2 from mytable";
        PlainSelect select = (PlainSelect) CCJSqlParserUtil.parse(sql);
        final OracleHint[] holder = new OracleHint[1];
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder() {

            @Override
            public void visit(OracleHint hint) {
                super.visit(hint);
                holder[0] = hint;
            }

        };
        tablesNamesFinder.getTables((Statement) select);
        assertNull(holder[0]);
    }

    @Test
    public void testGetTablesIssue194() throws Exception {
        String sql = "SELECT 1";
        Statement statement = TestUtils.assertSqlCanBeParsedAndDeparsed(sql, true);
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        Set<String> tableList = tablesNamesFinder.getTables(statement);
        assertEquals(0, tableList.size());
    }

    @Test
    public void testGetTablesIssue284() throws Exception {
        String sqlStr = "SELECT NVL( (SELECT 1 FROM DUAL), 1) AS A FROM TEST1";
        assertThat(TablesNamesFinder.findTables(sqlStr)).containsExactly("DUAL", "TEST1");
    }

    @Test
    public void testUpdateGetTablesIssue295() throws JSQLParserException {
        String sqlStr =
                "UPDATE component SET col = 0 WHERE (component_id,ver_num) IN (SELECT component_id,ver_num FROM component_temp)";
        assertThat(TablesNamesFinder.findTables(sqlStr)).containsExactly("component",
                "component_temp");
    }

    @Test
    public void testGetTablesForMerge() throws Exception {
        String sqlStr =
                "MERGE INTO employees e  USING hr_records h  ON (e.id = h.emp_id) WHEN MATCHED THEN  UPDATE SET e.address = h.address  WHEN NOT MATCHED THEN    INSERT (id, address) VALUES (h.emp_id, h.address);";
        assertThat(TablesNamesFinder.findTables(sqlStr)).containsExactlyInAnyOrder("employees",
                "hr_records");
    }

    @Test
    public void testgetTablesForMergeUsingQuery() throws Exception {
        String sqlStr =
                "MERGE INTO employees e USING (SELECT * FROM hr_records WHERE start_date > ADD_MONTHS(SYSDATE, -1)) h  ON (e.id = h.emp_id)  WHEN MATCHED THEN  UPDATE SET e.address = h.address WHEN NOT MATCHED THEN INSERT (id, address) VALUES (h.emp_id, h.address)";
        assertThat(TablesNamesFinder.findTables(sqlStr)).containsExactlyInAnyOrder("employees",
                "hr_records");
    }

    @Test
    public void testUpsertValues() throws Exception {
        String sqlStr = "UPSERT INTO MY_TABLE1 (a) VALUES (5)";
        assertThat(TablesNamesFinder.findTables(sqlStr)).containsExactlyInAnyOrder("MY_TABLE1");
    }

    @Test
    public void testUpsertSelect() throws Exception {
        String sqlStr = "UPSERT INTO mytable (mycolumn) SELECT mycolumn FROM mytable2";
        assertThat(TablesNamesFinder.findTables(sqlStr)).containsExactly("mytable", "mytable2");
    }

    @Test
    public void testCaseWhenSubSelect() throws JSQLParserException {
        String sqlStr = "select case (select count(*) from mytable2) when 1 then 0 else -1 end";
        assertThat(TablesNamesFinder.findTables(sqlStr)).containsExactly("mytable2");
    }

    @Test
    public void testCaseWhenSubSelect2() throws JSQLParserException {
        String sqlStr = "select case when (select count(*) from mytable2) = 1 then 0 else -1 end";
        assertThat(TablesNamesFinder.findTables(sqlStr)).containsExactly("mytable2");
    }

    @Test
    public void testCaseWhenSubSelect3() throws JSQLParserException {
        String sqlStr = "select case when 1 = 2 then 0 else (select count(*) from mytable2) end";
        assertThat(TablesNamesFinder.findTables(sqlStr)).containsExactly("mytable2");
    }

    @Test
    public void testExpressionIssue515() throws JSQLParserException {
        TablesNamesFinder finder = new TablesNamesFinder();
        Set<String> tableList = finder
                .getTables(CCJSqlParserUtil.parseCondExpression("SOME_TABLE.COLUMN = 'A'"));
        assertEquals(1, tableList.size());
        assertTrue(tableList.contains("SOME_TABLE"));
    }

    @Test
    public void testSelectHavingSubquery() throws Exception {
        String sqlStr =
                "SELECT * FROM TABLE1 GROUP BY COL1 HAVING SUM(COL2) > (SELECT COUNT(*) FROM TABLE2)";
        assertThat(TablesNamesFinder.findTables(sqlStr)).containsExactly("TABLE1", "TABLE2");
    }

    @Test
    public void testMySQLValueListExpression() throws JSQLParserException {
        String sqlStr = "SELECT * FROM TABLE1 WHERE (a, b) = (c, d)";
        assertThat(TablesNamesFinder.findTables(sqlStr)).containsExactly("TABLE1");
    }

    @Test
    public void testSkippedSchemaIssue600() throws JSQLParserException {
        String sqlStr = "delete from schema.table where id = 1";
        assertThat(TablesNamesFinder.findTables(sqlStr)).containsExactly("schema.table");
    }

    @Test
    public void testCommentTable() throws JSQLParserException {
        String sqlStr = "comment on table schema.table is 'comment1'";
        assertThat(TablesNamesFinder.findTables(sqlStr)).containsExactly("schema.table");
    }

    @Test
    public void testCommentColumn() throws JSQLParserException {
        String sqlStr = "comment on column schema.table.column1 is 'comment1'";
        assertThat(TablesNamesFinder.findTables(sqlStr)).containsExactly("schema.table");
    }

    @Test
    public void testCommentColumn2() {
        Comment comment = new Comment();
        comment.setColumn(new Column());
        TablesNamesFinder finder = new TablesNamesFinder();
        Set<String> tableList = finder.getTables(comment);
        assertEquals(0, tableList.size());
    }

    @Test
    public void testDescribe() {
        DescribeStatement describe = new DescribeStatement(new Table("foo", "product"));
        TablesNamesFinder finder = new TablesNamesFinder();
        Set<String> tableList = finder.getTables(describe);
        assertEquals(1, tableList.size());
        assertThat(tableList).contains("foo.product");
    }

    @Test
    public void testBetween() throws JSQLParserException {
        String exprStr = "mycol BETWEEN (select col2 from mytable) AND (select col3 from mytable2)";
        assertThat(TablesNamesFinder.findTablesInExpression(exprStr))
                .containsExactlyInAnyOrder("mytable", "mytable2");
    }

    @Test
    public void testRemoteLink() throws JSQLParserException {
        String sqlStr = "select * from table1@remote";
        assertThat(TablesNamesFinder.findTables(sqlStr)).containsExactlyInAnyOrder("table1@remote");
    }

    @Test
    public void testCreateSequence_throwsException() throws JSQLParserException {
        String sql = "CREATE SEQUENCE my_seq";
        Statement stmt = CCJSqlParserUtil.parse(sql);
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        assertThatThrownBy(() -> tablesNamesFinder.getTables(stmt))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessage("Finding tables from CreateSequence is not supported");
    }

    @Test
    public void testAlterSequence_throwsException() throws JSQLParserException {
        String sql = "ALTER SEQUENCE my_seq";
        Statement stmt = CCJSqlParserUtil.parse(sql);
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        assertThatThrownBy(() -> tablesNamesFinder.getTables(stmt))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessage("Finding tables from AlterSequence is not supported");
    }

    @Test
    public void testCreateSynonym_throwsException() throws JSQLParserException {
        String sql = "CREATE SYNONYM foo FOR bar";
        Statement stmt = CCJSqlParserUtil.parse(sql);
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        assertThatThrownBy(() -> tablesNamesFinder.getTables(stmt))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessage("Finding tables from CreateSynonym is not supported");
    }

    @Test
    public void testNPEIssue1009() throws JSQLParserException {
        String sqlStr =
                " SELECT * FROM (SELECT * FROM biz_fund_info WHERE tenant_code = ? AND ((ta_code, manager_code) IN ((?, ?)) OR department_type IN (?)))";
        assertThat(TablesNamesFinder.findTables(sqlStr)).containsExactlyInAnyOrder("biz_fund_info");
    }

    @Test
    public void testAtTimeZoneExpression() throws JSQLParserException {
        String sqlStr =
                "SELECT DATE(date1 AT TIME ZONE 'UTC' AT TIME ZONE 'australia/sydney') AS another_date FROM mytbl";
        assertThat(TablesNamesFinder.findTables(sqlStr)).containsExactlyInAnyOrder("mytbl");
    }

    @Test
    public void testUsing() throws JSQLParserException {
        String sqlStr = "DELETE A USING B.C D WHERE D.Z = 1";
        assertThat(TablesNamesFinder.findTables(sqlStr)).containsExactlyInAnyOrder("A", "B.C");
    }

    @Test
    public void testJsonFunction() throws JSQLParserException {
        String sqlStr = "SELECT JSON_ARRAY(  1, 2, 3 ) FROM mytbl";
        assertThat(TablesNamesFinder.findTables(sqlStr)).containsExactlyInAnyOrder("mytbl");
    }

    @Test
    public void testJsonAggregateFunction() throws JSQLParserException {
        String sqlStr = "SELECT JSON_ARRAYAGG( (SELECT * from dual) FORMAT JSON) FROM mytbl";
        assertThat(TablesNamesFinder.findTables(sqlStr)).containsExactlyInAnyOrder("dual", "mytbl");
    }

    @Test
    public void testConnectedByRootOperator() throws JSQLParserException {
        String sqlStr = "SELECT CONNECT_BY_ROOT last_name as name"
                + ", salary "
                + "FROM employees "
                + "WHERE department_id = 110 "
                + "CONNECT BY PRIOR employee_id = manager_id";
        assertThat(TablesNamesFinder.findTables(sqlStr)).containsExactlyInAnyOrder("employees");
    }

    @Test
    void testJoinSubSelect() throws JSQLParserException {
        String sqlStr = "select * from A left join B on A.id=B.id and A.age = (select age from C)";
        assertThat(TablesNamesFinder.findTables(sqlStr)).containsExactlyInAnyOrder("A", "B", "C");
    }
}
