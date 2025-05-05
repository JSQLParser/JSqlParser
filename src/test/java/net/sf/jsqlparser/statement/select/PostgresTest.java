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
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.JsonExpression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;

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
        String sqlStr = ""
                + "select\n"
                + "  t0.operatienr\n"
                + "  , case\n"
                + "    when\n"
                + "        case when (t0.vc_begintijd_operatie is null or lpad((extract('hours' from t0.vc_begintijd_operatie::timestamp))::text,2,'0') ||':'|| lpad(extract('minutes' from t0.vc_begintijd_operatie::timestamp)::text,2,'0') = '00:00') then null\n"
                + "             else (greatest(((extract('hours' from (t0.vc_eindtijd_operatie::timestamp-t0.vc_begintijd_operatie::timestamp))*60 + extract('minutes' from (t0.vc_eindtijd_operatie::timestamp-t0.vc_begintijd_operatie::timestamp)))/60)::numeric(12,2),0))*60\n"
                + "    end = 0 then null\n"
                + "        else '25. Meer dan 4 uur'\n"
                + "    end\n"
                + "  as snijtijd_interval";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    public void testJSonExpressionIssue1696() throws JSQLParserException {
        String sqlStr = "SELECT '{\"key\": \"value\"}'::json -> 'key' AS X";
        PlainSelect plainSelect = (PlainSelect) assertSqlCanBeParsedAndDeparsed(sqlStr, true);
        SelectItem<?> selectExpressionItem =
                plainSelect.getSelectItems().get(0);
        Assertions.assertEquals(new StringValue("key"),
                selectExpressionItem.getExpression(JsonExpression.class).getIdent(0).getKey());
    }

    @Test
    public void testJSonOperatorIssue1571() throws JSQLParserException {
        String sqlStr =
                "select visit_hour,json_array_elements(into_sex_json)->>'name',json_array_elements(into_sex_json)->>'value' from period_market";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testPostgresQuotingIssue1335() throws JSQLParserException {
        String sqlStr =
                "INSERT INTO \"table\"\"with\"\"quotes\" (\"column\"\"with\"\"quotes\")\n"
                        + "VALUES ('1'), ('2'), ('3');\n"
                        + "\n"
                        + "UPDATE \"table\"\"with\"\"quotes\" SET \"column\"\"with\"\"quotes\" = '1.0'  \n"
                        + "WHERE \"column\"\"with\"\"quotes\" = '1';\n"
                        + "\n"
                        + "SELECT \"column\"\"with\"\"quotes\" FROM  \"table\"\"with\"\"quotes\"\n"
                        + "WHERE \"column\"\"with\"\"quotes\" IS NOT NULL;";

        Statements statements = CCJSqlParserUtil.parseStatements(sqlStr);
        Assertions.assertEquals(3, statements.size());

        Insert insert = statements.get(Insert.class, 0);
        Assertions.assertEquals(
                "\"table\"\"with\"\"quotes\"", insert.getTable().getFullyQualifiedName());

        PlainSelect select = statements.get(PlainSelect.class, 2);
        List<SelectItem<?>> selectItems = select.getSelectItems();

        Assertions.assertEquals(
                "\"column\"\"with\"\"quotes\"",
                selectItems.get(0).getExpression(Column.class).getColumnName());
    }

    @Test
    void testNextValueIssue1863() throws JSQLParserException {
        String sqlStr = "SELECT nextval('client_id_seq')";
        assertSqlCanBeParsedAndDeparsed(sqlStr);
    }

    @Test
    void testDollarQuotedText() throws JSQLParserException {
        String sqlStr = "SELECT $tag$This\nis\na\nselect\ntest\n$tag$ from dual where a=b";
        PlainSelect st = (PlainSelect) CCJSqlParserUtil.parse(sqlStr);

        StringValue stringValue = st.getSelectItem(0).getExpression(StringValue.class);

        Assertions.assertEquals("This\nis\na\nselect\ntest\n", stringValue.getValue());
    }

    @Test
    void testQuotedIdentifier() throws JSQLParserException {
        String sqlStr = "SELECT \"This is a Test Column\" AS [Alias] from `This is a Test Table`";
        PlainSelect st = (PlainSelect) CCJSqlParserUtil.parse(sqlStr);

        Column column = st.getSelectItem(0).getExpression(Column.class);
        Assertions.assertEquals("This is a Test Column", column.getUnquotedName());
        Assertions.assertEquals("\"This is a Test Column\"", column.getColumnName());

        Alias alias = st.getSelectItem(0).getAlias();
        Assertions.assertEquals("Alias", alias.getUnquotedName());
        Assertions.assertEquals("[Alias]", alias.getName());

        Table table = st.getFromItem(Table.class);
        Assertions.assertEquals("This is a Test Table", table.getUnquotedName());
        Assertions.assertEquals("`This is a Test Table`", table.getName());

    }
}
