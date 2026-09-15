/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2024 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.DescribeStatement;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DuckDBTest {

    @Test
    void testFileTable() throws JSQLParserException {
        String sqlStr = "SELECT * FROM '/tmp/test.parquet'";
        PlainSelect select = (PlainSelect) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
        Table table = (Table) select.getFromItem();

        Assertions.assertEquals("'/tmp/test.parquet'", table.getName());
    }

    @Test
    void testCreateWithStruct() throws JSQLParserException {
        String sqlStr =
                "CREATE TABLE starbake.array_test (\n" +
                        "  keys VARCHAR[] NOT NULL,\n" +
                        "  values1 struct( field1 varchar(255), field2 double) NOT NULL,\n" +
                        "  values2 struct( field1 varchar(255), field2 double) NOT NULL\n" +
                        ");";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testGlobOperator() throws JSQLParserException {
        String sqlStr = "SELECT * FROM t WHERE b GLOB 'y*'";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testNotGlobOperator() throws JSQLParserException {
        String sqlStr = "SELECT * FROM t WHERE b NOT GLOB 'y*'";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testGlobRemainsUsableAsIdentifier() throws JSQLParserException {
        String sqlStr = "SELECT glob FROM t";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testSemiJoinWithoutLeft() throws JSQLParserException {
        String sqlStr = "SELECT * FROM t SEMI JOIN u USING (id)";
        PlainSelect select = (PlainSelect) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
        Join join = select.getJoins().get(0);

        Assertions.assertTrue(join.isSemi());
        Assertions.assertFalse(join.isLeft());
        Assertions.assertNull(((Table) select.getFromItem()).getAlias());
    }

    @Test
    void testAntiJoinWithoutLeft() throws JSQLParserException {
        String sqlStr = "SELECT * FROM t ANTI JOIN u ON t.id = u.id";
        PlainSelect select = (PlainSelect) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
        Join join = select.getJoins().get(0);

        Assertions.assertTrue(join.isAnti());
        Assertions.assertNull(((Table) select.getFromItem()).getAlias());
    }

    @Test
    void testLeftAntiJoin() throws JSQLParserException {
        String sqlStr = "SELECT * FROM t LEFT ANTI JOIN u ON t.id = u.id";
        PlainSelect select = (PlainSelect) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
        Join join = select.getJoins().get(0);

        Assertions.assertTrue(join.isLeft());
        Assertions.assertTrue(join.isAnti());
    }

    @Test
    void testAntiRemainsUsableAsIdentifier() throws JSQLParserException {
        String sqlStr = "SELECT anti FROM t anti";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testMapColumnType() throws JSQLParserException {
        String sqlStr = "CREATE TABLE t (m MAP(VARCHAR, INTEGER))";
        CreateTable createTable =
                (CreateTable) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        Assertions.assertEquals("MAP",
                createTable.getColumnDefinitions().get(0).getColDataType().getDataType());
        Assertions.assertEquals(java.util.Arrays.asList("VARCHAR", "INTEGER"),
                createTable.getColumnDefinitions().get(0).getColDataType()
                        .getArgumentsStringList());
    }

    @Test
    void testMapCast() throws JSQLParserException {
        String sqlStr = "SELECT CAST(m AS MAP(VARCHAR, INTEGER)) FROM t";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testDecimalTypeArgumentsStillParse() throws JSQLParserException {
        String sqlStr = "CREATE TABLE t (a DECIMAL(10, 2), b VARCHAR(255))";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testInsertByName() throws JSQLParserException {
        String sqlStr = "INSERT INTO t BY NAME SELECT 1 AS b, 2 AS a";
        Insert insert = (Insert) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        Assertions.assertEquals(Insert.ColumnMatching.BY_NAME, insert.getColumnMatching());
    }

    @Test
    void testInsertByPosition() throws JSQLParserException {
        String sqlStr = "INSERT INTO t BY POSITION SELECT 1, 2";
        Insert insert = (Insert) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        Assertions.assertEquals(Insert.ColumnMatching.BY_POSITION, insert.getColumnMatching());
    }

    @Test
    void testInsertWithoutColumnMatching() throws JSQLParserException {
        String sqlStr = "INSERT INTO t (a, b) VALUES (1, 2)";
        Insert insert = (Insert) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        Assertions.assertNull(insert.getColumnMatching());
    }

    @Test
    void testUsingSamplePercentShorthand() throws JSQLParserException {
        String sqlStr = "SELECT * FROM t USING SAMPLE 10%";
        PlainSelect select = (PlainSelect) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
        SampleClause sampleClause = select.getFromItem().getSampleClause();

        Assertions.assertEquals("%", sampleClause.getPercentageUnit());
        Assertions.assertNull(sampleClause.getMethod());
    }

    @Test
    void testUsingSamplePercentWithMethodInBrackets() throws JSQLParserException {
        String sqlStr = "SELECT * FROM t USING SAMPLE 10 PERCENT (bernoulli)";
        PlainSelect select = (PlainSelect) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        Assertions.assertEquals(SampleClause.SampleMethod.BERNOULLI,
                select.getFromItem().getSampleClause().getMethod());
    }

    @Test
    void testUsingSampleRowsWithSeed() throws JSQLParserException {
        String sqlStr = "SELECT * FROM t USING SAMPLE 10 ROWS (system, 377)";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testUsingSampleReservoir() throws JSQLParserException {
        String sqlStr = "SELECT * FROM t USING SAMPLE reservoir (50 ROWS)";
        PlainSelect select = (PlainSelect) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        Assertions.assertEquals(SampleClause.SampleMethod.RESERVOIR,
                select.getFromItem().getSampleClause().getMethod());
    }

    @Test
    void testDescribeQuery() throws JSQLParserException {
        String sqlStr = "DESCRIBE SELECT * FROM t";
        DescribeStatement describe =
                (DescribeStatement) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        Assertions.assertNotNull(describe.getSelect());
        Assertions.assertNull(describe.getTable());
    }

    @Test
    void testDescribeQueryTablesNamesFinder() throws JSQLParserException {
        String sqlStr = "DESCRIBE SELECT * FROM ds.t";

        Assertions.assertEquals(java.util.Collections.singletonList("ds.t"),
                new net.sf.jsqlparser.util.TablesNamesFinder<Void>()
                        .getTableList(net.sf.jsqlparser.parser.CCJSqlParserUtil.parse(sqlStr)));
    }

    @Test
    void testDescribeTableStillParses() throws JSQLParserException {
        String sqlStr = "DESCRIBE t";
        DescribeStatement describe =
                (DescribeStatement) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        Assertions.assertEquals("t", describe.getTable().getName());
    }
}
