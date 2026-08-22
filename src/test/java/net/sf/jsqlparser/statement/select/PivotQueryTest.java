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
import static org.junit.jupiter.api.Assertions.assertNotNull;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.view.CreateView;
import net.sf.jsqlparser.test.TestUtils;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.junit.jupiter.api.Test;

class PivotQueryTest {

    @Test
    void testDuckDBPivotStatement() throws JSQLParserException {
        String sql = "PIVOT sales ON region USING SUM(amount)";

        Statement statement = TestUtils.assertSqlCanBeParsedAndDeparsed(sql, true);
        PivotQuery pivotQuery = assertInstanceOf(PivotQuery.class, statement);
        Table table = assertInstanceOf(Table.class, pivotQuery.getFromItem());

        assertThat(table.getFullyQualifiedName()).isEqualTo("sales");
        assertThat(pivotQuery.getOnExpressions()).hasSize(1);
        assertThat(pivotQuery.getUsingItems()).hasSize(1);
        assertThat(TablesNamesFinder.findTables(sql)).containsExactly("sales");
    }

    @Test
    void testDuckDBPivotInSubquery() throws JSQLParserException {
        String sql = "SELECT * FROM (PIVOT sales ON region USING SUM(amount))";

        Select select = (Select) TestUtils.assertSqlCanBeParsedAndDeparsed(sql, true);
        ParenthesedSelect parenthesedSelect =
                assertInstanceOf(ParenthesedSelect.class, select.getPlainSelect().getFromItem());

        assertInstanceOf(PivotQuery.class, parenthesedSelect.getSelect());
        assertThat(TablesNamesFinder.findTables(sql)).containsExactly("sales");
    }

    @Test
    void testDuckDBPivotFullSimplifiedSyntax() throws JSQLParserException {
        String sql = "PIVOT sales ON region, category "
                + "USING SUM(amount) AS total, COUNT(*) AS count "
                + "GROUP BY year ORDER BY year DESC LIMIT 10";

        PivotQuery pivotQuery = (PivotQuery) TestUtils.assertSqlCanBeParsedAndDeparsed(sql, true);

        assertThat(pivotQuery.getOnExpressions()).hasSize(2);
        assertThat(pivotQuery.getUsingItems()).hasSize(2);
        assertThat(pivotQuery.getGroupByExpressions()).hasSize(1);
        assertThat(pivotQuery.getOrderByElements()).hasSize(1);
        assertNotNull(pivotQuery.getLimit());
    }

    @Test
    void testDuckDBPivotOptionalClauses() throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed("PIVOT sales ON region", true);
        TestUtils.assertSqlCanBeParsedAndDeparsed("PIVOT sales USING SUM(amount)", true);
        TestUtils.assertSqlCanBeParsedAndDeparsed("PIVOT sales GROUP BY region", true);
    }

    @Test
    void testDuckDBPivotInCreateView() throws JSQLParserException {
        String sql = "CREATE VIEW v AS PIVOT sales ON region USING SUM(amount)";

        CreateView createView =
                (CreateView) TestUtils.assertSqlCanBeParsedAndDeparsed(sql, true);

        assertInstanceOf(PivotQuery.class, createView.getSelect());
    }

    @Test
    void testExistingPivotClauseStillWorks() throws JSQLParserException {
        String sql = "SELECT * FROM sales "
                + "PIVOT (SUM(amount) FOR region IN ('US', 'EU'))";

        Select select = (Select) TestUtils.assertSqlCanBeParsedAndDeparsed(sql, true);
        Table table = assertInstanceOf(Table.class, select.getPlainSelect().getFromItem());

        assertNotNull(table.getPivot());
    }
}
