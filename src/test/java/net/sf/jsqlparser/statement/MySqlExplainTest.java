/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

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
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.HexValue;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.UserVariable;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.parser.feature.FeatureConfiguration;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.ExplainStatement.Format;
import net.sf.jsqlparser.statement.ExplainStatement.Option;
import net.sf.jsqlparser.statement.ExplainStatement.OptionType;
import net.sf.jsqlparser.statement.ExplainStatement.TargetKind;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.upsert.Upsert;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.validation.Validation;
import net.sf.jsqlparser.util.validation.feature.FeaturesAllowed;
import net.sf.jsqlparser.util.validation.feature.MySqlVersion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** MySQL's EXPLAIN, DESCRIBE and DESC forms. */
class MySqlExplainTest {

    private static Statement mysql(String sql) throws JSQLParserException {
        return assertSqlCanBeParsedAndDeparsed(sql, true, p -> p.withDialect(Dialect.MYSQL));
    }

    private static ExplainStatement explain(String sql) throws JSQLParserException {
        return assertInstanceOf(ExplainStatement.class, mysql(sql));
    }

    private static void assertRejected(String sql, Dialect dialect) {
        assertThrows(JSQLParserException.class,
                () -> CCJSqlParserUtil.parse(sql, p -> p.withDialect(dialect)), sql);
    }

    @ParameterizedTest
    @ValueSource(strings = {"EXPLAIN FORMAT = JSON SELECT * FROM t",
            "EXPLAIN FORMAT = TREE UPDATE t SET a = 1",
            "EXPLAIN FORMAT = TRADITIONAL DELETE FROM t WHERE a = 1",
            "EXPLAIN FORMAT = JSON INSERT INTO t VALUES (1)",
            "EXPLAIN FORMAT = JSON REPLACE INTO t VALUES (1)", "EXPLAIN REPLACE INTO t VALUES (1)",
            "EXPLAIN FORMAT = TREE TABLE t", "EXPLAIN FORMAT = TREE (SELECT a FROM t)",
            "EXPLAIN FORMAT = JSON WITH x AS (SELECT a FROM t) SELECT * FROM x",
            "EXPLAIN ANALYZE FORMAT = TREE SELECT * FROM t",
            "EXPLAIN ANALYZE FORMAT = JSON SELECT * FROM t",
            "EXPLAIN FORMAT = JSON INTO @plan SELECT * FROM t",
            "EXPLAIN INTO @plan SELECT * FROM t", "EXPLAIN FOR CONNECTION 1",
            "EXPLAIN FORMAT = JSON FOR CONNECTION 7", "EXPLAIN ANALYZE FOR CONNECTION 1",
            "EXPLAIN FOR SCHEMA s SELECT * FROM t",
            "EXPLAIN FORMAT = TREE FOR DATABASE s UPDATE t SET a = 1",
            "EXPLAIN ANALYZE FOR DATABASE s SELECT * FROM t",
            "DESC FORMAT = JSON SELECT * FROM t", "DESCRIBE ANALYZE SELECT * FROM t",
            "DESC ANALYZE FORMAT = TREE SELECT * FROM t", "DESCRIBE FOR CONNECTION 1",
            "DESCRIBE UPDATE t SET a = 1", "DESC INSERT INTO t VALUES (1)",
            "DESC DELETE FROM t", "DESCRIBE REPLACE INTO t VALUES (1)",
            "DESC VALUES ROW(1)", "DESC FORMAT = JSON INTO @plan SELECT * FROM t"})
    void explainForms(String sql) throws JSQLParserException {
        ExplainStatement explain = explain(sql);
        assertEquals(sql.substring(0, sql.indexOf(' ')), explain.getKeyword());
        // the forms MySQL alone has are read without a dialect too
        assertEquals(sql, assertSqlCanBeParsedAndDeparsed(sql, true).toString());
    }

    @Test
    void describeTableStatementNeedsTheMySqlDialect() throws JSQLParserException {
        ExplainStatement explain = explain("DESC TABLE t");
        assertEquals("TABLE t", explain.getStatement().toString());
        assertEquals("DESCRIBE TABLE t", explain("DESCRIBE TABLE t").toString());
        assertEquals("DESC TABLE t",
                CCJSqlParserUtil.parse("DESC TABLE t", p -> p.withDialect(Dialect.MARIADB))
                        .toString());
        // Snowflake's DESCRIBE TABLE t describes a table, so a bare parser takes neither reading
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse("DESC TABLE t"));
        assertEquals("DESC ANALYZE TABLE t",
                CCJSqlParserUtil.parse("DESC ANALYZE TABLE t").toString());
    }

    @Test
    void formatOption() throws JSQLParserException {
        ExplainStatement explain = explain("EXPLAIN ANALYZE FORMAT = TREE SELECT * FROM t");
        assertEquals(List.of(OptionType.ANALYZE, OptionType.FORMAT),
                explain.getOptionList().stream().map(Option::getType)
                        .collect(java.util.stream.Collectors.toList()));
        Option format = explain.getOption(OptionType.FORMAT);
        assertEquals("TREE", format.getValue());
        assertTrue(format.isUseEqual());
        assertFalse(explain.getOption(OptionType.ANALYZE).isUseEqual());
        assertEquals(Format.TREE, explain.getFormat());
        assertEquals(TargetKind.STATEMENT, explain.getTargetKind());
        assertInstanceOf(Select.class, explain.getStatement());
        assertNull(explain.getIntoVariable());
        assertNull(explain.getConnectionId());

        assertNull(explain("EXPLAIN SELECT * FROM t").getFormat());
        assertEquals(Format.JSON,
                ((ExplainStatement) CCJSqlParserUtil.parse("EXPLAIN FORMAT JSON SELECT 1"))
                        .getFormat());
    }

    @ParameterizedTest
    @ValueSource(strings = {"EXPLAIN FORMAT=json SELECT 1", "EXPLAIN format = 'JSON' SELECT 1",
            "EXPLAIN FORMAT = \"Json\" SELECT 1", "EXPLAIN FORMAT = `json` SELECT 1"})
    void formatNamesAreCaseInsensitiveAndMayBeQuoted(String sql) throws JSQLParserException {
        ExplainStatement explain =
                (ExplainStatement) CCJSqlParserUtil.parse(sql, p -> p.withDialect(Dialect.MYSQL));
        assertEquals(Format.JSON, explain.getFormat());
        assertEquals("EXPLAIN FORMAT = JSON SELECT 1", explain.toString());
        assertDeparse(explain, "EXPLAIN FORMAT = JSON SELECT 1");
    }

    @Test
    void intoVariable() throws JSQLParserException {
        ExplainStatement explain = explain("EXPLAIN FORMAT = JSON INTO @plan SELECT * FROM t");
        assertEquals("plan", explain.getIntoVariable().getName());
        assertEquals(Format.JSON, explain.getFormat());

        explain = (ExplainStatement) CCJSqlParserUtil.parse(
                "EXPLAIN FORMAT = JSON INTO @'my plan' SELECT 1",
                p -> p.withDialect(Dialect.MYSQL));
        assertEquals("EXPLAIN FORMAT = JSON INTO @'my plan' SELECT 1", explain.toString());
    }

    @Test
    void forConnection() throws JSQLParserException {
        ExplainStatement explain = explain("EXPLAIN FORMAT = JSON FOR CONNECTION 7");
        assertEquals(TargetKind.CONNECTION, explain.getTargetKind());
        assertEquals(7L, assertInstanceOf(LongValue.class, explain.getConnectionId()).getValue());
        assertNull(explain.getStatement());
        assertNull(explain.getTable());

        explain = explain("EXPLAIN FOR CONNECTION 0x1F");
        assertInstanceOf(HexValue.class, explain.getConnectionId());
        assertEquals("EXPLAIN FOR CONNECTION 0x1F", explain.toString());
    }

    @Test
    void forSchema() throws JSQLParserException {
        ExplainStatement explain = explain("EXPLAIN FOR SCHEMA s SELECT * FROM t");
        assertEquals("s", explain.getSchemaName());
        assertTrue(explain.isSchemaKeyword());

        explain = explain("EXPLAIN FOR DATABASE 'my db' SELECT * FROM t");
        assertEquals("'my db'", explain.getSchemaName());
        assertFalse(explain.isSchemaKeyword());
    }

    @Test
    void explainedStatements() throws JSQLParserException {
        assertInstanceOf(Update.class, explain("DESCRIBE UPDATE t SET a = 1").getStatement());
        assertInstanceOf(Delete.class, explain("DESC DELETE FROM t").getStatement());
        assertInstanceOf(Upsert.class,
                explain("EXPLAIN REPLACE INTO t VALUES (1)").getStatement());
        assertEquals("DESC", explain("DESC ANALYZE SELECT * FROM t").getKeyword());
    }

    @ParameterizedTest
    @ValueSource(strings = {"DESCRIBE t a", "DESC t a", "EXPLAIN t a", "DESCRIBE t 'a%'",
            "EXPLAIN db.t 'a_'", "DESC t `a b`", "DESCRIBE t 0x61", "DESCRIBE t comment"})
    void tableColumnPattern(String sql) throws JSQLParserException {
        Statement statement = mysql(sql);
        // DESCRIBE x y could also be Snowflake's DESCRIBE of an object kind
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql));
        assertEquals(sql, CCJSqlParserUtil.parse(sql, p -> p.withDialect(Dialect.MARIADB))
                .toString());
        Object pattern = statement instanceof ExplainStatement
                ? ((ExplainStatement) statement).getColumnPattern()
                : ((DescribeStatement) statement).getColumnPattern();
        String text = sql.split(" ", 3)[2];
        assertEquals(text, pattern.toString());
        Class<?> type = text.startsWith("'") ? StringValue.class
                : text.startsWith("0x") ? HexValue.class : Column.class;
        assertTrue(type.isInstance(pattern), text);
        assertThat(new TablesNamesFinder<>().getTables(statement)).hasSize(1);
    }

    @Test
    void columnPatternModel() throws JSQLParserException {
        DescribeStatement describe = (DescribeStatement) mysql("DESCRIBE t 'a%'");
        assertEquals("t", describe.getTable().getFullyQualifiedName());
        assertEquals("a%", ((StringValue) describe.getColumnPattern()).getValue());

        ExplainStatement explain = explain("EXPLAIN t a");
        assertEquals(TargetKind.TABLE, explain.getTargetKind());
        assertEquals("a", ((Column) explain.getColumnPattern()).getColumnName());
        assertTrue(explain.getOptionList().isEmpty());

        assertEquals("DESC t", new DescribeStatement(new Table("t")).setDescribeType("DESC")
                .toString());
        assertEquals("DESC t c", new DescribeStatement(new Table("t")).setDescribeType("DESC")
                .withColumnPattern(new Column("c")).toString());
    }

    @Test
    void existingReadingsAreKept() throws JSQLParserException {
        // DESCRIBE of a query and of a table stays DescribeStatement
        assertInstanceOf(DescribeStatement.class, mysql("DESC SELECT * FROM t"));
        assertInstanceOf(DescribeStatement.class, mysql("DESCRIBE (SELECT a FROM t)"));
        for (String table : List.of("DESC analyze", "DESC replace", "DESC format", "DESC t")) {
            DescribeStatement describe = (DescribeStatement) mysql(table);
            assertNull(describe.getColumnPattern());
        }
        // EXPLAIN ANALYZE SELECT keeps its option model, EXPLAIN table its table
        ExplainStatement explain = explain("EXPLAIN ANALYZE SELECT * FROM t");
        assertNull(explain.getOption(OptionType.ANALYZE).getValue());
        assertEquals("replace", explain("EXPLAIN replace").getTable().getName());
        // the generic options without an equals sign remain where they were read
        assertSqlCanBeParsedAndDeparsed("EXPLAIN FORMAT JSON SELECT * FROM t", true,
                p -> p.withDialect(Dialect.MYSQL));
        assertSqlCanBeParsedAndDeparsed("EXPLAIN PLAN FOR SELECT 1", true,
                p -> p.withDialect(Dialect.ORACLE));
        assertSqlCanBeParsedAndDeparsed("EXPLAIN (FORMAT JSON) SELECT 1", true,
                p -> p.withDialect(Dialect.POSTGRESQL));
    }

    @ParameterizedTest
    @ValueSource(strings = {"EXPLAIN FORMAT = XML SELECT 1", "EXPLAIN FORMAT = TEXT SELECT 1",
            "EXPLAIN FORMAT = 'LONG' SELECT 1", "EXPLAIN FORMAT = SELECT 1",
            "EXPLAIN FORMAT = JSON ANALYZE SELECT 1",
            "EXPLAIN ANALYZE FORMAT = JSON INTO @v SELECT 1",
            "EXPLAIN INTO @v FORMAT = JSON SELECT 1", "EXPLAIN FORMAT = JSON INTO @@v SELECT 1",
            "EXPLAIN FORMAT = JSON INTO v SELECT 1", "EXPLAIN FOR CONNECTION",
            "EXPLAIN FOR CONNECTION -1", "EXPLAIN FOR CONNECTION 1.5",
            "EXPLAIN FOR CONNECTION @c", "EXPLAIN FOR SCHEMA s FOR CONNECTION 1",
            "EXPLAIN FOR SCHEMA s", "EXPLAIN FORMAT = JSON t", "EXPLAIN FORMAT = JSON",
            "DESCRIBE t a b", "DESCRIBE t a.b", "EXPLAIN t a, b", "DESC FOR CONNECTION 1 a"})
    void rejectedForms(String sql) {
        assertRejected(sql, Dialect.MYSQL);
    }

    @ParameterizedTest
    @ValueSource(strings = {"EXPLAIN FORMAT = JSON SELECT 1", "EXPLAIN FOR CONNECTION 1",
            "DESCRIBE t a", "EXPLAIN t 'a%'", "DESC FORMAT = JSON SELECT 1",
            "DESC UPDATE t SET a = 1", "EXPLAIN REPLACE INTO t VALUES (1)"})
    void otherDialectsAreUnchanged(String sql) {
        for (Dialect dialect : List.of(Dialect.POSTGRESQL, Dialect.ORACLE, Dialect.SQLSERVER,
                Dialect.H2)) {
            assertRejected(sql, dialect);
        }
    }

    @Test
    void summarizeTakesNoColumn() {
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse("SUMMARIZE t a"));
    }

    @Test
    void statementBoundaries() throws JSQLParserException {
        Statements statements = CCJSqlParserUtil.parseStatements(
                "EXPLAIN FORMAT = JSON SELECT 1; DESCRIBE t a; EXPLAIN FOR CONNECTION 3;\n"
                        + "DESC t\nCOMMIT;\nDESC t 'a%'; EXPLAIN t",
                p -> p.withDialect(Dialect.MYSQL));
        assertEquals(List.of("EXPLAIN FORMAT = JSON SELECT 1", "DESCRIBE t a",
                "EXPLAIN FOR CONNECTION 3", "DESC t", "COMMIT", "DESC t 'a%'", "EXPLAIN t"),
                statements.stream().map(Object::toString)
                        .collect(java.util.stream.Collectors.toList()));
        assertEquals("DESCRIBE t COMMIT", CCJSqlParserUtil
                .parse("DESCRIBE t COMMIT", p -> p.withDialect(Dialect.MYSQL)).toString());
    }

    @Test
    void tablesAndFeatures() throws JSQLParserException {
        assertThat(TablesNamesFinder.findTables("EXPLAIN FORMAT = JSON SELECT * FROM t"))
                .containsExactly("t");
        assertThat(TablesNamesFinder.findTables("EXPLAIN FOR CONNECTION 3")).isEmpty();
        // EXPLAIN table describes the table, as DESCRIBE table does
        assertThat(TablesNamesFinder.findTables("EXPLAIN db.t")).containsExactly("db.t");

        StatementFeatures into = CCJSqlParserUtil
                .parse("EXPLAIN FORMAT = JSON INTO @plan SELECT * FROM t").getFeatures();
        assertTrue(into.is(StmtFeature.MODIFIES_SESSION));
        assertFalse(into.returnsResultSet());
        assertTrue(CCJSqlParserUtil.parse("EXPLAIN FOR CONNECTION 3").getFeatures()
                .returnsResultSet());
    }

    @Test
    void validation() {
        FeatureConfiguration config =
                new FeatureConfiguration().setValue(Feature.dialect, Dialect.MYSQL.name());
        for (String sql : List.of("EXPLAIN FORMAT = JSON SELECT * FROM t", "DESCRIBE t a",
                "EXPLAIN FOR CONNECTION 3", "DESC FORMAT = TREE UPDATE t SET a = 1")) {
            assertTrue(new Validation(config, List.of(MySqlVersion.V8_0), sql).validate()
                    .isEmpty(), sql);
        }
        assertThat(new Validation(List.of(FeaturesAllowed.SELECT),
                "EXPLAIN FORMAT = JSON SELECT * FROM t").validate().get(0).getErrors())
                .anyMatch(error -> error.getMessage().contains(Feature.explain.name()));
    }

    @Test
    void buildsMySqlForms() throws JSQLParserException {
        ExplainStatement explain = new ExplainStatement("EXPLAIN")
                .setConnectionId(new LongValue(3));
        explain.addOption(new Option(OptionType.FORMAT).withValue("TREE").withUseEqual(true));
        assertEquals("EXPLAIN FORMAT = TREE FOR CONNECTION 3", explain.toString());
        assertDeparse(explain, "EXPLAIN FORMAT = TREE FOR CONNECTION 3");

        Statement select = CCJSqlParserUtil.parse("SELECT 1");
        explain.setStatement(select);
        assertNull(explain.getConnectionId());
        assertEquals(TargetKind.STATEMENT, explain.getTargetKind());
        explain.setTable(new Table("t"));
        assertNull(explain.getStatement());
        assertEquals(TargetKind.TABLE, explain.getTargetKind());

        ExplainStatement into = new ExplainStatement("DESC")
                .withIntoVariable(new UserVariable("plan")).withSchemaName("s")
                .withSchemaKeyword(true);
        into.setStatement(select);
        assertEquals("DESC INTO @plan FOR SCHEMA s SELECT 1", into.toString());
        assertDeparse(into, "DESC INTO @plan FOR SCHEMA s SELECT 1");

        assertEquals(Format.TREE, Format.from("tree"));
        assertNull(Format.find("LONG"));
    }
}
