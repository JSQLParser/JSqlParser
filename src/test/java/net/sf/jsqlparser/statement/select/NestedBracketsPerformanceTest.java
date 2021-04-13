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

import java.util.logging.Logger;
import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import net.sf.jsqlparser.JSQLParserException;

/**
 *
 * @author tw
 */
public class NestedBracketsPerformanceTest {

    @Test
    public void testIssue766() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat('1','2'),'3'),'4'),'5'),'6'),'7'),'8'),'9'),'10'),'11'),'12'),'13'),'14'),'15'),'16'),'17'),'18'),'19'),'20'),'21'),col1 FROM tbl t1", true);
    }

    @Test
    public void testIssue766_2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT concat(concat(concat('1', '2'), '3'), '4'), col1 FROM tbl t1");
    }

    @Test
    public void testIssue235() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT CASE WHEN ( CASE WHEN ( CASE WHEN ( CASE WHEN ( 1 ) THEN 0 END ) THEN 0 END ) THEN 0 END ) THEN 0 END FROM a", true);
    }

    @Test(timeout = 100000)
    @Ignore
    public void testIssue496() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("select isNull(charLen(TEST_ID,0)+ isNull(charLen(TEST_DVC,0)+ isNull(charLen(TEST_NO,0)+ isNull(charLen(ATEST_ID,0)+ isNull(charLen(TESTNO,0)+ isNull(charLen(TEST_CTNT,0)+ isNull(charLen(TEST_MESG_CTNT,0)+ isNull(charLen(TEST_DTM,0)+ isNull(charLen(TEST_DTT,0)+ isNull(charLen(TEST_ADTT,0)+ isNull(charLen(TEST_TCD,0)+ isNull(charLen(TEST_PD,0)+ isNull(charLen(TEST_VAL,0)+ isNull(charLen(TEST_YN,0)+ isNull(charLen(TEST_DTACM,0)+ isNull(charLen(TEST_MST,0) from test_info_m");
    }

    @Test
    public void testIssue856() throws JSQLParserException {
        String sql = "SELECT " + buildRecursiveBracketExpression("if(month(today()) = 3, sum(\"Table5\".\"Month 002\"), $1)", "0", 5) + " FROM mytbl";
        assertSqlCanBeParsedAndDeparsed(sql);
    }

    @Test
    public void testRecursiveBracketExpressionIssue1019() {
        assertEquals("IF(1=1, 1, 2)", buildRecursiveBracketExpression("IF(1=1, $1, 2)", "1", 0));
        assertEquals("IF(1=1, IF(1=1, 1, 2), 2)", buildRecursiveBracketExpression("IF(1=1, $1, 2)", "1", 1));
        assertEquals("IF(1=1, IF(1=1, IF(1=1, 1, 2), 2), 2)", buildRecursiveBracketExpression("IF(1=1, $1, 2)", "1", 2));
    }

    @Test
    public void testRecursiveBracketExpressionIssue1019_2() throws JSQLParserException {
        doIncreaseOfParseTimeTesting("IF(1=1, $1, 2)", "1", 10);
    }

    @Test
    public void testIssue1013() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT ((((((((((((((((tblA)))))))))))))))) FROM mytable");
    }

    @Test
    public void testIssue1013_2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM ((((((((((((((((tblA))))))))))))))))");
    }

    @Test
    public void testIssue1013_3() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM (((tblA)))");
    }

    @Test
    public void testIssue1013_4() throws JSQLParserException {
        String s = "tblA";
        for (int i = 1; i < 100; i++) {
            s = "(" + s + ")";
        }
        String sql = "SELECT * FROM " + s;
        LOG.info("testing " + sql);
        assertSqlCanBeParsedAndDeparsed(sql);
    }
    private static final Logger LOG = Logger.getLogger(NestedBracketsPerformanceTest.class.getName());

    /**
     * Try to avoid or border exceptionally big parsing time increments by
     * adding more bracket constructs.
     *
     * @throws JSQLParserException
     */
    @Test
    public void testIncreaseOfParseTime() throws JSQLParserException {
        doIncreaseOfParseTimeTesting("concat($1,'B')", "'A'", 20);
    }

    private void doIncreaseOfParseTimeTesting(String template, String finalExpression, int maxDepth) throws JSQLParserException {
        long oldDurationTime = 1000;
        int countProblematic = 0;
        for (int i = 0; i < maxDepth; i++) {
            String sql = "SELECT " + buildRecursiveBracketExpression(template, finalExpression, i) + " FROM mytbl";
            long startTime = System.currentTimeMillis();
            assertSqlCanBeParsedAndDeparsed(sql, true);
            long durationTime = System.currentTimeMillis() - startTime;

            if (i > 0) {
                System.out.println("old duration " + oldDurationTime + " new duration time " + durationTime + " for " + sql);
            }
            if (oldDurationTime * 10 < durationTime) {
                countProblematic++;
            }
            if (countProblematic > 5) {
                fail("too large increment of parsing time");
            }

            oldDurationTime = Math.max(durationTime, 1);
        }
    }

    @Test
    public void testRecursiveBracketExpression() {
        assertEquals("concat('A','B')", buildRecursiveBracketExpression("concat($1,'B')", "'A'", 0));
        assertEquals("concat(concat('A','B'),'B')", buildRecursiveBracketExpression("concat($1,'B')", "'A'", 1));
        assertEquals("concat(concat(concat('A','B'),'B'),'B')", buildRecursiveBracketExpression("concat($1,'B')", "'A'", 2));
    }

    private String buildRecursiveBracketExpression(String template, String finalExpression, int depth) {
        if (depth == 0) {
            return template.replace("$1", finalExpression);
        }
        return template.replace("$1", buildRecursiveBracketExpression(template, finalExpression, depth - 1));
    }
}
