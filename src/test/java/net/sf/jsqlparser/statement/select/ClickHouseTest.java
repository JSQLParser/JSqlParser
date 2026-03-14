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

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ClickHouseTest {

    @Test
    public void testGlobalJoin() throws JSQLParserException {
        String sql =
                "SELECT a.*,b.* from lineorder_all as a  global left join supplier_all as b on a.LOLINENUMBER=b.SSUPPKEY";
        assertSqlCanBeParsedAndDeparsed(sql, true);
    }

    @Test
    public void testGlobalAnyLeftJoin() throws JSQLParserException {
        String sql = "SELECT * FROM events e GLOBAL ANY LEFT JOIN users u ON e.user_id = u.id";
        PlainSelect select = (PlainSelect) assertSqlCanBeParsedAndDeparsed(sql, true);
        Join join = select.getJoins().get(0);
        Assertions.assertTrue(join.isGlobal());
        Assertions.assertTrue(join.isAny());
        Assertions.assertTrue(join.isLeft());
    }

    @Test
    public void testGlobalAllRightJoin() throws JSQLParserException {
        String sql = "SELECT * FROM events e GLOBAL ALL RIGHT JOIN users u ON e.user_id = u.id";
        PlainSelect select = (PlainSelect) assertSqlCanBeParsedAndDeparsed(sql, true);
        Join join = select.getJoins().get(0);
        Assertions.assertTrue(join.isGlobal());
        Assertions.assertTrue(join.isAll());
        Assertions.assertTrue(join.isRight());
    }

    @Test
    public void testLeftAnyJoinOrderVariant() throws JSQLParserException {
        String sql = "SELECT * FROM events e LEFT ANY JOIN users u ON e.user_id = u.id";
        Select statement = (Select) CCJSqlParserUtil.parse(sql);
        PlainSelect select = (PlainSelect) statement.getSelectBody();
        Join join = select.getJoins().get(0);
        Assertions.assertTrue(join.isAny());
        Assertions.assertTrue(join.isLeft());
    }

    @Test
    public void testRightAllJoinOrderVariant() throws JSQLParserException {
        String sql = "SELECT * FROM events e RIGHT ALL JOIN users u ON e.user_id = u.id";
        Select statement = (Select) CCJSqlParserUtil.parse(sql);
        PlainSelect select = (PlainSelect) statement.getSelectBody();
        Join join = select.getJoins().get(0);
        Assertions.assertTrue(join.isAll());
        Assertions.assertTrue(join.isRight());
    }

    @Test
    public void testFunctionWithAttributesIssue1742() throws JSQLParserException {
        String sql = "SELECT f1(arguments).f2.f3 from dual";
        assertSqlCanBeParsedAndDeparsed(sql, true);

        sql = "SELECT f1(arguments).f2(arguments).f3.f4 from dual";
        assertSqlCanBeParsedAndDeparsed(sql, true);

        sql = "SELECT schemaName.f1(arguments).f2(arguments).f3.f4 from dual";
        assertSqlCanBeParsedAndDeparsed(sql, true);
    }

    @Test
    public void testGlobalIn() throws JSQLParserException {
        String sql =
                "SELECT lo_linenumber,lo_orderkey from lo_linenumber where lo_linenumber global in (1,2,3)";
        assertSqlCanBeParsedAndDeparsed(sql, true);
    }

    @Test
    public void testGlobalKeywordIssue1883() throws JSQLParserException {
        String sqlStr = "select a.* from  a global join  b on a.name = b.name ";
        PlainSelect select = (PlainSelect) assertSqlCanBeParsedAndDeparsed(sqlStr, true);
        Assertions.assertTrue(select.getJoins().get(0).isGlobal());
    }

    @Test
    public void testPreWhereClause() throws JSQLParserException {
        String sqlStr = "SELECT * FROM table1 PREWHERE column_name = 'value'";
        PlainSelect select = (PlainSelect) assertSqlCanBeParsedAndDeparsed(sqlStr, true);
        Assertions.assertNotNull(select.getPreWhere());
        Assertions.assertNull(select.getWhere());
    }

    @Test
    public void testPreWhereWithWhereClause() throws JSQLParserException {
        String sqlStr =
                "SELECT * FROM table1 PREWHERE column_name = 'value' WHERE id > 10";
        PlainSelect select = (PlainSelect) assertSqlCanBeParsedAndDeparsed(sqlStr, true);
        Assertions.assertNotNull(select.getPreWhere());
        Assertions.assertNotNull(select.getWhere());
    }

    @Test
    public void testParameterizedAggregateFunctionIssue2125() throws JSQLParserException {
        String sql =
                "SELECT toStartOfDay(timestamp) AS date, count(1) AS count, quantile(0.95)(cost) AS cost95 FROM apm_log_event";
        Select select = (Select) assertSqlCanBeParsedAndDeparsed(sql, true);

        Function function = ((PlainSelect) select.getSelectBody())
                .getSelectItem(2)
                .getExpression(Function.class);
        Assertions.assertNotNull(function.getParameters());
        Assertions.assertNotNull(function.getChainedParameters());
        Assertions.assertEquals(1, function.getParameters().size());
        Assertions.assertEquals(1, function.getChainedParameters().size());
    }

    @Test
    public void testSettingsClauseIssue2362() throws JSQLParserException {
        String sql = "SELECT *\nFROM events\nSETTINGS max_threads = 1";
        PlainSelect select = (PlainSelect) assertSqlCanBeParsedAndDeparsed(sql, true);
        Assertions.assertNotNull(select.getSettings());
        Assertions.assertEquals(1, select.getSettings().size());
        Assertions.assertEquals("max_threads = 1", select.getSettings().get(0).toString());
    }

    @Test
    public void testMultipleSettingsClauseIssue2362() throws JSQLParserException {
        String sql = "SELECT * FROM events SETTINGS max_threads = 1, max_rows_to_read = 1000";
        PlainSelect select = (PlainSelect) assertSqlCanBeParsedAndDeparsed(sql, true);
        Assertions.assertNotNull(select.getSettings());
        Assertions.assertEquals(2, select.getSettings().size());
    }
}
