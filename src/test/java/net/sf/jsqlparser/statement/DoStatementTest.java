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

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.parser.feature.FeatureConfiguration;
import net.sf.jsqlparser.statement.select.ParenthesedSelect;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SelectVisitorAdapter;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import net.sf.jsqlparser.util.validation.Validation;
import net.sf.jsqlparser.util.validation.feature.FeaturesAllowed;
import net.sf.jsqlparser.util.validation.feature.MySqlVersion;
import net.sf.jsqlparser.util.validation.feature.PostgresqlVersion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class DoStatementTest {
    @ParameterizedTest
    @ValueSource(strings = {"DO $$BEGIN NULL; END$$", "DO LANGUAGE plpgsql $$BEGIN NULL; END$$",
            "DO $body$BEGIN NULL; END$body$ LANGUAGE plpgsql", "DO 'BEGIN NULL; END'",
            "DO LANGUAGE plpython3u 'print(1)'"})
    void roundTripsBodyAndLanguagePosition(String sql) throws Exception {
        DoStatement statement = (DoStatement) assertSqlCanBeParsedAndDeparsed(sql, false,
                p -> p.withDialect(Dialect.POSTGRESQL));
        assertEquals(sql, CCJSqlParserUtil.parse(statement.toString(),
                p -> p.withDialect(Dialect.POSTGRESQL)).toString());
        assertEquals(sql.startsWith("DO LANGUAGE"), statement.isLanguageBeforeCode());
        assertEquals(
                sql.contains("LANGUAGE") ? sql.contains("plpython") ? "plpython3u" : "plpgsql"
                        : null,
                statement.getLanguage());
        assertTrue(statement.getFeatures().isOpaque());
        assertTrue(statement.getFeatures().mayModifyData());
        assertThrows(UnsupportedOperationException.class,
                () -> new TablesNamesFinder().getTables(statement));
    }

    @Test
    void preservesProceduralBodyAndFollowingStatementsIssue1946() throws Exception {
        String sql;
        try (InputStream input = getClass().getResourceAsStream("/postgresql/do-issue1946.sql")) {
            assertNotNull(input);
            sql = new String(input.readAllBytes(), StandardCharsets.UTF_8).strip();
        }
        String body = sql.substring(sql.indexOf("$$"), sql.lastIndexOf("$$") + 2);
        Statements statements =
                CCJSqlParserUtil.parseStatements("SELECT 0;\n" + sql + "\nSELECT 1;",
                        p -> p.withDialect(Dialect.POSTGRESQL).withUnsupportedStatements(false));
        assertEquals(3, statements.size());
        assertEquals("SELECT 0", statements.get(0).toString());
        DoStatement block = assertInstanceOf(DoStatement.class, statements.get(1));
        assertEquals(body, block.getCode().toString());
        assertEquals(body.substring(2, body.length() - 2), block.getCode().getValue());
        assertEquals("SELECT 1", statements.get(2).toString());

        StringBuilder output = new StringBuilder();
        for (Statement statement : statements) {
            statement.accept(new StatementDeParser(output), null);
            output.append(";\n");
        }
        assertEquals("SELECT 0;\nDO " + body + ";\nSELECT 1;\n", output.toString());
        Statements reparsed = CCJSqlParserUtil.parseStatements(output.toString(),
                p -> p.withDialect(Dialect.POSTGRESQL).withUnsupportedStatements(false));
        assertEquals(3, reparsed.size());
        assertEquals(body,
                assertInstanceOf(DoStatement.class, reparsed.get(1)).getCode().toString());
        assertEquals("SELECT 0", reparsed.get(0).toString());
        assertEquals("SELECT 1", reparsed.get(2).toString());
    }

    @Test
    void supportsBodyVisitorsAndAstEdits() throws Exception {
        DoStatement statement = (DoStatement) CCJSqlParserUtil.parse("DO $$BEGIN NULL; END$$",
                p -> p.withDialect(Dialect.POSTGRESQL));
        List<String> bodies = new ArrayList<>();
        statement.accept(new StatementVisitorAdapter<>(new SelectVisitorAdapter<>(
                new ExpressionVisitorAdapter<Void>() {
                    @Override
                    public <S> Void visit(StringValue value, S context) {
                        assertEquals("context", context);
                        bodies.add(value.toString());
                        return null;
                    }
                })), "context");
        assertEquals(List.of("$$BEGIN NULL; END$$"), bodies);
        statement.withCode(new StringValue("$new$BEGIN PERFORM 1; END$new$"))
                .withLanguage("plpgsql");
        statement.setLanguageBeforeCode(true);
        assertEquals("DO LANGUAGE plpgsql $new$BEGIN PERFORM 1; END$new$", statement.toString());
        StringBuilder output = new StringBuilder();
        ExpressionDeParser expressions = new ExpressionDeParser() {
            @Override
            public <S> StringBuilder visit(StringValue value, S context) {
                return getBuilder().append("$$BEGIN NULL; END$$");
            }
        };
        statement.accept(new StatementDeParser(expressions, new SelectDeParser(), output));
        assertEquals("DO LANGUAGE plpgsql $$BEGIN NULL; END$$", output.toString());
    }

    @Test
    void gatesDialectAndRejectsMalformedWrappers() throws Exception {
        String sql = "DO $$BEGIN NULL; END$$";
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql));
        for (Dialect dialect : Dialect.values()) {
            if (dialect == Dialect.MYSQL || dialect == Dialect.MARIADB) {
                // MySQL's DO evaluates the literal as an expression instead
                DoStatement expressions = (DoStatement) CCJSqlParserUtil.parse(sql,
                        p -> p.withDialect(dialect));
                assertNull(expressions.getCode());
                assertEquals(1, expressions.getSelectItems().size());
            } else if (dialect != Dialect.POSTGRESQL) {
                assertThrows(JSQLParserException.class,
                        () -> CCJSqlParserUtil.parse(sql, p -> p.withDialect(dialect)));
            }
        }
        for (String malformed : List.of("DO", "DO LANGUAGE plpgsql", "DO 123",
                "DO LANGUAGE plpgsql $$x$$ LANGUAGE plpgsql", "DO $tag$unterminated")) {
            assertThrows(JSQLParserException.class,
                    () -> CCJSqlParserUtil.parse(malformed,
                            p -> p.withDialect(Dialect.POSTGRESQL)));
        }
    }

    @Test
    void validatesWrapperCapabilityWithoutClaimingBodyValidation() {
        FeatureConfiguration config = new FeatureConfiguration()
                .setValue(Feature.dialect, Dialect.POSTGRESQL.name());
        assertTrue(new Validation(config, List.of(PostgresqlVersion.V10),
                "DO $$arbitrary language body$$")
                .validate().isEmpty());
        assertFalse(new Validation(config, List.of(new FeaturesAllowed(Feature.select)),
                "DO $$arbitrary language body$$").validate().isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"DO SLEEP(0)", "DO 1, @a := 2", "DO (SELECT 1)",
            "DO GET_LOCK('l', 10), RELEASE_LOCK('l')", "DO 1 AS a", "DO 1 a", "DO *",
            "DO (SELECT COUNT(*) FROM t)"})
    void mySqlExpressions(String sql) throws Exception {
        DoStatement statement = (DoStatement) assertSqlCanBeParsedAndDeparsed(sql, true,
                p -> p.withDialect(Dialect.MYSQL));
        assertNull(statement.getCode());
        assertNull(statement.getLanguage());
        assertFalse(statement.getSelectItems().isEmpty());
        // without a dialect DO followed by an expression other than a string is MySQL's
        assertEquals(sql, assertSqlCanBeParsedAndDeparsed(sql, true).toString());
        assertEquals(sql, CCJSqlParserUtil.parse(sql, p -> p.withDialect(Dialect.MARIADB))
                .toString());
    }

    @Test
    void mySqlExpressionModel() throws Exception {
        DoStatement statement = (DoStatement) CCJSqlParserUtil
                .parse("DO SLEEP(1), (SELECT a FROM t) AS x", p -> p.withDialect(Dialect.MYSQL));
        List<SelectItem<?>> items = statement.getSelectItems();
        assertEquals(2, items.size());
        assertEquals("SLEEP", assertInstanceOf(Function.class, items.get(0).getExpression())
                .getName());
        assertInstanceOf(ParenthesedSelect.class, items.get(1).getExpression());
        assertEquals("x", items.get(1).getAlias().getName());

        statement.withSelectItems(List.of(new SelectItem<>(new StringValue("v"))));
        assertEquals("DO 'v'", statement.toString());
        StringBuilder output = new StringBuilder();
        statement.accept(new StatementDeParser(output));
        assertEquals("DO 'v'", output.toString());
    }

    @Test
    void mySqlStringNeedsTheDialect() throws Exception {
        // a string after DO could also be PostgreSQL's code block
        String block = "DO 'BEGIN NULL; END'";
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(block));
        assertThrows(JSQLParserException.class,
                () -> CCJSqlParserUtil.parse("DO LANGUAGE plpgsql 'BEGIN NULL; END'"));
        DoStatement statement = (DoStatement) assertSqlCanBeParsedAndDeparsed("DO 'x', 'y'",
                true, p -> p.withDialect(Dialect.MYSQL));
        assertNull(statement.getCode());
        assertEquals(2, statement.getSelectItems().size());
        assertNotNull(((DoStatement) CCJSqlParserUtil.parse(block,
                p -> p.withDialect(Dialect.POSTGRESQL))).getCode());
    }

    @ParameterizedTest
    @ValueSource(strings = {"DO", "DO 1,", "DO 1 INTO @a", "DO 1 FROM t", "DO 1 WHERE 1 = 1",
            "DO , 1"})
    void rejectsMalformedMySqlExpressions(String sql) {
        assertThrows(JSQLParserException.class,
                () -> CCJSqlParserUtil.parse(sql, p -> p.withDialect(Dialect.MYSQL)));
    }

    @Test
    void otherDialectsDoNotReadMySqlExpressions() {
        for (Dialect dialect : List.of(Dialect.POSTGRESQL, Dialect.ORACLE, Dialect.SQLSERVER,
                Dialect.H2)) {
            assertThrows(JSQLParserException.class,
                    () -> CCJSqlParserUtil.parse("DO SLEEP(0)", p -> p.withDialect(dialect)));
        }
    }

    @Test
    void mySqlStatementBoundaries() throws Exception {
        Statements statements = CCJSqlParserUtil.parseStatements(
                "DO GET_LOCK('l', 10); SELECT 1; DO RELEASE_LOCK('l')\nSELECT 2",
                p -> p.withDialect(Dialect.MYSQL));
        assertEquals(List.of("DO GET_LOCK('l', 10)", "SELECT 1", "DO RELEASE_LOCK('l')",
                "SELECT 2"),
                statements.stream().map(Object::toString).collect(Collectors.toList()));
    }

    @Test
    void mySqlTablesFeaturesAndVisitors() throws Exception {
        assertThat(TablesNamesFinder.findTables("DO (SELECT COUNT(*) FROM t), SLEEP(0)"))
                .containsExactly("t");

        StatementFeatures reads = CCJSqlParserUtil.parse("DO (SELECT a FROM t)").getFeatures();
        assertTrue(reads.is(StmtFeature.READS_DATA));
        assertFalse(reads.returnsResultSet());
        assertFalse(reads.isOpaque());
        StatementFeatures sleep = CCJSqlParserUtil.parse("DO SLEEP(1)").getFeatures();
        assertTrue(sleep.getUnresolvedReferences().contains("sleep"));
        assertTrue(sleep.mayModifyData());

        List<String> functions = new ArrayList<>();
        CCJSqlParserUtil.parse("DO SLEEP(1), RELEASE_LOCK('l')").accept(
                new StatementVisitorAdapter<>(new SelectVisitorAdapter<>(
                        new ExpressionVisitorAdapter<Void>() {
                            @Override
                            public <S> Void visit(Function function, S context) {
                                functions.add(function.getName());
                                return null;
                            }
                        })),
                null);
        assertEquals(List.of("SLEEP", "RELEASE_LOCK"), functions);
    }

    @Test
    void validatesMySqlExpressions() {
        FeatureConfiguration config =
                new FeatureConfiguration().setValue(Feature.dialect, Dialect.MYSQL.name());
        assertTrue(new Validation(config, List.of(MySqlVersion.V8_0), "DO SLEEP(1), 2")
                .validate().isEmpty());
        assertFalse(new Validation(config, List.of(new FeaturesAllowed(Feature.select)),
                "DO SLEEP(1)").validate().isEmpty());
    }
}
