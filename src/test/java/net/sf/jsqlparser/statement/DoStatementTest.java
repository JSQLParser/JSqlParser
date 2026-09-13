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
import static org.junit.jupiter.api.Assertions.*;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.parser.feature.FeatureConfiguration;
import net.sf.jsqlparser.statement.select.SelectVisitorAdapter;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import net.sf.jsqlparser.util.validation.Validation;
import net.sf.jsqlparser.util.validation.feature.FeaturesAllowed;
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
        assertEquals("SELECT 0;\n" + sql + "\nSELECT 1;\n", output.toString());
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
            if (dialect != Dialect.POSTGRESQL) {
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
}
