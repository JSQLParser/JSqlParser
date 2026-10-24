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
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


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
}
