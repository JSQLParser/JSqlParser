/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2022 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.JsonExpression;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PostgresTest {
    @Test
    public void testExtractFunction() throws JSQLParserException {
        String sqlStr = "SELECT EXTRACT(HOUR FROM TIMESTAMP '2001-02-16 20:38:40')";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        sqlStr = "SELECT EXTRACT('HOUR' FROM TIMESTAMP '2001-02-16 20:38:40')";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        sqlStr = "SELECT EXTRACT('HOURS' FROM TIMESTAMP '2001-02-16 20:38:40')";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    public void testExtractFunctionIssue1582() throws JSQLParserException {
        String sqlStr = "" +
                "select\n" +
                "              t0.operatienr\n" +
                "            , case\n" +
                "                when\n" +
                "                    case when (t0.vc_begintijd_operatie is null or lpad((extract('hours' from t0.vc_begintijd_operatie::timestamp))::text,2,'0') ||':'|| lpad(extract('minutes' from t0.vc_begintijd_operatie::timestamp)::text,2,'0') = '00:00') then null\n" +
                "                         else (greatest(((extract('hours' from (t0.vc_eindtijd_operatie::timestamp-t0.vc_begintijd_operatie::timestamp))*60 + extract('minutes' from (t0.vc_eindtijd_operatie::timestamp-t0.vc_begintijd_operatie::timestamp)))/60)::numeric(12,2),0))*60\n" +
                "                end = 0 then null\n" +
                "                    else '25. Meer dan 4 uur'\n" +
                "                end                                                                                                                                                  \n" +
                "              as snijtijd_interval";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    public void testJSonExpressionIssue1696() throws JSQLParserException {
        String sqlStr="SELECT '{\"key\": \"value\"}'::json -> 'key' AS X";
        Select select = (Select) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        SelectExpressionItem selectExpressionItem = (SelectExpressionItem) plainSelect.getSelectItems().get(0);
        Assertions.assertEquals("'key'", selectExpressionItem.getExpression(JsonExpression.class).getIdents().get(0));
    }
}
