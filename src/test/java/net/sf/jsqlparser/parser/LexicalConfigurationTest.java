/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.parser;

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.parser.feature.FeatureConfiguration;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.select.PlainSelect;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class LexicalConfigurationTest {
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void parserOptionsCanChangeAfterReinitialization(boolean enabled) throws Exception {
        CCJSqlParser parser = new CCJSqlParser("SELECT \"first\"")
                .withDoubleQuotedStrings(!enabled);
        assertQuotedExpression(parser, !enabled, "first");
        parser.ReInit("SELECT \"second\"");
        parser.withDoubleQuotedStrings(enabled);
        assertQuotedExpression(parser, enabled, "second");
        assertSqlCanBeParsedAndDeparsed("SELECT \"second\"", false,
                p -> p.withDoubleQuotedStrings(enabled));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void sharedConfigurationCanBeMutatedInPlace(boolean enabled) throws Exception {
        FeatureConfiguration configuration = new FeatureConfiguration()
                .setValue(Feature.allowDoubleQuotedStrings, !enabled);
        CCJSqlParser first = new CCJSqlParser("SELECT \"first\"")
                .withConfiguration(configuration);
        CCJSqlParser second = new CCJSqlParser("SELECT \"first\"")
                .withConfiguration(configuration);
        assertQuotedExpression(first, !enabled, "first");
        assertQuotedExpression(second, !enabled, "first");
        configuration.setValue(Feature.allowDoubleQuotedStrings, enabled);
        first.ReInit("SELECT \"second\"");
        second.ReInit("SELECT \"second\"");
        second.withConfiguration(configuration);
        assertQuotedExpression(first, enabled, "second");
        assertQuotedExpression(second, enabled, "second");
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void parserConfigurationCanBeReplaced(boolean enabled) throws Exception {
        CCJSqlParser parser = new CCJSqlParser("SELECT \"first\"")
                .withDoubleQuotedStrings(!enabled);
        assertQuotedExpression(parser, !enabled, "first");
        parser.ReInit("SELECT \"second\"");
        parser.withConfiguration(new FeatureConfiguration()
                .setValue(Feature.allowDoubleQuotedStrings, enabled));
        assertQuotedExpression(parser, enabled, "second");
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void mySqlDialectCanChangeAfterReinitialization(boolean mysql) throws Exception {
        CCJSqlParser parser = new CCJSqlParser("SELECT seed")
                .withDialect(mysql ? Dialect.ANSI_SQL : Dialect.MYSQL);
        parser.Statement();
        parser.ReInit("SELECT 1--2");
        parser.withDialect(mysql ? Dialect.MYSQL : Dialect.ANSI_SQL);
        PlainSelect select = (PlainSelect) parser.Statement();
        assertEquals(mysql ? "SELECT 1 - -2" : "SELECT 1", select.toString());
        if (!mysql) {
            assertEquals(1, assertInstanceOf(LongValue.class,
                    select.getSelectItem(0).getExpression()).getValue());
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void hashCommentsCanChangeAfterReinitialization(boolean enabled) throws Exception {
        CCJSqlParser parser = new CCJSqlParser("SELECT seed").withHashLineComments(!enabled);
        parser.Statement();
        parser.ReInit("SELECT 1 # 2");
        parser.withHashLineComments(enabled);
        PlainSelect select = (PlainSelect) parser.Statement();
        assertEquals(enabled ? "SELECT 1" : "SELECT 1 # 2", select.toString());
        if (enabled) {
            assertEquals(1, assertInstanceOf(LongValue.class,
                    select.getSelectItem(0).getExpression()).getValue());
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void blankLineSeparatorsCanChangeAfterReinitialization(boolean enabled) throws Exception {
        CCJSqlParser parser = new CCJSqlParser("SELECT seed").withBlankLinesAsSeparator(!enabled);
        parser.Statement();
        parser.ReInit(enabled ? "SELECT 1\n\n\nSELECT 2" : "SELECT 1\n\n\n + 2");
        parser.withBlankLinesAsSeparator(enabled);
        Statements statements = parser.Statements();
        assertEquals(enabled ? 2 : 1, statements.size());
        assertEquals(enabled ? "SELECT 1" : "SELECT 1 + 2", statements.get(0).toString());
        if (enabled) {
            assertEquals("SELECT 2", statements.get(1).toString());
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void streamConfigurationCanBeReplaced(boolean enabled) throws Exception {
        CCJSqlParser parser = new CCJSqlParser("SELECT seed").withHashLineComments(!enabled);
        parser.Statement();
        parser.ReInit("SELECT 1 # 2");
        parser.withConfiguration(new FeatureConfiguration()
                .setValue(Feature.allowHashLineComments, enabled));
        assertEquals(enabled ? "SELECT 1" : "SELECT 1 # 2", parser.Statement().toString());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void directTokenManagerObservesInPlaceChanges(boolean enabled) {
        CCJSqlParserTokenManager lexer = lexer("\"first\" \"second\" \"third\"");
        lexer.configuration.setValue(Feature.allowDoubleQuotedStrings, !enabled);
        assertQuotedToken(lexer, !enabled, "first");
        lexer.configuration.setValue(Feature.allowDoubleQuotedStrings, enabled);
        assertQuotedToken(lexer, enabled, "second");
        lexer.configuration.setValue(Feature.allowDoubleQuotedStrings, !enabled);
        assertQuotedToken(lexer, !enabled, "third");
        assertEquals(CCJSqlParserConstants.EOF, lexer.getNextToken().kind);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void directTokenManagerObservesReinitialization(boolean enabled) {
        CCJSqlParserTokenManager lexer = lexer("\"first\"");
        lexer.configuration.setValue(Feature.allowDoubleQuotedStrings, !enabled);
        assertQuotedToken(lexer, !enabled, "first");
        lexer.ReInit(new SimpleCharStream(new StringProvider("\"second\"")));
        lexer.configuration.setValue(Feature.allowDoubleQuotedStrings, enabled);
        assertQuotedToken(lexer, enabled, "second");
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void directTokenManagerObservesConfigurationReplacement(boolean enabled) {
        CCJSqlParserTokenManager lexer = lexer("\"first\" \"second\" \"third\"");
        FeatureConfiguration original = new FeatureConfiguration()
                .setValue(Feature.allowDoubleQuotedStrings, !enabled);
        lexer.configuration = original;
        assertQuotedToken(lexer, !enabled, "first");
        lexer.configuration = new FeatureConfiguration()
                .setValue(Feature.allowDoubleQuotedStrings, enabled);
        assertQuotedToken(lexer, enabled, "second");
        original.setValue(Feature.allowDoubleQuotedStrings, enabled);
        lexer.configuration = original;
        assertQuotedToken(lexer, enabled, "third");
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void publicConfigurationRemainsAuthoritativeWithAWiredStream(boolean enabled)
            throws Exception {
        CCJSqlParser parser = new CCJSqlParser("\"first\" \"second\"")
                .withDoubleQuotedStrings(!enabled);
        assertQuotedToken(parser.token_source, !enabled, "first");
        parser.token_source.configuration = new FeatureConfiguration()
                .setValue(Feature.allowDoubleQuotedStrings, enabled);
        assertQuotedToken(parser.token_source, enabled, "second");
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void directSquareBracketQuotationUsesPublicConfiguration(boolean enabled) {
        CCJSqlParserTokenManager lexer = lexer("seed [second]");
        lexer.configuration.setValue(Feature.allowSquareBracketQuotation, !enabled);
        assertEquals("seed", lexer.getNextToken().image);
        lexer.configuration.setValue(Feature.allowSquareBracketQuotation, enabled);
        Token token = lexer.getNextToken();
        assertEquals(enabled ? CCJSqlParserConstants.S_QUOTED_IDENTIFIER
                : lexer.squaredBracketOpenIndex, token.kind);
        assertEquals(enabled ? "[second]" : "[", token.image);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void directBackslashEscapesUsePublicConfiguration(boolean enabled) {
        CCJSqlParserTokenManager lexer = lexer("'first' 'a\\'b'");
        lexer.configuration.setValue(Feature.allowBackslashEscapeCharacter, !enabled);
        assertEquals("'first'", lexer.getNextToken().image);
        lexer.configuration.setValue(Feature.allowBackslashEscapeCharacter, enabled);
        Token token = lexer.getNextToken();
        assertEquals(CCJSqlParserConstants.S_CHAR_LITERAL, token.kind);
        assertEquals(enabled ? "'a\\'b'" : "'a\\'", token.image);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void directPostgreSqlDialectUsesPublicConfiguration(boolean postgresql) {
        CCJSqlParserTokenManager lexer = lexer("seed a#>b");
        lexer.configuration.setValue(Feature.dialect,
                postgresql ? Dialect.ANSI_SQL.name() : Dialect.POSTGRESQL.name());
        assertEquals("seed", lexer.getNextToken().image);
        lexer.configuration.setValue(Feature.dialect,
                postgresql ? Dialect.POSTGRESQL.name() : Dialect.ANSI_SQL.name());
        assertEquals(postgresql ? "a" : "a#", lexer.getNextToken().image);
        assertEquals(postgresql ? "#>" : ">", lexer.getNextToken().image);
        assertEquals("b", lexer.getNextToken().image);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void directBigQueryDialectUsesPublicConfiguration(boolean bigquery) {
        CCJSqlParserTokenManager lexer = lexer("seed MATCH_RECOGNIZE (ORDER BY ts PATTERN (A$))");
        lexer.configuration.setValue(Feature.dialect,
                bigquery ? Dialect.ANSI_SQL.name() : Dialect.BIGQUERY.name());
        assertEquals("seed", lexer.getNextToken().image);
        lexer.configuration.setValue(Feature.dialect,
                bigquery ? Dialect.BIGQUERY.name() : Dialect.ANSI_SQL.name());
        List<String> images = new ArrayList<>();
        Token token;
        while ((token = lexer.getNextToken()).kind != CCJSqlParserConstants.EOF) {
            images.add(token.image);
        }
        assertEquals(bigquery
                ? List.of("MATCH_RECOGNIZE", "(", "ORDER", "BY", "ts", "PATTERN", "(", "A", "$",
                        ")", ")")
                : List.of("MATCH_RECOGNIZE", "(", "ORDER", "BY", "ts", "PATTERN", "(", "A$", ")",
                        ")"),
                images);
    }

    @Test
    void directHashCommentsStillRequireStreamWiring() {
        CCJSqlParserTokenManager lexer = lexer("a#b");
        lexer.configuration.setValue(Feature.allowHashLineComments, true);
        assertEquals("a#b", lexer.getNextToken().image);
        assertEquals(CCJSqlParserConstants.EOF, lexer.getNextToken().kind);
    }

    @Test
    void configurationSubclassesCanSupplyDynamicValues() {
        AtomicBoolean enabled = new AtomicBoolean();
        FeatureConfiguration configuration = new FeatureConfiguration() {
            @Override
            public Object getValue(Feature feature) {
                return feature == Feature.allowDoubleQuotedStrings ? enabled.get()
                        : super.getValue(feature);
            }
        };
        CCJSqlParserTokenManager lexer = lexer("\"first\" \"second\" \"third\"");
        lexer.configuration = configuration;
        assertQuotedToken(lexer, false, "first");
        enabled.set(true);
        assertQuotedToken(lexer, true, "second");
        enabled.set(false);
        assertQuotedToken(lexer, false, "third");
    }

    @Test
    void streamConfigurationSubclassesCanSupplyDynamicValues() throws Exception {
        AtomicBoolean mysql = new AtomicBoolean();
        FeatureConfiguration configuration = new FeatureConfiguration() {
            @Override
            public Object getValue(Feature feature) {
                return feature == Feature.dialect
                        ? (mysql.get() ? Dialect.MYSQL.name() : Dialect.ANSI_SQL.name())
                        : super.getValue(feature);
            }
        };
        CCJSqlParser parser = new CCJSqlParser("SELECT seed").withConfiguration(configuration);
        parser.Statement();
        mysql.set(true);
        parser.ReInit("SELECT 1--2");
        assertEquals("SELECT 1 - -2", parser.Statement().toString());
        mysql.set(false);
        parser.ReInit("SELECT 1--2");
        assertEquals("SELECT 1", parser.Statement().toString());
    }

    private static CCJSqlParserTokenManager lexer(String sql) {
        return new CCJSqlParserTokenManager(new SimpleCharStream(new StringProvider(sql)));
    }

    private static void assertQuotedToken(CCJSqlParserTokenManager lexer, boolean string,
            String value) {
        Token token = lexer.getNextToken();
        assertEquals(string ? CCJSqlParserConstants.S_CHAR_LITERAL
                : CCJSqlParserConstants.S_QUOTED_IDENTIFIER, token.kind);
        assertEquals('"' + value + '"', token.image);
    }

    private static void assertQuotedExpression(CCJSqlParser parser, boolean string, String value)
            throws Exception {
        PlainSelect select = (PlainSelect) parser.Statement();
        Expression expression = select.getSelectItem(0).getExpression();
        if (string) {
            assertEquals(value, assertInstanceOf(StringValue.class, expression).getValue());
        } else {
            assertEquals('"' + value + '"',
                    assertInstanceOf(Column.class, expression).getColumnName());
        }
        assertEquals("SELECT \"" + value + "\"", select.toString());
    }
}
