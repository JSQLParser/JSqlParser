/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2023 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression.operators.relational;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseAnd;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;


class ComparisonOperatorTest {

    @Test
    public void testDoubleAnd() throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed("SELECT * FROM foo WHERE a && b");
        Assertions.assertInstanceOf(DoubleAnd.class, CCJSqlParserUtil.parseExpression("a && b"));
    }

    @Test
    public void testContains() throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed("SELECT * FROM foo WHERE a &> b");
        Assertions.assertInstanceOf(Contains.class, CCJSqlParserUtil.parseExpression("a &> b"));
    }

    @Test
    public void testContainedBy() throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed("SELECT * FROM foo WHERE a <& b");
        Assertions.assertInstanceOf(ContainedBy.class, CCJSqlParserUtil.parseExpression("a <& b"));
    }

    @Test
    void testCosineSimilarity() throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(
                "SELECT (embedding <=> '[3,1,2]') AS cosine_similarity FROM items;");
        Assertions.assertInstanceOf(CosineSimilarity.class,
                CCJSqlParserUtil.parseExpression("embedding <=> '[3,1,2]'"));
    }

    @Test
    void testVectorDistanceOperatorsInProjection() throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed("SELECT a <-> b FROM t");
        TestUtils.assertSqlCanBeParsedAndDeparsed("SELECT a <#> b FROM t");
        TestUtils.assertSqlCanBeParsedAndDeparsed("SELECT a <=> b FROM t");
        Assertions.assertInstanceOf(GeometryDistance.class,
                CCJSqlParserUtil.parseExpression("a <-> b"));
        Assertions.assertInstanceOf(GeometryDistance.class,
                CCJSqlParserUtil.parseExpression("a <#> b"));
        Assertions.assertInstanceOf(CosineSimilarity.class,
                CCJSqlParserUtil.parseExpression("a <=> b"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"<->", "<#>", "<=>"})
    void testVectorDistanceOperatorsFollowedByComparison(String distance)
            throws JSQLParserException {
        String[] comparators = {"<", ">", "="};
        for (String comparator : comparators) {
            TestUtils.assertSqlCanBeParsedAndDeparsed(
                    "SELECT * FROM t WHERE a " + distance + " b " + comparator + " c");
            TestUtils.assertSqlCanBeParsedAndDeparsed(
                    "SELECT * FROM t WHERE (a " + distance + " b) " + comparator + " c");
        }
        Assertions.assertInstanceOf(MinorThan.class,
                CCJSqlParserUtil.parseExpression("a " + distance + " b < c"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"<->", "<#>", "<=>"})
    void testVectorDistanceOperatorsInProjectionFollowedByComparison(String distance)
            throws JSQLParserException {
        String[] comparators = {"<", ">", "="};
        for (String comparator : comparators) {
            TestUtils.assertSqlCanBeParsedAndDeparsed(
                    "SELECT a " + distance + " b " + comparator + " c FROM t");
            TestUtils.assertSqlCanBeParsedAndDeparsed(
                    "SELECT (a " + distance + " b) " + comparator + " c FROM t");
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"<->", "<#>", "<=>"})
    void testVectorDistanceOperatorsShareBitwisePrecedence(String distance)
            throws JSQLParserException {
        Class<?> distanceType =
                "<=>".equals(distance) ? CosineSimilarity.class : GeometryDistance.class;

        TestUtils.assertSqlCanBeParsedAndDeparsed("SELECT a " + distance + " b & c FROM t");
        TestUtils.assertSqlCanBeParsedAndDeparsed("SELECT a & b " + distance + " c FROM t");

        BinaryExpression bitwiseRoot =
                (BinaryExpression) CCJSqlParserUtil.parseExpression("a " + distance + " b & c");
        Assertions.assertInstanceOf(BitwiseAnd.class, bitwiseRoot);
        Assertions.assertInstanceOf(distanceType, bitwiseRoot.getLeftExpression());

        BinaryExpression distanceRoot =
                (BinaryExpression) CCJSqlParserUtil.parseExpression("a & b " + distance + " c");
        Assertions.assertInstanceOf(distanceType, distanceRoot);
        Assertions.assertInstanceOf(BitwiseAnd.class, distanceRoot.getLeftExpression());
    }

    @Test
    void testGeometryDistanceOrderByControl() throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed("SELECT * FROM t ORDER BY a <-> b");
        TestUtils.assertSqlCanBeParsedAndDeparsed("SELECT * FROM t ORDER BY a <#> b");
    }

    @Test
    void testVectorDistancePrecedenceAboveComparisonBelowArithmetic() throws JSQLParserException {
        BinaryExpression geometryComparison = Assertions.assertInstanceOf(MinorThan.class,
                CCJSqlParserUtil.parseExpression("a <-> b + c < 5"));
        GeometryDistance geometryDistance = Assertions.assertInstanceOf(GeometryDistance.class,
                geometryComparison.getLeftExpression());
        Assertions.assertInstanceOf(Addition.class, geometryDistance.getRightExpression());

        BinaryExpression cosineComparison = Assertions.assertInstanceOf(MinorThan.class,
                CCJSqlParserUtil.parseExpression("a <=> b + c < 5"));
        CosineSimilarity cosineSimilarity = Assertions.assertInstanceOf(CosineSimilarity.class,
                cosineComparison.getLeftExpression());
        Assertions.assertInstanceOf(Addition.class, cosineSimilarity.getRightExpression());
    }

    @Test
    void testComparisonChainsRemainInvalid() {
        Assertions.assertThrows(JSQLParserException.class,
                () -> CCJSqlParserUtil.parse("SELECT * FROM t WHERE a < b < c"));
        Assertions.assertThrows(JSQLParserException.class,
                () -> CCJSqlParserUtil.parse("SELECT * FROM t WHERE a <-> b < c < d"));
    }
}
