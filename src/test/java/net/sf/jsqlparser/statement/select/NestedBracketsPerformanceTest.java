/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.Test;

/**
 *
 * @author tw
 */
public class NestedBracketsPerformanceTest {

    @Test
    public void testIssue766() throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed("SELECT concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat('1','2'),'3'),'4'),'5'),'6'),'7'),'8'),'9'),'10'),'11'),'12'),'13'),'14'),'15'),'16'),'17'),'18'),'19'),'20'),'21'),col1 FROM tbl t1", true);
    }

    @Test
    public void testIssue766_2() throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed("SELECT concat(concat(concat('1', '2'), '3'), '4'), col1 FROM tbl t1");
    }
}
