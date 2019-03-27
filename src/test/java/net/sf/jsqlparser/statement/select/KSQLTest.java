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

import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import org.junit.Test;

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static net.sf.jsqlparser.test.TestUtils.assertStatementCanBeDeparsedAs;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class KSQLTest {

    @Test
    public void testKSQLWindowedJoin() throws Exception {
        String sql;
        Statement statement;

        sql = "SELECT *\n"
                + "FROM table1 t1\n"
                + "INNER JOIN table2 t2\n"
                + "WITHIN (5 HOURS)\n"
                + "ON t1.id = t2.id\n";

        statement = CCJSqlParserUtil.parse(sql);

        System.out.println(statement.toString());

        Select select = (Select) statement;
        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        assertEquals(1, plainSelect.getJoins().size());
        assertEquals("table2", ((Table) plainSelect.getJoins().get(0).getRightItem()).
                getFullyQualifiedName());
        assertTrue(plainSelect.getJoins().get(0).isWindowJoin());
        assertEquals(5L, plainSelect.getJoins().get(0).getJoinWindow().getDuration());
        assertEquals("HOURS", plainSelect.getJoins().get(0).getJoinWindow().getTimeUnit().toString());
        assertFalse(plainSelect.getJoins().get(0).getJoinWindow().isBeforeAfterWindow());
        assertStatementCanBeDeparsedAs(select, sql, true);

        assertSqlCanBeParsedAndDeparsed(sql, true);
    }

    @Test
    public void testKSQLBeforeAfterWindowedJoin() throws Exception {
        String sql;
        Statement statement;
        sql = "SELECT *\n"
                + "FROM table1 t1\n"
                + "INNER JOIN table2 t2\n"
                + "WITHIN (1 MINUTE, 5 MINUTES)\n"
                + "ON t1.id = t2.id\n";

        statement = CCJSqlParserUtil.parse(sql);

        System.out.println(statement.toString());

        Select select = (Select) statement;
        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        assertEquals(1, plainSelect.getJoins().size());
        assertEquals("table2", ((Table) plainSelect.getJoins().get(0).getRightItem()).
                getFullyQualifiedName());
        assertTrue(plainSelect.getJoins().get(0).isWindowJoin());
        assertEquals(1L, plainSelect.getJoins().get(0).getJoinWindow().getBeforeDuration());
        assertEquals("MINUTE", plainSelect.getJoins().get(0).getJoinWindow().getBeforeTimeUnit().toString());
        assertEquals(5L, plainSelect.getJoins().get(0).getJoinWindow().getAfterDuration());
        assertEquals("MINUTES", plainSelect.getJoins().get(0).getJoinWindow().getAfterTimeUnit().toString());
        assertTrue(plainSelect.getJoins().get(0).getJoinWindow().isBeforeAfterWindow());
        assertStatementCanBeDeparsedAs(select, sql, true);

        assertSqlCanBeParsedAndDeparsed(sql, true);
    }
}
