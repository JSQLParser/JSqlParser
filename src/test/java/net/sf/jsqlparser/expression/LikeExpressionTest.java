/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2021 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Test;

/**
 *
 * @author <a href="mailto:andreas@manticore-projects.com">Andreas Reichel</a>
 */
public class LikeExpressionTest {

    @Test
    public void testLikeWithEscapeExpressionIssue420() throws JSQLParserException {
        TestUtils.assertExpressionCanBeParsedAndDeparsed("a LIKE ?1 ESCAPE ?2", true);

        TestUtils.assertSqlCanBeParsedAndDeparsed("select * from dual where a LIKE ?1 ESCAPE ?2", true);
    }
}
