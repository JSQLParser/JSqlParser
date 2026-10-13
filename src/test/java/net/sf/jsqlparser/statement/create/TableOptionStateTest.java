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

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.alter.AlterExpressionTableOption;
import net.sf.jsqlparser.statement.alter.AlterOperation;
import net.sf.jsqlparser.statement.create.table.ColumnOption;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.ForeignTableOptions;
import net.sf.jsqlparser.statement.create.table.IndexOption;
import net.sf.jsqlparser.statement.create.table.TableOption;
import net.sf.jsqlparser.util.TableDefinitionTraversal;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class TableOptionStateTest {
    @Test
    void replacingTokensUpdatesCreateLookupAndAlterOperation() throws JSQLParserException {
        CreateTable create = create("ENGINE=InnoDB", Dialect.MYSQL);
        Alter alter = (Alter) parse("ALTER TABLE t ENGINE=InnoDB", Dialect.MYSQL);
        AlterExpressionTableOption action =
                (AlterExpressionTableOption) alter.getAlterExpressions().get(0);
        for (TableOption option : List.of(create.getTableOptions().get(0),
                action.getStructuredTableOption())) {
            List<String> raw = new ArrayList<>(List.of("ROW_FORMAT", "=", "DYNAMIC"));
            option.setTokens(raw);
            assertSame(raw, option.getTokens());
            assertEquals(TableOption.Kind.OTHER, option.getKind());
            assertNull(option.getName());
            assertNull(option.getValue());
            assertFalse(option.isUseEquals());
            raw.set(2, "COMPRESSED");
            assertEquals("ROW_FORMAT = COMPRESSED", option.toString());
        }
        assertTrue(create.getTableOption(TableOption.Kind.ENGINE).isEmpty());
        assertTrue(create.getTableOption(TableOption.Kind.ROW_FORMAT).isEmpty());
        assertSame(create.getTableOptions().get(0),
                create.getTableOption(TableOption.Kind.OTHER).orElseThrow());
        assertEquals(AlterOperation.SET_TABLE_OPTION, action.getOperation());
        assertNull(action.getEngineOption());
        assertRoundTrip(create, Dialect.MYSQL);
        assertRoundTrip(alter, Dialect.MYSQL);
    }

    @ParameterizedTest
    @MethodSource("structuredSetters")
    void eachStructuredSetterLeavesOpaqueTokens(Consumer<TableOption> setter) {
        TableOption option = TableOption.raw("CUSTOM", "opaque text");
        setter.accept(option);
        assertFalse(option.getTokens().contains("CUSTOM"));
        assertFalse(option.getTokens().contains("opaque text"));
    }

    private static Stream<Consumer<TableOption>> structuredSetters() {
        return Stream.of(option -> option.setKind(TableOption.Kind.COMMENT),
                option -> option.setName("COMMENT"), option -> option.setValue("'new'"),
                option -> option.setUseEquals(true),
                option -> option.setTablespaceStorage(ColumnOption.Storage.MEMORY));
    }

    @Test
    void rawToStructuredEditsAreOrderIndependentAndDoNotRestoreOldValues()
            throws JSQLParserException {
        CreateTable create = create("ENGINE=InnoDB", Dialect.MYSQL);
        TableOption option = create.getTableOptions().get(0);
        option.setTokens(List.of("ROW_FORMAT", "=", "DYNAMIC"));
        option.setValue("'new'");
        assertEquals("'new'", option.getValue());
        assertNull(option.getName());
        assertEquals(TableOption.Kind.OTHER, option.getKind());
        option.withName("COMMENT").withUseEquals(true).withKind(TableOption.Kind.COMMENT);
        assertEquals(List.of("COMMENT", "=", "'new'"), option.getTokens());
        assertThrows(UnsupportedOperationException.class, () -> option.getTokens().add("ignored"));
        assertEquals("CREATE TABLE t (id INT) COMMENT = 'new'", create.toString());
        assertRoundTrip(create, Dialect.MYSQL);
        option.setTokens(null);
        assertEquals("COMMENT = 'new'", option.toString());
        option.setTokens(List.of("ENGINE", "CSV"));
        option.setTokens(null);
        assertEquals(TableOption.Kind.OTHER, option.getKind());
        assertNull(option.getName());
        assertNull(option.getValue());
        assertTrue(option.getTokens().isEmpty());
    }

    @Test
    void unionReplacementStopsVisitingDetachedSourcesAndPreservesEmptyLists()
            throws JSQLParserException {
        CreateTable create = create("UNION=(old_source)", Dialect.MYSQL);
        TableOption option = create.getTableOptions().get(0);
        List<Table> oldSources = option.getUnionTables();
        option.setTokens(List.of("UNION", "=", "(raw_source)"));
        assertNull(option.getUnionTables());
        assertEquals(Set.of("t"), new TablesNamesFinder().getTables(create));
        oldSources.get(0).setName("detached");
        assertEquals("UNION = (raw_source)", option.toString());
        assertRoundTrip(create, Dialect.MYSQL);
        option.setUnionTables(new ArrayList<>());
        assertEquals("()", option.getValue());
        assertEquals("UNION ()", option.toString());
        option.getUnionTables().add(new Table("new_source"));
        assertEquals(Set.of("t", "new_source"), new TablesNamesFinder().getTables(create));
        assertRoundTrip(create, Dialect.MYSQL);
        option.setUnionTables(null);
        assertNull(option.getUnionTables());
        assertNull(option.getValue());
        assertEquals("UNION", option.toString());
    }

    @Test
    void storageSelectionResetsScalarSpellingAndKeepsExpressionRendering()
            throws JSQLParserException {
        CreateTable create = create("ENGINE=InnoDB", Dialect.MYSQL);
        TableOption option = create.getTableOptions().get(0);
        LongValue fillFactor = new LongValue(70);
        List<IndexOption> parameters = new ArrayList<>(
                List.of(new IndexOption("fillfactor", fillFactor, true)));
        option.setStorageParameters(parameters);
        assertSame(parameters, option.getStorageParameters());
        assertFalse(option.isUseEquals());
        assertEquals("WITH (fillfactor = 70)", option.toString());
        assertRoundTrip(create, Dialect.POSTGRESQL);
        StringBuilder customized = new StringBuilder();
        option.appendTo(customized, expression -> customized.append("80"));
        assertEquals("WITH (fillfactor = 80)", customized.toString());
        List<Expression> visited = new ArrayList<>();
        TableDefinitionTraversal.visit(option, visited::add, ignored -> {
        });
        assertEquals(List.of(fillFactor), visited);
        option.setTokens(List.of("WITH", "(fillfactor = 90)"));
        assertNull(option.getStorageParameters());
        visited.clear();
        TableDefinitionTraversal.visit(option, visited::add, ignored -> {
        });
        assertTrue(visited.isEmpty());
        assertRoundTrip(create, Dialect.POSTGRESQL);
        option.setStorageParameters(null);
        assertEquals(TableOption.Kind.STORAGE_PARAMETERS, option.getKind());
        assertNull(option.getValue());
        assertEquals("WITH", option.toString());
    }

    @Test
    void foreignServerReplacementUpdatesCreateProjectionAndDoesNotReviveOldOptions()
            throws JSQLParserException {
        CreateTable create = (CreateTable) parse(
                "CREATE FOREIGN TABLE t(id INT) SERVER old_srv OPTIONS(table_name 'old')",
                Dialect.POSTGRESQL);
        TableOption option = create.getTableOptions().get(0);
        ForeignTableOptions original = option.getForeignTableOptions();
        option.setTokens(List.of("SERVER", "raw_srv"));
        assertNull(create.getForeignTableOptions());
        assertNull(option.getForeignTableOptions());
        original.setServer("detached");
        assertRoundTrip(create, Dialect.POSTGRESQL);
        ForeignTableOptions replacement = new ForeignTableOptions();
        replacement.setServer("new_srv");
        option.setForeignTableOptions(replacement);
        assertSame(replacement, create.getForeignTableOptions());
        assertEquals(TableOption.Kind.FOREIGN_SERVER, option.getKind());
        assertEquals(List.of("SERVER", "new_srv"), option.getTokens());
        assertRoundTrip(create, Dialect.POSTGRESQL);
        option.setValue("scalar_srv");
        assertNull(create.getForeignTableOptions());
        assertEquals("SERVER scalar_srv", option.toString());
        assertRoundTrip(create, Dialect.POSTGRESQL);
    }

    @Test
    void tablespaceStorageDoesNotLeakAcrossRawAndStructuredReplacements()
            throws JSQLParserException {
        CreateTable create = create("TABLESPACE=old_ts STORAGE DISK", Dialect.MYSQL);
        TableOption option = create.getTableOptions().get(0);
        option.setTokens(List.of("TABLESPACE", "raw_ts"));
        assertNull(option.getTablespaceStorage());
        option.setTablespaceStorage(ColumnOption.Storage.MEMORY);
        assertEquals(TableOption.Kind.TABLESPACE, option.getKind());
        assertEquals("TABLESPACE", option.getName());
        assertNull(option.getValue());
        option.setValue("new_ts");
        assertEquals(List.of("TABLESPACE", "new_ts", "STORAGE", "MEMORY"), option.getTokens());
        assertRoundTrip(create, Dialect.MYSQL);
        option.setUnionTables(List.of(new Table("source")));
        assertNull(option.getTablespaceStorage());
        assertRoundTrip(create, Dialect.MYSQL);
        assertThrows(IllegalArgumentException.class,
                () -> option.setTablespaceStorage(ColumnOption.Storage.PLAIN));
        assertEquals("UNION (source)", option.toString());
    }

    private static CreateTable create(String options, Dialect dialect) throws JSQLParserException {
        return (CreateTable) parse("CREATE TABLE t(id INT) " + options, dialect);
    }

    private static Statement parse(String sql, Dialect dialect) throws JSQLParserException {
        return CCJSqlParserUtil.parse(sql, parser -> parser.withDialect(dialect));
    }

    private static void assertRoundTrip(Statement statement, Dialect dialect)
            throws JSQLParserException {
        StringBuilder out = new StringBuilder();
        statement.accept(new StatementDeParser(out));
        assertEquals(statement.toString(), out.toString());
        assertEquals(out.toString(), parse(out.toString(), dialect).toString());
    }
}
