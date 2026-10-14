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
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.TableOption;
import net.sf.jsqlparser.statement.create.table.TableOption.Kind;
import net.sf.jsqlparser.statement.create.table.TableOption.OnCommitAction;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class PostgreSqlTableOptionsTest {
    private static final Dialect[] DIALECTS = {Dialect.POSTGRESQL, null};

    @ParameterizedTest
    @CsvSource(value = {"PRESERVE_ROWS|ON COMMIT PRESERVE ROWS",
            "DELETE_ROWS|ON COMMIT DELETE ROWS", "DROP|ON COMMIT DROP"}, delimiter = '|')
    void onCommitIsOneTypedOption(OnCommitAction action, String clause)
            throws JSQLParserException {
        String sql = "CREATE TEMPORARY TABLE t (a INT) " + clause;
        for (Dialect dialect : DIALECTS) {
            CreateTable table = parse(sql, dialect);
            assertEquals(1, table.getTableOptions().size());
            TableOption option = table.getTableOption(Kind.ON_COMMIT).orElseThrow();
            assertEquals(action, option.getOnCommitAction());
            assertEquals("ON COMMIT", option.getName());
            assertEquals(action.toString(), option.getValue());
            assertEquals(Arrays.asList(clause.split(" ")), option.getTokens());
            assertEquals(Arrays.asList(clause.split(" ")), table.getTableOptionsStrings());
            assertRoundTrip(table, sql, dialect);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"CREATE TABLE t (a INT) USING heap",
            "CREATE TABLE t USING heap AS SELECT 1 AS a",
            "CREATE TABLE t (a) USING heap WITH (fillfactor = 70) TABLESPACE ts AS SELECT 1 WITH NO DATA"})
    void accessMethodIsOneTypedOption(String sql) throws JSQLParserException {
        for (Dialect dialect : DIALECTS) {
            CreateTable table = parse(sql, dialect);
            TableOption option = table.getTableOptions().get(0);
            assertEquals(Kind.ACCESS_METHOD, option.getKind());
            assertEquals("USING", option.getName());
            assertEquals("heap", option.getValue());
            assertEquals(List.of("USING", "heap"), option.getTokens());
            assertEquals(List.of("USING", "heap"), table.getTableOptionsStrings().subList(0, 2));
            assertEquals(sql.contains(" AS "), table.getSelect() != null);
            assertEquals(Set.of("t"), new TablesNamesFinder().getTables(table));
            assertRoundTrip(table, sql, dialect);
        }
    }

    @Test
    void postgreSqlClausesKeepTheirOrderAndLegacyTokens() throws JSQLParserException {
        String sql = "CREATE TEMP TABLE t (a INT) USING heap WITH (fillfactor = 70) "
                + "ON COMMIT DELETE ROWS TABLESPACE ts";
        for (Dialect dialect : DIALECTS) {
            CreateTable table = parse(sql, dialect);
            assertEquals(
                    List.of(Kind.ACCESS_METHOD, Kind.STORAGE_PARAMETERS, Kind.ON_COMMIT,
                            Kind.TABLESPACE),
                    table.getTableOptions().stream().map(TableOption::getKind)
                            .collect(Collectors.toList()));
            assertEquals(List.of("USING", "heap", "WITH", "(fillfactor = 70)", "ON", "COMMIT",
                    "DELETE", "ROWS", "TABLESPACE", "ts"), table.getTableOptionsStrings());
            assertRoundTrip(table, sql, dialect);
        }

        String query = "CREATE TEMP TABLE t (a) USING heap ON COMMIT DROP AS SELECT 1 WITH NO DATA";
        for (Dialect dialect : DIALECTS) {
            CreateTable table = parse(query, dialect);
            assertEquals(List.of(Kind.ACCESS_METHOD, Kind.ON_COMMIT),
                    table.getTableOptions().stream().map(TableOption::getKind)
                            .collect(Collectors.toList()));
            assertEquals(Boolean.FALSE, table.getWithData());
            assertRoundTrip(table, query, dialect);
        }
    }

    @Test
    void accessMethodFollowsPostgreSqlPartitionKey() throws JSQLParserException {
        String sql = "CREATE TABLE t (a INT) PARTITION BY RANGE (a) USING heap TABLESPACE ts";
        CreateTable table = parse(sql, Dialect.POSTGRESQL);
        assertTrue(table.isTableOptionsAfterPartition());
        assertEquals("heap",
                table.getTableOption(Kind.ACCESS_METHOD).orElseThrow().getValue());
        assertRoundTrip(table, sql, Dialect.POSTGRESQL);
    }

    @ParameterizedTest
    @CsvSource(value = {"PRESERVE_DEFINITION|ON COMMIT PRESERVE DEFINITION",
            "DROP_DEFINITION|ON COMMIT DROP DEFINITION"}, delimiter = '|')
    void oraclePrivateTemporaryTablesUseTheSameOption(OnCommitAction action, String clause)
            throws JSQLParserException {
        String sql = "CREATE PRIVATE TEMPORARY TABLE ora$ptt_t (a INT) " + clause;
        for (Dialect dialect : new Dialect[] {Dialect.ORACLE, null}) {
            CreateTable table = parse(sql, dialect);
            assertEquals(1, table.getTableOptions().size());
            assertEquals(action, table.getTableOptions().get(0).getOnCommitAction());
            assertEquals(Arrays.asList(clause.split(" ")), table.getTableOptionsStrings());
            assertRoundTrip(table, sql, dialect);
        }
    }

    @Test
    void usingIndexTablespaceRemainsRaw() throws JSQLParserException {
        String sql = "CREATE TABLE t (a INT) USING INDEX TABLESPACE ts";
        CreateTable table = parse(sql, null);
        assertEquals(Kind.OTHER, table.getTableOptions().get(0).getKind());
        assertEquals(List.of("USING INDEX", "TABLESPACE ts"), table.getTableOptionsStrings());
        assertRoundTrip(table, sql, null);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ON COMMIT", "ON COMMIT DELETE", "ON COMMIT PRESERVE",
            "ON COMMIT ROWS", "ON COMMIT DELETE DEFINITION"})
    void rejectsIncompleteOnCommitActions(String clause) {
        for (Dialect dialect : DIALECTS) {
            assertThrows(JSQLParserException.class,
                    () -> parse("CREATE TEMPORARY TABLE t (a INT) " + clause, dialect));
        }
    }

    @Test
    void editsReplaceTheOptionContents() throws JSQLParserException {
        CreateTable table = parse("CREATE TEMP TABLE t (a INT) USING heap ON COMMIT DROP",
                Dialect.POSTGRESQL);
        TableOption accessMethod = table.getTableOption(Kind.ACCESS_METHOD).orElseThrow();
        TableOption onCommit = table.getTableOption(Kind.ON_COMMIT).orElseThrow();

        accessMethod.setValue("columnar");
        onCommit.setOnCommitAction(OnCommitAction.PRESERVE_ROWS);
        assertEquals(List.of("USING", "columnar", "ON", "COMMIT", "PRESERVE", "ROWS"),
                table.getTableOptionsStrings());
        assertThrows(UnsupportedOperationException.class, () -> onCommit.getTokens().add("x"));
        assertRoundTrip(table,
                "CREATE TEMP TABLE t (a INT) USING columnar ON COMMIT PRESERVE ROWS",
                Dialect.POSTGRESQL);

        // Raw tokens discard the action; a structured setter never restores it.
        onCommit.setTokens(new ArrayList<>(List.of("ON", "COMMIT", "DROP")));
        assertEquals(Kind.OTHER, onCommit.getKind());
        assertNull(onCommit.getOnCommitAction());
        assertTrue(table.getTableOption(Kind.ON_COMMIT).isEmpty());
        onCommit.setOnCommitAction(OnCommitAction.DELETE_ROWS);
        assertEquals(Kind.ON_COMMIT, onCommit.getKind());
        assertEquals("ON COMMIT DELETE ROWS", onCommit.toString());
        onCommit.setKind(Kind.COMMENT);
        assertNull(onCommit.getOnCommitAction());
        onCommit.setOnCommitAction(OnCommitAction.DROP);
        onCommit.setValue("DROP");
        assertNull(onCommit.getOnCommitAction());
        assertEquals("ON COMMIT DROP", onCommit.toString());

        onCommit.setOnCommitAction(null);
        assertEquals(Kind.ON_COMMIT, onCommit.getKind());
        assertNull(onCommit.getValue());
        assertEquals(List.of("ON", "COMMIT"), onCommit.getTokens());
        assertEquals("ON COMMIT", onCommit.toString());
    }

    @Test
    void factoriesBuildOptionsWithoutParsing() throws JSQLParserException {
        CreateTable table = new CreateTable().withTable(new Table("t"))
                .withColumnDefinitions(List.of(new ColumnDefinition("a", new ColDataType("INT"))))
                .withTableOptions(new ArrayList<>(List.of(TableOption.accessMethod("heap"),
                        TableOption.onCommit(OnCommitAction.DROP))));
        table.setCreateOptionsStrings(List.of("TEMPORARY"));
        assertEquals(Kind.ON_COMMIT, table.getTableOptions().get(1).getKind());
        assertRoundTrip(table, "CREATE TEMPORARY TABLE t (a INT) USING heap ON COMMIT DROP",
                Dialect.POSTGRESQL);
    }

    private static CreateTable parse(String sql, Dialect dialect) throws JSQLParserException {
        return (CreateTable) (dialect == null
                ? CCJSqlParserUtil.parse(sql)
                : CCJSqlParserUtil.parse(sql, parser -> parser.withDialect(dialect)));
    }

    private static void assertRoundTrip(CreateTable table, String expected, Dialect dialect)
            throws JSQLParserException {
        assertEquals(expected, table.toString());
        StringBuilder deparsed = new StringBuilder();
        table.accept(new StatementDeParser(deparsed), null);
        assertEquals(expected, deparsed.toString());
        CreateTable reparsed = parse(deparsed.toString(), dialect);
        assertEquals(expected, reparsed.toString());
        assertEquals(table.getTableOptionsStrings(), reparsed.getTableOptionsStrings());
        assertEquals(2, (dialect == null
                ? CCJSqlParserUtil.parseStatements(expected + "; SELECT 1")
                : CCJSqlParserUtil.parseStatements(expected + "; SELECT 1",
                        parser -> parser.withDialect(dialect)))
                .size());
    }
}
