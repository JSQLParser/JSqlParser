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
import org.junit.Ignore;
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

    @Test
    public void testIssue235() throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed("SELECT CASE WHEN ( CASE WHEN ( CASE WHEN ( CASE WHEN ( 1 ) THEN 0 END ) THEN 0 END ) THEN 0 END ) THEN 0 END FROM a", true);
    }

    @Test(timeout = 100000)
    @Ignore
    public void testIssue496() throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed("select isNull(charLen(TEST_ID,0)+ isNull(charLen(TEST_DVC,0)+ isNull(charLen(TEST_NO,0)+ isNull(charLen(ATEST_ID,0)+ isNull(charLen(TESTNO,0)+ isNull(charLen(TEST_CTNT,0)+ isNull(charLen(TEST_MESG_CTNT,0)+ isNull(charLen(TEST_DTM,0)+ isNull(charLen(TEST_DTT,0)+ isNull(charLen(TEST_ADTT,0)+ isNull(charLen(TEST_TCD,0)+ isNull(charLen(TEST_PD,0)+ isNull(charLen(TEST_VAL,0)+ isNull(charLen(TEST_YN,0)+ isNull(charLen(TEST_DTACM,0)+ isNull(charLen(TEST_MST,0) from test_info_m");
    }
}
