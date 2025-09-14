/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2023 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.statement.Statement;
import org.junit.jupiter.api.Test;

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.junit.jupiter.api.Assertions.*;

class TranscodingFunctionTest {

    @Test
    void testTranscoding() throws JSQLParserException {
        String functionStr = "CONVERT( 'abc' USING utf8mb4 )";
        String sqlStr = "SELECT " + functionStr;
        assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        TranscodingFunction transcodingFunction = new TranscodingFunction()
                .withExpression(new StringValue("abc"))
                .withTranscodingName("utf8mb4");
        assertEquals(functionStr, transcodingFunction.toString());
    }

    @Test
    void testIssue644() throws JSQLParserException {
        String sqlStr = "SELECT CONVERT(int, a) FROM A";
        assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testIssue688() throws JSQLParserException {
        String sqlStr = "select * from a order by convert(a.name using gbk) desc";
        assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testIssue1257() throws JSQLParserException {
        String sqlStr = "SELECT id,name,version,identity,type,desc,enable,content\n"
                + "FROM tbl_template\n"
                + "WHERE (name like ?)\n"
                + "ORDER BY convert(name using GBK) ASC";
        assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }


    @Test
    public void testUnPivotWithAlias() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT cast(1 as Decimal(18,2))", true);

        Statement st = assertSqlCanBeParsedAndDeparsed(
                "SELECT Convert( Decimal(18,2) , 1 )", true);
    }

    @Test
    void testIssue2304() throws JSQLParserException {
        String sqlStr = "SELECT TRY_CONVERT(NUMERIC(8,6), '1234') AS LATITUDE_NBR;";
        assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }
}
