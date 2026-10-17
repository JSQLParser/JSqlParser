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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Stream;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.AllValue;
import net.sf.jsqlparser.expression.BooleanValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.OracleNamedFunctionParameter;
import net.sf.jsqlparser.expression.OverlapsCondition;
import net.sf.jsqlparser.expression.PostgresNamedFunctionParameter;
import net.sf.jsqlparser.expression.TimeKeyExpression;
import net.sf.jsqlparser.expression.operators.relational.Between;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExistsExpression;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.SupportsOldOracleJoinSyntax;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.PlainSelect;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class LookaheadDispatchTest {
    @ParameterizedTest
    @MethodSource("singleTokenExpressions")
    void preservesSingleTokenExpressionKinds(String sql, Class<? extends Expression> type,
            String deparsed) throws Exception {
        CCJSqlParser parser = CCJSqlParserUtil.newParser(sql);
        Expression expression = parser.PrimaryExpression();
        assertInstanceOf(type, expression);
        assertEquals(deparsed, expression.toString());
        assertEquals(CCJSqlParserConstants.EOF, parser.getNextToken().kind);
        Expression parsed = CCJSqlParserUtil.parseExpression(sql, false);
        assertInstanceOf(type, parsed);
        assertEquals(deparsed, parsed.toString());
    }

    static Stream<Arguments> singleTokenExpressions() {
        return Stream.of(
                Arguments.of("TRUE", BooleanValue.class, "true"),
                Arguments.of("FALSE", BooleanValue.class, "false"),
                Arguments.of("ALL", AllValue.class, "ALL"),
                Arguments.of("CURRENT", TimeKeyExpression.class, "CURRENT"),
                Arguments.of("CURRENT_DATE", TimeKeyExpression.class, "CURRENT_DATE"),
                Arguments.of("CURRENT_TIME", TimeKeyExpression.class, "CURRENT_TIME"),
                Arguments.of("CURRENT_TIMESTAMP", TimeKeyExpression.class, "CURRENT_TIMESTAMP"),
                Arguments.of("CURRENT_TIMEZONE", TimeKeyExpression.class, "CURRENT_TIMEZONE"),
                Arguments.of("CURRENT_DATE()", TimeKeyExpression.class, "CURRENT_DATE()"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"\"TRUE\"", "\"CURRENT_DATE\"", "\"ALL\""})
    void preservesQuotedNames(String name) throws JSQLParserException {
        PlainSelect select = (PlainSelect) assertSqlCanBeParsedAndDeparsed("SELECT " + name);
        assertEquals(name, assertInstanceOf(Column.class, select.getSelectItem(0).getExpression())
                .getColumnName());
    }

    @ParameterizedTest
    @ValueSource(strings = {"TRUE", "ALL", "CURRENT_DATE"})
    void honorsInterruptionBeforeSingleTokenExpressions(String sql) {
        CCJSqlParser parser = CCJSqlParserUtil.newParser(sql);
        parser.interrupted = true;
        assertThrows(ParseException.class, parser::PrimaryExpression);
        assertEquals(sql, parser.getNextToken().image);
    }

    @ParameterizedTest
    @MethodSource("conditions")
    void preservesConditionAlternatives(String sql, Class<? extends Expression> type)
            throws Exception {
        CCJSqlParser conditionParser = CCJSqlParserUtil.newParser(sql);
        Expression condition = conditionParser.Condition();
        assertInstanceOf(type, condition);
        assertEquals(sql, condition.toString());
        assertEquals(CCJSqlParserConstants.EOF, conditionParser.getNextToken().kind);

        CCJSqlParser sqlConditionParser = CCJSqlParserUtil.newParser(sql);
        Expression sqlCondition = sqlConditionParser.SQLCondition();
        assertInstanceOf(type, sqlCondition);
        assertEquals(sql, sqlCondition.toString());
        assertEquals(CCJSqlParserConstants.EOF, sqlConditionParser.getNextToken().kind);

        PlainSelect select = (PlainSelect) assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM t WHERE " + sql);
        assertInstanceOf(type, select.getWhere());
    }

    static Stream<Arguments> conditions() {
        return Stream.of(
                Arguments.of("a", Column.class),
                Arguments.of("exists", Column.class),
                Arguments.of("exists.col", Column.class),
                Arguments.of("EXISTS (SELECT 1)", ExistsExpression.class),
                Arguments.of("EXISTS (1)", ExistsExpression.class),
                Arguments.of("a IN (1, 2)", InExpression.class),
                Arguments.of("a BETWEEN 1 AND 2", Between.class),
                Arguments.of("a IS NULL", IsNullExpression.class),
                Arguments.of("(a, b) OVERLAPS (c, d)", OverlapsCondition.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", "exists", "exists.col"})
    void preservesComparisons(String column) throws Exception {
        String sql = column + " = 1";
        CCJSqlParser parser = CCJSqlParserUtil.newParser(sql);
        EqualsTo condition = assertInstanceOf(EqualsTo.class, parser.Condition());
        assertEquals(column, assertInstanceOf(Column.class, condition.getLeftExpression())
                .getFullyQualifiedName());
        assertEquals(sql, condition.toString());
        assertEquals(CCJSqlParserConstants.EOF, parser.getNextToken().kind);
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM t WHERE " + sql);
    }

    @Test
    void preservesOraclePriorPosition() throws Exception {
        CCJSqlParser parser = CCJSqlParserUtil.newParser("PRIOR a = b");
        EqualsTo condition = assertInstanceOf(EqualsTo.class, parser.Condition());
        assertEquals(SupportsOldOracleJoinSyntax.ORACLE_PRIOR_START,
                condition.getOraclePriorPosition());
        assertEquals("a", assertInstanceOf(Column.class, condition.getLeftExpression())
                .getColumnName());
        assertEquals("b", assertInstanceOf(Column.class, condition.getRightExpression())
                .getColumnName());
        assertEquals(CCJSqlParserConstants.EOF, parser.getNextToken().kind);
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM t CONNECT BY PRIOR a = b");
    }

    @ParameterizedTest
    @ValueSource(strings = {"a IN (", "a BETWEEN 1 AND", "EXISTS (SELECT",
            "(a, b) OVERLAPS ("})
    void rejectsMalformedConditions(String sql) {
        assertThrows(ParseException.class, () -> CCJSqlParserUtil.newParser(sql).Condition());
        assertThrows(ParseException.class, () -> CCJSqlParserUtil.newParser(sql).SQLCondition());
        assertThrows(JSQLParserException.class,
                () -> CCJSqlParserUtil.parse("SELECT * FROM t WHERE " + sql));
    }

    @ParameterizedTest
    @ValueSource(strings = {"a =", "exists ="})
    void rejectsMalformedComparisons(String sql) {
        assertThrows(ParseException.class, () -> CCJSqlParserUtil.newParser(sql).Condition());
        assertThrows(JSQLParserException.class,
                () -> CCJSqlParserUtil.parse("SELECT * FROM t WHERE " + sql));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void honorsInterruptionBeforeConditionDispatch(boolean sqlCondition) {
        CCJSqlParser parser = CCJSqlParserUtil.newParser("1");
        parser.interrupted = true;
        assertThrows(ParseException.class, () -> {
            if (sqlCondition) {
                parser.SQLCondition();
            } else {
                parser.Condition();
            }
        });
        assertEquals("1", parser.getNextToken().image);
    }

    @ParameterizedTest
    @CsvSource(value = {"arg|=>", "arg|:=", "OUTER|=>", "OUTER|:=", "\"arg\"|=>",
            "\"arg\"|:=", "overlaps|=>", "overlaps|:="}, delimiter = '|')
    void preservesNamedParameterKindsInEveryArgumentPosition(String name, String assignment)
            throws JSQLParserException {
        for (String prefix : new String[] {"", "a.b, "}) {
            String sql = "SELECT f(" + prefix + name + " " + assignment + " value_col)";
            PlainSelect select = (PlainSelect) assertSqlCanBeParsedAndDeparsed(sql);
            Function function = assertInstanceOf(Function.class,
                    select.getSelectItem(0).getExpression());
            int index = prefix.isEmpty() ? 0 : 1;
            assertEquals(index + 1, function.getParameters().size());
            if (index == 1) {
                assertEquals("a.b", assertInstanceOf(Column.class,
                        function.getParameters().get(0)).getFullyQualifiedName());
            }
            Expression argument = function.getParameters().get(index);
            Expression value;
            if ("=>".equals(assignment)) {
                OracleNamedFunctionParameter parameter =
                        assertInstanceOf(OracleNamedFunctionParameter.class, argument);
                assertEquals(name, parameter.getName());
                value = parameter.getExpression();
            } else {
                PostgresNamedFunctionParameter parameter =
                        assertInstanceOf(PostgresNamedFunctionParameter.class, argument);
                assertEquals(name, parameter.getName());
                value = parameter.getExpression();
            }
            assertEquals("value_col", assertInstanceOf(Column.class, value).getColumnName());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"a.b => 1", "a.b := 1", "1 => 1", "1 := 1", "'a' => 1",
            "'a' := 1", "arg =>", "arg :="})
    void rejectsInvalidNamedParametersInEveryArgumentPosition(String argument) {
        for (String prefix : new String[] {"", "a.b, "}) {
            assertThrows(JSQLParserException.class,
                    () -> CCJSqlParserUtil.parse("SELECT f(" + prefix + argument + ")"));
        }
    }
}
