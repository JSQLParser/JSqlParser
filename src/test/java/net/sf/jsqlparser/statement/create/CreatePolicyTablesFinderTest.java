/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2025 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for TablesNamesFinder integration with PostgreSQL CREATE POLICY statements.
 *
 * <p>
 * These tests verify that TablesNamesFinder correctly identifies ALL tables referenced in a CREATE
 * POLICY statement, including:
 * <ul>
 * <li>The policy's target table</li>
 * <li>Tables in USING expression subqueries</li>
 * <li>Tables in WITH CHECK expression subqueries</li>
 * <li>Tables in complex expressions (JOINs, CTEs, nested subqueries)</li>
 * </ul>
 *
 * <p>
 * <strong>Current Status:</strong> These tests will FAIL until
 * TablesNamesFinder.visit(CreatePolicy) is updated to traverse USING and WITH CHECK expressions.
 * This is incomplete feature support, not a regression - CREATE POLICY parsing works correctly, but
 * analysis tools don't yet have complete integration.
 *
 * <p>
 * <strong>Expected Behavior:</strong> Once fixed, TablesNamesFinder should find tables in policy
 * expressions using the same pattern as other statements (CreateView, Insert, Update).
 */
public class CreatePolicyTablesFinderTest {

    // =========================================================================
    // Helper Methods
    // =========================================================================

    /**
     * Parse SQL and extract table names using TablesNamesFinder.
     */
    private List<String> getTablesFromSQL(String sql) throws JSQLParserException {
        Statement stmt = CCJSqlParserUtil.parse(sql);
        TablesNamesFinder finder = new TablesNamesFinder();
        return finder.getTableList(stmt);
    }

    /**
     * Assert that the actual table list contains exactly the expected tables.
     */
    private void assertContainsAllTables(List<String> actual, String... expected) {
        assertEquals(expected.length, actual.size(),
                "Expected " + expected.length + " tables but found " + actual.size() + ". " +
                        "Expected: " + java.util.Arrays.toString(expected) + ", " +
                        "Actual: " + actual);

        for (String table : expected) {
            assertTrue(actual.contains(table),
                    "Expected to find table '" + table + "' but it was missing. " +
                            "Found tables: " + actual);
        }
    }

    // =========================================================================
    // Simple Subqueries - Basic USE Cases
    // =========================================================================

    @Test
    public void testTablesFinderWithSubqueryInUsing() throws JSQLParserException {
        String sql = "CREATE POLICY tenant_policy ON documents " +
                "USING (tenant_id IN (SELECT tenant_id FROM tenant_access))";

        List<String> tables = getTablesFromSQL(sql);

        // Should find: target table + table in USING subquery
        assertContainsAllTables(tables, "documents", "tenant_access");
    }

    @Test
    public void testTablesFinderWithSubqueryInWithCheck() throws JSQLParserException {
        String sql = "CREATE POLICY data_policy ON user_data " +
                "WITH CHECK (status IN (SELECT allowed_status FROM status_config))";

        List<String> tables = getTablesFromSQL(sql);

        // Should find: target table + table in WITH CHECK subquery
        assertContainsAllTables(tables, "user_data", "status_config");
    }

    @Test
    public void testTablesFinderWithBothUsingAndWithCheck() throws JSQLParserException {
        String sql = "CREATE POLICY dual_check_policy ON records " +
                "USING (user_id IN (SELECT id FROM active_users)) " +
                "WITH CHECK (status IN (SELECT status FROM valid_statuses))";

        List<String> tables = getTablesFromSQL(sql);

        // Should find: target table + table in USING + table in WITH CHECK
        assertContainsAllTables(tables, "records", "active_users", "valid_statuses");
    }

    // =========================================================================
    // Complex Expressions - Multiple/Nested Subqueries
    // =========================================================================

    @Test
    public void testTablesFinderWithMultipleSubqueries() throws JSQLParserException {
        String sql = "CREATE POLICY complex_policy ON documents " +
                "USING (" +
                "  tenant_id IN (SELECT tenant_id FROM tenant_access) " +
                "  AND status IN (SELECT status FROM allowed_statuses) " +
                "  AND department_id = (SELECT id FROM departments WHERE name = 'Engineering')" +
                ")";

        List<String> tables = getTablesFromSQL(sql);

        // Should find: target table + 3 tables from subqueries
        assertContainsAllTables(tables, "documents", "tenant_access", "allowed_statuses",
                "departments");
    }

    @Test
    public void testTablesFinderWithNestedSubqueries() throws JSQLParserException {
        String sql = "CREATE POLICY nested_policy ON orders " +
                "USING (customer_id IN (" +
                "  SELECT customer_id FROM customer_access " +
                "  WHERE region_id IN (SELECT id FROM regions WHERE active = true)" +
                "))";

        List<String> tables = getTablesFromSQL(sql);

        // Should find: target table + tables from nested subqueries
        assertContainsAllTables(tables, "orders", "customer_access", "regions");
    }

    @Test
    public void testTablesFinderWithJoinsInSubquery() throws JSQLParserException {
        String sql = "CREATE POLICY join_policy ON orders " +
                "USING (EXISTS (" +
                "  SELECT 1 FROM customers c " +
                "  JOIN customer_access ca ON c.id = ca.customer_id " +
                "  WHERE c.id = orders.customer_id" +
                "))";

        List<String> tables = getTablesFromSQL(sql);

        // Should find: target table + tables from JOIN in subquery
        assertContainsAllTables(tables, "orders", "customers", "customer_access");
    }

    // =========================================================================
    // Advanced SQL Features - CTEs, Schema Qualification, Functions
    // =========================================================================

    @Test
    public void testTablesFinderWithCTE() throws JSQLParserException {
        String sql = "CREATE POLICY cte_policy ON documents " +
                "USING (tenant_id IN (" +
                "  WITH active_tenants AS (SELECT id FROM tenants WHERE active = true) " +
                "  SELECT id FROM active_tenants" +
                "))";

        List<String> tables = getTablesFromSQL(sql);

        // Should find: target table + table referenced in CTE
        assertContainsAllTables(tables, "documents", "tenants");
    }

    @Test
    public void testTablesFinderWithSchemaQualifiedTables() throws JSQLParserException {
        String sql = "CREATE POLICY schema_policy ON myschema.documents " +
                "USING (tenant_id IN (SELECT id FROM otherschema.tenants))";

        List<String> tables = getTablesFromSQL(sql);

        // Should find both schema-qualified tables
        assertEquals(2, tables.size(),
                "Should find both schema-qualified tables. Found: " + tables);

        // Check if tables are found (with or without schema prefix depending on TablesNamesFinder
        // behavior)
        boolean foundDocuments = tables.stream()
                .anyMatch(t -> t.contains("documents"));
        boolean foundTenants = tables.stream()
                .anyMatch(t -> t.contains("tenants"));

        assertTrue(foundDocuments, "Should find documents table. Found: " + tables);
        assertTrue(foundTenants, "Should find tenants table. Found: " + tables);
    }

    @Test
    public void testTablesFinderWithTableFunctions() throws JSQLParserException {
        // PostgreSQL table-valued functions can be used in FROM clauses
        String sql = "CREATE POLICY function_policy ON documents " +
                "USING (tenant_id IN (" +
                "  SELECT tenant_id FROM get_accessible_tenants(current_user_id())" +
                "))";

        List<String> tables = getTablesFromSQL(sql);

        // Should at least find the target table
        // Note: Table-valued functions might not be reported as "tables" depending on
        // implementation
        assertTrue(tables.contains("documents"),
                "Should at least find the target table. Found: " + tables);
    }

    // =========================================================================
    // Edge Cases - EXISTS, UNION, Empty Policies
    // =========================================================================

    @Test
    public void testTablesFinderWithExistsClause() throws JSQLParserException {
        String sql = "CREATE POLICY exists_policy ON documents " +
                "USING (EXISTS (" +
                "  SELECT 1 FROM tenant_access " +
                "  WHERE tenant_id = documents.tenant_id AND active = true" +
                "))";

        List<String> tables = getTablesFromSQL(sql);

        // Should find: target table + table in EXISTS subquery
        assertContainsAllTables(tables, "documents", "tenant_access");
    }

    @Test
    public void testTablesFinderWithUnionInSubquery() throws JSQLParserException {
        String sql = "CREATE POLICY union_policy ON documents " +
                "USING (tenant_id IN (" +
                "  SELECT tenant_id FROM primary_tenants " +
                "  UNION " +
                "  SELECT tenant_id FROM secondary_tenants" +
                "))";

        List<String> tables = getTablesFromSQL(sql);

        // Should find: target table + both tables in UNION
        assertContainsAllTables(tables, "documents", "primary_tenants", "secondary_tenants");
    }

    @Test
    public void testTablesFinderEmptyPolicy() throws JSQLParserException {
        // Policy with no USING or WITH CHECK clauses
        String sql = "CREATE POLICY simple_policy ON documents";

        List<String> tables = getTablesFromSQL(sql);

        // Should only find the target table
        assertContainsAllTables(tables, "documents");
    }
}
