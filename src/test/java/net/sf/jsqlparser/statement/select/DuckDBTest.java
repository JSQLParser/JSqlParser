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
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.LambdaExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
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
import net.sf.jsqlparser.statement.CopyStatement;
import net.sf.jsqlparser.statement.create.function.CreateFunction;
import net.sf.jsqlparser.statement.create.macro.CreateMacro;
import net.sf.jsqlparser.statement.create.extension.CreateExtensionRepository;
import net.sf.jsqlparser.statement.create.trigger.CreateTrigger;
import net.sf.jsqlparser.test.TestUtils;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;

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

    @Test
    void testCopyFromFile() throws JSQLParserException {
        String sqlStr = "COPY t FROM 'in.csv'";
        CopyStatement copy =
                (CopyStatement) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        Assertions.assertTrue(copy.isFrom());
        Assertions.assertEquals("t", copy.getTable().getName());
        Assertions.assertEquals("'in.csv'", copy.getPath());
    }

    @Test
    void testCopyColumnsFromFileWithOptions() throws JSQLParserException {
        String sqlStr = "COPY t (a, b) FROM 'in.csv' (AUTO_DETECT true)";
        CopyStatement copy =
                (CopyStatement) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        Assertions.assertEquals(2, copy.getColumns().size());
        Assertions.assertEquals(java.util.Collections.singletonList("AUTO_DETECT true"),
                copy.getOptions());
    }

    @Test
    void testCopyQueryToFile() throws JSQLParserException {
        String sqlStr =
                "COPY (SELECT * FROM t) TO 'out.parquet' (FORMAT PARQUET, COMPRESSION ZSTD)";
        CopyStatement copy =
                (CopyStatement) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        Assertions.assertFalse(copy.isFrom());
        Assertions.assertNotNull(copy.getSelect());
    }

    @Test
    void testCopyTableToFile() throws JSQLParserException {
        String sqlStr = "COPY t TO 'out.csv' (FORMAT CSV, DELIMITER '|')";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testCopyTablesNamesFinder() throws JSQLParserException {
        String sqlStr = "COPY ds.t FROM 'in.csv'";

        Assertions.assertEquals(java.util.Collections.singletonList("ds.t"),
                new net.sf.jsqlparser.util.TablesNamesFinder<Void>()
                        .getTableList(net.sf.jsqlparser.parser.CCJSqlParserUtil.parse(sqlStr)));
    }

    @Test
    void testCreateScalarMacro() throws JSQLParserException {
        String sqlStr = "CREATE MACRO add_one(a) AS a + 1";
        CreateMacro createMacro =
                (CreateMacro) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        Assertions.assertEquals("add_one", createMacro.getName());
        Assertions.assertEquals(1, createMacro.getParameters().size());
        Assertions.assertFalse(createMacro.isTable());
    }

    @Test
    void testCreateOrReplaceTemporaryMacroWithDefault() throws JSQLParserException {
        String sqlStr = "CREATE OR REPLACE TEMPORARY MACRO add(a, b := 5) AS a + b";
        CreateMacro createMacro =
                (CreateMacro) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        Assertions.assertTrue(createMacro.isOrReplace());
        Assertions.assertTrue(createMacro.isTemporary());
        Assertions.assertEquals("5",
                createMacro.getParameters().get(1).getDefaultValue().toString());
    }

    @Test
    void testCreateTableMacro() throws JSQLParserException {
        String sqlStr = "CREATE MACRO get_t(i) AS TABLE SELECT * FROM t WHERE id = i";
        CreateMacro createMacro =
                (CreateMacro) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        Assertions.assertTrue(createMacro.isTable());
        Assertions.assertNotNull(createMacro.getSelect());
    }

    @Test
    void testCreateMacroIfNotExists() throws JSQLParserException {
        String sqlStr = "CREATE MACRO IF NOT EXISTS add(a, b) AS a + b";
        CreateMacro createMacro =
                (CreateMacro) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        Assertions.assertTrue(createMacro.isIfNotExists());
        Assertions.assertEquals("add", createMacro.getName());
    }

    @Test
    void testCreateMacroWithTypedParameters() throws JSQLParserException {
        String sqlStr = "CREATE MACRO add(a INTEGER, b DOUBLE, c := 5) AS a + b + c";
        CreateMacro createMacro =
                (CreateMacro) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        Assertions.assertEquals("INTEGER", createMacro.getParameters().get(0).getType().toString());
        Assertions.assertEquals("DOUBLE", createMacro.getParameters().get(1).getType().toString());
        Assertions.assertNull(createMacro.getParameters().get(2).getType());
        Assertions.assertEquals("5",
                createMacro.getParameters().get(2).getDefaultValue().toString());
    }

    @Test
    void testCreateOverloadedMacro() throws JSQLParserException {
        String sqlStr = "CREATE OR REPLACE MACRO is_maximal(a TINYINT) AS a = 127, "
                + "(a INT) AS a = 2147483647";
        CreateMacro createMacro =
                (CreateMacro) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        Assertions.assertEquals(2, createMacro.getOverloads().size());
        Assertions.assertEquals("TINYINT",
                createMacro.getOverloads().get(0).getParameters().get(0).getType().toString());
        Assertions.assertEquals("a = 2147483647",
                createMacro.getOverloads().get(1).getExpression().toString());
    }

    @Test
    void testCreateOverloadedTableMacro() throws JSQLParserException {
        String sqlStr = "CREATE MACRO get_t(i) AS TABLE SELECT * FROM t WHERE id = i, "
                + "(i, j) AS TABLE SELECT * FROM u WHERE id = i AND x = j";
        CreateMacro createMacro =
                (CreateMacro) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        Assertions.assertEquals(2, createMacro.getOverloads().size());
        Assertions.assertTrue(createMacro.getOverloads().get(1).isTable());
        Assertions.assertEquals(2, createMacro.getOverloads().get(1).getParameters().size());
        Assertions.assertEquals(new HashSet<>(Arrays.asList("t", "u")),
                TablesNamesFinder.findTables(sqlStr));
    }

    @Test
    void testCreateFunctionAsMacro() throws JSQLParserException {
        for (String sqlStr : Arrays.asList(
                "CREATE FUNCTION add(a, b) AS a + b",
                "CREATE OR REPLACE TEMPORARY FUNCTION IF NOT EXISTS add(a INTEGER) AS a + 1",
                "CREATE FUNCTION get_t(i) AS TABLE SELECT * FROM t WHERE id = i")) {
            CreateMacro createMacro =
                    (CreateMacro) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

            Assertions.assertTrue(createMacro.isFunctionKeyword());
        }
    }

    @Test
    void testCreateFunctionOfOtherDialectsIsNotAMacro() throws JSQLParserException {
        for (String sqlStr : Arrays.asList(
                "CREATE FUNCTION add(a integer, b integer) RETURNS integer AS 'select $1 + $2' LANGUAGE SQL",
                "CREATE FUNCTION add(IN a integer, OUT b integer) AS 'select $1 + 1' LANGUAGE SQL",
                "CREATE FUNCTION add(IN a integer, OUT b integer) AS $$ select $1 + 1 $$ LANGUAGE SQL",
                "CREATE FUNCTION add(IN a integer, OUT b integer) AS $function$ select $1 + 1 $function$ LANGUAGE SQL",
                "CREATE OR REPLACE FUNCTION ds.add(x INT64) AS (x + 1) OPTIONS (description = 'd')")) {
            Assertions.assertInstanceOf(CreateFunction.class, CCJSqlParserUtil.parse(sqlStr));
        }
    }

    @Test
    void testCreateExtensionRepository() throws JSQLParserException {
        String sqlStr =
                "CREATE EXTENSION REPOSITORY my_repo WITH PREFIX 'https://extensions.example.org'";
        CreateExtensionRepository createRepository =
                (CreateExtensionRepository) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        Assertions.assertEquals("my_repo", createRepository.getName());
        Assertions.assertEquals("'https://extensions.example.org'",
                createRepository.getPrefix().toString());
    }

    @Test
    void testCreateExtensionRepositoryIfNotExists() throws JSQLParserException {
        String sqlStr = "CREATE EXTENSION REPOSITORY IF NOT EXISTS my_repo";
        CreateExtensionRepository createRepository =
                (CreateExtensionRepository) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        Assertions.assertTrue(createRepository.isIfNotExists());
    }

    @Test
    void testCreatePostgresExtensionStillParses() throws JSQLParserException {
        String sqlStr = "CREATE EXTENSION IF NOT EXISTS hstore WITH SCHEMA public CASCADE";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testCreateTriggerWithTransitionTablesAndStatementBody() throws JSQLParserException {
        String sqlStr = "CREATE TRIGGER trg_audit AFTER UPDATE ON target "
                + "REFERENCING OLD TABLE AS o NEW TABLE AS n FOR EACH STATEMENT "
                + "INSERT INTO audit SELECT n.id, o.val, n.val FROM o JOIN n ON o.id = n.id";
        CreateTrigger createTrigger =
                (CreateTrigger) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        Assertions.assertEquals(2, createTrigger.getTransitionRelations().size());
        Assertions.assertEquals(CreateTrigger.Orientation.STATEMENT,
                createTrigger.getOrientation());
        Assertions.assertNotNull(createTrigger.getBody());
    }

    @Test
    void testCreateTriggerBeforeDeleteWithStatementBody() throws JSQLParserException {
        String sqlStr = "CREATE TRIGGER trg BEFORE DELETE ON t REFERENCING OLD TABLE AS o "
                + "FOR EACH STATEMENT DELETE FROM audit WHERE id IN (SELECT id FROM o)";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testPostgreSqlTriggerStillParses() throws JSQLParserException {
        String sqlStr = "CREATE TRIGGER trg AFTER UPDATE ON target "
                + "REFERENCING OLD TABLE AS o NEW TABLE AS n FOR EACH STATEMENT "
                + "EXECUTE FUNCTION f()";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testFromFirstWithSelect() throws JSQLParserException {
        String sqlStr = "FROM t SELECT a, b";
        PlainSelect select = (PlainSelect) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        Assertions.assertTrue(select.isFromFirst());
        Assertions.assertEquals(2, select.getSelectItems().size());
    }

    @Test
    void testFromFirstWithWhere() throws JSQLParserException {
        String sqlStr = "FROM t WHERE a > 1";
        PlainSelect select = (PlainSelect) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        Assertions.assertTrue(select.isFromFirst());
        Assertions.assertNull(select.getSelectItems());
    }

    @Test
    void testFromFirstWithClauses() throws JSQLParserException {
        String sqlStr = "FROM t SELECT a, count(*) WHERE a > 1 GROUP BY a HAVING count(*) > 2 "
                + "ORDER BY a LIMIT 10";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testFromFirstWithJoin() throws JSQLParserException {
        String sqlStr = "FROM t JOIN u ON t.id = u.id SELECT t.a";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testFromFirstTablesNamesFinder() throws JSQLParserException {
        String sqlStr = "FROM ds.t WHERE a > 1";

        Assertions.assertEquals(java.util.Collections.singletonList("ds.t"),
                new net.sf.jsqlparser.util.TablesNamesFinder<Void>()
                        .getTableList(net.sf.jsqlparser.parser.CCJSqlParserUtil.parse(sqlStr)));
    }

    @Test
    void testBarePipeQueryStillParses() throws JSQLParserException {
        String sqlStr = "FROM t |> WHERE a > 1";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testUnPivotStatement() throws JSQLParserException {
        String sqlStr = "UNPIVOT monthly_sales ON jan, feb INTO NAME month VALUE sales";
        UnPivotQuery unPivotQuery =
                (UnPivotQuery) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        Assertions.assertEquals("month", unPivotQuery.getNameColumn());
        Assertions.assertEquals(2, unPivotQuery.getOnExpressions().size());
        Assertions.assertEquals(1, unPivotQuery.getValueColumns().size());
    }

    @Test
    void testUnPivotStatementWithoutIntoClause() throws JSQLParserException {
        String sqlStr = "UNPIVOT monthly_sales ON jan, feb";
        UnPivotQuery unPivotQuery =
                (UnPivotQuery) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        Assertions.assertNull(unPivotQuery.getNameColumn());
    }

    @Test
    void testUnPivotStatementTablesNamesFinder() throws JSQLParserException {
        String sqlStr = "UNPIVOT ds.monthly_sales ON jan, feb INTO NAME month VALUE sales";

        Assertions.assertEquals(java.util.Collections.singletonList("ds.monthly_sales"),
                new net.sf.jsqlparser.util.TablesNamesFinder<Void>()
                        .getTableList(net.sf.jsqlparser.parser.CCJSqlParserUtil.parse(sqlStr)));
    }

    @Test
    void testUnPivotInFromClauseStillParses() throws JSQLParserException {
        String sqlStr = "SELECT * FROM monthly_sales UNPIVOT (sales FOR month IN (jan, feb))";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testApproxNearestJoin() throws JSQLParserException {
        String sqlStr = "SELECT * FROM queries q INNER JOIN products t "
                + "APPROX NEAREST 2 BY SIMILARITY array_cosine_similarity(q.vec, t.vec)";
        PlainSelect select = (PlainSelect) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
        Join join = select.getJoins().get(0);

        Assertions.assertEquals(2L, join.getApproxNearest());
        Assertions.assertEquals("array_cosine_similarity(q.vec, t.vec)",
                join.getSimilarity().toString());
    }

    @Test
    void testApproxNearestJoinWithoutAlias() throws JSQLParserException {
        String sqlStr = "SELECT * FROM queries JOIN products "
                + "APPROX NEAREST 5 BY SIMILARITY array_distance(queries.vec, products.vec)";
        PlainSelect select = (PlainSelect) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        Assertions.assertNull(((Table) select.getJoins().get(0).getFromItem()).getAlias());
    }

    @Test
    void testApproxRemainsUsableAsIdentifier() throws JSQLParserException {
        String sqlStr = "SELECT approx, nearest, similarity FROM t";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testRecursiveCteUsingKey() throws JSQLParserException {
        String sqlStr = "WITH RECURSIVE tbl (a, b) USING KEY (a, avg(b)) AS "
                + "(SELECT 1, 2 UNION ALL SELECT a + 1, b FROM tbl WHERE a < 3) SELECT * FROM tbl";
        PlainSelect select = (PlainSelect) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
        WithItem<?> withItem = select.getWithItemsList().get(0);

        Assertions.assertEquals(2, withItem.getUsingKeyExpressions().size());
    }

    @Test
    void testRecursiveCteUsingKeySingleColumn() throws JSQLParserException {
        String sqlStr = "WITH RECURSIVE tbl USING KEY (a) AS (SELECT 1 AS a) SELECT * FROM tbl";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testRecursiveCteWithoutUsingKey() throws JSQLParserException {
        String sqlStr = "WITH RECURSIVE tbl (a) AS (SELECT 1) SELECT * FROM tbl";
        PlainSelect select = (PlainSelect) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        Assertions.assertNull(select.getWithItemsList().get(0).getUsingKeyExpressions());
    }

    @Test
    void testLambdaKeyword() throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(
                "SELECT list_transform(l, lambda x : x + 1) FROM t", true);
        TestUtils.assertSqlCanBeParsedAndDeparsed("SELECT list_filter(l, lambda x : x > 0) FROM t",
                true);
        TestUtils.assertSqlCanBeParsedAndDeparsed(
                "SELECT list_filter(l, lambda x : x IS NOT NULL AND x <> 'a') FROM t", true);
        TestUtils.assertSqlCanBeParsedAndDeparsed(
                "SELECT list_reduce(l, lambda acc, x : acc + x) FROM t", true);
        TestUtils.assertSqlCanBeParsedAndDeparsed(
                "SELECT list_filter([(CASE WHEN a THEN 1 END)], lambda x : x IS NOT NULL) FROM t",
                true);
        TestUtils.assertSqlCanBeParsedAndDeparsed(
                "SELECT list_transform(l, lambda x : list_filter(x, lambda y : y > 1)) FROM t",
                true);
    }

    @Test
    void testLambdaKeywordExpression() throws JSQLParserException {
        PlainSelect select = (PlainSelect) TestUtils.assertSqlCanBeParsedAndDeparsed(
                "SELECT list_reduce(l, lambda acc, x : acc + x) FROM t", true);
        Function function = select.getSelectItem(0).getExpression(Function.class);
        LambdaExpression lambda = (LambdaExpression) function.getParameters().get(1);

        Assertions.assertTrue(lambda.isLambdaKeyword());
        Assertions.assertEquals(Arrays.asList("acc", "x"), lambda.getIdentifiers());
    }

    @Test
    void testLambdaKeywordFollowedByArguments() throws JSQLParserException {
        PlainSelect select = (PlainSelect) TestUtils.assertSqlCanBeParsedAndDeparsed(
                "SELECT f(l, lambda x : x + 1, 2) FROM t", true);
        Function function = select.getSelectItem(0).getExpression(Function.class);

        Assertions.assertEquals(3, function.getParameters().size());
        Assertions.assertEquals("x + 1",
                ((LambdaExpression) function.getParameters().get(1)).getExpression().toString());
    }

    @Test
    void testLambdaAsIdentifier() throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed("SELECT lambda FROM t", true);
        TestUtils.assertSqlCanBeParsedAndDeparsed("SELECT lambda x, y FROM t", true);
        TestUtils.assertSqlCanBeParsedAndDeparsed("SELECT t.lambda FROM lambda t", true);
        TestUtils.assertSqlCanBeParsedAndDeparsed("SELECT CAST(a AS lambda) FROM t", true);
        TestUtils.assertSqlCanBeParsedAndDeparsed("SELECT 'a' COLLATE lambda FROM t", true);
        TestUtils.assertSqlCanBeParsedAndDeparsed("CREATE TABLE t (a lambda)", true);
        TestUtils.assertSqlCanBeParsedAndDeparsed("ALTER TABLE t DROP COLUMN lambda", true);
        TestUtils.assertSqlCanBeParsedAndDeparsed("ALTER TABLE t RENAME lambda TO x", true);
    }

    @Test
    void testFromFirstAndTableAsQueryOperands() throws JSQLParserException {
        for (String sqlStr : new String[] {"SELECT * FROM (FROM t)",
                "SELECT * FROM (FROM t WHERE a > 1) x JOIN (SELECT 1) y ON true",
                "SELECT * FROM t WHERE a IN (FROM u)", "SELECT (FROM u SELECT max(a))",
                "FROM a UNION ALL FROM b", "FROM a WHERE x > 1 UNION ALL SELECT 1",
                "SELECT 1 EXCEPT FROM b WHERE x", "FROM a SELECT x UNION FROM b SELECT x",
                "WITH t AS (SELECT 1), u AS (SELECT 2) TABLE t", "TABLE a UNION TABLE b",
                "SELECT 1 UNION TABLE b", "(TABLE a EXCEPT ALL TABLE b)",
                "SELECT * FROM TABLE(f(1))"}) {
            TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
        }

        SetOperationList union = (SetOperationList) CCJSqlParserUtil
                .parse("TABLE a UNION TABLE b ORDER BY 1 LIMIT 2");
        Assertions.assertEquals("1", union.getOrderByElements().get(0).toString());
        Assertions.assertNotNull(union.getLimit());
        Assertions.assertNull(((TableStatement) union.getSelect(1)).getOrderByElements());

        union = (SetOperationList) CCJSqlParserUtil.parse("FROM a UNION FROM b ORDER BY 1");
        Assertions.assertTrue(((PlainSelect) union.getSelect(0)).isFromFirst());
        Assertions.assertEquals("1", union.getOrderByElements().get(0).toString());
    }

    @Test
    void testTableQueryKeepsTableColumnsAndTrailingClauses() throws JSQLParserException {
        // TABLE also names columns, e.g. in ClickHouse's system.parts
        for (String sqlStr : new String[] {
                "SELECT database, table FROM system.parts WHERE (database = 'default') AND (table = 'hits')",
                "SELECT (table), (table.a), (table + 1) FROM t",
                "SELECT a FROM t WHERE (table, b) IN ((1, 2))",
                "SELECT * FROM (TABLE s.t) x", "TABLE t FETCH FIRST 1 ROWS ONLY",
                "WITH a AS (SELECT 1) TABLE a FETCH FIRST 1 ROWS ONLY",
                "CREATE TABLE x AS TABLE a UNION TABLE b"}) {
            TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
        }
        Assertions.assertInstanceOf(net.sf.jsqlparser.statement.piped.FromQuery.class,
                ((ParenthesedSelect) CCJSqlParserUtil.parse("(FROM t)")).getSelect());
    }

    @Test
    void testLimitPercent() throws JSQLParserException {
        for (String sqlStr : new String[] {"SELECT a FROM t LIMIT 10%",
                "SELECT a FROM t LIMIT 10.5%", "SELECT a FROM t LIMIT 40 PERCENT",
                "SELECT a FROM t LIMIT 50% OFFSET 2", "SELECT a FROM t LIMIT (SELECT 10)%",
                "SELECT * FROM (SELECT a FROM t LIMIT 10%) x",
                "SELECT a FROM t LIMIT (SELECT a FROM u LIMIT 5%)", "SELECT a FROM t LIMIT NULL%",
                // modulo and a column named offset are unchanged
                "SELECT a FROM t LIMIT 10 % 3", "SELECT a FROM t LIMIT a % offset",
                "SELECT a FROM t LIMIT a % offset + 1", "SELECT a FROM t LIMIT a % offset OFFSET 2",
                "SELECT a FROM t LIMIT (SELECT a % offset + 1 FROM u)"}) {
            TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
        }
        PlainSelect select = (PlainSelect) CCJSqlParserUtil.parse("SELECT a FROM t LIMIT 10%");
        Assertions.assertEquals("%", select.getLimit().getPercentUnit());
        Assertions.assertEquals("10", select.getLimit().getRowCount().toString());

        select = (PlainSelect) CCJSqlParserUtil.parse("SELECT a FROM t LIMIT a % offset + 1");
        Assertions.assertNull(select.getLimit().getPercentUnit());
        Assertions.assertNull(select.getOffset());
    }

    @Test
    void testTableSampleWithoutOrWithReservoirMethod() throws JSQLParserException {
        for (String sqlStr : new String[] {"SELECT a FROM t TABLESAMPLE 10%",
                "SELECT a FROM t TABLESAMPLE 10 ROWS", "SELECT a FROM t t1 TABLESAMPLE 10 PERCENT",
                "SELECT a FROM t TABLESAMPLE RESERVOIR (10 ROWS) REPEATABLE (1)",
                "SELECT a FROM (SELECT 1) TABLESAMPLE 10%",
                "SELECT a FROM read_csv('x') TABLESAMPLE 10%",
                "SELECT a FROM range(10) r USING SAMPLE 10%"}) {
            TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
        }
        // after DuckDB's TABLESAMPLE n, OFFSET belongs to the query
        PlainSelect select = (PlainSelect) CCJSqlParserUtil
                .parse("SELECT a FROM t TABLESAMPLE 10 OFFSET 3");
        Assertions.assertEquals("3", select.getOffset().getOffset().toString());
    }

    @Test
    void testDoubleEqualsOperator() throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed("SELECT a FROM t WHERE a == 1 AND b = 2", true);
        Assertions.assertEquals("==",
                ((net.sf.jsqlparser.expression.operators.relational.EqualsTo) CCJSqlParserUtil
                        .parseCondExpression("a == 1")).getStringExpression());
    }

    @Test
    void testExponentiationOperator() throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed("SELECT 2 ** 3, 2 * 3 ** 2, -2 ** 2", true);
        // above the multiplicative operators, left-associative, below the unary sign
        Assertions.assertEquals("Multiplication(2, Exponentiation(3, 2))",
                tree(CCJSqlParserUtil.parseExpression("2 * 3 ** 2")));
        Assertions.assertEquals("Exponentiation(Exponentiation(2, 3), 2)",
                tree(CCJSqlParserUtil.parseExpression("2 ** 3 ** 2")));
        Assertions.assertEquals("Exponentiation(-2, 2)",
                tree(CCJSqlParserUtil.parseExpression("-2 ** 2")));
    }

    @Test
    void testLikeOperators() throws JSQLParserException {
        for (String operator : new String[] {"~~", "~~*", "~~~", "!~~", "!~~*"}) {
            TestUtils.assertSqlCanBeParsedAndDeparsed(
                    "SELECT a FROM t WHERE a " + operator + " 'x%'", true);
        }
        net.sf.jsqlparser.expression.operators.relational.LikeExpression like =
                (net.sf.jsqlparser.expression.operators.relational.LikeExpression) CCJSqlParserUtil
                        .parseCondExpression("a !~~* 'x%'");
        Assertions.assertEquals(
                net.sf.jsqlparser.expression.operators.relational.LikeExpression.KeyWord.ILIKE,
                like.getLikeKeyWord());
        Assertions.assertTrue(like.isNot());
        // the regular expression operators are unchanged
        Assertions.assertInstanceOf(
                net.sf.jsqlparser.expression.operators.relational.RegExpMatchOperator.class,
                CCJSqlParserUtil.parseCondExpression("a !~* 'x'"));
    }

    @Test
    void testChainedSigns() throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(
                "SELECT - -1, +-+1, ++-++-+i, ~ ~a, x - -1 FROM t",
                true);
        Assertions.assertEquals("- - -a", CCJSqlParserUtil.parseExpression("- - -a").toString());
    }

    @Test
    void testLikeOperatorFollowsTheModel() throws JSQLParserException {
        net.sf.jsqlparser.expression.operators.relational.LikeExpression like =
                (net.sf.jsqlparser.expression.operators.relational.LikeExpression) CCJSqlParserUtil
                        .parseCondExpression("a ~~ 'x'");
        like.setNot(true);
        Assertions.assertEquals("a !~~ 'x'", like.toString());
        like.setLikeKeyWord(
                net.sf.jsqlparser.expression.operators.relational.LikeExpression.KeyWord.SIMILAR_TO);
        Assertions.assertEquals("a NOT SIMILAR TO 'x'", like.toString());
        Assertions.assertThrows(IllegalArgumentException.class, () -> like.setLikeOperator("!~~~"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> like.setLikeOperator(null));
    }

    @Test
    void testMixedSignsStaySeparate() throws JSQLParserException {
        for (String expression : new String[] {"- ~1", "~ -1", "~ - ~a", "- ~ -a", "+ ~a"}) {
            String deparsed = CCJSqlParserUtil.parseExpression(expression).toString();
            Assertions.assertEquals(expression.replace(" ", ""),
                    deparsed.replace(" ", ""));
            Assertions.assertFalse(deparsed.contains("-~") || deparsed.contains("~-")
                    || deparsed.contains("~~") || deparsed.contains("+~"), deparsed);
        }
        TestUtils.assertSqlCanBeParsedAndDeparsed("SELECT - ~1, ~ -1, -+1", true);
    }

    private static String tree(net.sf.jsqlparser.expression.Expression expression) {
        if (expression instanceof net.sf.jsqlparser.expression.BinaryExpression) {
            net.sf.jsqlparser.expression.BinaryExpression binary =
                    (net.sf.jsqlparser.expression.BinaryExpression) expression;
            return expression.getClass().getSimpleName() + "(" + tree(binary.getLeftExpression())
                    + ", " + tree(binary.getRightExpression()) + ")";
        }
        return expression.toString();
    }
}
