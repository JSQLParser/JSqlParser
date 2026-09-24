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
import net.sf.jsqlparser.expression.ColumnsTransformer;
import net.sf.jsqlparser.expression.ColumnsTransformer.ColumnsTransformerType;
import net.sf.jsqlparser.expression.operators.relational.ParenthesedExpressionList;
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
        columns.addTransformer(new ColumnsTransformer(ColumnsTransformerType.EXCEPT)
                .setExceptColumns(new ParenthesedExpressionList<>(new Column("hidden"))));
        PlainSelect select = new PlainSelect().addSelectItem(columns).withFromItem(new Table("t"));

        String expected = "SELECT * EXCEPT (hidden) FROM t";
        assertEquals(expected, select.toString());
        assertDeparse(select, expected);
        assertSqlCanBeParsedAndDeparsed(expected);
    }

    @Test
    void setExceptColumnsOnParsedWildcard() throws JSQLParserException {
        PlainSelect select = (PlainSelect) CCJSqlParserUtil.parse("SELECT * FROM t");
        AllColumns columns = select.getSelectItem(0).getExpression(AllColumns.class);
        columns.addTransformer(new ColumnsTransformer(ColumnsTransformerType.EXCEPT)
                .setExceptColumns(new ParenthesedExpressionList<>(new Column("hidden"))));

        String expected = "SELECT * EXCEPT (hidden) FROM t";
        assertEquals(expected, select.toString());
        assertDeparse(select, expected);
        assertSqlCanBeParsedAndDeparsed(expected);
    }

    @Test
    void addExceptColumnToParsedTableWildcard() throws JSQLParserException {
        PlainSelect select = (PlainSelect) CCJSqlParserUtil.parse("SELECT t.* FROM t");
        AllTableColumns columns = select.getSelectItem(0).getExpression(AllTableColumns.class);
        columns.addTransformer(new ColumnsTransformer(ColumnsTransformerType.EXCEPT)
                .setExceptColumns(new ParenthesedExpressionList<>(new Column("hidden"))));

        String expected = "SELECT t.* EXCEPT (hidden) FROM t";
        assertEquals(expected, select.toString());
        assertDeparse(select, expected);
        assertSqlCanBeParsedAndDeparsed(expected);
    }

    @Test
    void preserveExplicitExcludeTransformer() throws JSQLParserException {
        AllColumns columns = new AllColumns();
        columns.addTransformer(new ColumnsTransformer(ColumnsTransformerType.EXCLUDE)
                .setExceptColumns(new ParenthesedExpressionList<>(new Column("hidden"))));
        PlainSelect select = new PlainSelect().addSelectItem(columns).withFromItem(new Table("t"));

        String expected = "SELECT * EXCLUDE (hidden) FROM t";
        assertEquals(expected, select.toString());
        assertDeparse(select, expected);
        assertSqlCanBeParsedAndDeparsed(expected);
    }
}
