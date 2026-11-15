/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.maintenance;

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
import net.sf.jsqlparser.schema.TableReference;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementFeatures;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.StmtFeature;
import net.sf.jsqlparser.statement.UnsupportedStatement;
import net.sf.jsqlparser.statement.UtilityOption;
import net.sf.jsqlparser.statement.UtilityOption.ValueType;
import net.sf.jsqlparser.statement.maintenance.VacuumStatement.OptionKind;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class VacuumStatementTest {

    private static final Consumer<CCJSqlParser> POSTGRESQL =
            parser -> parser.withDialect(Dialect.POSTGRESQL);

    private static VacuumStatement parse(String sql) throws JSQLParserException {
        Statement statement = assertSqlCanBeParsedAndDeparsed(sql, true, POSTGRESQL);
        assertEquals(sql, assertSqlCanBeParsedAndDeparsed(sql, true).toString());
        return (VacuumStatement) statement;
    }

    private static List<String> names(List<UtilityOption> options) {
        return options.stream().map(UtilityOption::getName).collect(Collectors.toList());
    }

    @Test
    void parenthesizedOptions() throws JSQLParserException {
        VacuumStatement vacuum = parse("VACUUM (VERBOSE, ANALYZE) t");

        assertTrue(vacuum.isParenthesizedOptions());
        assertEquals(List.of("VERBOSE", "ANALYZE"), names(vacuum.getOptions()));
        assertEquals(OptionKind.ANALYZE, vacuum.getOptions().get(1).getKind(OptionKind.class));
        assertEquals("t", vacuum.getTargets().get(0).getTable().getFullyQualifiedName());
    }

    @Test
    void unparenthesizedKeywordsAreOptions() throws JSQLParserException {
        VacuumStatement vacuum = parse("VACUUM FULL t");

        assertFalse(vacuum.isParenthesizedOptions());
        assertEquals(List.of("FULL"), names(vacuum.getOptions()));
        assertFalse(vacuum.getOptions().get(0).hasValue());

        VacuumStatement all = parse("VACUUM FULL FREEZE VERBOSE ANALYZE t (a, b), s.u");
        assertEquals(List.of(OptionKind.FULL, OptionKind.FREEZE, OptionKind.VERBOSE,
                OptionKind.ANALYZE),
                all.getOptions().stream().map(o -> o.getKind(OptionKind.class))
                        .collect(Collectors.toList()));
        assertEquals(2, all.getTargets().size());
        assertEquals("s.u", all.getTargets().get(1).getTable().getFullyQualifiedName());
    }

    @Test
    void typedOptionValuesAndColumns() throws JSQLParserException {
        VacuumStatement vacuum = parse("VACUUM (PARALLEL 2, INDEX_CLEANUP OFF) t (a)");

        UtilityOption parallel = vacuum.getOptions().get(0);
        assertEquals(OptionKind.PARALLEL, parallel.getKind(OptionKind.class));
        assertEquals(ValueType.NUMBER, parallel.getValueType());
        assertEquals(2L, parallel.getLongValue());

        UtilityOption cleanup = vacuum.getOptions().get(1);
        assertEquals(ValueType.BOOLEAN, cleanup.getValueType());
        assertEquals(Boolean.FALSE, cleanup.getBooleanValue());

        MaintenanceTarget target = vacuum.getTargets().get(0);
        assertEquals(List.of("a"), target.getColumns().stream()
                .map(Column::getColumnName).collect(Collectors.toList()));
        assertFalse(target.isPlainTable());
    }

    @Test
    void everyDocumentedOption() throws JSQLParserException {
        VacuumStatement vacuum = parse("VACUUM (FULL, FREEZE true, VERBOSE on, ANALYZE 1, "
                + "DISABLE_PAGE_SKIPPING false, SKIP_LOCKED off, INDEX_CLEANUP AUTO, "
                + "PROCESS_MAIN 0, PROCESS_TOAST TRUE, TRUNCATE FALSE, PARALLEL 0, "
                + "SKIP_DATABASE_STATS, ONLY_DATABASE_STATS 'on', BUFFER_USAGE_LIMIT '256kB')");

        assertTrue(vacuum.getTargets().isEmpty());
        assertEquals(OptionKind.values().length, vacuum.getOptions().size());
        for (int i = 0; i < OptionKind.values().length; i++) {
            assertEquals(OptionKind.values()[i],
                    vacuum.getOptions().get(i).getKind(OptionKind.class));
        }
        UtilityOption cleanup = vacuum.getOptions().get(6);
        assertEquals(ValueType.WORD, cleanup.getValueType());
        assertNull(cleanup.getBooleanValue());
        assertEquals(Boolean.TRUE, vacuum.getOptions().get(12).getBooleanValue());
        UtilityOption limit = vacuum.getOptions().get(13);
        assertEquals(ValueType.STRING, limit.getValueType());
        assertEquals("'256kB'", limit.getValue());
        assertEquals("256kB", limit.getStringValue());
    }

    @Test
    void bareVacuumAndLowerCaseKeywords() throws JSQLParserException {
        VacuumStatement bare = parse("VACUUM");
        assertTrue(bare.getOptions().isEmpty());
        assertTrue(bare.getTargets().isEmpty());

        VacuumStatement lower = (VacuumStatement) CCJSqlParserUtil.parse("vacuum freeze analyse t",
                POSTGRESQL);
        assertEquals("VACUUM freeze analyse t", lower.toString());
        assertEquals(OptionKind.ANALYZE, lower.getOptions().get(1).getKind(OptionKind.class));
    }

    @Test
    void onlyAndDescendantTargets() throws JSQLParserException {
        VacuumStatement vacuum = parse("VACUUM (ANALYZE) ONLY t (a), u *, ONLY (v)");

        TableReference only = vacuum.getTargets().get(0).getReference();
        assertTrue(only.isOnly());
        assertTrue(vacuum.getTargets().get(1).getReference().isIncludeDescendants());
        assertTrue(vacuum.getTargets().get(2).getReference().isParenthesized());
    }

    @Test
    void signedAndDecimalNumbers() throws JSQLParserException {
        VacuumStatement vacuum = parse("VACUUM (PARALLEL +2, BUFFER_USAGE_LIMIT 1.5) t");

        assertEquals(2L, vacuum.getOptions().get(0).getLongValue());
        assertNull(vacuum.getOptions().get(1).getLongValue());
        assertEquals("1.5", vacuum.getOptions().get(1).getValue());
    }

    @Test
    void acceptsEveryOptionOfTheGrammar() throws JSQLParserException {
        // PostgreSQL's grammar takes any name and value; the server checks them when it runs
        VacuumStatement vacuum = parse("VACUUM (FOO, PARALLEL -1, PARALLEL 1.5, VERBOSE maybe, "
                + "INDEX_CLEANUP full, BUFFER_USAGE_LIMIT, PARALLEL) t");

        UtilityOption foo = vacuum.getOptions().get(0);
        assertNull(foo.getKind(OptionKind.class));
        assertFalse(foo.hasValue());
        UtilityOption negative = vacuum.getOptions().get(1);
        assertEquals(ValueType.NUMBER, negative.getValueType());
        assertEquals(-1L, negative.getLongValue());
        UtilityOption decimal = vacuum.getOptions().get(2);
        assertEquals(ValueType.NUMBER, decimal.getValueType());
        assertNull(decimal.getLongValue());
        UtilityOption maybe = vacuum.getOptions().get(3);
        assertEquals(OptionKind.VERBOSE, maybe.getKind(OptionKind.class));
        assertEquals(ValueType.WORD, maybe.getValueType());
        assertNull(maybe.getBooleanValue());
        assertEquals(ValueType.WORD, vacuum.getOptions().get(4).getValueType());
        assertFalse(vacuum.getOptions().get(5).hasValue());

        // a lone name in parentheses is an option too, as in PostgreSQL
        VacuumStatement option = parse("VACUUM (t)");
        assertEquals("t", option.getOptions().get(0).getName());
        assertTrue(option.getTargets().isEmpty());
    }

    @Test
    void quotedNamesAndDollarQuotedValues() throws JSQLParserException {
        VacuumStatement vacuum = parse(
                "VACUUM (\"verbose\", \"VERBOSE\" off, INDEX_CLEANUP \"auto\", "
                        + "BUFFER_USAGE_LIMIT $$256kB$$) t");

        // like PostgreSQL, a quoted name matches only in lower case
        assertEquals(OptionKind.VERBOSE, vacuum.getOptions().get(0).getKind(OptionKind.class));
        assertNull(vacuum.getOptions().get(1).getKind(OptionKind.class));
        UtilityOption cleanup = vacuum.getOptions().get(2);
        assertEquals(ValueType.WORD, cleanup.getValueType());
        assertEquals("auto", cleanup.getStringValue());
        UtilityOption limit = vacuum.getOptions().get(3);
        assertEquals(ValueType.STRING, limit.getValueType());
        assertEquals("256kB", limit.getStringValue());
        assertThrows(JSQLParserException.class,
                () -> CCJSqlParserUtil.parse("VACUUM ([verbose]) t", POSTGRESQL));

        // a quoted name or value may spell a word that PostgreSQL reserves
        VacuumStatement reserved = parse("VACUUM (\"table\", VERBOSE \"user\", ANALYSE true) t");
        assertNull(reserved.getOptions().get(0).getKind(OptionKind.class));
        assertEquals("user", reserved.getOptions().get(1).getStringValue());
        assertEquals(OptionKind.ANALYZE, reserved.getOptions().get(2).getKind(OptionKind.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "VACUUM () t",
            "VACUUM (VERBOSE,) t",
            "VACUUM (VERBOSE, ) t",
            "VACUUM (SELECT) t",
            "VACUUM (TABLE) t",
            "VACUUM (USER) t",
            "VACUUM (VERBOSE analyze) t",
            "VACUUM (INDEX_CLEANUP default) t",
            "VACUUM (VERBOSE 'a' 'b') t",
            "VACUUM VERBOSE FULL t",
            "VACUUM ANALYZE VERBOSE t",
            "VACUUM ANALYZE FULL t",
            "VACUUM t USING x",
            "VACUUM FULL (VERBOSE) t",
            "VACUUM ONLY t *",
            "VACUUM t ()",
            "VACUUM t (a.b)",
            "VACUUM t, ",
            "VACUUM t INTO 'f'"
    })
    void rejectsFormsOutsidePostgreSqlGrammar(String sql) {
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql, POSTGRESQL));
    }

    @Test
    void otherDialectsDoNotParseVacuum() {
        for (Dialect dialect : List.of(Dialect.MYSQL, Dialect.ORACLE, Dialect.SQLSERVER)) {
            assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse("VACUUM t",
                    parser -> parser.withDialect(dialect)), dialect.name());
        }
    }

    @Test
    void otherProductsVariantsStayUnsupportedWithoutDialect() throws JSQLParserException {
        for (String sql : List.of("VACUUM INTO 'backup.db'", "VACUUM main INTO 'backup.db'",
                "VACUUM t RETAIN 168 HOURS", "VACUUM SORT ONLY t")) {
            Statement statement = CCJSqlParserUtil.parse(sql,
                    parser -> parser.withUnsupportedStatements(true));
            assertInstanceOf(UnsupportedStatement.class, statement, sql);
        }
        Statement postgres = CCJSqlParserUtil.parse("VACUUM t",
                parser -> parser.withUnsupportedStatements(true));
        assertInstanceOf(VacuumStatement.class, postgres);
    }

    @Test
    void endsAtStatementBoundaries() throws JSQLParserException {
        Statements statements = CCJSqlParserUtil.parseStatements(
                "SHOW search_path; VACUUM t\nCHECKPOINT\nVACUUM; VACUUM (ANALYZE) a, b (c)\nSELECT 1",
                POSTGRESQL);

        assertEquals(6, statements.size());
        assertEquals("VACUUM t", statements.get(1).toString());
        assertEquals("CHECKPOINT", statements.get(2).toString());
        assertEquals("VACUUM", statements.get(3).toString());
        assertEquals("VACUUM (ANALYZE) a, b (c)", statements.get(4).toString());
        assertEquals("SELECT 1", statements.get(5).toString());
    }

    @Test
    void endsAnUnsupportedStatement() throws JSQLParserException {
        Statements statements = CCJSqlParserUtil.parseStatements(
                "IMPORT INTO t FROM LOCAL CSV FILE 'f.csv'; VACUUM FULL t",
                parser -> parser.withDialect(Dialect.POSTGRESQL)
                        .withUnsupportedStatements(true));

        assertEquals(2, statements.size());
        assertInstanceOf(UnsupportedStatement.class, statements.get(0));
        assertEquals("VACUUM FULL t", statements.get(1).toString());
    }

    @Test
    void buildsAndDeparses() {
        VacuumStatement vacuum = new VacuumStatement()
                .addOptions(new UtilityOption("VERBOSE"), new UtilityOption("FULL"))
                .addTargets(new MaintenanceTarget(new Table("t")).addColumns(new Column("a")));
        // the unparenthesized form is rendered in PostgreSQL's keyword order
        assertDeparse(vacuum, "VACUUM FULL VERBOSE t (a)");

        vacuum.addOptions(UtilityOption.of("PARALLEL", 4));
        assertDeparse(vacuum, "VACUUM (VERBOSE, FULL, PARALLEL 4) t (a)");

        vacuum.setOptions(List.of(new UtilityOption("FULL")));
        vacuum.setParenthesizedOptions(true);
        assertDeparse(vacuum, "VACUUM (FULL) t (a)");

        vacuum.setOptions(List.of(new UtilityOption("FULL"), new UtilityOption("FULL")));
        vacuum.setParenthesizedOptions(false);
        assertDeparse(vacuum, "VACUUM (FULL, FULL) t (a)");

        vacuum.setOptions(null);
        vacuum.withTargets(null);
        assertDeparse(vacuum, "VACUUM");
    }

    @Test
    void findsTablesButNotColumns() throws JSQLParserException {
        Statement statement = CCJSqlParserUtil.parse("VACUUM (ANALYZE) a (x, y), ONLY s.b",
                POSTGRESQL);

        assertThat(new TablesNamesFinder<>().getTables(statement))
                .containsExactlyInAnyOrder("a", "s.b");
        assertThat(new TablesNamesFinder<>().getTables(
                CCJSqlParserUtil.parse("VACUUM", POSTGRESQL))).isEmpty();
    }

    @Test
    void reportsStatementFeatures() throws JSQLParserException {
        StatementFeatures features = CCJSqlParserUtil.parse("VACUUM FULL t").getFeatures();

        assertThat(features.getCertain()).containsExactlyInAnyOrder(StmtFeature.MODIFIES_SCHEMA,
                StmtFeature.READS_DATA);
    }

    @Test
    void wordsRemainIdentifiers() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT vacuum, freeze FROM vacuum.freeze AS vacuum",
                true, POSTGRESQL);
        assertSqlCanBeParsedAndDeparsed("CREATE TABLE vacuum (vacuum INT, freeze INT)", true,
                POSTGRESQL);
        assertSqlCanBeParsedAndDeparsed("VACUUM vacuum (vacuum)", true, POSTGRESQL);
        assertSqlCanBeParsedAndDeparsed("VACUUM FULL cluster, checkpoint", true, POSTGRESQL);
        assertSqlCanBeParsedAndDeparsed("INSERT INTO vacuum (freeze) VALUES (1)", true);
    }
}
