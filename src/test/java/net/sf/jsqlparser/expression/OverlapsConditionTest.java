/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2022 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Test;

public class OverlapsConditionTest {

    @Test
    public void testOverlapsCondition() throws JSQLParserException {
        TestUtils.assertExpressionCanBeParsedAndDeparsed("(t1.start, t1.end) overlaps (t2.start, t2.end)", true);
        TestUtils.assertSqlCanBeParsedAndDeparsed("select * from dual where (start_one, end_one) overlaps (start_two, end_two)", true);
        TestUtils.assertSqlCanBeParsedAndDeparsed("select * from t1 left join t2 on (t1.start, t1.end) overlaps (t2.start, t2.end)", true);
    }
}
