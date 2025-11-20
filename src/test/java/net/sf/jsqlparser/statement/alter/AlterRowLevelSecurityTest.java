/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2025 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.alter;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import org.junit.jupiter.api.Test;

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for PostgreSQL ALTER TABLE ... ROW LEVEL SECURITY statements
 */
public class AlterRowLevelSecurityTest {

    @Test
    public void testEnableRowLevelSecurity() throws JSQLParserException {
        String sql = "ALTER TABLE table1 ENABLE ROW LEVEL SECURITY";
        assertSqlCanBeParsedAndDeparsed(sql, true);

        Statement stmt = CCJSqlParserUtil.parse(sql);
        assertInstanceOf(Alter.class, stmt);
        Alter alter = (Alter) stmt;
        assertEquals("table1", alter.getTable().getName());
        assertEquals(AlterOperation.ENABLE_ROW_LEVEL_SECURITY,
                alter.getAlterExpressions().get(0).getOperation());
    }

    @Test
    public void testEnableRowLevelSecurityWithSchema() throws JSQLParserException {
        String sql = "ALTER TABLE customer_custom_data.phone_opt_out ENABLE ROW LEVEL SECURITY";
        assertSqlCanBeParsedAndDeparsed(sql, true);

        Alter alter = (Alter) CCJSqlParserUtil.parse(sql);
        assertEquals("customer_custom_data.phone_opt_out",
                alter.getTable().getFullyQualifiedName());
        assertEquals(AlterOperation.ENABLE_ROW_LEVEL_SECURITY,
                alter.getAlterExpressions().get(0).getOperation());
    }

    @Test
    public void testDisableRowLevelSecurity() throws JSQLParserException {
        String sql = "ALTER TABLE table1 DISABLE ROW LEVEL SECURITY";
        assertSqlCanBeParsedAndDeparsed(sql, true);

        Alter alter = (Alter) CCJSqlParserUtil.parse(sql);
        assertEquals(AlterOperation.DISABLE_ROW_LEVEL_SECURITY,
                alter.getAlterExpressions().get(0).getOperation());
    }

    @Test
    public void testForceRowLevelSecurity() throws JSQLParserException {
        String sql = "ALTER TABLE table1 FORCE ROW LEVEL SECURITY";
        assertSqlCanBeParsedAndDeparsed(sql, true);

        Alter alter = (Alter) CCJSqlParserUtil.parse(sql);
        assertEquals(AlterOperation.FORCE_ROW_LEVEL_SECURITY,
                alter.getAlterExpressions().get(0).getOperation());
    }

    @Test
    public void testNoForceRowLevelSecurity() throws JSQLParserException {
        String sql = "ALTER TABLE table1 NO FORCE ROW LEVEL SECURITY";
        assertSqlCanBeParsedAndDeparsed(sql, true);

        Alter alter = (Alter) CCJSqlParserUtil.parse(sql);
        assertEquals(AlterOperation.NO_FORCE_ROW_LEVEL_SECURITY,
                alter.getAlterExpressions().get(0).getOperation());
    }

    @Test
    public void testMultipleStatements() throws JSQLParserException {
        // Test CREATE POLICY followed by ENABLE RLS
        String sql = "CREATE POLICY policy1 ON table1 USING (id = user_id()); " +
                "ALTER TABLE table1 ENABLE ROW LEVEL SECURITY";

        net.sf.jsqlparser.statement.Statements stmts = CCJSqlParserUtil.parseStatements(sql);
        assertEquals(2, stmts.getStatements().size());

        assertInstanceOf(net.sf.jsqlparser.statement.create.policy.CreatePolicy.class,
                stmts.getStatements().get(0));
        assertInstanceOf(Alter.class, stmts.getStatements().get(1));
    }

    @Test
    public void testEnableKeysStillWorks() throws JSQLParserException {
        // Ensure our changes don't break existing ENABLE KEYS syntax
        String sql = "ALTER TABLE table1 ENABLE KEYS";
        assertSqlCanBeParsedAndDeparsed(sql, true);

        Alter alter = (Alter) CCJSqlParserUtil.parse(sql);
        assertEquals(AlterOperation.ENABLE_KEYS,
                alter.getAlterExpressions().get(0).getOperation());
    }

    @Test
    public void testDisableKeysStillWorks() throws JSQLParserException {
        // Ensure our changes don't break existing DISABLE KEYS syntax
        String sql = "ALTER TABLE table1 DISABLE KEYS";
        assertSqlCanBeParsedAndDeparsed(sql, true);

        Alter alter = (Alter) CCJSqlParserUtil.parse(sql);
        assertEquals(AlterOperation.DISABLE_KEYS,
                alter.getAlterExpressions().get(0).getOperation());
    }
}
