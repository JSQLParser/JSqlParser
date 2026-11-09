/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.analyze;

import static net.sf.jsqlparser.test.TestUtils.assertDeparse;
import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.UnsupportedStatement;
import net.sf.jsqlparser.statement.UtilityOption;
import net.sf.jsqlparser.statement.analyze.Analyze.OptionKind;
import net.sf.jsqlparser.statement.maintenance.MaintenanceTarget;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PostgreSqlAnalyzeTest {

    private static final Consumer<CCJSqlParser> POSTGRESQL =
            parser -> parser.withDialect(Dialect.POSTGRESQL);

    private static Analyze parse(String sql) throws JSQLParserException {
        Statement statement = assertSqlCanBeParsedAndDeparsed(sql, true, POSTGRESQL);
        assertEquals(sql, assertSqlCanBeParsedAndDeparsed(sql, true).toString());
        return (Analyze) statement;
    }

    private static List<String> columns(MaintenanceTarget target) {
        return target.getColumns().stream().map(Column::getColumnName)
                .collect(Collectors.toList());
    }

    @Test
    void verboseWithColumnList() throws JSQLParserException {
        Analyze analyze = parse("ANALYZE VERBOSE t (a)");

        assertFalse(analyze.isParenthesizedOptions());
        assertEquals(OptionKind.VERBOSE, analyze.getOptions().get(0).getKind(OptionKind.class));
        assertEquals(List.of("a"), columns(analyze.getTargets().get(0)));
        assertEquals("t", analyze.getTable().getFullyQualifiedName());
        assertFalse(analyze.isTableKeyword());
        assertNull(analyze.getModifier());
    }

    @Test
    void parenthesizedOptionsAndTableList() throws JSQLParserException {
        Analyze analyze = parse("ANALYZE (SKIP_LOCKED) t, s");

        assertTrue(analyze.isParenthesizedOptions());
        assertEquals(OptionKind.SKIP_LOCKED,
                analyze.getOptions().get(0).getKind(OptionKind.class));
        assertEquals(List.of("t", "s"), analyze.getTables().stream()
                .map(Table::getFullyQualifiedName).collect(Collectors.toList()));
    }

    @Test
    void everyOptionWithValuesAndOnlyTarget() throws JSQLParserException {
        Analyze analyze = parse(
                "ANALYZE (VERBOSE false, SKIP_LOCKED, BUFFER_USAGE_LIMIT 256) ONLY s.t (a, b), u *");

        assertEquals(Boolean.FALSE, analyze.getOptions().get(0).getBooleanValue());
        assertEquals(256L, analyze.getOptions().get(2).getLongValue());
        MaintenanceTarget only = analyze.getTargets().get(0);
        assertTrue(only.getReference().isOnly());
        assertEquals(List.of("a", "b"), columns(only));
        assertTrue(analyze.getTargets().get(1).getReference().isIncludeDescendants());
    }

    @Test
    void bareAnalyze() throws JSQLParserException {
        Analyze analyze = parse("ANALYZE");

        assertTrue(analyze.getTargets().isEmpty());
        assertNull(analyze.getTable());
        assertTrue(analyze.getTables().isEmpty());
        assertEquals("ANALYZE", analyze.toString());
        assertEquals("ANALYZE (VERBOSE)", parse("ANALYZE (VERBOSE)").toString());
    }

    @Test
    void verboseWordDependsOnDialect() throws JSQLParserException {
        Analyze postgres = (Analyze) CCJSqlParserUtil.parse("ANALYZE verbose", POSTGRESQL);
        assertEquals("verbose", postgres.getOptions().get(0).getName());
        assertTrue(postgres.getTargets().isEmpty());

        // without a dialect and in other dialects a lone verbose stays a table name
        Analyze generic = (Analyze) CCJSqlParserUtil.parse("ANALYZE verbose");
        assertTrue(generic.getOptions().isEmpty());
        assertEquals("verbose", generic.getTable().getFullyQualifiedName());
        Analyze mysql = (Analyze) CCJSqlParserUtil.parse("ANALYZE verbose, t",
                parser -> parser.withDialect(Dialect.MYSQL));
        assertEquals(2, mysql.getTables().size());

        // a following table name selects the option without a dialect too
        Analyze option = (Analyze) CCJSqlParserUtil.parse("ANALYZE VERBOSE t");
        assertEquals("VERBOSE", option.getOptions().get(0).getName());
        assertEquals("t", option.getTable().getFullyQualifiedName());
    }

    @Test
    void mySqlFormsAreUnchanged() throws JSQLParserException {
        for (String sql : List.of("ANALYZE TABLE t1 UPDATE HISTOGRAM ON c1, c2 WITH 64 BUCKETS",
                "ANALYZE TABLE t1 UPDATE HISTOGRAM ON c1 WITH 32 BUCKETS AUTO UPDATE",
                "ANALYZE TABLE t1 UPDATE HISTOGRAM ON c1 MANUAL UPDATE",
                "ANALYZE TABLE t1 UPDATE HISTOGRAM ON c1 USING DATA '{}'",
                "ANALYZE NO_WRITE_TO_BINLOG TABLE t, s",
                "ANALYZE LOCAL TABLE t DROP HISTOGRAM ON c",
                "ANALYZE t1, t2")) {
            for (Dialect dialect : new Dialect[] {Dialect.MYSQL, Dialect.MARIADB,
                    Dialect.POSTGRESQL}) {
                Analyze analyze = (Analyze) assertSqlCanBeParsedAndDeparsed(sql, true,
                        parser -> parser.withDialect(dialect));
                assertTrue(analyze.getOptions().isEmpty());
                assertTrue(analyze.getTargets().stream().allMatch(MaintenanceTarget::isPlainTable));
            }
        }
    }

    @Test
    void acceptsEveryOptionOfTheGrammar() throws JSQLParserException {
        Analyze analyze = parse("ANALYZE (FULL, VERBOSE 2, BUFFER_USAGE_LIMIT) t");

        assertNull(analyze.getOptions().get(0).getKind(OptionKind.class));
        assertEquals(UtilityOption.ValueType.NUMBER, analyze.getOptions().get(1).getValueType());
        assertNull(analyze.getOptions().get(1).getBooleanValue());
        assertEquals(OptionKind.BUFFER_USAGE_LIMIT,
                analyze.getOptions().get(2).getKind(OptionKind.class));
        assertFalse(analyze.getOptions().get(2).hasValue());

        Analyze option = parse("ANALYZE (t)");
        assertEquals("t", option.getOptions().get(0).getName());
        assertTrue(option.getTargets().isEmpty());
    }

    @Test
    void analyseSpelling() throws JSQLParserException {
        for (String sql : List.of("ANALYSE", "ANALYSE t", "ANALYSE VERBOSE t (a)",
                "ANALYSE (VERBOSE, SKIP_LOCKED) ONLY t, s")) {
            Analyze analyze = parse(sql);
            assertEquals(Analyze.Command.ANALYSE, analyze.getCommand(), sql);
        }
        Analyze lower = (Analyze) CCJSqlParserUtil.parse("analyse verbose t", POSTGRESQL);
        assertEquals("ANALYSE verbose t", lower.toString());
        assertEquals(Analyze.Command.ANALYZE, parse("ANALYZE t").getCommand());

        assertDeparse(new Analyze().withCommand(Analyze.Command.ANALYSE)
                .addOptions(new UtilityOption("VERBOSE")).addTables(new Table("t")),
                "ANALYSE VERBOSE t");
        assertThrows(NullPointerException.class, () -> new Analyze().setCommand(null));
    }

    @Test
    void analyseRemainsAnIdentifier() throws JSQLParserException {
        for (String sql : List.of("SELECT analyse FROM analyse AS analyse",
                "CREATE TABLE analyse (analyse INT)", "INSERT INTO analyse (analyse) VALUES (1)")) {
            assertSqlCanBeParsedAndDeparsed(sql, true, POSTGRESQL);
            assertSqlCanBeParsedAndDeparsed(sql, true);
        }
        assertSqlCanBeParsedAndDeparsed("SELECT analyse FROM analyse", true,
                parser -> parser.withDialect(Dialect.MYSQL));
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM t PROCEDURE ANALYSE()", true,
                parser -> parser.withDialect(Dialect.MYSQL));
    }

    @Test
    void analyseStartsAStatementOnlyInThePostgreSqlForm() throws JSQLParserException {
        Statements statements = CCJSqlParserUtil.parseStatements(
                "SELECT 1; ANALYSE t\nANALYSE; ANALYSE (VERBOSE)\nSELECT 2", POSTGRESQL);
        assertEquals(5, statements.size());
        assertEquals("ANALYSE t", statements.get(1).toString());
        assertEquals("ANALYSE", statements.get(2).toString());
        assertEquals("ANALYSE (VERBOSE)", statements.get(3).toString());

        // without a dialect other forms stay unsupported statements, and other dialects never
        // read ANALYSE as a statement
        assertInstanceOf(UnsupportedStatement.class, CCJSqlParserUtil.parse("ANALYSE t FOO",
                parser -> parser.withUnsupportedStatements(true)));
        assertInstanceOf(Analyze.class, CCJSqlParserUtil.parse("ANALYSE t",
                parser -> parser.withUnsupportedStatements(true)));
        assertInstanceOf(UnsupportedStatement.class, CCJSqlParserUtil.parse("ANALYSE t",
                parser -> parser.withDialect(Dialect.MYSQL).withUnsupportedStatements(true)));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "ANALYSE TABLE t",
            "ANALYSE LOCAL t",
            "ANALYSE t UPDATE HISTOGRAM ON a",
            "ANALYZE (SELECT) t",
            "ANALYZE (TABLE) t",
            "ANALYZE (VERBOSE on, SKIP_LOCKED only) t",
            "ANALYZE TABLE t (a)",
            "ANALYZE LOCAL (VERBOSE) t",
            "ANALYZE VERBOSE TABLE t",
            "ANALYZE (VERBOSE) t UPDATE HISTOGRAM ON a",
            "ANALYZE ONLY t DROP HISTOGRAM ON a",
            "ANALYZE (VERBOSE) t UPDATE HISTOGRAM ON a WITH 8 BUCKETS AUTO UPDATE",
            "ANALYZE VERBOSE t UPDATE HISTOGRAM ON a MANUAL UPDATE",
            "ANALYZE t (a) UPDATE HISTOGRAM ON a USING DATA '{}'",
            "ANALYZE t * UPDATE HISTOGRAM ON a",
            "ANALYZE UPDATE HISTOGRAM ON a",
            "ANALYZE TABLE",
            "ANALYZE LOCAL",
            "ANALYZE t (a.b)",
            "ANALYZE t (a) UPDATE HISTOGRAM ON a"
    })
    void rejectsMixedOrInvalidForms(String sql) {
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql, POSTGRESQL));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ANALYZE", "ANALYZE (VERBOSE) t", "ANALYZE VERBOSE t (a)",
            "ANALYZE t (a)", "ANALYZE ONLY t", "ANALYSE t", "ANALYSE"})
    void otherDialectsKeepTheirReading(String sql) {
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql,
                parser -> parser.withDialect(Dialect.MYSQL)));
    }

    @Test
    void endsAtStatementBoundaries() throws JSQLParserException {
        Statements statements = CCJSqlParserUtil.parseStatements(
                "SHOW search_path; ANALYZE; CHECKPOINT\nANALYZE VERBOSE t (a)\nSELECT 1",
                POSTGRESQL);

        assertEquals(5, statements.size());
        assertInstanceOf(Analyze.class, statements.get(1));
        assertEquals("ANALYZE", statements.get(1).toString());
        assertEquals("ANALYZE VERBOSE t (a)", statements.get(3).toString());
    }

    @Test
    void legacyAccessorsAreDerivedFromTargets() throws JSQLParserException {
        Analyze analyze = parse("ANALYZE (VERBOSE) ONLY t (a), s");

        List<Table> tables = analyze.getTables();
        assertThrows(UnsupportedOperationException.class, () -> tables.add(new Table("x")));
        analyze.addTables(new Table("x"));
        assertEquals(2, tables.size());
        assertEquals(3, analyze.getTables().size());

        analyze.setTable(new Table("u"));
        assertDeparse(analyze, "ANALYZE (VERBOSE) u");
        analyze.setTables(List.of(new Table("a"), new Table("b")));
        assertDeparse(analyze, "ANALYZE (VERBOSE) a, b");
        analyze.setTable(null);
        assertDeparse(analyze, "ANALYZE (VERBOSE)");
    }

    @Test
    void histogramClauseKeepsPlainTargets() throws JSQLParserException {
        Analyze analyze = (Analyze) assertSqlCanBeParsedAndDeparsed(
                "ANALYZE TABLE t1, t2 UPDATE HISTOGRAM ON c1 WITH 16 BUCKETS AUTO UPDATE");

        assertEquals(2, analyze.getTargets().size());
        assertTrue(analyze.getTargets().stream().allMatch(MaintenanceTarget::isPlainTable));
        assertTrue(analyze.getOptions().isEmpty());
        assertEquals(16, analyze.getHistogramBucketCount());
        assertEquals(Analyze.HistogramUpdateMode.AUTO, analyze.getHistogramUpdateMode());

        analyze.setTable(new Table("u"));
        assertDeparse(analyze,
                "ANALYZE TABLE u UPDATE HISTOGRAM ON c1 WITH 16 BUCKETS AUTO UPDATE");
    }

    @Test
    void buildsAndDeparses() {
        Analyze analyze = new Analyze().addOptions(new UtilityOption("VERBOSE"))
                .addTargets(new MaintenanceTarget(new Table("t")).addColumns(new Column("a")));
        assertDeparse(analyze, "ANALYZE VERBOSE t (a)");

        analyze.withParenthesizedOptions(true)
                .addOptions(UtilityOption.ofString("BUFFER_USAGE_LIMIT", "1MB"));
        assertDeparse(analyze, "ANALYZE (VERBOSE, BUFFER_USAGE_LIMIT '1MB') t (a)");

        analyze.withOptions(null).withTargets(null);
        assertDeparse(analyze, "ANALYZE");
    }

    @Test
    void findsTablesButNotColumns() throws JSQLParserException {
        Statement statement = CCJSqlParserUtil.parse("ANALYZE VERBOSE a (x), ONLY s.b (y)",
                POSTGRESQL);

        assertThat(new TablesNamesFinder<>().getTables(statement))
                .containsExactlyInAnyOrder("a", "s.b");
    }
}
