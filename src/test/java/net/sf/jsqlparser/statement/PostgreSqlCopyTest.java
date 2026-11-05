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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.CopyLegacyOption.Kind;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PostgreSqlCopyTest {
    @ParameterizedTest
    @ValueSource(strings = {
            "COPY users (email, name) FROM STDIN WITH (FORMAT csv, HEADER true)",
            "COPY users TO STDOUT WITH (FORMAT csv, DELIMITER ';', NULL '')",
            "COPY users TO STDOUT WITH (FORMAT csv, FORCE_QUOTE (email, name))",
            "COPY users TO STDOUT WITH (FORMAT csv, FORCE_QUOTE *)",
            "COPY (SELECT email FROM users) TO STDOUT WITH (FORMAT csv, HEADER true)",
            "COPY users FROM STDIN WITH (FORMAT csv, HEADER MATCH, FORCE_NULL (name))",
            "COPY users FROM STDIN WITH (FORMAT csv, ON_ERROR ignore, ENCODING 'UTF8')",
            "COPY users TO '/tmp/export.csv' WITH (FORMAT csv)",
            "COPY users TO STDOUT (FORMAT csv, HEADER true)",
            "COPY users FROM STDIN",
            "COPY users TO STDOUT WITH",
            "COPY (WITH c AS (SELECT email FROM users) SELECT email FROM c) TO STDOUT WITH (FORMAT csv)",
            "COPY (SELECT 1 UNION ALL SELECT 2) TO STDOUT WITH (FORMAT csv)"
    })
    void preservesCopyOptionsAndWithKeyword(String sql) throws JSQLParserException {
        CopyStatement copy = parse(sql);
        assertEquals(sql.contains(" WITH ") || sql.endsWith(" WITH"), copy.isWithKeyword());
        assertRoundTrip(copy);
        assertEquals(copy.toString(), CCJSqlParserUtil.parse(copy.toString()).toString());
    }

    @Test
    void optionalWithKeywordCanBeEditedWithoutChangingTheOptions() throws JSQLParserException {
        CopyStatement copy = parse("COPY users TO STDOUT WITH (FORMAT csv, HEADER true)");
        assertTrue(copy.isWithKeyword());
        copy.setWithKeyword(false);
        assertEquals("COPY users TO STDOUT (FORMAT csv, HEADER true)", copy.toString());
        assertFalse(parse(copy.toString()).isWithKeyword());
        copy.withWithKeyword(true);
        assertRoundTrip(copy);
        copy.setOptions(null);
        assertEquals("COPY users TO STDOUT WITH", copy.toString());
        assertRoundTrip(copy);
    }

    @Test
    void passesQueryExpressionsToTheConfiguredDeparser() throws JSQLParserException {
        CopyStatement copy = parse("COPY (SELECT 7 FROM users WHERE id > 8) "
                + "TO STDOUT WITH (FORMAT csv)");
        StringBuilder buffer = new StringBuilder();
        ExpressionDeParser expressions = new ExpressionDeParser() {
            @Override
            public <S> StringBuilder visit(LongValue value, S context) {
                return getBuilder().append(value.getValue() + 100);
            }
        };
        copy.accept(new StatementDeParser(expressions, new SelectDeParser(), buffer), null);
        assertEquals("COPY (SELECT 107 FROM users WHERE id > 108) TO STDOUT WITH (FORMAT csv)",
                buffer.toString());
        assertRoundTrip(parse(buffer.toString()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"WITH ()", "WITH (FORMAT csv,)", "WITH (,FORMAT csv)",
            "WITH FORMAT csv", "WITH (FORMAT csv,, HEADER true)", "WITH (FORCE_QUOTE (email)"})
    void rejectsMalformedWithOptions(String options) {
        assertThrows(JSQLParserException.class, () -> parse("COPY users TO STDOUT " + options));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "COPY t TO STDOUT CSV HEADER",
            "COPY t FROM STDIN WITH CSV HEADER DELIMITER ';' NULL ''",
            "COPY t (a, b) TO '/tmp/x.csv' WITH DELIMITER AS ',' CSV QUOTE AS '\"' ESCAPE AS '\\' "
                    + "FORCE QUOTE a, b",
            "COPY t FROM STDIN CSV FORCE NOT NULL a",
            "COPY t FROM STDIN CSV FORCE NOT NULL *",
            "COPY t FROM STDIN CSV FORCE NULL a, b",
            "COPY t FROM STDIN CSV FORCE NULL *",
            "COPY t TO STDOUT BINARY",
            "COPY t TO STDOUT CSV FORCE QUOTE *",
            "COPY t TO STDOUT CSV FORCE QUOTE \"A b\", c",
            "COPY t TO STDOUT CSV FORCE QUOTE \"all\", header, quote",
            "COPY t FROM STDIN WITH BINARY",
            "COPY t FROM STDIN FREEZE CSV",
            "COPY t FROM STDIN ENCODING 'UTF8'",
            "COPY t FROM STDIN NULL AS 'x' DELIMITER AS '|'",
            "COPY t TO STDOUT HEADER CSV",
            "COPY t TO STDOUT DELIMITER '|' CSV",
            "COPY t TO STDOUT QUOTE '\"' ESCAPE E'\\\\'",
            "COPY (SELECT a FROM t) TO STDOUT WITH CSV HEADER",
            "COPY BINARY t TO STDOUT",
            "COPY BINARY t (a, b) FROM '/tmp/x'",
            "COPY BINARY s.t TO STDOUT WITH ENCODING 'UTF8'",
            "COPY BINARY t FROM STDIN FREEZE"})
    void parsesTheOptionSyntaxFromBefore90(String sql) throws JSQLParserException {
        for (CopyStatement copy : new CopyStatement[] {parse(sql),
                (CopyStatement) CCJSqlParserUtil.parse(sql)}) {
            assertEquals(sql, copy.toString());
            assertNull(copy.getOptions());
            assertRoundTrip(copy);
        }
    }

    @Test
    void keepsEachLegacyOptionTyped() throws JSQLParserException {
        CopyStatement copy = parse("COPY t (a, b) TO '/tmp/x.csv' WITH DELIMITER AS ',' CSV "
                + "QUOTE AS '\"' ESCAPE '\\' FORCE QUOTE a, \"B\" ENCODING 'UTF8'");
        assertTrue(copy.isWithKeyword());
        assertFalse(copy.isBinary());
        assertEquals("'/tmp/x.csv'", copy.getPath());
        List<CopyLegacyOption> options = copy.getLegacyOptions();
        assertEquals(Arrays.asList(Kind.DELIMITER, Kind.CSV, Kind.QUOTE, Kind.ESCAPE,
                Kind.FORCE_QUOTE, Kind.ENCODING),
                options.stream().map(CopyLegacyOption::getKind).collect(Collectors.toList()));

        CopyLegacyOption delimiter = options.get(0);
        assertTrue(delimiter.isAsKeyword());
        assertEquals(",", delimiter.getValue().getValue());
        assertNull(options.get(1).getValue());
        assertFalse(options.get(3).isAsKeyword());
        assertEquals("\\", options.get(3).getValue().getValue());

        CopyLegacyOption forceQuote = options.get(4);
        assertFalse(forceQuote.isAllColumns());
        assertEquals(Arrays.asList("a", "\"B\""), forceQuote.getColumns().stream()
                .map(Column::getColumnName).collect(Collectors.toList()));
        assertEquals("FORCE QUOTE a, \"B\"", forceQuote.toString());
        assertEquals("UTF8", options.get(5).getValue().getValue());

        CopyLegacyOption all = parse("COPY t FROM STDIN CSV FORCE NOT NULL *").getLegacyOptions()
                .get(1);
        assertEquals(Kind.FORCE_NOT_NULL, all.getKind());
        assertTrue(all.isAllColumns());
        assertTrue(all.getColumns().isEmpty());

        CopyStatement binary = parse("COPY BINARY s.t (a) FROM STDIN");
        assertTrue(binary.isBinary());
        assertEquals("s.t", binary.getTable().getFullyQualifiedName());
        assertTrue(binary.getLegacyOptions().isEmpty());
    }

    @Test
    void describesWhatEachKindTakes() {
        assertFalse(Kind.CSV.takesString());
        assertTrue(Kind.NULL.takesString());
        assertTrue(Kind.NULL.allowsAsKeyword());
        assertTrue(Kind.ENCODING.takesString());
        assertFalse(Kind.ENCODING.allowsAsKeyword());
        assertTrue(Kind.FORCE_NULL.takesColumns());
        assertFalse(Kind.HEADER.takesColumns());
        assertEquals("FORCE NOT NULL", Kind.FORCE_NOT_NULL.getKeyword());
    }

    @Test
    void buildsAndDeparsesLegacyOptions() throws JSQLParserException {
        CopyStatement copy = new CopyStatement().withTable(new Table("t")).withFrom(true)
                .withPath("STDIN").withWithKeyword(true)
                .addLegacyOptions(new CopyLegacyOption(Kind.CSV),
                        new CopyLegacyOption(Kind.NULL).withAsKeyword(true)
                                .withValue(new StringValue("x")),
                        new CopyLegacyOption(Kind.FORCE_NULL).addColumns(new Column("a"),
                                new Column("b")),
                        new CopyLegacyOption(Kind.FORCE_NOT_NULL).withAllColumns(true));
        assertEquals("COPY t FROM STDIN WITH CSV NULL AS 'x' FORCE NULL a, b FORCE NOT NULL *",
                copy.toString());
        assertRoundTrip(copy);

        copy.setLegacyOptions(null);
        assertEquals("COPY t FROM STDIN WITH", copy.toString());
        copy.withBinary(true).withLegacyOptions(
                Arrays.asList(new CopyLegacyOption(Kind.FREEZE)));
        assertEquals("COPY BINARY t FROM STDIN WITH FREEZE", copy.toString());
        assertRoundTrip(copy);
        copy.setBinary(false);
        copy.withLegacyOptions(Arrays.asList(new CopyLegacyOption(Kind.CSV),
                new CopyLegacyOption(Kind.HEADER)));
        assertEquals("COPY t FROM STDIN WITH CSV HEADER", copy.toString());
        assertRoundTrip(copy);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "COPY t TO STDOUT CSV (FORMAT csv)",
            "COPY t TO STDOUT (FORMAT csv) CSV",
            "COPY t TO STDOUT ENCODING AS 'UTF8'",
            "COPY t TO STDOUT ENCODING UTF8",
            "COPY t TO STDOUT DELIMITER",
            "COPY t TO STDOUT DELIMITER ,",
            "COPY t TO STDOUT DELIMITER AS AS ','",
            "COPY t TO STDOUT NULL 1",
            "COPY t TO STDOUT CSV QUOTE",
            "COPY t TO STDOUT HEADER true",
            "COPY t TO STDOUT CSV, HEADER",
            "COPY t TO STDOUT CSV HEADER WITH",
            "COPY t TO STDOUT WITH WITH CSV",
            "COPY t TO STDOUT FORCE QUOTE",
            "COPY t TO STDOUT FORCE QUOTE a,",
            "COPY t TO STDOUT FORCE QUOTE (a)",
            "COPY t TO STDOUT FORCE QUOTE t.a",
            "COPY t TO STDOUT FORCE QUOTE a, *",
            "COPY t TO STDOUT FORCE QUOTE all",
            "COPY t TO STDOUT FORCE NOT NULL a, freeze",
            "COPY t TO STDOUT FORCE NOT a",
            "COPY t TO STDOUT FORCE",
            "COPY BINARY (SELECT 1) TO STDOUT",
            "COPY BINARY t",
            "COPY BINARY BINARY t TO STDOUT"})
    void rejectsWhatPostgreSqlRejects(String sql) {
        assertThrows(JSQLParserException.class, () -> parse(sql));
    }

    @Test
    void otherDialectsKeepRejectingTheLegacyForms() throws JSQLParserException {
        for (Dialect dialect : new Dialect[] {Dialect.MYSQL, Dialect.ORACLE, Dialect.SQLSERVER}) {
            for (String sql : new String[] {"COPY t TO STDOUT CSV HEADER",
                    "COPY t FROM STDIN NULL ''", "COPY BINARY t TO STDOUT"}) {
                assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql,
                        parser -> parser.withDialect(dialect)), dialect + " " + sql);
            }
        }
        // without a word after it, BINARY still names DuckDB's table
        CopyStatement duckDb =
                (CopyStatement) CCJSqlParserUtil.parse("COPY binary TO 'out.csv' (FORMAT CSV)");
        assertFalse(duckDb.isBinary());
        assertEquals("binary", duckDb.getTable().getName());
    }

    @Test
    void endsAtStatementBoundaries() throws JSQLParserException {
        Statements statements = CCJSqlParserUtil.parseStatements(
                "COPY t FROM STDIN CSV HEADER; COPY t TO STDOUT BINARY\nSELECT 1;"
                        + "COPY BINARY t TO STDOUT WITH;COPY t TO STDOUT FORCE QUOTE a, b",
                parser -> parser.withDialect(Dialect.POSTGRESQL));
        assertEquals(5, statements.size());
        assertEquals("COPY t TO STDOUT BINARY", statements.get(1).toString());
        assertEquals("COPY BINARY t TO STDOUT WITH", statements.get(3).toString());

        // DuckDB's FORCE INSTALL starts the next statement, FORCE QUOTE continues the options
        Statements duckDb = CCJSqlParserUtil.parseStatements(
                "SELECT 1; COPY t TO 'x.csv' CSV\nFORCE INSTALL httpfs");
        assertEquals(3, duckDb.size());
        assertEquals("COPY t TO 'x.csv' CSV", duckDb.get(1).toString());
        assertInstanceOf(ExtensionStatement.class, duckDb.get(2));
    }

    @Test
    void findsTheTableBehindTheLegacyOptions() throws JSQLParserException {
        assertThat(new TablesNamesFinder<>().getTables(
                parse("COPY BINARY s.t TO STDOUT CSV FORCE QUOTE a")))
                .containsExactly("s.t");
        assertThat(parse("COPY t FROM STDIN CSV").getFeatures().getCertain())
                .containsExactlyInAnyOrder(StmtFeature.READS_DATA, StmtFeature.MODIFIES_DATA);
    }

    private static CopyStatement parse(String sql) throws JSQLParserException {
        return (CopyStatement) CCJSqlParserUtil.parse(sql, p -> p.withDialect(Dialect.POSTGRESQL));
    }

    private static void assertRoundTrip(CopyStatement copy) throws JSQLParserException {
        StringBuilder buffer = new StringBuilder();
        copy.accept(new StatementDeParser(buffer), null);
        assertEquals(copy.toString(), buffer.toString());
        assertEquals(copy.toString(), parse(buffer.toString()).toString());
    }
}
