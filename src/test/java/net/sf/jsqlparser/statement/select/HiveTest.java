/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

import net.sf.jsqlparser.schema.Table;
import org.junit.jupiter.api.Test;

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HiveTest {

    @Test
    public void testLeftSemiJoin() throws Exception {
        String sql = "SELECT\n"
                + "    Something\n"
                + "FROM\n"
                + "    Sometable\n"
                + "LEFT SEMI JOIN\n"
                + "    Othertable\n";

        PlainSelect plainSelect = (PlainSelect) assertSqlCanBeParsedAndDeparsed(sql, true);
        assertEquals(1, plainSelect.getJoins().size());
        assertEquals("Othertable",
                ((Table) plainSelect.getJoins().get(0).getRightItem()).getFullyQualifiedName());
        assertTrue(plainSelect.getJoins().get(0).isLeft());
        assertTrue(plainSelect.getJoins().get(0).isSemi());
    }

    @Test
    public void testGroupByGroupingSets() throws Exception {
        String sql = "SELECT\n"
                + "    C1, C2, C3, MAX(Value)\n"
                + "FROM\n"
                + "    Sometable\n"
                + "GROUP BY C1, C2, C3 GROUPING SETS ((C1, C2), (C1, C2, C3), ())";
        assertSqlCanBeParsedAndDeparsed(sql, true);
    }

    @Test
    public void testGroupSimplified() throws Exception {
        String sql = "SELECT\n"
                + "    * \n"
                + "FROM\n"
                + "    Sometable\n"
                + "GROUP BY GROUPING SETS (())";
        assertSqlCanBeParsedAndDeparsed(sql, true);
    }
}
