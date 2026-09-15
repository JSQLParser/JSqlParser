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
import net.sf.jsqlparser.statement.PragmaStatement;
import net.sf.jsqlparser.statement.ExtensionStatement;
import net.sf.jsqlparser.statement.AttachStatement;
import net.sf.jsqlparser.statement.DetachStatement;
import net.sf.jsqlparser.statement.ConnectStatement;
import net.sf.jsqlparser.statement.DisconnectStatement;
import net.sf.jsqlparser.statement.PrepareStatement;
import net.sf.jsqlparser.statement.DeallocateStatement;
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

    @Test
    void testPragmaWithArguments() throws JSQLParserException {
        String sqlStr = "PRAGMA table_info('t')";
        PragmaStatement pragma =
                (PragmaStatement) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        Assertions.assertEquals("table_info", pragma.getName());
        Assertions.assertEquals(1, pragma.getParameters().size());
    }

    @Test
    void testPragmaAssignment() throws JSQLParserException {
        String sqlStr = "PRAGMA memory_limit = '1GB'";
        PragmaStatement pragma =
                (PragmaStatement) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        Assertions.assertEquals("'1GB'", pragma.getValue().toString());
    }

    @Test
    void testPragmaWithoutArguments() throws JSQLParserException {
        String sqlStr = "PRAGMA database_list";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testPragmaRemainsUsableAsIdentifier() throws JSQLParserException {
        String sqlStr = "SELECT pragma FROM t";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testLoadExtension() throws JSQLParserException {
        String sqlStr = "LOAD httpfs";
        ExtensionStatement extension =
                (ExtensionStatement) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        Assertions.assertEquals(ExtensionStatement.Operation.LOAD, extension.getOperation());
        Assertions.assertEquals("httpfs", extension.getExtensionName());
    }

    @Test
    void testInstallExtension() throws JSQLParserException {
        String sqlStr = "INSTALL spatial";
        ExtensionStatement extension =
                (ExtensionStatement) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        Assertions.assertEquals(ExtensionStatement.Operation.INSTALL, extension.getOperation());
        Assertions.assertFalse(extension.isForce());
    }

    @Test
    void testForceInstallExtensionFromRepository() throws JSQLParserException {
        String sqlStr = "FORCE INSTALL h3 FROM community";
        ExtensionStatement extension =
                (ExtensionStatement) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        Assertions.assertTrue(extension.isForce());
        Assertions.assertEquals("community", extension.getRepository());
    }

    @Test
    void testInstallExtensionFromUrl() throws JSQLParserException {
        String sqlStr = "INSTALL 'path/to/ext.duckdb_extension'";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testLoadRemainsUsableAsIdentifier() throws JSQLParserException {
        String sqlStr = "SELECT load, install FROM t";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testAttachDatabaseWithAlias() throws JSQLParserException {
        String sqlStr = "ATTACH 'file.db' AS mydb";
        AttachStatement attach =
                (AttachStatement) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        Assertions.assertEquals("'file.db'", attach.getDatabasePath());
        Assertions.assertEquals("mydb", attach.getAlias());
    }

    @Test
    void testAttachDatabaseWithOptions() throws JSQLParserException {
        String sqlStr = "ATTACH DATABASE IF NOT EXISTS 'file.db' AS mydb (TYPE SQLITE, READ_ONLY)";
        AttachStatement attach =
                (AttachStatement) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        Assertions.assertTrue(attach.isIfNotExists());
        Assertions.assertTrue(attach.isUsingDatabaseKeyword());
        Assertions.assertEquals(java.util.Arrays.asList("TYPE SQLITE", "READ_ONLY"),
                attach.getOptions());
    }

    @Test
    void testDetachDatabase() throws JSQLParserException {
        String sqlStr = "DETACH mydb";
        DetachStatement detach =
                (DetachStatement) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        Assertions.assertEquals("mydb", detach.getDatabaseName());
    }

    @Test
    void testDetachDatabaseIfExists() throws JSQLParserException {
        String sqlStr = "DETACH DATABASE IF EXISTS mydb";
        DetachStatement detach =
                (DetachStatement) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        Assertions.assertTrue(detach.isIfExists());
    }

    @Test
    void testAttachRemainsUsableAsIdentifier() throws JSQLParserException {
        String sqlStr = "SELECT attach, detach FROM t";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testConnectToRemoteDatabase() throws JSQLParserException {
        String sqlStr = "CONNECT 'postgres://localhost/mydb'";
        ConnectStatement connect =
                (ConnectStatement) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        Assertions.assertEquals("'postgres://localhost/mydb'", connect.getTarget());
        Assertions.assertNull(connect.getAlias());
    }

    @Test
    void testConnectWithAlias() throws JSQLParserException {
        String sqlStr = "CONNECT 'postgres://localhost/mydb' AS remote";
        ConnectStatement connect =
                (ConnectStatement) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        Assertions.assertEquals("remote", connect.getAlias());
    }

    @Test
    void testDisconnect() throws JSQLParserException {
        String sqlStr = "DISCONNECT";
        DisconnectStatement disconnect =
                (DisconnectStatement) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        Assertions.assertNull(disconnect.getName());
    }

    @Test
    void testDisconnectNamedConnection() throws JSQLParserException {
        String sqlStr = "DISCONNECT remote";
        DisconnectStatement disconnect =
                (DisconnectStatement) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        Assertions.assertEquals("remote", disconnect.getName());
    }

    @Test
    void testConnectByStillParses() throws JSQLParserException {
        String sqlStr = "SELECT * FROM t START WITH id = 1 CONNECT BY PRIOR id = parent_id";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testPrepareStatement() throws JSQLParserException {
        String sqlStr = "PREPARE q AS SELECT * FROM t WHERE a = $1";
        PrepareStatement prepare =
                (PrepareStatement) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        Assertions.assertEquals("q", prepare.getName());
        Assertions.assertNotNull(prepare.getStatement());
    }

    @Test
    void testPrepareInsertStatement() throws JSQLParserException {
        String sqlStr = "PREPARE ins AS INSERT INTO t VALUES ($1, $2)";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testPrepareTablesNamesFinder() throws JSQLParserException {
        String sqlStr = "PREPARE q AS SELECT * FROM ds.t";

        Assertions.assertEquals(java.util.Collections.singletonList("ds.t"),
                new net.sf.jsqlparser.util.TablesNamesFinder<Void>()
                        .getTableList(net.sf.jsqlparser.parser.CCJSqlParserUtil.parse(sqlStr)));
    }

    @Test
    void testDeallocatePreparedStatement() throws JSQLParserException {
        String sqlStr = "DEALLOCATE PREPARE q";
        DeallocateStatement deallocate =
                (DeallocateStatement) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        Assertions.assertEquals("q", deallocate.getName());
        Assertions.assertTrue(deallocate.isUsingPrepareKeyword());
    }

    @Test
    void testExecutePreparedStatement() throws JSQLParserException {
        String sqlStr = "EXECUTE q (1)";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }
}
