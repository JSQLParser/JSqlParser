/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2022 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.test;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;

public class AssortedFeatureTests {

    static class ReplaceColumnAndLongValues extends ExpressionDeParser {

        @Override
        public <K> StringBuilder visit(StringValue stringValue, K parameters) {
            this.getBuilder().append("?");
            return null;
        }

        @Override
        public <K> StringBuilder visit(LongValue longValue, K parameters) {
            this.getBuilder().append("?");
            return null;
        }
    }

    public static String cleanStatement(String sql) throws JSQLParserException {
        StringBuilder buffer = new StringBuilder();
        ExpressionDeParser expr = new ReplaceColumnAndLongValues();

        SelectDeParser selectDeparser = new SelectDeParser(expr, buffer);
        expr.setSelectVisitor(selectDeparser);
        expr.setBuilder(buffer);

        StatementDeParser stmtDeparser = new StatementDeParser(expr, selectDeparser, buffer);

        Statement stmt = CCJSqlParserUtil.parse(sql);

        stmt.accept(stmtDeparser);
        return stmtDeparser.getBuilder().toString();
    }

    @Test
    public void testIssue1608() throws JSQLParserException {
        System.out.println(cleanStatement("SELECT 'abc', 5 FROM mytable WHERE col='test'"));
        System.out.println(
                cleanStatement("UPDATE table1 A SET A.columna = 'XXX' WHERE A.cod_table = 'YYY'"));
        System.out.println(cleanStatement(
                "INSERT INTO example (num, name, address, tel) VALUES (1, 'name', 'test ', '1234-1234')"));
        System.out.println(cleanStatement("DELETE FROM table1 where col=5 and col2=4"));
    }

    @Test
    void addSelectItemTest() throws JSQLParserException {
        String provided = "SELECT col1 FROM WHATEVER";
        String expected = "SELECT col1, Sum(1, 2) AS col2 FROM WHATEVER";

        PlainSelect select = (PlainSelect) CCJSqlParserUtil.parse(provided);

        Function f = new Function("Sum", new LongValue(1), new LongValue(2));
        SelectItem<?> i = new SelectItem<>(f, new Alias("col2", true));

        select
                .getSelectItems()
                .add(i);

        TestUtils.assertStatementCanBeDeparsedAs(select, expected);
    }

    @Test
    void removeSelectItemTest() throws JSQLParserException {
        String provided = "SELECT col1, Sum(1, 2) AS col2 FROM WHATEVER";
        String expected = "SELECT col1 FROM WHATEVER";

        PlainSelect select = (PlainSelect) CCJSqlParserUtil.parse(provided);

        select
                .getSelectItems()
                .remove(1);

        TestUtils.assertStatementCanBeDeparsedAs(select, expected);
    }

    @Test
    void sweapSelectItemTest() throws JSQLParserException {
        String provided = "SELECT col1 FROM WHATEVER";
        String expected = "SELECT Sum(1, 2) AS col1 FROM WHATEVER";

        PlainSelect select = (PlainSelect) CCJSqlParserUtil.parse(provided);

        Function f = new Function("Sum", new LongValue(1), new LongValue(2));
        SelectItem<?> i = new SelectItem<>(f, new Alias("col1", true));
        select
                .getSelectItems()
                .remove(0);
        select
                .getSelectItems()
                .add(0, i);

        TestUtils.assertStatementCanBeDeparsedAs(select, expected);
    }
}
