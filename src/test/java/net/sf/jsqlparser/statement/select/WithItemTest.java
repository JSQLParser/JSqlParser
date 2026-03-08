/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2025 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class WithItemTest {

    @Test
    void testNotMaterializedIssue2251() throws JSQLParserException {
        String sqlStr = "WITH devices AS NOT MATERIALIZED (\n"
                + "  SELECT\n"
                + "    d.uuid AS device_uuid\n"
                + "  FROM active_devices d\n"
                + ")\n"
                + "SELECT 1;";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "WITH\n" +
                    "  FUNCTION doubleup(x integer)\n" +
                    "    RETURNS integer\n" +
                    "    RETURN x * 2\n" +
                    "SELECT doubleup(21);\n",
            "WITH\n" +
                    "  FUNCTION doubleup(x integer)\n" +
                    "    RETURNS integer\n" +
                    "    RETURN x * 2,\n" +
                    "  FUNCTION doubleupplusone(x integer)\n" +
                    "    RETURNS integer\n" +
                    "    RETURN doubleup(x) + 1\n" +
                    "SELECT doubleupplusone(21);",
            "WITH\n" +
                    "  FUNCTION takesArray(x array<double>)\n" +
                    "    RETURNS double\n" +
                    "    RETURN x[1] + x[2] + x[3]\n" +
                    "SELECT takesArray(ARRAY[1.0, 2.0, 3.0]);"

    })
    void testWithFunction(String sqlStr) throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testRecursiveWithSearchBreadthClause() throws JSQLParserException {
        String sqlStr = "WITH RECURSIVE team_hierarchy AS (\n"
                + "    SELECT employee_id, first_name, manager_id, ARRAY[employee_id] AS path\n"
                + "    FROM employees\n"
                + "    WHERE manager_id IS NULL\n"
                + "    UNION ALL\n"
                + "    SELECT e.employee_id, e.first_name, e.manager_id, th.path || e.employee_id\n"
                + "    FROM employees e\n"
                + "    INNER JOIN team_hierarchy th ON e.manager_id = th.employee_id\n"
                + ")\n"
                + "SEARCH BREADTH FIRST BY employee_id SET order_col\n"
                + "SELECT employee_id, first_name, path, order_col FROM team_hierarchy ORDER BY order_col";

        Select select = (Select) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
        WithSearchClause searchClause = select.getWithItemsList().get(0).getSearchClause();

        assertNotNull(searchClause);
        assertEquals(WithSearchClause.SearchOrder.BREADTH, searchClause.getSearchOrder());
        assertEquals("employee_id", searchClause.getSearchColumns().get(0).toString());
        assertEquals("order_col", searchClause.getSequenceColumnName());
    }

    @Test
    void testRecursiveWithSearchDepthClause() throws JSQLParserException {
        String sqlStr = "WITH RECURSIVE search_tree AS (\n"
                + "    SELECT id, parent_id FROM nodes WHERE parent_id IS NULL\n"
                + "    UNION ALL\n"
                + "    SELECT n.id, n.parent_id FROM nodes n JOIN search_tree st ON st.id = n.parent_id\n"
                + ")\n"
                + "SEARCH DEPTH FIRST BY id, parent_id SET traversal_order\n"
                + "SELECT traversal_order FROM search_tree";

        Select select = (Select) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
        WithSearchClause searchClause = select.getWithItemsList().get(0).getSearchClause();

        assertNotNull(searchClause);
        assertEquals(WithSearchClause.SearchOrder.DEPTH, searchClause.getSearchOrder());
        assertEquals("id", searchClause.getSearchColumns().get(0).toString());
        assertEquals("parent_id", searchClause.getSearchColumns().get(1).toString());
        assertEquals("traversal_order", searchClause.getSequenceColumnName());
    }
}
