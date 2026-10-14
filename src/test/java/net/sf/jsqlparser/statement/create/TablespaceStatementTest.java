/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create;

import static net.sf.jsqlparser.test.TestUtils.assertDeparse;
import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementFeatures;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.StmtFeature;
import net.sf.jsqlparser.statement.UnsupportedStatement;
import net.sf.jsqlparser.statement.alter.AlterTablespace;
import net.sf.jsqlparser.statement.alter.AlterTablespaceMove;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.IndexOption;
import net.sf.jsqlparser.statement.create.table.TableOption;
import net.sf.jsqlparser.statement.create.tablespace.CreateTablespace;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TablespaceStatementTest {

    private static Statement parse(String sql, Dialect dialect) throws JSQLParserException {
        return CCJSqlParserUtil.parse(sql, parser -> parser.withDialect(dialect));
    }

    /** Parses with the dialect, compares both renderers with the input and reparses the output. */
    private static <T extends Statement> T assertRoundTrip(String sql, Dialect dialect,
            Class<T> type) throws JSQLParserException {
        Statement statement =
                assertSqlCanBeParsedAndDeparsed(sql, true, parser -> parser.withDialect(dialect));
        assertInstanceOf(type, statement);
        Statement reparsed = parse(statement.toString(), dialect);
        assertInstanceOf(type, reparsed);
        assertEquals(statement.toString(), reparsed.toString());
        return type.cast(statement);
    }

    private static List<TableOption.Kind> kinds(List<TableOption> options) {
        return options.stream().map(TableOption::getKind).collect(Collectors.toList());
    }

    private static List<String> names(List<IndexOption> parameters) {
        return parameters.stream().map(IndexOption::getName).collect(Collectors.toList());
    }

    @Test
    void mysqlCreateTablespaceWithDataFileAndEngine() throws JSQLParserException {
        CreateTablespace statement = assertRoundTrip(
                "CREATE TABLESPACE ts1 ADD DATAFILE 'ts1.ibd' ENGINE=InnoDB", Dialect.MYSQL,
                CreateTablespace.class);

        assertEquals("ts1", statement.getName());
        assertFalse(statement.isUndo());
        assertEquals("ts1.ibd", statement.getDataFile().getValue());
        TableOption engine = statement.getOption(TableOption.Kind.ENGINE).orElseThrow();
        assertEquals("InnoDB", engine.getValue());
        assertTrue(engine.isUseEquals());
        assertNull(statement.getOwner());
        assertNull(statement.getLocation());
        assertNull(statement.getParameters());
        assertEquals("CREATE TABLESPACE ts1 ADD DATAFILE 'ts1.ibd' ENGINE = InnoDB",
                statement.toString());
    }

    @Test
    void mysqlOptionsKeepSourceOrderAndReuseTableOptionModels() throws JSQLParserException {
        CreateTablespace statement = (CreateTablespace) parse(
                "CREATE TABLESPACE ts1 ADD DATAFILE 'ts1.ibd' AUTOEXTEND_SIZE 4M, "
                        + "FILE_BLOCK_SIZE = 8192 ENCRYPTION 'Y', ENGINE_ATTRIBUTE = '{}' "
                        + "ENGINE InnoDB",
                Dialect.MYSQL);

        assertEquals(List.of(TableOption.Kind.AUTOEXTEND_SIZE, TableOption.Kind.FILE_BLOCK_SIZE,
                TableOption.Kind.ENCRYPTION, TableOption.Kind.ENGINE_ATTRIBUTE,
                TableOption.Kind.ENGINE), kinds(statement.getOptions()));
        assertEquals(List.of("4M", "8192", "'Y'", "'{}'", "InnoDB"), statement.getOptions()
                .stream().map(TableOption::getValue).collect(Collectors.toList()));
        assertEquals(List.of(false, true, false, true, false), statement.getOptions().stream()
                .map(TableOption::isUseEquals).collect(Collectors.toList()));
        // commas between options are optional and not retained
        String expected = "CREATE TABLESPACE ts1 ADD DATAFILE 'ts1.ibd' AUTOEXTEND_SIZE 4M "
                + "FILE_BLOCK_SIZE = 8192 ENCRYPTION 'Y' ENGINE_ATTRIBUTE = '{}' ENGINE InnoDB";
        assertEquals(expected, statement.toString());
        assertDeparse(statement, expected);
        assertRoundTrip(expected, Dialect.MYSQL, CreateTablespace.class);

        CreateTable table = (CreateTable) parse(
                "CREATE TABLE t (a INT) AUTOEXTEND_SIZE 4M ENCRYPTION 'Y' ENGINE InnoDB",
                Dialect.MYSQL);
        for (TableOption tableOption : table.getTableOptions()) {
            TableOption tablespaceOption =
                    statement.getOption(tableOption.getKind()).orElseThrow();
            assertEquals(tableOption.getTokens(), tablespaceOption.getTokens());
        }
    }

    @Test
    void mysqlDataFileAndOptionsAreOptional() throws JSQLParserException {
        CreateTablespace statement =
                assertRoundTrip("CREATE TABLESPACE ts1", Dialect.MYSQL, CreateTablespace.class);
        assertNull(statement.getDataFile());
        assertTrue(statement.getOptions().isEmpty());

        statement = assertRoundTrip("CREATE TABLESPACE `ts 1` ENGINE = 'InnoDB'", Dialect.MYSQL,
                CreateTablespace.class);
        assertEquals("`ts 1`", statement.getName());
        assertNull(statement.getDataFile());
        assertEquals("'InnoDB'", statement.getOption(TableOption.Kind.ENGINE).orElseThrow()
                .getValue());
    }

    @Test
    void mysqlUndoTablespaces() throws JSQLParserException {
        CreateTablespace create = assertRoundTrip(
                "CREATE UNDO TABLESPACE u1 ADD DATAFILE 'u1.ibu' ENGINE = InnoDB", Dialect.MYSQL,
                CreateTablespace.class);
        assertTrue(create.isUndo());
        assertEquals("u1", create.getName());
        assertEquals("u1.ibu", create.getDataFile().getValue());
        assertEquals(List.of(TableOption.Kind.ENGINE), kinds(create.getOptions()));

        AlterTablespace inactive = assertRoundTrip("ALTER UNDO TABLESPACE u1 SET INACTIVE",
                Dialect.MYSQL, AlterTablespace.class);
        assertTrue(inactive.isUndo());
        assertEquals(AlterTablespace.Action.SET_STATE, inactive.getAction());
        assertEquals(AlterTablespace.State.INACTIVE, inactive.getState());
        assertTrue(inactive.getOptions().isEmpty());

        AlterTablespace active = assertRoundTrip(
                "ALTER UNDO TABLESPACE u1 SET ACTIVE ENGINE = InnoDB", Dialect.MYSQL,
                AlterTablespace.class);
        assertEquals(AlterTablespace.State.ACTIVE, active.getState());
        assertEquals(List.of(TableOption.Kind.ENGINE), kinds(active.getOptions()));
    }

    @Test
    void mysqlAlterTablespace() throws JSQLParserException {
        AlterTablespace rename = assertRoundTrip("ALTER TABLESPACE ts1 RENAME TO ts2",
                Dialect.MYSQL, AlterTablespace.class);
        assertEquals("ts1", rename.getName());
        assertFalse(rename.isUndo());
        assertEquals(AlterTablespace.Action.RENAME, rename.getAction());
        assertEquals("ts2", rename.getNewName());

        AlterTablespace options = (AlterTablespace) parse(
                "ALTER TABLESPACE ts1 AUTOEXTEND_SIZE = 64M, ENCRYPTION = 'N' ENGINE = InnoDB "
                        + "ENGINE_ATTRIBUTE '{}'",
                Dialect.MYSQL);
        assertEquals(AlterTablespace.Action.OPTIONS, options.getAction());
        assertEquals(List.of(TableOption.Kind.AUTOEXTEND_SIZE, TableOption.Kind.ENCRYPTION,
                TableOption.Kind.ENGINE, TableOption.Kind.ENGINE_ATTRIBUTE),
                kinds(options.getOptions()));
        assertEquals("'N'", options.getOption(TableOption.Kind.ENCRYPTION).orElseThrow()
                .getValue());
        String expected = "ALTER TABLESPACE ts1 AUTOEXTEND_SIZE = 64M ENCRYPTION = 'N' "
                + "ENGINE = InnoDB ENGINE_ATTRIBUTE '{}'";
        assertEquals(expected, options.toString());
        assertDeparse(options, expected);
        assertRoundTrip(expected, Dialect.MYSQL, AlterTablespace.class);
    }

    @Test
    void postgresqlCreateTablespace() throws JSQLParserException {
        CreateTablespace statement = assertRoundTrip(
                "CREATE TABLESPACE fast OWNER u1 LOCATION '/ssd1/pg' "
                        + "WITH (random_page_cost = 1.1, effective_io_concurrency = 200)",
                Dialect.POSTGRESQL, CreateTablespace.class);

        assertEquals("fast", statement.getName());
        assertEquals("u1", statement.getOwner());
        assertEquals("/ssd1/pg", statement.getLocation().getValue());
        assertEquals(List.of("random_page_cost", "effective_io_concurrency"),
                names(statement.getParameters()));
        assertEquals("1.1", statement.getParameters().get(0).getValue().toString());
        assertNull(statement.getDataFile());
        assertTrue(statement.getOptions().isEmpty());
        List<Expression> values = new ArrayList<>();
        statement.visitExpressions(values::add);
        assertEquals(2, values.size());

        statement = assertRoundTrip("CREATE TABLESPACE fast LOCATION '/ssd1/pg'",
                Dialect.POSTGRESQL, CreateTablespace.class);
        assertNull(statement.getOwner());
        assertNull(statement.getParameters());

        statement = assertRoundTrip("CREATE TABLESPACE \"Fast\" OWNER CURRENT_USER LOCATION ''",
                Dialect.POSTGRESQL, CreateTablespace.class);
        assertEquals("CURRENT_USER", statement.getOwner());
        assertEquals("", statement.getLocation().getValue());
    }

    @Test
    void postgresqlAlterTablespace() throws JSQLParserException {
        AlterTablespace rename = assertRoundTrip("ALTER TABLESPACE fast RENAME TO faster",
                Dialect.POSTGRESQL, AlterTablespace.class);
        assertEquals(AlterTablespace.Action.RENAME, rename.getAction());
        assertEquals("faster", rename.getNewName());

        AlterTablespace owner = assertRoundTrip("ALTER TABLESPACE fast OWNER TO SESSION_USER",
                Dialect.POSTGRESQL, AlterTablespace.class);
        assertEquals(AlterTablespace.Action.OWNER, owner.getAction());
        assertEquals("SESSION_USER", owner.getOwner());

        AlterTablespace set = assertRoundTrip(
                "ALTER TABLESPACE fast SET (random_page_cost = 1.1, seq_page_cost = 2)",
                Dialect.POSTGRESQL, AlterTablespace.class);
        assertEquals(AlterTablespace.Action.SET_PARAMETERS, set.getAction());
        assertEquals(List.of("random_page_cost", "seq_page_cost"), names(set.getParameters()));
        List<Expression> values = new ArrayList<>();
        set.visitExpressions(values::add);
        assertEquals(2, values.size());

        AlterTablespace reset = assertRoundTrip(
                "ALTER TABLESPACE fast RESET (random_page_cost, seq_page_cost)",
                Dialect.POSTGRESQL, AlterTablespace.class);
        assertEquals(AlterTablespace.Action.RESET_PARAMETERS, reset.getAction());
        assertEquals(List.of("random_page_cost", "seq_page_cost"), reset.getResetParameters());
        values.clear();
        reset.visitExpressions(values::add);
        assertTrue(values.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"CREATE TABLESPACE ts1 ADD DATAFILE 'ts1.ibd' ENGINE = InnoDB",
            "CREATE UNDO TABLESPACE u1 ADD DATAFILE 'u1.ibu'",
            "CREATE TABLESPACE fast OWNER u1 LOCATION '/ssd1/pg' WITH (seq_page_cost = 1)",
            "ALTER TABLESPACE ts1 RENAME TO ts2", "ALTER TABLESPACE ts1 ENCRYPTION = 'Y'",
            "ALTER UNDO TABLESPACE u1 SET INACTIVE", "ALTER TABLESPACE fast OWNER TO bob",
            "ALTER TABLESPACE fast SET (seq_page_cost = 1)",
            "ALTER TABLESPACE fast RESET (seq_page_cost)"})
    void statementsOnlyModifySchemaAndKeepScriptBoundaries(String sql)
            throws JSQLParserException {
        Statement statement = assertSqlCanBeParsedAndDeparsed(sql, true);
        assertTrue(statement instanceof CreateTablespace || statement instanceof AlterTablespace);
        StatementFeatures features = statement.getFeatures();
        assertEquals(EnumSet.of(StmtFeature.MODIFIES_SCHEMA), features.getCertain());
        assertTrue(features.getUncertain().isEmpty());
        assertTrue(new TablesNamesFinder().getTables(statement).isEmpty());

        Statements script = CCJSqlParserUtil.parseStatements(sql + ";\n" + sql + "; SELECT 1");
        assertEquals(3, script.size());
        assertEquals(statement.getClass(), script.get(0).getClass());
        assertEquals(statement.getClass(), script.get(1).getClass());
    }

    @ParameterizedTest
    @ValueSource(strings = {"CREATE UNDO TABLESPACE u1",
            "CREATE UNDO TABLESPACE u1 ADD DATAFILE 'u1.ibu' ENCRYPTION = 'Y'",
            "CREATE TABLESPACE ts1 ADD DATAFILE 'ts1.ibd' KEY_BLOCK_SIZE = 8",
            "CREATE TABLESPACE ts1 AUTOEXTEND_SIZE = 4X",
            "CREATE TABLESPACE ts1 ADD DATAFILE 'ts1.ibd', ENGINE = InnoDB",
            "CREATE TABLESPACE ts1 ADD DATAFILE ts1",
            "CREATE OR REPLACE TABLESPACE ts1",
            "CREATE TABLESPACE fast OWNER u1",
            "CREATE TABLESPACE fast LOCATION '/ssd1/pg' ENGINE = InnoDB",
            "CREATE TABLESPACE fast ADD DATAFILE 'f.ibd' LOCATION '/ssd1/pg'",
            "ALTER TABLESPACE ts1 FILE_BLOCK_SIZE = 8K",
            "ALTER UNDO TABLESPACE u1 SET ACTIVE ENCRYPTION = 'Y'"})
    void rejectsFormsOutsideTheStatementDefinitions(String sql) {
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql));
    }

    @Test
    void postgresqlParameterValuesRequireEquals() {
        assertThrows(JSQLParserException.class, () -> parse(
                "CREATE TABLESPACE fast LOCATION '/ssd1/pg' WITH (random_page_cost 1.1)",
                Dialect.POSTGRESQL));
        assertThrows(JSQLParserException.class,
                () -> parse("ALTER TABLESPACE fast SET (random_page_cost 1.1)",
                        Dialect.POSTGRESQL));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "ALTER TABLESPACE users ADD DATAFILE '/u01/users02.dbf' SIZE 100M AUTOEXTEND ON",
            "ALTER TABLESPACE users OFFLINE", "ALTER TABLESPACE users READ ONLY",
            "ALTER TABLESPACE users ENCRYPTION ONLINE USING 'AES256' ENCRYPT",
            "ALTER TABLESPACE ts1 ADD DATAFILE 'ts1_2.dat' INITIAL_SIZE = 1M",
            "ALTER TABLESPACE ts1 ENGINE = NDB WAIT",
            "ALTER TABLESPACE ts1 RENAME TO ts2 ENGINE = InnoDB",
            "ALTER UNDO TABLESPACE u1 RENAME TO u2"})
    void otherAlterTablespaceFormsRemainUnsupportedStatements(String sql)
            throws JSQLParserException {
        Statement statement = CCJSqlParserUtil.parse(sql);
        assertInstanceOf(UnsupportedStatement.class, statement);
        assertEquals(sql, statement.toString());

        Statements script = CCJSqlParserUtil.parseStatements(
                sql + ";\nALTER TABLESPACE ts1 RENAME TO ts2;");
        assertInstanceOf(UnsupportedStatement.class, script.get(0));
        assertInstanceOf(AlterTablespace.class, script.get(1));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ALTER TABLE ALL IN TABLESPACE slow SET TABLESPACE fast",
            "ALTER INDEX ALL IN TABLESPACE slow OWNED BY alice SET TABLESPACE fast NOWAIT",
            "ALTER MATERIALIZED VIEW ALL IN TABLESPACE slow SET TABLESPACE fast"})
    void bulkTablespaceMovesRemainSeparate(String sql) throws JSQLParserException {
        assertRoundTrip(sql, Dialect.POSTGRESQL, AlterTablespaceMove.class);
    }

    @Test
    void clauseWordsRemainIdentifiers() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE TABLE t (undo INT, location INT, datafile INT, "
                + "inactive INT, file_block_size INT, owner INT)");
        CreateTablespace create = assertRoundTrip("CREATE TABLESPACE undo ADD DATAFILE 'u.ibd'",
                Dialect.MYSQL, CreateTablespace.class);
        assertEquals("undo", create.getName());
        assertFalse(create.isUndo());
        AlterTablespace alter = assertRoundTrip("ALTER TABLESPACE location RENAME TO owner",
                Dialect.POSTGRESQL, AlterTablespace.class);
        assertEquals("location", alter.getName());
        assertEquals("owner", alter.getNewName());
    }

    @Test
    void buildsAndDeparses() {
        CreateTablespace create = new CreateTablespace();
        create.setName("fast");
        create.setOwner("bob");
        create.setLocation(new StringValue("/data"));
        create.setParameters(List.of(new IndexOption("seq_page_cost", new LongValue(2), true)));
        String expected = "CREATE TABLESPACE fast OWNER bob LOCATION '/data' "
                + "WITH (seq_page_cost = 2)";
        assertEquals(expected, create.toString());
        assertDeparse(create, expected);

        AlterTablespace alter = new AlterTablespace();
        alter.setUndo(true);
        alter.setName("u1");
        alter.setAction(AlterTablespace.Action.SET_STATE);
        alter.setState(AlterTablespace.State.INACTIVE);
        alter.addOptions(new TableOption(TableOption.Kind.ENGINE, "ENGINE", "InnoDB", true));
        expected = "ALTER UNDO TABLESPACE u1 SET INACTIVE ENGINE = InnoDB";
        assertEquals(expected, alter.toString());
        assertDeparse(alter, expected);

        alter.setAction(null);
        assertThrows(IllegalStateException.class, alter::toString);
    }
}
