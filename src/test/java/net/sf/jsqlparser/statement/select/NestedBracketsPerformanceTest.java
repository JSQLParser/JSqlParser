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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.logging.Logger;

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 *
 * @author tw
 */
public class NestedBracketsPerformanceTest {

    private static final Logger LOG =
            Logger.getLogger(NestedBracketsPerformanceTest.class.getName());

    @Test
    @Timeout(2000)
    public void testIssue766() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat('1','2'),'3'),'4'),'5'),'6'),'7'),'8'),'9'),'10'),'11'),'12'),'13'),'14'),'15'),'16'),'17'),'18'),'19'),'20'),'21'),col1 FROM tbl t1",
                true);
    }

    @Test
    @Timeout(2000)
    public void testIssue766_2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT concat(concat(concat('1', '2'), '3'), '4'), col1 FROM tbl t1");
    }

    @Test
    @Timeout(2000)
    public void testIssue235() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT CASE WHEN ( CASE WHEN ( CASE WHEN ( CASE WHEN ( 1 ) THEN 0 END ) THEN 0 END ) THEN 0 END ) THEN 0 END FROM a",
                true);
    }

    @Test
    @Timeout(2000)
    public void testNestedCaseWhenWithoutBracketsIssue1162() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE VIEW VIEW_NAME1 AS\n" + "SELECT CASE\n"
                + "WHEN WDGFLD.PORTTYPE = 1 THEN 'INPUT PORT'\n" + "ELSE CASE\n"
                + "WHEN WDGFLD.PORTTYPE = 1 THEN 'INPUT PORT'\n" + "ELSE CASE\n"
                + "WHEN WDGFLD.PORTTYPE = 1 THEN 'INPUT PORT'\n" + "ELSE CASE\n"
                + "WHEN WDGFLD.PORTTYPE = 1 THEN 'INPUT PORT'\n" + "ELSE CASE\n"
                + "WHEN WDGFLD.PORTTYPE = 1 THEN 'INPUT PORT'\n" + "ELSE CASE\n"
                + "WHEN WDGFLD.PORTTYPE = 1 THEN 'INPUT PORT'\n" + "ELSE CASE\n"
                + "WHEN WDGFLD.PORTTYPE = 1 THEN 'INPUT PORT'\n" + "ELSE CASE\n"
                + "WHEN WDGFLD.PORTTYPE = 1 THEN 'INPUT PORT'\n" + "ELSE CASE\n"
                + "WHEN WDGFLD.PORTTYPE = 1 THEN 'INPUT PORT'\n" + "ELSE CASE\n"
                + "WHEN WDGFLD.PORTTYPE = 1 THEN 'INPUT PORT'\n" + "ELSE CASE\n"
                + "WHEN WDGFLD.PORTTYPE = 1 THEN 'INPUT PORT'\n" + "ELSE CASE\n"
                + "WHEN WDGFLD.PORTTYPE = 1 THEN 'INPUT PORT'\n" + "ELSE CASE\n"
                + "WHEN WDGFLD.PORTTYPE = 1 THEN 'INPUT PORT'\n"
                + "ELSE CASE WHEN WDGFLD.PORTTYPE = 1 THEN 'INPUT PORT' ELSE '0' END END END END END END END END END END END END END END COLUMNALIAS\n"
                + "FROM TABLE1", true);
    }

    @Test
    @Timeout(2000)
    public void testNestedCaseWhenWithBracketsIssue1162() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE VIEW VIEW_NAME1 AS\n" + "SELECT CASE\n"
                + "WHEN WDGFLD.PORTTYPE = 1 THEN 'INPUT PORT'\n" + "ELSE (CASE\n"
                + "WHEN WDGFLD.PORTTYPE = 1 THEN 'INPUT PORT'\n" + "ELSE (CASE\n"
                + "WHEN WDGFLD.PORTTYPE = 1 THEN 'INPUT PORT'\n" + "ELSE (CASE\n"
                + "WHEN WDGFLD.PORTTYPE = 1 THEN 'INPUT PORT'\n" + "ELSE (CASE\n"
                + "WHEN WDGFLD.PORTTYPE = 1 THEN 'INPUT PORT'\n" + "ELSE (CASE\n"
                + "WHEN WDGFLD.PORTTYPE = 1 THEN 'INPUT PORT'\n" + "ELSE (CASE\n"
                + "WHEN WDGFLD.PORTTYPE = 1 THEN 'INPUT PORT'\n" + "ELSE (CASE\n"
                + "WHEN WDGFLD.PORTTYPE = 1 THEN 'INPUT PORT'\n" + "ELSE (CASE\n"
                + "WHEN WDGFLD.PORTTYPE = 1 THEN 'INPUT PORT'\n" + "ELSE (CASE\n"
                + "WHEN WDGFLD.PORTTYPE = 1 THEN 'INPUT PORT'\n" + "ELSE (CASE\n"
                + "WHEN WDGFLD.PORTTYPE = 1 THEN 'INPUT PORT'\n" + "ELSE (CASE\n"
                + "WHEN WDGFLD.PORTTYPE = 1 THEN 'INPUT PORT'\n" + "ELSE (CASE\n"
                + "WHEN WDGFLD.PORTTYPE = 1 THEN 'INPUT PORT'\n"
                + "ELSE (CASE WHEN WDGFLD.PORTTYPE = 1 THEN 'INPUT PORT' ELSE '0' END) END) END) END) END) END) END) END) END) END) END) END) END) END COLUMNALIAS\n"
                + "FROM TABLE1", true);
    }

    @Test
    @Timeout(2000)
    @Disabled
    public void testIssue496() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "select isNull(charLen(TEST_ID,0)+ isNull(charLen(TEST_DVC,0)+ isNull(charLen(TEST_NO,0)+ isNull(charLen(ATEST_ID,0)+ isNull(charLen(TESTNO,0)+ isNull(charLen(TEST_CTNT,0)+ isNull(charLen(TEST_MESG_CTNT,0)+ isNull(charLen(TEST_DTM,0)+ isNull(charLen(TEST_DTT,0)+ isNull(charLen(TEST_ADTT,0)+ isNull(charLen(TEST_TCD,0)+ isNull(charLen(TEST_PD,0)+ isNull(charLen(TEST_VAL,0)+ isNull(charLen(TEST_YN,0)+ isNull(charLen(TEST_DTACM,0)+ isNull(charLen(TEST_MST,0) from test_info_m",
                true);
    }

    @Test
    // @Todo Investigate performance deterioration since JSQLParser 5.0pre development
    public void testIssue856() throws JSQLParserException {
        String sql = "SELECT "
                + buildRecursiveBracketExpression(
                        "if(month(today()) = 3, sum(\"Table5\".\"Month 002\"), $1)", "0", 3)
                + " FROM mytbl";
        assertSqlCanBeParsedAndDeparsed(sql);
    }

    @Test
    @Timeout(2000)
    public void testRecursiveBracketExpressionIssue1019() {
        assertEquals("IF(1=1, 1, 2)", buildRecursiveBracketExpression("IF(1=1, $1, 2)", "1", 0));
        assertEquals("IF(1=1, IF(1=1, 1, 2), 2)",
                buildRecursiveBracketExpression("IF(1=1, $1, 2)", "1", 1));
        assertEquals("IF(1=1, IF(1=1, IF(1=1, 1, 2), 2), 2)",
                buildRecursiveBracketExpression("IF(1=1, $1, 2)", "1", 2));
    }

    // maxDepth = 10 collides with the Parser Timeout = 6 seconds
    // temporarily restrict it to maxDepth = 4 for the moment
    // @todo: implement methods to set the Parser Timeout explicitly and on demand
    // @todo Investigate performance deterioration since JSQLParser 5.0pre development
    @Test
    public void testRecursiveBracketExpressionIssue1019_2() throws JSQLParserException {
        doIncreaseOfParseTimeTesting("IF(1=1, $1, 2)", "1", 4);
    }

    @Test
    @Timeout(2000)
    public void testIssue1013() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT ((((((((((((((((tblA)))))))))))))))) FROM mytable");
    }

    @Test
    @Timeout(2000)
    public void testIssue1013_2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM ((((((((((((((((tblA))))))))))))))))");
    }

    @Test
    public void testIssue1013_3() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM (((tblA)))");
    }

    @Test
    @Timeout(2000)
    public void testIssue1013_4() throws JSQLParserException {
        String s = "tblA";
        for (int i = 1; i < 100; i++) {
            s = "(" + s + ")";
        }
        String sql = "SELECT * FROM " + s;
        LOG.info("testing " + sql);
        assertSqlCanBeParsedAndDeparsed(sql);
    }

    /**
     * Try to avoid or border huge parsing time increments by adding more bracket constructs.
     *
     * @throws JSQLParserException
     */
    // @Test(timeout = 6000)
    public void testIncreaseOfParseTime() throws JSQLParserException {
        doIncreaseOfParseTimeTesting("concat($1,'B')", "'A'", 50);
    }

    private void doIncreaseOfParseTimeTesting(String template, String finalExpression, int maxDepth)
            throws JSQLParserException {
        long oldDurationTime = 2000;
        int countProblematic = 0;
        for (int i = 0; i < maxDepth; i++) {
            String sql = "SELECT " + buildRecursiveBracketExpression(template, finalExpression, i)
                    + " FROM mytbl";
            long startTime = System.currentTimeMillis();
            assertSqlCanBeParsedAndDeparsed(sql, true);
            long durationTime = System.currentTimeMillis() - startTime;

            if (i > 0) {
                System.out.println("old duration " + oldDurationTime + " new duration time "
                        + durationTime + " for " + sql);
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
        assertEquals("concat('A','B')",
                buildRecursiveBracketExpression("concat($1,'B')", "'A'", 0));
        assertEquals("concat(concat('A','B'),'B')",
                buildRecursiveBracketExpression("concat($1,'B')", "'A'", 1));
        assertEquals("concat(concat(concat('A','B'),'B'),'B')",
                buildRecursiveBracketExpression("concat($1,'B')", "'A'", 2));
    }

    private String buildRecursiveBracketExpression(String template, String finalExpression,
            int depth) {
        if (depth == 0) {
            return template.replace("$1", finalExpression);
        }
        return template.replace("$1",
                buildRecursiveBracketExpression(template, finalExpression, depth - 1));
    }

    @Test
    @Timeout(2000)
    public void testIssue1103() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT\n" + "ROUND(ROUND(ROUND(ROUND(ROUND(ROUND(ROUND(ROUND(\n"
                        + "ROUND(ROUND(ROUND(ROUND(ROUND(ROUND(ROUND(ROUND(\n"
                        + "ROUND(ROUND(ROUND(ROUND(ROUND(ROUND(ROUND(ROUND(\n"
                        + "ROUND(ROUND(ROUND(ROUND(ROUND(ROUND(ROUND(ROUND(0\n"
                        + ",0),0),0),0),0),0),0),0)\n" + ",0),0),0),0),0),0),0),0)\n"
                        + ",0),0),0),0),0),0),0),0)\n" + ",0),0),0),0),0),0),0),0)",
                true);
    }

    @Test
    public void testDeepFunctionParameters() throws JSQLParserException {
        String sqlStr = "SELECT  a.*\n"
                + "        , To_Char( a.eingangsdat, 'MM.YY' ) AS eingmonat\n"
                + "        , ( SELECT Trim( b.atext )\n"
                + "            FROM masseinheiten x\n"
                + "                , a_lmt b\n"
                + "            WHERE x.a_text_id = b.a_text_id\n"
                + "                AND b.sprach_kz = sprache\n"
                + "                AND x.masseinh_id = a.masseinh_id ) AS reklamengesonst_bez\n"
                + "        , ( SELECT Trim( name ) || ' ' || Trim( vorname ) AS eingangerfasser_name\n"
                + "            FROM personal\n"
                + "            WHERE mandanten_id = m_personal\n"
                + "                AND personal_id = eingangerfasser ) AS eingangerfasser_name\n"
                + "        , Nvl( (    SELECT Max( change_date )\n"
                + "                    FROM besch_statusaenderung\n"
                + "                    WHERE beschwerden_id = a.beschwerden_id\n"
                + "                        AND beschstatus_id = 9\n"
                + "                        AND Nvl( inaktiv, 'F' ) != 'T' ), sysdate ) AS abschlussdatum\n"
                + "        , a.sachstand\n"
                + "        , a.bewertung\n"
                + "        , a.massnahmen\n"
                + "        , ( Decode( Nvl( (  SELECT Max( Trunc( change_date ) ) - Trunc( a.adate )\n"
                + "                            FROM besch_statusaenderung\n"
                + "                            WHERE beschwerden_id = a.beschwerden_id\n"
                + "                                AND beschstatus_id = 9\n"
                + "                                AND Nvl( inaktiv, 'F' ) != 'T' ), - 1 )\n"
                + "                    , - 1, Trunc( sysdate ) - Trunc( a.adate ) - (  SELECT Count()\n"
                + "                                                                    FROM firmenkalender\n"
                + "                                                                    WHERE firma_id = firmen_id\n"
                + "                                                                        AND Nvl( b_verkauf, 'F' ) = 'T'\n"
                + "                                                                        AND kal_datum BETWEEN Trunc( a.adate )\n"
                + "                                                                                             AND Trunc( sysdate ) )\n"
                + "                    , Nvl( (    SELECT Max( Trunc( change_date ) ) - Trunc( a.adate )\n"
                + "                                FROM besch_statusaenderung\n"
                + "                                WHERE beschwerden_id = a.beschwerden_id\n"
                + "                                    AND beschstatus_id = 9\n"
                + "                                    AND Nvl( inaktiv, 'F' ) != 'T' ), - 1 )\n"
                + "                             - (    SELECT Count()\n"
                + "                                    FROM firmenkalender\n"
                + "                                    WHERE firma_id = firmen_id\n"
                + "                                        AND Nvl( b_verkauf, 'F' ) = 'T'\n"
                + "                                        AND kal_datum BETWEEN Trunc( a.adate )\n"
                + "                                                             AND (  SELECT Max( Trunc( change_date ) )\n"
                + "                                                                    FROM besch_statusaenderung\n"
                + "                                                                    WHERE beschwerden_id = a.beschwerden_id\n"
                + "                                                                        AND beschstatus_id = 9\n"
                + "                                                                        AND Nvl( inaktiv, 'F' ) != 'T' ) ) ) + 1 ) AS laufzeit\n"
                + "        , Nvl( (    SELECT grenzwert\n"
                + "                    FROM beschfehler\n"
                + "                    WHERE beschfehler_id = a.beschwkat_id ), 0 ) AS grenzwert\n"
                + "        , Nvl( (    SELECT warnwert\n"
                + "                    FROM beschfehler\n"
                + "                    WHERE beschfehler_id = a.beschwkat_id ), 0 ) AS warnwert\n"
                + "        , a.beschstatus_id AS pruef_status\n"
                + "        , ( CASE\n"
                + "                    WHEN ( ( Decode( Nvl( ( SELECT Max( Trunc( change_date ) ) - Trunc( a.adate )\n"
                + "                                            FROM besch_statusaenderung\n"
                + "                                            WHERE beschwerden_id = a.beschwerden_id\n"
                + "                                                AND beschstatus_id = 9\n"
                + "                                                AND Nvl( inaktiv, 'F' ) != 'T' ), - 1 )\n"
                + "                                        , - 1, Trunc( sysdate ) - Trunc( a.adate ) - (  SELECT Count()\n"
                + "                                                                                        FROM firmenkalender\n"
                + "                                                                                        WHERE firma_id = firmen_id\n"
                + "                                                                                            AND Nvl( b_verkauf, 'F' ) = 'T'\n"
                + "                                                                                            AND kal_datum BETWEEN Trunc( a.adate )\n"
                + "                                                                                                                 AND Trunc( sysdate ) )\n"
                + "                                        , Nvl( (    SELECT Max( Trunc( change_date ) ) - Trunc( a.adate )\n"
                + "                                                    FROM besch_statusaenderung\n"
                + "                                                    WHERE beschwerden_id = a.beschwerden_id\n"
                + "                                                        AND beschstatus_id = 9\n"
                + "                                                        AND Nvl( inaktiv, 'F' ) != 'T' ), - 1 )\n"
                + "                                                 - (    SELECT Count()\n"
                + "                                                        FROM firmenkalender\n"
                + "                                                        WHERE firma_id = firmen_id\n"
                + "                                                            AND Nvl( b_verkauf, 'F' ) = 'T'\n"
                + "                                                            AND kal_datum BETWEEN Trunc( a.adate )\n"
                + "                                                                                 AND (  SELECT Max( Trunc( change_date ) )\n"
                + "                                                                                        FROM besch_statusaenderung\n"
                + "                                                                                        WHERE beschwerden_id = a.beschwerden_id\n"
                + "                                                                                            AND beschstatus_id = 9\n"
                + "                                                                                            AND Nvl( inaktiv, 'F' ) != 'T' ) ) ) + 1 ) - Nvl( ( SELECT grenzwert\n"
                + "                                                                                                                                                FROM beschfehler\n"
                + "                                                                                                                                                WHERE beschfehler_id = a.beschwkat_id ), 0 ) ) < 0\n"
                + "                        THEN 0\n"
                + "                    ELSE ( ( Decode( Nvl( ( SELECT Max( Trunc( change_date ) ) - Trunc( a.adate )\n"
                + "                                            FROM besch_statusaenderung\n"
                + "                                            WHERE beschwerden_id = a.beschwerden_id\n"
                + "                                                AND beschstatus_id = 9\n"
                + "                                                AND Nvl( inaktiv, 'F' ) != 'T' ), - 1 )\n"
                + "                                        , - 1, Trunc( sysdate ) - Trunc( a.adate ) - (  SELECT Count()\n"
                + "                                                                                        FROM firmenkalender\n"
                + "                                                                                        WHERE firma_id = firmen_id\n"
                + "                                                                                            AND Nvl( b_verkauf, 'F' ) = 'T'\n"
                + "                                                                                            AND kal_datum BETWEEN Trunc( a.adate )\n"
                + "                                                                                                                 AND Trunc( sysdate ) )\n"
                + "                                        , Nvl( (    SELECT Max( Trunc( change_date ) ) - Trunc( a.adate )\n"
                + "                                                    FROM besch_statusaenderung\n"
                + "                                                    WHERE beschwerden_id = a.beschwerden_id\n"
                + "                                                        AND beschstatus_id = 9\n"
                + "                                                        AND Nvl( inaktiv, 'F' ) != 'T' ), - 1 )\n"
                + "                                                 - (    SELECT Count( * )\n"
                + "                                                        FROM firmenkalender\n"
                + "                                                        WHERE firma_id = firmen_id\n"
                + "                                                            AND Nvl( b_verkauf, 'F' ) = 'T'\n"
                + "                                                            AND kal_datum BETWEEN Trunc( a.adate )\n"
                + "                                                                                 AND (  SELECT Max( Trunc( change_date ) )\n"
                + "                                                                                        FROM besch_statusaenderung\n"
                + "                                                                                        WHERE beschwerden_id = a.beschwerden_id\n"
                + "                                                                                            AND beschstatus_id = 9\n"
                + "                                                                                            AND Nvl( inaktiv, 'F' ) != 'T' ) ) ) + 1 ) - Nvl( ( SELECT grenzwert\n"
                + "                                                                                                                                                FROM beschfehler\n"
                + "                                                                                                                                                WHERE beschfehler_id = a.beschwkat_id ), 0 ) )\n"
                + "                END ) AS grenz_ueber\n"
                + "FROM beschwerden a\n"
                + "WHERE a.mandanten_id = m_beschwerde\n"
                + "    AND a.rec_status <> '9'\n"
                + "    AND EXISTS (    SELECT 1\n"
                + "                    FROM besch_statusaenderung\n"
                + "                    WHERE beschwerden_id = a.beschwerden_id )\n"
                + "    AND Nvl( (  SELECT grenzwert\n"
                + "                FROM beschfehler\n"
                + "                WHERE beschfehler_id = a.beschwkat_id ), 0 ) > 0\n";

        assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }
}
