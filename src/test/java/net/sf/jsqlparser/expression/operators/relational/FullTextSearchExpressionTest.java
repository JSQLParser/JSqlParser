/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2021 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression.operators.relational;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:andreas@manticore-projects.com">Andreas Reichel</a>
 */
public class FullTextSearchExpressionTest {

    @Test
    public void testFullTextSearchExpressionWithParameters() throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed("select match (name) against (?) as full_text from commodity", true);
        TestUtils.assertSqlCanBeParsedAndDeparsed("select match (name) against (:parameter) as full_text from commodity", true);
    }

    @Test
    public void testIssue1223() throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed("select\n" + "c.*,\n" + "match (name) against (?) as full_text\n" + "from\n" + "commodity c\n" + "where\n" + "match (name) against (?)\n" + "and c.deleted = 0\n" + "order by\n" + "full_text desc", true);
    }
}
