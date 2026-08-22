/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.MapExpression;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.view.CreateView;
import net.sf.jsqlparser.test.TestUtils;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.junit.jupiter.api.Test;

class DuckDBCompatibilityTest {

    @Test
    void testMapLiteral() throws JSQLParserException {
        String sql = "SELECT MAP {'key': 'value', 'key2': 'other'} AS m FROM t";

        Select select = (Select) TestUtils.assertSqlCanBeParsedAndDeparsed(sql, true);
        MapExpression mapExpression = assertInstanceOf(MapExpression.class,
                select.getPlainSelect().getSelectItem(0).getExpression());

        assertThat(mapExpression.getEntries()).hasSize(2);
        assertThat(TablesNamesFinder.findTables(sql)).containsExactly("t");
    }

    @Test
    void testCreateViewWithMapLiteral() throws JSQLParserException {
        String sql = "CREATE VIEW v AS SELECT "
                + "MAP {'category': name, 'count': name} AS summary FROM products";

        CreateView createView =
                (CreateView) TestUtils.assertSqlCanBeParsedAndDeparsed(sql, true);
        MapExpression mapExpression = assertInstanceOf(MapExpression.class,
                createView.getSelect().getPlainSelect().getSelectItem(0).getExpression());

        assertThat(mapExpression.getEntries()).hasSize(2);
        assertThat(TablesNamesFinder.findTables(sql)).contains("products");
    }

    @Test
    void testPivotStatement() throws JSQLParserException {
        String sql = "PIVOT sales ON region USING SUM(amount)";

        Statement statement = TestUtils.assertSqlCanBeParsedAndDeparsed(sql, true);

        assertInstanceOf(PivotQuery.class, statement);
    }

    @Test
    void testPivotInSubquery() throws JSQLParserException {
        String sql = "SELECT * FROM (PIVOT sales ON region USING SUM(amount))";

        Select select = (Select) TestUtils.assertSqlCanBeParsedAndDeparsed(sql, true);
        ParenthesedSelect parenthesedSelect =
                assertInstanceOf(ParenthesedSelect.class, select.getPlainSelect().getFromItem());

        assertInstanceOf(PivotQuery.class, parenthesedSelect.getSelect());
    }
}
