/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression.operators.relational;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.NotExpression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TruthValuePredicateTest {

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "a > 0 IS TRUE | false | true",
            "a IN (1, 2) IS FALSE | false | false",
            "a IS NULL IS NOT TRUE | true | true",
            "a > 0 IS NOT FALSE | true | false",
            "a BETWEEN 1 AND 2 IS TRUE | false | true",
            "a LIKE 'x%' IS FALSE | false | false",
            "EXISTS (SELECT 1 FROM t) IS TRUE | false | true",
            "a IS TRUE | false | true",
            "a IS FALSE | false | false",
            "a IS NOT TRUE | true | true",
            "a IS NOT FALSE | true | false",
            "(a > 0) IS TRUE | false | true"
    })
    void booleanTestWrapsPredicate(String condition, boolean not, boolean isTrue)
            throws JSQLParserException {
        PlainSelect select = (PlainSelect) TestUtils
                .assertSqlCanBeParsedAndDeparsed("SELECT * FROM t WHERE " + condition);
        IsBooleanExpression expression =
                assertInstanceOf(IsBooleanExpression.class, select.getWhere());
        assertEquals(not, expression.isNot());
        assertEquals(isTrue, expression.isTrue());
        assertEquals(condition.substring(0, condition.lastIndexOf(" IS ")),
                expression.getLeftExpression().toString());
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "a > 0 IS UNKNOWN | false",
            "a IN (1, 2) IS NOT UNKNOWN | true",
            "a IS NULL IS UNKNOWN | false",
            "a IS UNKNOWN | false",
            "a IS NOT UNKNOWN | true"
    })
    void unknownTestWrapsPredicate(String condition, boolean not) throws JSQLParserException {
        PlainSelect select = (PlainSelect) TestUtils
                .assertSqlCanBeParsedAndDeparsed("SELECT * FROM t WHERE " + condition);
        IsUnknownExpression expression =
                assertInstanceOf(IsUnknownExpression.class, select.getWhere());
        assertEquals(not, expression.isNot());
        assertEquals(condition.substring(0, condition.lastIndexOf(" IS ")),
                expression.getLeftExpression().toString());
    }

    @Test
    void truthValueTestBindsBeforeLogicalOperators() throws JSQLParserException {
        PlainSelect select = (PlainSelect) TestUtils.assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM t WHERE a > 0 IS TRUE AND b IN (1, 2) IS FALSE OR c IS NULL");
        OrExpression or = assertInstanceOf(OrExpression.class, select.getWhere());
        AndExpression and = assertInstanceOf(AndExpression.class, or.getLeftExpression());
        IsBooleanExpression left =
                assertInstanceOf(IsBooleanExpression.class, and.getLeftExpression());
        assertInstanceOf(GreaterThan.class, left.getLeftExpression());
        IsBooleanExpression right =
                assertInstanceOf(IsBooleanExpression.class, and.getRightExpression());
        assertInstanceOf(InExpression.class, right.getLeftExpression());
        assertInstanceOf(IsNullExpression.class, or.getRightExpression());
    }

    @Test
    void leadingNotWrapsTruthValueTest() throws JSQLParserException {
        PlainSelect select = (PlainSelect) TestUtils.assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM t WHERE NOT a > 0 IS TRUE");
        NotExpression not = assertInstanceOf(NotExpression.class, select.getWhere());
        IsBooleanExpression expression =
                assertInstanceOf(IsBooleanExpression.class, not.getExpression());
        assertInstanceOf(GreaterThan.class, expression.getLeftExpression());
    }

    @Test
    void selectItemAcceptsTruthValueTest() throws JSQLParserException {
        PlainSelect select = (PlainSelect) TestUtils
                .assertSqlCanBeParsedAndDeparsed("SELECT a > 0 IS TRUE FROM t");
        Expression expression = select.getSelectItem(0).getExpression();
        IsBooleanExpression predicate = assertInstanceOf(IsBooleanExpression.class, expression);
        assertInstanceOf(GreaterThan.class, predicate.getLeftExpression());
    }

    @ParameterizedTest
    @ValueSource(strings = {"a > 0 IS", "a > 0 IS NOT", "a > 0 IS MAYBE"})
    void invalidTruthValueTestIsRejected(String condition) {
        assertThrows(JSQLParserException.class, () -> TestUtils
                .assertSqlCanBeParsedAndDeparsed("SELECT * FROM t WHERE " + condition));
    }

    @ParameterizedTest
    @ValueSource(strings = {"a > 0 IS MAYBE", "a > 0 IS NULL"})
    void partialParsingLeavesUnrecognizedSuffix(String condition) throws JSQLParserException {
        assertEquals("a > 0", CCJSqlParserUtil.parseExpression(condition).toString());
        assertEquals("a > 0", CCJSqlParserUtil.parseCondExpression(condition).toString());
    }
}
