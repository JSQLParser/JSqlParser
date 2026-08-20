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

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.schema.Table;
import org.junit.jupiter.api.Test;

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
                ((Table) plainSelect.getJoins().get(0).getFromItem()).getFullyQualifiedName());
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

    @Test
    public void testLateralViewManyColumnAliasesIssue2433() throws Exception {
        // Hive/Spark LATERAL VIEW allows an arbitrary number of column aliases
        // (e.g. json_tuple yielding many columns). Only the first two were absorbed;
        // any further aliases leaked into the FROM clause as implicit cross-join
        // tables, so the failure was silent and even round-tripped identically.
        String sql = "SELECT a FROM t"
                + " LATERAL VIEW json_tuple(j, 'a', 'b', 'c', 'd', 'e', 'f', 'g')"
                + " x AS c1, c2, c3, c4, c5, c6, c7";

        Select select = (Select) assertSqlCanBeParsedAndDeparsed(sql, true);
        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();

        // The extra aliases must not leak as cross-join tables.
        assertNull(plainSelect.getJoins());

        java.util.List<LateralView> lateralViews = plainSelect.getLateralViews();
        assertNotNull(lateralViews);
        assertEquals(1, lateralViews.size());

        Alias columnAlias = lateralViews.get(0).getColumnAlias();
        assertNotNull(columnAlias);
        assertNotNull(columnAlias.getAliasColumns());
        assertEquals(7, columnAlias.getAliasColumns().size());
    }
}
