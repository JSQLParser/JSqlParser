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
import net.sf.jsqlparser.expression.OracleHint;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.DescribeStatement;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.comment.Comment;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TablesNamesFinderTest {

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
    public void testCreateTableSelect() throws Exception {
        String sqlStr = "CREATE TABLE mytable AS SELECT mycolumn FROM mytable2";
        assertThat(TablesNamesFinder.findTables(sqlStr)).containsExactlyInAnyOrder("mytable",
                "mytable2");
    }

    @Test
    public void testCreateViewSelect() throws Exception {
        String sqlStr = "CREATE VIEW mytable AS SELECT mycolumn FROM mytable2";
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

    @Test
    public void testOracleHint() throws JSQLParserException {
        String sql = "select --+ HINT\ncol2 from mytable";
        PlainSelect select = (PlainSelect) CCJSqlParserUtil.parse(sql);
        final OracleHint[] holder = new OracleHint[1];
        TablesNamesFinder<Void> tablesNamesFinder = new TablesNamesFinder<Void>() {

            @Override
            public <K> Void visit(OracleHint hint, K parameters) {
                super.visit(hint, parameters);
                holder[0] = hint;
                return null;
            }

        };
        tablesNamesFinder.getTables((Statement) select);
        assertNull(holder[0]);
    }

    @Test
    public void testGetTablesIssue194() throws Exception {
        String sql = "SELECT 1";
        Statement statement = TestUtils.assertSqlCanBeParsedAndDeparsed(sql, true);
        TablesNamesFinder<Void> tablesNamesFinder = new TablesNamesFinder<Void>();
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
        Set<String> tableNames = TablesNamesFinder.findTables(sqlStr);
        assertThat(tableNames).containsExactlyInAnyOrder("A", "B", "C");

        String exprStr = "A.id=B.id and A.age = (select age from C)";
        tableNames = TablesNamesFinder.findTablesInExpression(exprStr);
        assertThat(tableNames).containsExactlyInAnyOrder("A", "B", "C");
    }

    @Test
    void testRefreshMaterializedView() throws JSQLParserException {
        String sqlStr1 = "REFRESH MATERIALIZED VIEW CONCURRENTLY my_view WITH DATA";
        Set<String> tableNames1 = TablesNamesFinder.findTables(sqlStr1);
        assertThat(tableNames1).containsExactlyInAnyOrder("my_view");

        String sqlStr2 = "REFRESH MATERIALIZED VIEW CONCURRENTLY my_view";
        Set<String> tableNames2 = TablesNamesFinder.findTables(sqlStr2);
        assertThat(tableNames2).containsExactlyInAnyOrder("my_view");

        String sqlStr3 = "REFRESH MATERIALIZED VIEW my_view";
        Set<String> tableNames3 = TablesNamesFinder.findTables(sqlStr3);
        assertThat(tableNames3).containsExactlyInAnyOrder("my_view");

        String sqlStr4 = "REFRESH MATERIALIZED VIEW my_view WITH DATA";
        Set<String> tableNames4 = TablesNamesFinder.findTables(sqlStr4);
        assertThat(tableNames4).containsExactlyInAnyOrder("my_view");

        String sqlStr5 = "REFRESH MATERIALIZED VIEW my_view WITH NO DATA";
        Set<String> tableNames5 = TablesNamesFinder.findTables(sqlStr5);
        assertThat(tableNames5).containsExactlyInAnyOrder("my_view");

        String sqlStr6 = "REFRESH MATERIALIZED VIEW CONCURRENTLY my_view WITH NO DATA";
        Set<String> tableNames6 = TablesNamesFinder.findTables(sqlStr6);
        assertThat(tableNames6).isEmpty();
    }

    @Test
    void testFromParenthesesJoin() throws JSQLParserException {
        String sqlStr = "select * from (t1 left join  t2 on t1.id = t2.id) t_select";
        Set<String> tables = TablesNamesFinder.findTables(sqlStr);
        assertThat(tables).containsExactly("t1", "t2");

    }

    @Test
    void testOtherSources() throws JSQLParserException {
        String sqlStr = "WITH Datetimes AS (\n" +
                "  SELECT DATETIME '2005-01-03 12:34:56' as datetime UNION ALL\n" +
                "  SELECT DATETIME '2007-12-31' UNION ALL\n" +
                "  SELECT DATETIME '2009-01-01' UNION ALL\n" +
                "  SELECT DATETIME '2009-12-31' UNION ALL\n" +
                "  SELECT DATETIME '2017-01-02' UNION ALL\n" +
                "  SELECT DATETIME '2017-05-26'\n" +
                ")\n" +
                "SELECT\n" +
                "  datetime,\n" +
                "  EXTRACT(ISOYEAR FROM datetime) AS isoyear,\n" +
                "  EXTRACT(WEEK FROM datetime) AS isoweek,\n" +
                "  EXTRACT(YEAR FROM datetime) AS year,\n" +
                "  /*APPROXIMATION: WEEK*/ EXTRACT(WEEK FROM datetime) AS week\n" +
                "FROM Datetimes\n" +
                "ORDER BY datetime\n" +
                ";";
        Set<String> tables = TablesNamesFinder.findTablesOrOtherSources(sqlStr);
        assertThat(tables).containsExactly("Datetimes");
    }

    @Test
    void testSubqueryAliasesIssue1987() throws JSQLParserException {
        String sqlStr = "select * from (select * from a) as a1, b;";
        Set<String> tables = TablesNamesFinder.findTablesOrOtherSources(sqlStr);
        assertThat(tables).containsExactlyInAnyOrder("a", "b", "a1");

        tables = TablesNamesFinder.findTables(sqlStr);
        assertThat(tables).containsExactlyInAnyOrder("a", "b");
        assertThat(tables).doesNotContain("a1");

        sqlStr = "select * from b, (select * from a) as a1";
        tables = TablesNamesFinder.findTablesOrOtherSources(sqlStr);
        assertThat(tables).containsExactlyInAnyOrder("a1", "a", "b");

        tables = TablesNamesFinder.findTables(sqlStr);
        assertThat(tables).containsExactlyInAnyOrder("a", "b");
        assertThat(tables).doesNotContain("a1");

        sqlStr = "SELECT * FROM b, (SELECT * FROM a) as a1 WHERE b.id IN ( SELECT id FROM a1 )";
        tables = TablesNamesFinder.findTablesOrOtherSources(sqlStr);
        assertThat(tables).containsExactlyInAnyOrder("a1", "a", "b");

        tables = TablesNamesFinder.findTables(sqlStr);
        assertThat(tables).containsExactlyInAnyOrder("a", "b");
        assertThat(tables).doesNotContain("a1");

        sqlStr = "select (a_alias.col1), b_alias.col2\n" +
                "from b b_alias, a as a_alias, c join b on c.id = b.id\n" +
                "where b_alias.id = a_alias.id and c.id = b_alias.id";
        tables = TablesNamesFinder.findTables(sqlStr);
        assertThat(tables).containsExactlyInAnyOrder("a", "b", "c");

        sqlStr = "with\n" +
                "temp1 as (( select * from b )),\n" +
                "temp2 as ( select (((temp1_alias1.id))) from temp1 temp1_alias1 )\n" +
                "select a_alias.col1, temp1_alias2.col2\n" +
                "from temp1 temp1_alias2, a as a_alias, temp2 join c c_alias on c_alias.id = temp2.id\n"
                +
                "where c.id = temp1_alias2.id";
        tables = TablesNamesFinder.findTables(sqlStr);
        assertThat(tables).containsExactlyInAnyOrder("a", "b", "c");

        sqlStr = "select a.id, (select max(val) from e) as maxval\n" +
                "from a, (select * from b, (select * from c) c_alias) as bc_nested\n" +
                "            where a.id in ( select id from bc_nested join (select * from d) d_alias on bc_nested.id = d_alias.id ) \n"
                +
                "            and a.max > (select max(val) from bc_nested, f) and a.desc like 'abc'";
        tables = TablesNamesFinder.findTables(sqlStr);
        assertThat(tables).containsExactlyInAnyOrder("a", "b", "c", "d", "e", "f");

        sqlStr = " select (select max(val) from e) as maxval, id\n" +
                "            from  (select * from b, (select * from c) c_alias) as bc_nested, a\n" +
                "            where a.max > (select max(val) from bc_nested, f) and \n" +
                "            a.id in ( select id from (select * from d) d_alias join bc_nested on bc_nested.id = d_alias.id )";
        tables = TablesNamesFinder.findTables(sqlStr);
        assertThat(tables).containsExactlyInAnyOrder("a", "b", "c", "d", "e", "f");

        sqlStr = "select a.id, bc_nested.id\n" +
                "            from (select * from b, (select * from c) c_alias) as bc_nested, a\n" +
                "            where a.id in (((\n" +
                "               select id from d join \n" +
                "                   (select * from bc_nested join \n" +
                "                       (select * from e) e_alias on bc_nested.id = e_alias.id\n" +
                "                   ) bc_nested_alias \n" +
                "                   on bc_nested_alias.id = d.id\n" +
                "            )))";
        tables = TablesNamesFinder.findTables(sqlStr);
        assertThat(tables).containsExactlyInAnyOrder("a", "b", "c", "d", "e");

        sqlStr = "select id\n" +
                "from (select * from c, (select * from b) b_alias) as bc_nested, a\n" +
                "where a.id in (\n" +
                "select id from (select * from d \n" +
                "join (select * from e) e_alias on d.id = e_alias.id) bc_nested_alias\n" +
                "join bc_nested on bc_nested_alias.id = bc_nested.id\n" +
                ")";
        tables = TablesNamesFinder.findTables(sqlStr);
        assertThat(tables).containsExactlyInAnyOrder("a", "b", "c", "d", "e");

        sqlStr = "with\n" +
                "    temp1 as (\n" +
                "        select a1.id as id, b.content as content from a a1\n" +
                "        join b on a1.id = b.id\n" +
                "    ),\n" +
                "    temp2 as (\n" +
                "        select b.id as id, b.value as value from b, c cross join temp1 where\n" +
                "        b.id = c.id and b.value = \"b.value\"\n" +
                "    )\n" +
                "select temp1.id, ( select tid from d where cid = 29974 ) as tid \n" +
                "from ( select tid from e, (select * from f) where cid = 29974) e_alias, temp1 cross join temp2\n"
                +
                "where exist ( select * from e, e_alias where e.test = dtest.test ) and temp1.max = (select max(column_1) from g)";
        tables = TablesNamesFinder.findTables(sqlStr);
        assertThat(tables).containsExactlyInAnyOrder("a", "b", "c", "d", "e", "f", "g");
    }

    @Test
    void testSubqueryAliasesIssue2035() throws JSQLParserException {
        String sqlStr = "SELECT * FROM (SELECT * FROM A) AS A \n" +
                "JOIN B ON A.a = B.a \n" +
                "JOIN C ON A.a = C.a;";
        Set<String> tables = TablesNamesFinder.findTablesOrOtherSources(sqlStr);
        assertThat(tables).containsExactlyInAnyOrder("A", "B", "C");

        tables = TablesNamesFinder.findTables(sqlStr);
        assertThat(tables).containsExactlyInAnyOrder("B", "C");
    }

    @Test
    void testTableRenamingIssue2028() throws JSQLParserException {
        List<String> IGNORE_SCHEMAS =
                Arrays.asList("mysql", "information_schema", "performance_schema");
        final String prefix = "test_";

        //@formatter:off
        String sql =
                "UPDATE table_1 a\n" +
                "SET a.a1 = (    SELECT b1\n" +
                "                FROM table_2 b\n" +
                "                WHERE b.xx = 'xx' )\n" +
                "    , a.a2 = (  SELECT b2\n" +
                "                FROM table_2 b\n" +
                "                WHERE b.yy = 'yy' )\n" +
                ";";
        String expected =
                "UPDATE test_table_1 a\n" +
                "SET a.a1 = (    SELECT b1\n" +
                "                FROM test_table_2 b\n" +
                "                WHERE b.xx = 'xx' )\n" +
                "    , a.a2 = (  SELECT b2\n" +
                "                FROM test_table_2 b\n" +
                "                WHERE b.yy = 'yy' )\n" +
                ";";
        //@formatter:on

        TablesNamesFinder<Void> finder = new TablesNamesFinder<>() {
            @Override
            public <S> Void visit(Table table, S context) {
                String schemaName = table.getSchemaName();
                if (schemaName != null && IGNORE_SCHEMAS.contains(schemaName.toLowerCase())) {
                    return super.visit(table, context);
                }
                String originTableName = table.getName();
                table.setName(prefix + originTableName);
                if (originTableName.startsWith("`")) {
                    table.setName("`" + prefix + originTableName.replace("`", "") + "`");
                }
                return super.visit(table, context);
            }
        };
        finder.init(false);

        Statement statement = CCJSqlParserUtil.parse(sql);
        statement.accept(finder);

        TestUtils.assertStatementCanBeDeparsedAs(statement, expected, true);
    }

    @Test
    void testAlterTableIssue2062() throws JSQLParserException {
        String sqlStr = "ALTER TABLE the_cool_db.the_table\n"
                + "    ADD test VARCHAR (40)\n"
                + ";";
        Set<String> tables = TablesNamesFinder.findTablesOrOtherSources(sqlStr);
        assertThat(tables).containsExactlyInAnyOrder("the_cool_db.the_table");

        tables = TablesNamesFinder.findTables(sqlStr);
        assertThat(tables).containsExactlyInAnyOrder("the_cool_db.the_table");
    }

    @Test
    void testInsertTableIssue() throws JSQLParserException {
        String sqlStr = "INSERT INTO  the_cool_db.the_table\n"
                + "    VALUES ( 'something' ) \n"
                + ";";
        Set<String> tables = TablesNamesFinder.findTablesOrOtherSources(sqlStr);
        assertThat(tables).containsExactlyInAnyOrder("the_cool_db.the_table");

        tables = TablesNamesFinder.findTables(sqlStr);
        assertThat(tables).containsExactlyInAnyOrder("the_cool_db.the_table");
    }

    @Test
    void testIssue2183() throws JSQLParserException {
        String sqlStr = "SELECT\n" +
                "\tsubscriber_id,\n" +
                "\tsum(1) OVER (PARTITION BY subscriber_id\n" +
                "ORDER BY\n" +
                "\tstat_time ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW ) AS stop_id\n" +
                "FROM\n" +
                "\t(\n" +
                "\tSELECT\n" +
                "\t\tsubscriber_id,\n" +
                "\t\tstat_time\n" +
                "\tFROM\n" +
                "\t\tlocation_subscriber AS mid2 WINDOW w AS (PARTITION BY subscriber_id\n" +
                "\tORDER BY\n" +
                "\t\tstat_time ROWS BETWEEN 1 PRECEDING AND 1 PRECEDING ) )";
        Set<String> tables = TablesNamesFinder.findTables(sqlStr);
        assertThat(tables).containsExactlyInAnyOrder("location_subscriber");
    }

    @Test
    void testIssue2305() throws JSQLParserException {
        String sqlStr = "SELECT  tbl.fk_id\n" +
                "        , tbl.etape\n" +
                "FROM (  tbl\n" +
                "            JOIN (  SELECT  tbl_1.fk_id\n" +
                "                            , Max( tbl_1.date1 ) AS max_1\n" +
                "                    FROM tbl tbl_1\n" +
                "                    GROUP BY tbl_1.fk_id ) sub2\n" +
                "                ON ( ( ( sub2.fk_id = tbl.fk_id )\n" +
                "                            AND ( sub2.max_1 = tbl.date1 ) ) ) )\n" +
                ";";
        Set<String> tables = TablesNamesFinder.findTables(sqlStr);
        assertThat(tables).containsExactlyInAnyOrder("tbl");
    }

    @Test
    void assertWithItemWithFunctionDeclarationDoesNotThrowException() throws JSQLParserException {
        String sqlStr = "WITH FUNCTION my_with_item(param1 INT) RETURNS INT RETURN param1 + 1 SELECT * FROM my_table;";
        assertThatCode(() -> TablesNamesFinder.findTables(sqlStr))
                .doesNotThrowAnyException();
    }

    @Test
    void assertWithItemWithFunctionDeclarationReturnsTableInSelect() throws JSQLParserException {
        String sqlStr = "WITH FUNCTION my_with_item(param1 INT) RETURNS INT RETURN param1 + 1 SELECT * FROM my_table;";
        assertThat(TablesNamesFinder.findTables(sqlStr)).containsExactly("my_table");
    }
}

