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
import net.sf.jsqlparser.statement.create.policy.CreatePolicy;
import net.sf.jsqlparser.statement.create.policy.PolicyCommand;
import net.sf.jsqlparser.statement.create.policy.PolicyMode;
import org.junit.jupiter.api.Test;

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for PostgreSQL CREATE POLICY statement (Row Level Security)
 */
public class CreatePolicyTest {

    @Test
    public void testCreatePolicyBasic() throws JSQLParserException {
        String sql = "CREATE POLICY policy_name ON table_name";
        assertSqlCanBeParsedAndDeparsed(sql, true);

        Statement stmt = CCJSqlParserUtil.parse(sql);
        assertInstanceOf(CreatePolicy.class, stmt);
        CreatePolicy policy = (CreatePolicy) stmt;
        assertEquals("policy_name", policy.getPolicyName());
        assertEquals("table_name", policy.getTable().getName());
        assertNull(policy.getPolicyMode());
        assertEquals(PolicyMode.PERMISSIVE, policy.getEffectivePolicyMode());
        assertNull(policy.getPolicyCommand());
        assertEquals(PolicyCommand.ALL, policy.getEffectivePolicyCommand());
    }

    @Test
    public void testCreatePolicyWithSchema() throws JSQLParserException {
        String sql =
                "CREATE POLICY single_tenant_access_policy ON customer_custom_data.phone_opt_out";
        assertSqlCanBeParsedAndDeparsed(sql, true);

        Statement stmt = CCJSqlParserUtil.parse(sql);
        CreatePolicy policy = (CreatePolicy) stmt;
        assertEquals("single_tenant_access_policy", policy.getPolicyName());
        assertEquals("customer_custom_data.phone_opt_out",
                policy.getTable().getFullyQualifiedName());
    }

    @Test
    public void testCreatePolicyWithForClause() throws JSQLParserException {
        String sql = "CREATE POLICY policy1 ON table1 FOR SELECT";
        assertSqlCanBeParsedAndDeparsed(sql, true);

        CreatePolicy policy = (CreatePolicy) CCJSqlParserUtil.parse(sql);
        assertEquals("SELECT", policy.getCommand());
        assertEquals(PolicyCommand.SELECT, policy.getPolicyCommand());
    }

    @Test
    public void testCreatePolicyWithAllCommands() throws JSQLParserException {
        String[] commands = {"ALL", "SELECT", "INSERT", "UPDATE", "DELETE"};
        for (String cmd : commands) {
            String sql = "CREATE POLICY p ON t FOR " + cmd;
            assertSqlCanBeParsedAndDeparsed(sql, true);
            CreatePolicy policy = (CreatePolicy) CCJSqlParserUtil.parse(sql);
            assertEquals(cmd, policy.getCommand());
            assertEquals(PolicyCommand.from(cmd), policy.getPolicyCommand());
        }
    }

    @Test
    public void testCreateRestrictivePolicy() throws JSQLParserException {
        String sql = "CREATE POLICY tenant_policy ON users AS RESTRICTIVE FOR SELECT TO app "
                + "USING (tenant_id = current_user)";
        assertSqlCanBeParsedAndDeparsed(sql, true);

        CreatePolicy policy = (CreatePolicy) CCJSqlParserUtil.parse(sql);
        assertEquals(PolicyMode.RESTRICTIVE, policy.getPolicyMode());
        assertEquals(PolicyMode.RESTRICTIVE, policy.getEffectivePolicyMode());
        assertEquals(PolicyCommand.SELECT, policy.getPolicyCommand());
        assertEquals("app", policy.getRoles().get(0));
        assertNotNull(policy.getUsingExpression());
    }

    @Test
    public void testCreateExplicitPermissivePolicy() throws JSQLParserException {
        String sql = "CREATE POLICY tenant_policy ON users AS PERMISSIVE";
        assertSqlCanBeParsedAndDeparsed(sql, true);

        CreatePolicy policy = (CreatePolicy) CCJSqlParserUtil.parse(sql);
        assertEquals(PolicyMode.PERMISSIVE, policy.getPolicyMode());
        assertEquals(PolicyMode.PERMISSIVE, policy.getEffectivePolicyMode());
    }

    @Test
    public void testCreatePolicyRejectsUnknownMode() {
        assertThrows(JSQLParserException.class,
                () -> CCJSqlParserUtil.parse("CREATE POLICY p ON t AS UNKNOWN"));
    }

    @Test
    public void testCreatePolicyWithSingleRole() throws JSQLParserException {
        String sql = "CREATE POLICY policy1 ON table1 TO role1";
        assertSqlCanBeParsedAndDeparsed(sql, true);

        CreatePolicy policy = (CreatePolicy) CCJSqlParserUtil.parse(sql);
        assertEquals(1, policy.getRoles().size());
        assertEquals("role1", policy.getRoles().get(0));
    }

    @Test
    public void testCreatePolicyWithMultipleRoles() throws JSQLParserException {
        String sql = "CREATE POLICY policy1 ON table1 TO role1, role2, role3";
        assertSqlCanBeParsedAndDeparsed(sql, true);

        CreatePolicy policy = (CreatePolicy) CCJSqlParserUtil.parse(sql);
        assertEquals(3, policy.getRoles().size());
        assertEquals("role1", policy.getRoles().get(0));
        assertEquals("role2", policy.getRoles().get(1));
        assertEquals("role3", policy.getRoles().get(2));
    }

    @Test
    public void testCreatePolicyWithUsing() throws JSQLParserException {
        String sql = "CREATE POLICY policy1 ON table1 USING (user_id = current_user_id())";
        assertSqlCanBeParsedAndDeparsed(sql, true);

        CreatePolicy policy = (CreatePolicy) CCJSqlParserUtil.parse(sql);
        assertNotNull(policy.getUsingExpression());
    }

    @Test
    public void testCreatePolicyWithWithCheck() throws JSQLParserException {
        String sql = "CREATE POLICY policy1 ON table1 WITH CHECK (status = 'active')";
        assertSqlCanBeParsedAndDeparsed(sql, true);

        CreatePolicy policy = (CreatePolicy) CCJSqlParserUtil.parse(sql);
        assertNotNull(policy.getWithCheckExpression());
    }

    @Test
    public void testCreatePolicyComplete() throws JSQLParserException {
        String sql =
                "CREATE POLICY single_tenant_access_policy ON customer_custom_data.phone_opt_out " +
                        "FOR SELECT " +
                        "TO gong_app_single_tenant_ro_role, gong_app_single_tenant_rw_role " +
                        "USING (company_id = current_setting('gong.tenant.company_id')::bigint)";
        assertSqlCanBeParsedAndDeparsed(sql, true);

        CreatePolicy policy = (CreatePolicy) CCJSqlParserUtil.parse(sql);
        assertEquals("single_tenant_access_policy", policy.getPolicyName());
        assertEquals("customer_custom_data.phone_opt_out",
                policy.getTable().getFullyQualifiedName());
        assertEquals("SELECT", policy.getCommand());
        assertEquals(2, policy.getRoles().size());
        assertNotNull(policy.getUsingExpression());
    }

    @Test
    public void testCreatePolicyWithBothUsingAndWithCheck() throws JSQLParserException {
        String sql = "CREATE POLICY policy1 ON table1 " +
                "USING (department_id = current_user_department()) " +
                "WITH CHECK (status IN ('draft', 'published'))";
        assertSqlCanBeParsedAndDeparsed(sql, true);

        CreatePolicy policy = (CreatePolicy) CCJSqlParserUtil.parse(sql);
        assertNotNull(policy.getUsingExpression());
        assertNotNull(policy.getWithCheckExpression());
    }

    @Test
    public void testCreatePolicyCompleteWithAllClauses() throws JSQLParserException {
        String sql = "CREATE POLICY admin_policy ON documents " +
                "FOR UPDATE " +
                "TO admin_role, superuser " +
                "USING (author_id = current_user_id()) " +
                "WITH CHECK (updated_at >= CURRENT_TIMESTAMP)";
        assertSqlCanBeParsedAndDeparsed(sql, true);

        CreatePolicy policy = (CreatePolicy) CCJSqlParserUtil.parse(sql);
        assertEquals("admin_policy", policy.getPolicyName());
        assertEquals("documents", policy.getTable().getName());
        assertEquals("UPDATE", policy.getCommand());
        assertEquals(2, policy.getRoles().size());
        assertNotNull(policy.getUsingExpression());
        assertNotNull(policy.getWithCheckExpression());
    }
}
