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
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;

/**
 *
 * @author <a href="mailto:andreas@manticore-projects.com">Andreas Reichel</a>
 */
public class CastExpressionTest {

    @Test
    public void testCastToRowConstructorIssue1267() throws JSQLParserException {
        TestUtils.assertExpressionCanBeParsedAndDeparsed(
                "CAST(ROW(dataid, value, calcMark) AS ROW(datapointid CHAR, value CHAR, calcMark CHAR))",
                true);
        TestUtils.assertExpressionCanBeParsedAndDeparsed(
                "CAST(ROW(dataid, value, calcMark) AS testcol)", true);
    }

    @Test
    void testDataKeywordIssue1969() throws Exception {
        String sqlStr = "SELECT * FROM myschema.myfunction('test'::data.text_not_null)";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testImplicitCast() throws JSQLParserException {
        String sqlStr = "SELECT UUID '4ac7a9e9-607c-4c8a-84f3-843f0191e3fd'";
        PlainSelect select = (PlainSelect) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        Assertions.assertTrue(select.getSelectItem(0).getExpression() instanceof CastExpression);

        sqlStr = "SELECT DECIMAL(5,3) '3.2'";
        select = (PlainSelect) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        Assertions.assertTrue(select.getSelectItem(0).getExpression() instanceof CastExpression);
    }

    @Test
    void testImplicitCastTimestampIssue1364() throws JSQLParserException {
        String sqlStr = "SELECT TIMESTAMP WITH TIME ZONE '2004-10-19 10:23:54+02'";
        PlainSelect select = (PlainSelect) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        Assertions.assertTrue(select.getSelectItem(0).getExpression() instanceof CastExpression);
    }

    @Test
    void testImplicitCastDoublePrecisionIssue1344() throws JSQLParserException {
        String sqlStr = "SELECT double precision '1'";
        PlainSelect select = (PlainSelect) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        Assertions.assertTrue(select.getSelectItem(0).getExpression() instanceof CastExpression);
    }


    @Test
    public void testCastToSigned() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT CAST(contact_id AS SIGNED) A");

        assertSqlCanBeParsedAndDeparsed(
                "SELECT CAST(contact_id AS SIGNED INTEGER) A");

        assertSqlCanBeParsedAndDeparsed(
                "SELECT CAST(contact_id AS UNSIGNED) A");

        assertSqlCanBeParsedAndDeparsed(
                "SELECT CAST(contact_id AS UNSIGNED INTEGER) A");

        assertSqlCanBeParsedAndDeparsed(
                "SELECT CAST(contact_id AS TIME WITHOUT TIME ZONE) A");
    }


    @Test
    void testDataTypeFrom() {
        CastExpression.DataType float64 = CastExpression.DataType.from("FLOAT64");
        Assertions.assertEquals(CastExpression.DataType.FLOAT64, float64);

        CastExpression.DataType float128 = CastExpression.DataType.from("FLOAT128");
        Assertions.assertEquals(CastExpression.DataType.UNKNOWN, float128);
    }

    @Test
    void testParenthesisCastIssue1997() throws JSQLParserException {
        String sqlStr = "SELECT ((foo)::text = ANY((ARRAY['bar'])::text[]))";
        assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        sqlStr = "SELECT ((foo)::text = ANY((((ARRAY['bar'])))::text[]))";
        assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testDateTimeCast() throws JSQLParserException {
        String sqlStr = "SELECT\n"
                + "  TIME(15, 30, 00) as time_hms,\n"
                + "  TIME(DATETIME '2008-12-25 15:30:00') AS time_dt,\n"
                + "  TIME(TIMESTAMP '2008-12-25 15:30:00+08', 'America/Los_Angeles')\n"
                + "as time_tstz;";
        assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }
}
