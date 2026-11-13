/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static net.sf.jsqlparser.util.validation.ValidationTestAsserts.validateNoErrors;
import static net.sf.jsqlparser.util.validation.ValidationTestAsserts.validateNotAllowed;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.AliasedExpression;
import net.sf.jsqlparser.expression.CastExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.TimezoneExpression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.IsBooleanExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNormalizedExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNormalizedExpression.NormalForm;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.parser.ASTNodeAccess;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.Node;
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.StmtFeature;
import net.sf.jsqlparser.statement.select.SelectIntoClause.Persistence;
import net.sf.jsqlparser.statement.select.SelectIntoClause.Position;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import net.sf.jsqlparser.util.validation.feature.DatabaseType;
import net.sf.jsqlparser.util.validation.feature.FeaturesAllowed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * PostgreSQL SELECT forms: SELECT INTO with a persistence, the alias of JOIN USING, AT LOCAL, IS
 * NORMALIZED, XMLATTRIBUTES in XMLELEMENT and the set quantifier of GROUP BY.
 */
class PostgreSqlSelectFormsTest {
    private static final Consumer<CCJSqlParser> POSTGRESQL =
            parser -> parser.withDialect(Dialect.POSTGRESQL);

    static Stream<String> statements() throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                PostgreSqlSelectFormsTest.class
                        .getResourceAsStream("/postgresql/select-forms.sql"),
                StandardCharsets.UTF_8))) {
            return reader.lines()
                    .filter(line -> !line.trim().isEmpty() && !line.trim().startsWith("--"))
                    .map(line -> line.substring(0, line.length() - 1))
                    .collect(Collectors.toList())
                    .stream();
        }
    }

    private static PlainSelect parse(String sql) throws JSQLParserException {
        return (PlainSelect) CCJSqlParserUtil.parse(sql, POSTGRESQL);
    }

    private static PlainSelect parse(String sql, Dialect dialect) throws JSQLParserException {
        return (PlainSelect) CCJSqlParserUtil.parse(sql, parser -> parser.withDialect(dialect));
    }

    private static Expression item(String sql) throws JSQLParserException {
        return parse(sql).getSelectItem(0).getExpression();
    }

    private static String deparse(Statement statement) {
        StringBuilder builder = new StringBuilder();
        statement.accept(new StatementDeParser(builder));
        return builder.toString();
    }

    private static String source(String sql, ASTNodeAccess part) {
        Node node = part.getASTNode();
        return sql.substring(node.jjtGetFirstToken().absoluteBegin - 1,
                node.jjtGetLastToken().absoluteEnd - 1);
    }

    private static void assertRejected(String sql, Consumer<CCJSqlParser> consumer) {
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql, consumer), sql);
    }

    @ParameterizedTest
    @MethodSource("statements")
    void testRoundTripWithAndWithoutDialect(String sql) throws Exception {
        Statement statement = assertSqlCanBeParsedAndDeparsed(sql, false, POSTGRESQL);
        assertEquals(sql, statement.toString());
        assertEquals(sql, deparse(statement));
        assertEquals(sql, CCJSqlParserUtil.parse(statement.toString(), POSTGRESQL).toString());

        // the forms are not ambiguous, so that they are read without a dialect as well
        assertEquals(sql, assertSqlCanBeParsedAndDeparsed(sql).toString());
        validateNoErrors(sql, 1, DatabaseType.POSTGRESQL);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "SELECT a, count(*) FROM t GROUP BY ALL a",
            "SELECT a, count(*) FROM t GROUP BY DISTINCT a",
            "SELECT a, b, count(*) FROM t GROUP BY ALL a, b",
            "SELECT a, b, count(*) FROM t GROUP BY DISTINCT ROLLUP(a, b), ROLLUP(a, c)",
            "SELECT a, b, count(*) FROM t GROUP BY ALL ROLLUP(a, b)",
            "SELECT a, b, count(*) FROM t GROUP BY DISTINCT CUBE(a, b)",
            "SELECT a, b, count(*) FROM t GROUP BY ALL GROUPING SETS ((a), (b), ())",
            "SELECT a, b, count(*) FROM t GROUP BY DISTINCT GROUPING SETS ((a, b), a, ())",
            "SELECT a, b, count(*) FROM t GROUP BY DISTINCT a, ROLLUP(b), CUBE(c)",
            "SELECT count(*) FROM t GROUP BY ALL ()",
            "SELECT count(*) FROM t GROUP BY DISTINCT ()",
            "SELECT a, count(*) FROM t GROUP BY DISTINCT (a)",
            "SELECT a, b, count(*) FROM t GROUP BY ALL (a, b)",
            "SELECT a, count(*) FROM t GROUP BY ALL a + 1, (a)",
            "SELECT a, b, count(*) FROM t GROUP BY DISTINCT a, b HAVING count(*) > 1"})
    void testGroupBySetQuantifierRoundTrips(String sql) throws Exception {
        PlainSelect select = (PlainSelect) assertSqlCanBeParsedAndDeparsed(sql, false, POSTGRESQL);
        assertEquals(sql, select.toString());
        assertEquals(sql.contains("GROUP BY ALL"), select.getGroupBy().isAll());
        assertEquals(sql.contains("GROUP BY DISTINCT"), select.getGroupBy().isDistinct());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "SELECT a, count(*) FROM t GROUP BY ALL",
            "SELECT a, count(*) FROM t GROUP BY DISTINCT",
            "SELECT a, count(*) FROM t GROUP BY ALL DISTINCT a",
            "SELECT a, count(*) FROM t GROUP BY DISTINCT ALL a",
            "SELECT a, count(*) FROM t GROUP BY ALL ALL a",
            "SELECT a, count(*) FROM t GROUP BY a ALL",
            "SELECT a, count(*) FROM t GROUP BY UNIQUE a"})
    void testGroupBySetQuantifierRejected(String sql) {
        assertRejected(sql, POSTGRESQL);
    }

    @Test
    void testSelectIntoPersistence() throws Exception {
        for (Persistence persistence : Persistence.values()) {
            for (boolean tableKeyword : new boolean[] {false, true}) {
                String sql = "SELECT a INTO " + persistence.getKeywords()
                        + (tableKeyword ? " TABLE" : "") + " sch.t2 FROM t";
                PlainSelect select = parse(sql);
                SelectIntoClause into = select.getIntoClause();
                assertEquals(persistence, into.getPersistence(), sql);
                assertEquals(tableKeyword, into.isTableKeyword(), sql);
                assertEquals(Position.BEFORE_FROM, into.getPosition());
                assertEquals("sch.t2", into.getTable().getFullyQualifiedName());
                assertEquals(persistence != Persistence.UNLOGGED, persistence.isTemporary());
                assertEquals(sql, select.toString());
                assertEquals(sql, deparse(select));
                // legacy views of the clause
                assertSame(into.getTables(), select.getIntoTables());
                assertNull(select.getIntoTempTable());
                assertNull(select.getMySqlSelectIntoClause());
            }
        }
        assertEquals(Persistence.LOCAL_TEMP, Persistence.from("local  temp"));

        SelectIntoClause plain = parse("SELECT * INTO TABLE t2 FROM t").getIntoClause();
        assertNull(plain.getPersistence());
        assertTrue(plain.isTableKeyword());

        // without a name after it the word is the table
        for (String name : Arrays.asList("temp", "temporary", "unlogged", "local", "global")) {
            SelectIntoClause into = parse("SELECT * INTO " + name + " FROM t").getIntoClause();
            assertNull(into.getPersistence());
            assertEquals(name, into.getTable().getName());
        }
        SelectIntoClause schema = parse("SELECT * INTO temp.t2 FROM t").getIntoClause();
        assertNull(schema.getPersistence());
        assertEquals("temp", schema.getTable().getSchemaName());

        // an empty select list
        assertSqlCanBeParsedAndDeparsed("SELECT INTO t2 FROM t", false, POSTGRESQL);
        assertSqlCanBeParsedAndDeparsed("SELECT INTO TEMP t2 FROM t", false, POSTGRESQL);
    }

    @Test
    void testSelectIntoRangeTablesAndFeatures() throws Exception {
        String sql = "SELECT a INTO LOCAL TEMP TABLE t2 FROM t WHERE a > 1";
        PlainSelect select = parse(sql);
        assertEquals("INTO LOCAL TEMP TABLE t2", source(sql, select.getIntoClause()));
        assertThat(new TablesNamesFinder<>().getTables((Statement) select))
                .containsExactlyInAnyOrder("t", "t2");
        assertThat(select.getFeatures().getCertain())
                .contains(StmtFeature.MODIFIES_SCHEMA, StmtFeature.MODIFIES_DATA,
                        StmtFeature.READS_DATA)
                .doesNotContain(StmtFeature.RETURNS_RESULT_SET);

        List<String> visited = new ArrayList<>();
        select.accept((SelectVisitor<Void>) new SelectVisitorAdapter<Void>(
                new ExpressionVisitorAdapter<Void>(), new FromItemVisitorAdapter<Void>() {
                    @Override
                    public <S> Void visit(Table table, S context) {
                        visited.add(table.getName());
                        return null;
                    }
                }), null);
        assertThat(visited).containsExactly("t2", "t");

        validateNotAllowed("SELECT * INTO UNLOGGED t2 FROM t", 1, 1, FeaturesAllowed.SELECT,
                Feature.selectInto);
        validateNoErrors("SELECT * INTO UNLOGGED t2 FROM t", 1, DatabaseType.POSTGRESQL);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "SELECT * INTO TABLE FROM t",
            "SELECT * INTO TEMP TABLE FROM t",
            "SELECT * INTO LOCAL TEMP FROM t",
            "SELECT * INTO UNLOGGED TEMP t2 FROM t",
            "SELECT * INTO TEMP UNLOGGED t2 FROM t",
            "SELECT * INTO LOCAL t2 FROM t",
            "SELECT * INTO GLOBAL t2 FROM t",
            "SELECT * INTO LOCAL UNLOGGED t2 FROM t",
            "SELECT * INTO LOCAL TABLE t2 FROM t",
            "SELECT * INTO TEMP TEMP t2 FROM t",
            "SELECT * INTO TABLE TEMP t2 FROM t",
            "SELECT * INTO TEMP t2, t3 FROM t",
            "SELECT * INTO TABLE t2, t3 FROM t",
            "SELECT * INTO t2, t3 FROM t",
            "SELECT * INTO TEMP t2 AS x FROM t",
            "SELECT * FROM t INTO TEMP t2",
            "SELECT * FROM t INTO t2",
            "SELECT * INTO TEMP TABLE t2 TABLE t3 FROM t",
            "SELECT * INTO TEMP IF NOT EXISTS t2 FROM t",
            "SELECT * INTO TEMP ONLY t2 FROM t",
            "SELECT * INTO t2 INTO t3 FROM t"})
    void testSelectIntoRejected(String sql) {
        assertRejected(sql, POSTGRESQL);
    }

    @Test
    void testSelectIntoInOtherDialects() throws Exception {
        // the persistence and TABLE keywords belong to PostgreSQL
        for (Dialect dialect : Arrays.asList(Dialect.ORACLE, Dialect.MYSQL, Dialect.SQLSERVER)) {
            for (String sql : Arrays.asList("SELECT * INTO TEMP t2 FROM t",
                    "SELECT * INTO UNLOGGED t2 FROM t", "SELECT * INTO LOCAL TEMP t2 FROM t",
                    "SELECT * INTO TABLE t2 FROM t", "SELECT INTO t2 FROM t")) {
                assertRejected(sql, parser -> parser.withDialect(dialect));
            }
            SelectIntoClause into = parse("SELECT * INTO temp FROM t", dialect).getIntoClause();
            assertNull(into.getPersistence());
            assertEquals("temp", into.getTable().getName());
        }

        // target lists of other products and procedural languages
        for (Consumer<CCJSqlParser> consumer : Arrays.<Consumer<CCJSqlParser>>asList(
                parser -> {
                }, parser -> parser.withDialect(Dialect.ORACLE))) {
            PlainSelect select = (PlainSelect) assertSqlCanBeParsedAndDeparsed(
                    "SELECT a, b INTO x, y FROM t", false, consumer);
            assertThat(select.getIntoTables()).extracting(Table::getName).containsExactly("x",
                    "y");
            assertSqlCanBeParsedAndDeparsed("SELECT * INTO #tmp FROM t", false, consumer);
        }

        // Informix writes INTO TEMP at the end
        String informix = "SELECT a FROM t INTO TEMP t2 WITH NO LOG";
        PlainSelect select = (PlainSelect) assertSqlCanBeParsedAndDeparsed(informix);
        assertEquals(Position.TRAILING, select.getIntoClause().getPosition());
        assertEquals(Persistence.TEMP, select.getIntoClause().getPersistence());
        assertEquals("t2", select.getIntoTempTable().getName());
        assertNull(select.getIntoTables());
        assertEquals("INTO TEMP t2", source(informix, select.getIntoClause()));
        assertThat(new TablesNamesFinder<>().getTables((Statement) select))
                .containsExactlyInAnyOrder("t", "t2");
        assertRejected("SELECT a INTO t3 FROM t INTO TEMP t2", parser -> {
        });
        assertRejected("SELECT a INTO OUTFILE 'f' FROM t INTO TEMP t2", parser -> {
        });
    }

    @Test
    void testSelectIntoLegacyAccessors() {
        PlainSelect select = new PlainSelect().addSelectItems(new Column("a"))
                .withFromItem(new Table("t"));
        assertNull(select.getIntoClause());
        assertNull(select.getIntoTables());
        assertNull(select.getIntoTempTable());

        select.addIntoTables(new Table("x"));
        select.addIntoTables(new Table("y"));
        assertEquals("SELECT a INTO x, y FROM t", select.toString());
        assertEquals(Position.BEFORE_FROM, select.getIntoClause().getPosition());

        select.getIntoClause().setPersistence(Persistence.UNLOGGED);
        select.setIntoTables(new ArrayList<>(Arrays.asList(new Table("z"))));
        assertEquals("SELECT a INTO UNLOGGED z FROM t", select.toString());

        select.setIntoTempTable(null);
        assertEquals("SELECT a INTO UNLOGGED z FROM t", select.toString());
        select.setIntoTables(null);
        assertNull(select.getIntoClause());

        select.withIntoTempTable(new Table("tmp"));
        assertEquals("SELECT a FROM t INTO TEMP tmp", select.toString());
        assertNull(select.getIntoTables());
        select.setIntoTables(null);
        assertEquals("tmp", select.getIntoTempTable().getName());
        select.setIntoTempTable(null);
        assertNull(select.getIntoClause());

        select.withIntoClause(new SelectIntoClause(new Table("t2"))
                .withPersistence(Persistence.GLOBAL_TEMPORARY).withTableKeyword(true));
        assertEquals("SELECT a INTO GLOBAL TEMPORARY TABLE t2 FROM t", select.toString());
        assertEquals("INTO GLOBAL TEMPORARY TABLE t2", select.getIntoClause().toString());
        assertEquals(new SelectIntoClause().withTables(new ArrayList<>()).addTable(new Table("q"))
                .withPosition(Position.TRAILING).toString(), "INTO q");
    }

    @Test
    void testJoinUsingAlias() throws Exception {
        String sql = "SELECT j.id FROM a LEFT JOIN b USING (id, x) AS j JOIN c USING (id)";
        PlainSelect select = parse(sql);
        Join join = select.getJoins().get(0);
        assertEquals("j", join.getUsingAlias().getName());
        assertThat(join.getUsingColumns()).extracting(Column::getColumnName).containsExactly("id",
                "x");
        assertNull(select.getJoins().get(1).getUsingAlias());
        assertEquals("LEFT JOIN b USING (id, x) AS j", source(sql, join));
        assertEquals(" USING (id, x) AS j", join.appendUsingTo(new StringBuilder()).toString());
        assertThat(new TablesNamesFinder<>().getTables((Statement) select))
                .containsExactlyInAnyOrder("a", "b", "c");

        join.setUsingAlias(null);
        assertEquals("SELECT j.id FROM a LEFT JOIN b USING (id, x) JOIN c USING (id)",
                select.toString());
        select.getJoins().get(1).withUsingAlias(new Alias("k"));
        assertEquals("SELECT j.id FROM a LEFT JOIN b USING (id, x) JOIN c USING (id) AS k",
                deparse(select));
        // the alias belongs to the USING columns
        assertEquals("", new Join().withUsingAlias(new Alias("k"))
                .appendUsingTo(new StringBuilder()).toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "SELECT * FROM a JOIN b USING (id) j",
            "SELECT * FROM a JOIN b USING (id) AS j (x)",
            "SELECT * FROM a JOIN b USING (id) AS",
            "SELECT * FROM a JOIN b ON a.id = b.id AS j",
            "SELECT * FROM a CROSS JOIN b USING (id) AS j",
            "SELECT * FROM a NATURAL JOIN b USING (id) AS j",
            "SELECT * FROM a, b USING (id) AS j",
            "SELECT * FROM a JOIN b USING (id) AS j AS k",
            "SELECT * FROM a JOIN b USING (id) AS j ON true",
            "SELECT * FROM a JOIN b USING (id) AS select",
            "SELECT * FROM a JOIN b USING (a.id) AS j",
            "SELECT * FROM a JOIN b USING () AS j"})
    void testJoinUsingAliasRejected(String sql) {
        assertRejected(sql, POSTGRESQL);
        assertRejected(sql, parser -> {
        });
    }

    @Test
    void testAtLocal() throws Exception {
        String sql = "SELECT ts AT LOCAL FROM t";
        TimezoneExpression local = (TimezoneExpression) item(sql);
        assertTrue(local.isAtLocal());
        assertThat(local.getTimezoneExpressions()).isEmpty();
        assertEquals("ts", local.getLeftExpression().toString());
        assertEquals("ts AT LOCAL", source(sql, local));

        // AT LOCAL ends a run of zones
        sql = "SELECT ts AT TIME ZONE 'UTC' AT TIME ZONE 'CET' AT LOCAL FROM t";
        TimezoneExpression zones = (TimezoneExpression) item(sql);
        assertTrue(zones.isAtLocal());
        assertThat(zones.getTimezoneExpressions()).extracting(Object::toString)
                .containsExactly("'UTC'", "'CET'");
        assertThat(zones.getTimezoneExpressions()).allMatch(StringValue.class::isInstance);
        assertEquals("ts AT TIME ZONE 'UTC' AT TIME ZONE 'CET' AT LOCAL", source(sql, zones));

        // and a conversion after it applies to the local value
        sql = "SELECT ts AT LOCAL AT TIME ZONE 'UTC' AT LOCAL AT LOCAL FROM t";
        TimezoneExpression outer = (TimezoneExpression) item(sql);
        assertTrue(outer.isAtLocal());
        assertThat(outer.getTimezoneExpressions()).isEmpty();
        TimezoneExpression middle = (TimezoneExpression) outer.getLeftExpression();
        assertTrue(middle.isAtLocal());
        assertThat(middle.getTimezoneExpressions()).hasSize(1);
        assertEquals("ts AT LOCAL AT TIME ZONE 'UTC' AT LOCAL", source(sql, middle));
        TimezoneExpression inner = (TimezoneExpression) middle.getLeftExpression();
        assertTrue(inner.isAtLocal());
        assertEquals("ts AT LOCAL", source(sql, inner));

        sql = "SELECT ts AT LOCAL::date AT LOCAL FROM t";
        TimezoneExpression afterCast = (TimezoneExpression) item(sql);
        CastExpression cast = (CastExpression) afterCast.getLeftExpression();
        assertEquals("ts AT LOCAL::date", source(sql, cast));
        assertTrue(((TimezoneExpression) cast.getLeftExpression()).isAtLocal());

        TimezoneExpression plain = (TimezoneExpression) item("SELECT ts AT TIME ZONE 'UTC' FROM t");
        assertFalse(plain.isAtLocal());
        assertEquals("ts AT TIME ZONE 'UTC' AT LOCAL", plain.setAtLocal(true).toString());
        assertEquals("now() AT LOCAL",
                new TimezoneExpression(new Function().withName("now")).setAtLocal(true)
                        .toString());

        // AT and LOCAL stay identifiers
        assertEquals("at", parse("SELECT ts at FROM t").getSelectItem(0).getAlias().getName());
        assertSqlCanBeParsedAndDeparsed("SELECT at, local FROM t", false, POSTGRESQL);
        assertEquals("ZONE", parse("SELECT ts AT LOCAL ZONE FROM t").getSelectItem(0).getAlias()
                .getName());

        assertThat(new TablesNamesFinder<>()
                .getTables(
                        CCJSqlParserUtil.parse("SELECT (SELECT max(ts) FROM u) AT LOCAL FROM t")))
                .containsExactlyInAnyOrder("t", "u");
        for (Dialect dialect : Arrays.asList(Dialect.ORACLE, Dialect.MYSQL)) {
            assertSqlCanBeParsedAndDeparsed("SELECT ts AT LOCAL FROM t", false,
                    parser -> parser.withDialect(dialect));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "SELECT ts AT LOCAL TIME ZONE 'UTC' FROM t",
            "SELECT ts AT TIME LOCAL FROM t",
            "SELECT ts AT ZONE 'UTC' FROM t",
            "SELECT ts AT LOCAL LOCAL x FROM t"})
    void testAtLocalRejected(String sql) {
        assertRejected(sql, POSTGRESQL);
    }

    @Test
    void testIsNormalized() throws Exception {
        for (boolean not : new boolean[] {false, true}) {
            List<NormalForm> forms = new ArrayList<>(Arrays.asList(NormalForm.values()));
            forms.add(null);
            for (NormalForm form : forms) {
                String sql = "SELECT s IS " + (not ? "NOT " : "")
                        + (form == null ? "" : form + " ") + "NORMALIZED FROM t";
                IsNormalizedExpression test = (IsNormalizedExpression) item(sql);
                assertEquals(not, test.isNot(), sql);
                assertEquals(form, test.getNormalForm(), sql);
                assertEquals("s", test.getLeftExpression(Column.class).getColumnName());
                assertEquals(sql.substring(7, sql.length() - 7), source(sql, test));
                assertEquals(sql, deparse(parse(sql)));
            }
        }
        assertEquals(NormalForm.NFKD,
                ((IsNormalizedExpression) item("SELECT s is nfkd normalized FROM t"))
                        .getNormalForm());
        assertTrue(NormalForm.isNormalForm("nfc"));
        assertFalse(NormalForm.isNormalForm("nfx"));

        // the test takes the whole operand and chains like the other IS tests
        String sql = "SELECT s || 'x' IS NFKC NORMALIZED IS NOT TRUE FROM t";
        IsBooleanExpression chained = (IsBooleanExpression) item(sql);
        IsNormalizedExpression test = (IsNormalizedExpression) chained.getLeftExpression();
        assertEquals("s || 'x'", test.getLeftExpression().toString());
        assertEquals("s || 'x' IS NFKC NORMALIZED", source(sql, test));

        IsNormalizedExpression afterNull =
                (IsNormalizedExpression) item("SELECT s IS NOT NULL IS NORMALIZED FROM t");
        assertThat(afterNull.getLeftExpression()).isInstanceOf(IsNullExpression.class);

        AndExpression and = (AndExpression) parse(
                "SELECT * FROM t WHERE s IS NFD NORMALIZED AND u IS NOT NORMALIZED").getWhere();
        assertThat(and.getLeftExpression()).isInstanceOf(IsNormalizedExpression.class);
        assertThat(and.getRightExpression()).isInstanceOf(IsNormalizedExpression.class);

        assertEquals("'a' IS NOT NFD NORMALIZED", new IsNormalizedExpression(new StringValue("a"))
                .withNot(true).withNormalForm(NormalForm.NFD).toString());
        assertEquals("b IS NORMALIZED",
                new IsNormalizedExpression().withLeftExpression(new Column("b")).toString());

        // the words stay identifiers
        assertSqlCanBeParsedAndDeparsed("SELECT nfc, nfd, nfkc, nfkd, normalized FROM normalized",
                false, POSTGRESQL);
        assertEquals("NFC", parse("SELECT s IS NORMALIZED NFC FROM t").getSelectItem(0).getAlias()
                .getName());
        for (Dialect dialect : Arrays.asList(Dialect.ORACLE, Dialect.MYSQL)) {
            assertSqlCanBeParsedAndDeparsed("SELECT s IS NOT NFC NORMALIZED FROM t", false,
                    parser -> parser.withDialect(dialect));
        }
    }

    @Test
    void testIsNormalizedVisitors() throws Exception {
        String sql = "SELECT (SELECT max(s) FROM u) IS NFC NORMALIZED FROM t";
        PlainSelect select = parse(sql);
        assertThat(new TablesNamesFinder<>().getTables((Statement) select))
                .containsExactlyInAnyOrder("t", "u");

        List<String> columns = new ArrayList<>();
        ExpressionVisitorAdapter<Void> adapter = new ExpressionVisitorAdapter<Void>() {
            @Override
            public <S> Void visit(Column column, S context) {
                columns.add(column.getColumnName());
                return null;
            }
        };
        Expression test = item("SELECT s IS NORMALIZED FROM t");
        test.accept(adapter, null);
        adapter.visit((IsNormalizedExpression) test);
        assertThat(columns).containsExactly("s", "s");

        StringBuilder builder = new StringBuilder();
        ExpressionDeParser expressionDeParser = new ExpressionDeParser() {
            @Override
            public <S> StringBuilder visit(Column column, S context) {
                return builder.append('<').append(column.getColumnName()).append('>');
            }
        };
        SelectDeParser selectDeParser = new SelectDeParser(expressionDeParser, builder);
        expressionDeParser.setSelectVisitor(selectDeParser);
        expressionDeParser.setBuilder(builder);
        parse("SELECT s IS NOT NFD NORMALIZED, ts AT TIME ZONE z AT LOCAL FROM t")
                .accept((SelectVisitor<StringBuilder>) selectDeParser, null);
        assertEquals("SELECT <s> IS NOT NFD NORMALIZED, <ts> AT TIME ZONE <z> AT LOCAL FROM t",
                builder.toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "SELECT s IS NFX NORMALIZED FROM t",
            "SELECT s IS NFC FROM t",
            "SELECT s IS NFC NOT NORMALIZED FROM t",
            "SELECT s IS NOT NOT NORMALIZED FROM t",
            "SELECT s IS 'NFC' NORMALIZED FROM t",
            "SELECT s IS NFC NFD NORMALIZED FROM t",
            "SELECT s NOT IS NORMALIZED FROM t",
            "SELECT s IS \"NFC\" NORMALIZED FROM t"})
    void testIsNormalizedRejected(String sql) {
        assertRejected(sql, POSTGRESQL);
        assertRejected(sql, parser -> {
        });
    }

    @Test
    void testXmlAttributes() throws Exception {
        String sql = "SELECT xmlelement(name foo, xmlattributes(a AS x, t.b, a + 1 AS \"y z\"),"
                + " 'content', s) FROM t";
        Function element = (Function) item(sql);
        assertEquals("name", element.getExtraKeyword());
        assertThat(element.getParameters()).hasSize(4);
        assertEquals("foo", element.getParameters().get(0).toString());
        Function attributes = (Function) element.getParameters().get(1);
        assertEquals("xmlattributes", attributes.getName());
        assertEquals("xmlattributes(a AS x, t.b, a + 1 AS \"y z\")", source(sql, attributes));
        AliasedExpression first = (AliasedExpression) attributes.getParameters().get(0);
        assertEquals("x", first.getAlias().getName());
        assertEquals("a", first.getExpression().toString());
        assertThat(attributes.getParameters().get(1)).isInstanceOf(Column.class);
        assertEquals("\"y z\"",
                ((AliasedExpression) attributes.getParameters().get(2)).getAlias().getName());
        assertEquals("a AS x", source(sql, first));

        sql = "SELECT xmlelement(name a, xmlattributes((SELECT max(c) FROM u) AS m),"
                + " xmlelement(name b, xmlattributes(v.d AS n))) FROM t, v";
        assertThat(new TablesNamesFinder<>().getTables(CCJSqlParserUtil.parse(sql, POSTGRESQL)))
                .containsExactlyInAnyOrder("t", "u", "v");

        // other products write the element name without NAME
        for (Consumer<CCJSqlParser> consumer : Arrays.<Consumer<CCJSqlParser>>asList(
                parser -> {
                }, parser -> parser.withDialect(Dialect.ORACLE))) {
            assertSqlCanBeParsedAndDeparsed(
                    "SELECT XMLELEMENT(\"Emp\", XMLATTRIBUTES(e.id AS \"id\"), e.name) FROM e",
                    false, consumer);
        }
        // the content and the plain function call are read as before
        assertSqlCanBeParsedAndDeparsed("SELECT xmlelement(name foo, s, a) FROM t", false,
                POSTGRESQL);
        assertSqlCanBeParsedAndDeparsed("SELECT xmlforest(a AS x, b) FROM t", false, POSTGRESQL);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "SELECT xmlelement(name foo, xmlattributes()) FROM t",
            "SELECT xmlelement(name foo, xmlattributes(a x)) FROM t",
            "SELECT xmlelement(name foo, 'content', xmlattributes(a AS x)) FROM t",
            "SELECT xmlelement(name foo, xmlattributes(a AS x), xmlattributes(b AS y)) FROM t",
            "SELECT xmlelement(foo, xmlattributes(a AS x)) FROM t",
            "SELECT xmlelement(name 'foo', xmlattributes(a AS x)) FROM t",
            "SELECT xmlelement(name foo, xmlattributes(a AS 'x')) FROM t",
            "SELECT xmlelement(name foo, xmlattributes(a AS x.y)) FROM t",
            "SELECT xmlattributes(a AS x) FROM t",
            "SELECT xmlelement(name foo xmlattributes(a AS x)) FROM t",
            "SELECT xmlelement(name foo, xmlattributes(a AS x),) FROM t",
            "SELECT xmlelement(name foo, xmlattributes(a AS x), b AS y) FROM t",
            "SELECT xmlelement(name foo, xmlattributes(DISTINCT a AS x)) FROM t",
            "SELECT xmlelement(name foo, xmlattributes(a AS x) ORDER BY a) FROM t",
            "SELECT xmlelement(name foo, s AS x) FROM t"})
    void testXmlAttributesRejected(String sql) {
        assertRejected(sql, POSTGRESQL);
    }

    @Test
    void testStatementBoundaries() throws Exception {
        String script = "SELECT * INTO TEMP t2 FROM t; SELECT * FROM a JOIN b USING (id) AS j;"
                + " SELECT ts AT LOCAL; SELECT s IS NFC NORMALIZED FROM t;"
                + " SELECT xmlelement(name foo, xmlattributes(a AS x)) FROM t;"
                + " SELECT a FROM t GROUP BY ALL a; SELECT 1";
        Statements statements = CCJSqlParserUtil.parseStatements(script, POSTGRESQL);
        assertThat(statements).hasSize(7).allMatch(PlainSelect.class::isInstance);
        assertEquals(script + ";", statements.toString().trim().replace(";\n", "; "));
        assertEquals(Persistence.TEMP,
                ((PlainSelect) statements.get(0)).getIntoClause().getPersistence());
        assertNull(((PlainSelect) statements.get(6)).getIntoClause());
    }

    @ParameterizedTest
    @EnumSource(value = Dialect.class, names = {"ORACLE", "MYSQL", "SQLSERVER"})
    void testEmptySelectListIntoBelongsToPostgreSql(Dialect dialect) {
        assertRejected("SELECT INTO t2 FROM t", parser -> parser.withDialect(dialect));
    }
}
