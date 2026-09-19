/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2023 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

import static net.sf.jsqlparser.test.TestUtils.assertDeparse;
import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.junit.jupiter.api.Assertions.assertEquals;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Test;


class AllColumnsTest {

    @Test
    void testBigQuerySyntax() throws JSQLParserException {
        String sqlStr =
                "SELECT * EXCEPT(order_id) REPLACE(\"widget\" AS item_name), \"more\" as more_fields\n"
                        + "FROM orders";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testDuckDBQuerySyntax() throws JSQLParserException {
        String sqlStr =
                "SELECT * EXCLUDE(order_id) REPLACE(\"widget\" AS item_name), \"more\" as more_fields\n"
                        + "FROM orders";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void addExceptColumnToNewWildcard() throws JSQLParserException {
        AllColumns columns = new AllColumns();
        columns.addExceptColumn(new Column("hidden"));
        PlainSelect select = new PlainSelect().addSelectItem(columns).withFromItem(new Table("t"));

        String expected = "SELECT * EXCEPT( hidden ) FROM t";
        assertEquals(expected, select.toString());
        assertDeparse(select, expected);
        assertSqlCanBeParsedAndDeparsed(expected);
    }

    @Test
    void setExceptColumnsOnParsedWildcard() throws JSQLParserException {
        PlainSelect select = (PlainSelect) CCJSqlParserUtil.parse("SELECT * FROM t");
        AllColumns columns = select.getSelectItem(0).getExpression(AllColumns.class);
        columns.setExceptColumns(new ExpressionList<>(new Column("hidden")));

        String expected = "SELECT * EXCEPT( hidden ) FROM t";
        assertEquals(expected, select.toString());
        assertDeparse(select, expected);
        assertSqlCanBeParsedAndDeparsed(expected);
    }

    @Test
    void addExceptColumnToParsedTableWildcard() throws JSQLParserException {
        PlainSelect select = (PlainSelect) CCJSqlParserUtil.parse("SELECT t.* FROM t");
        AllTableColumns columns = select.getSelectItem(0).getExpression(AllTableColumns.class);
        columns.addExceptColumn(new Column("hidden"));

        String expected = "SELECT t.* EXCEPT( hidden ) FROM t";
        assertEquals(expected, select.toString());
        assertDeparse(select, expected);
        assertSqlCanBeParsedAndDeparsed(expected);
    }

    @Test
    void preserveExplicitExceptKeywordAndEmptyColumns() {
        AllColumns columns = new AllColumns().setExceptKeyword("EXCLUDE");
        columns.addExceptColumn(new Column("hidden"));
        assertEquals("* EXCLUDE( hidden )", columns.toString());

        columns.setExceptColumns(new ExpressionList<>());
        assertEquals("*", columns.toString());
        columns.setExceptColumns(null);
        assertEquals("*", columns.toString());
    }
}
