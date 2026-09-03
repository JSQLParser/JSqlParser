/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create;

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.junit.jupiter.api.Assertions.assertEquals;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.statement.create.table.CheckConstraint;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import org.junit.jupiter.api.Test;

/** MySQL {@code CREATE TABLE} constraint syntax verified against MySQL 8.4.11. */
public class MySQLCreateTableConstraintTest {

    @Test
    public void testCheckConstraintEnforcement() throws JSQLParserException {
        String sql = "CREATE TABLE mysql_check_enforcement (score INT, "
                + "CONSTRAINT chk_positive CHECK (score >= 0) ENFORCED, "
                + "CONSTRAINT chk_cap CHECK (score < 100) NOT ENFORCED)";

        CreateTable createTable = (CreateTable) assertSqlCanBeParsedAndDeparsed(sql);

        CheckConstraint positive = (CheckConstraint) createTable.getIndexes().get(0);
        assertEquals(Boolean.TRUE, positive.getEnforced());
        assertEquals("chk_positive", positive.getName());

        CheckConstraint cap = (CheckConstraint) createTable.getIndexes().get(1);
        assertEquals(Boolean.FALSE, cap.getEnforced());
        assertEquals("chk_cap", cap.getName());
    }
}
