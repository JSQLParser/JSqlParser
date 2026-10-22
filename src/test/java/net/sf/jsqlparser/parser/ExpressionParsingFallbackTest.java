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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.feature.Feature;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class ExpressionParsingFallbackTest {
    private static final String PARTIAL_EXPRESSION = "[customer].[id] = 1 trailing tokens";

    @ParameterizedTest
    @EnumSource(ParserApi.class)
    void configuresCompleteExpressionsOnce(ParserApi api) throws JSQLParserException {
        List<CCJSqlParser> attempts = new ArrayList<>();
        Expression result = api.parse("[customer].[id] = 1", false, parser -> {
            attempts.add(parser);
            parser.withSquareBracketQuotation(true);
        });

        assertEquals("[customer].[id] = 1", result.toString());
        assertEquals(1, attempts.size());
    }

    @ParameterizedTest
    @EnumSource(ParserApi.class)
    void reappliesConfigurationToAFreshParserForPartialInput(ParserApi api)
            throws JSQLParserException {
        List<CCJSqlParser> attempts = new ArrayList<>();
        List<Boolean> modes = new ArrayList<>();
        Expression result = api.parse(PARTIAL_EXPRESSION, true, parser -> {
            attempts.add(parser);
            modes.add(parser.getAsBoolean(Feature.allowComplexParsing));
            parser.withSquareBracketQuotation(true);
        });

        assertEquals("[customer].[id] = 1", result.toString());
        assertEquals(List.of(false, true), modes);
        assertNotSame(attempts.get(0), attempts.get(1));
    }

    @ParameterizedTest
    @EnumSource(ParserApi.class)
    void rejectsTrailingTokensWhenPartialParsingIsDisabled(ParserApi api) {
        JSQLParserException exception = assertThrows(JSQLParserException.class,
                () -> api.parse(PARTIAL_EXPRESSION, false,
                        parser -> parser.withSquareBracketQuotation(true)));

        assertEquals("could only parse partial expression [customer].[id] = 1",
                exception.getMessage());
        assertNull(exception.getCause());
    }

    @ParameterizedTest
    @EnumSource(ParserApi.class)
    void preservesSyntaxFailureCauses(ParserApi api) {
        for (boolean partial : new boolean[] {false, true}) {
            JSQLParserException exception = assertThrows(JSQLParserException.class,
                    () -> api.parse(";", partial, null));

            ParseException cause = assertInstanceOf(ParseException.class, exception.getCause());
            assertEquals(cause.getMessage(), exception.getMessage());
        }
    }

    @ParameterizedTest
    @EnumSource(ParserApi.class)
    void leavesNullAndEmptyInputUnconfigured(ParserApi api) throws JSQLParserException {
        Consumer<CCJSqlParser> unexpectedConfiguration = parser -> {
            throw new AssertionError("Empty input must not configure a parser");
        };
        for (boolean partial : new boolean[] {false, true}) {
            assertNull(api.parse(null, partial, unexpectedConfiguration));
            assertNull(api.parse("", partial, unexpectedConfiguration));
        }
    }

    @ParameterizedTest
    @EnumSource(ParserApi.class)
    void acceptsNullConfiguration(ParserApi api) throws JSQLParserException {
        assertEquals("x = 92", api.parse("x=92", false, null).toString());
        assertEquals("x = 92", api.parse("x=92 trailing tokens", true, null).toString());
    }

    @ParameterizedTest
    @EnumSource(ParserApi.class)
    void propagatesConfigurationFailuresWithoutRetrying(ParserApi api) {
        IllegalStateException failure = new IllegalStateException("Configuration failed");
        List<CCJSqlParser> attempts = new ArrayList<>();
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> api.parse("x=92", false, parser -> {
                    attempts.add(parser);
                    throw failure;
                }));

        assertSame(failure, exception);
        assertEquals(1, attempts.size());
    }

    private enum ParserApi {
        EXPRESSION, CONDITION;

        Expression parse(String text, boolean allowPartialParse, Consumer<CCJSqlParser> consumer)
                throws JSQLParserException {
            return this == EXPRESSION
                    ? CCJSqlParserUtil.parseExpression(text, allowPartialParse, consumer)
                    : CCJSqlParserUtil.parseCondExpression(text, allowPartialParse, consumer);
        }
    }
}
