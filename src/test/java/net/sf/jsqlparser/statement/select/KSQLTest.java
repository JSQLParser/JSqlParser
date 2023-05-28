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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class KSQLTest {

    @Test
    public void testKSQLWindowedJoin() throws Exception {
        String sql = "SELECT *\n" + "FROM table1 t1\n" + "INNER JOIN table2 t2\n" + "WITHIN (5 HOURS)\n" + "ON t1.id = t2.id\n";
        PlainSelect plainSelect = (PlainSelect) assertSqlCanBeParsedAndDeparsed(sql, true);
        assertEquals(1, plainSelect.getJoins().size());
        assertEquals("table2", ((Table) plainSelect.getJoins().get(0).getRightItem()).getFullyQualifiedName());
        assertTrue(plainSelect.getJoins().get(0).isWindowJoin());
        assertEquals(5L, plainSelect.getJoins().get(0).getJoinWindow().getDuration());
        assertEquals("HOURS", plainSelect.getJoins().get(0).getJoinWindow().getTimeUnit().toString());
        assertFalse(plainSelect.getJoins().get(0).getJoinWindow().isBeforeAfterWindow());
    }

    @Test
    public void testKSQLBeforeAfterWindowedJoin() throws Exception {
        String sql = "SELECT *\n" + "FROM table1 t1\n" + "INNER JOIN table2 t2\n" + "WITHIN (1 MINUTE, 5 MINUTES)\n" + "ON t1.id = t2.id\n";
        PlainSelect plainSelect = (PlainSelect) assertSqlCanBeParsedAndDeparsed(sql, true);
        assertEquals(1, plainSelect.getJoins().size());
        assertEquals("table2", ((Table) plainSelect.getJoins().get(0).getRightItem()).getFullyQualifiedName());
        assertTrue(plainSelect.getJoins().get(0).isWindowJoin());
        assertEquals(1L, plainSelect.getJoins().get(0).getJoinWindow().getBeforeDuration());
        assertEquals("MINUTE", plainSelect.getJoins().get(0).getJoinWindow().getBeforeTimeUnit().toString());
        assertEquals(5L, plainSelect.getJoins().get(0).getJoinWindow().getAfterDuration());
        assertEquals("MINUTES", plainSelect.getJoins().get(0).getJoinWindow().getAfterTimeUnit().toString());
        assertTrue(plainSelect.getJoins().get(0).getJoinWindow().isBeforeAfterWindow());
    }

    @Test
    public void testKSQLHoppingWindows() throws Exception {
        String sql = "SELECT *\n" + "FROM table1 t1\n" + "WINDOW HOPPING (SIZE 30 SECONDS, ADVANCE BY 10 MINUTES)\n" + "GROUP BY region.id\n";
        PlainSelect plainSelect = (PlainSelect) assertSqlCanBeParsedAndDeparsed(sql, true);
        assertTrue(plainSelect.getKsqlWindow().isHoppingWindow());
        assertFalse(plainSelect.getKsqlWindow().isSessionWindow());
        assertFalse(plainSelect.getKsqlWindow().isTumblingWindow());
        assertEquals(30L, plainSelect.getKsqlWindow().getSizeDuration());
        assertEquals("SECONDS", plainSelect.getKsqlWindow().getSizeTimeUnit().toString());
        assertEquals(10L, plainSelect.getKsqlWindow().getAdvanceDuration());
        assertEquals("MINUTES", plainSelect.getKsqlWindow().getAdvanceTimeUnit().toString());
    }

    @Test
    public void testKSQLSessionWindows() throws Exception {
        String sql = "SELECT *\n" + "FROM table1 t1\n" + "WINDOW SESSION (5 MINUTES)\n" + "GROUP BY region.id\n";
        PlainSelect plainSelect = (PlainSelect) assertSqlCanBeParsedAndDeparsed(sql, true);
        assertTrue(plainSelect.getKsqlWindow().isSessionWindow());
        assertFalse(plainSelect.getKsqlWindow().isHoppingWindow());
        assertFalse(plainSelect.getKsqlWindow().isTumblingWindow());
        assertEquals(5L, plainSelect.getKsqlWindow().getSizeDuration());
        assertEquals("MINUTES", plainSelect.getKsqlWindow().getSizeTimeUnit().toString());
    }

    @Test
    public void testKSQLTumblingWindows() throws Exception {
        String sql = "SELECT *\n" + "FROM table1 t1\n" + "WINDOW TUMBLING (SIZE 30 SECONDS)\n" + "GROUP BY region.id\n";
        PlainSelect plainSelect = (PlainSelect) assertSqlCanBeParsedAndDeparsed(sql, true);
        assertTrue(plainSelect.getKsqlWindow().isTumblingWindow());
        assertFalse(plainSelect.getKsqlWindow().isSessionWindow());
        assertFalse(plainSelect.getKsqlWindow().isHoppingWindow());
        assertEquals(30L, plainSelect.getKsqlWindow().getSizeDuration());
        assertEquals("SECONDS", plainSelect.getKsqlWindow().getSizeTimeUnit().toString());
    }

    @Test
    public void testKSQLEmitChanges() throws Exception {
        String sql = "SELECT * FROM table1 t1 GROUP BY region.id EMIT CHANGES";
        PlainSelect plainSelect = (PlainSelect) assertSqlCanBeParsedAndDeparsed(sql, true);
        assertTrue(plainSelect.isEmitChanges());
    }

    @Test
    public void testKSQLEmitChangesWithLimit() throws Exception {
        String sql = "SELECT * FROM table1 t1 GROUP BY region.id EMIT CHANGES LIMIT 2";
        PlainSelect plainSelect = (PlainSelect) assertSqlCanBeParsedAndDeparsed(sql, true);
        assertTrue(plainSelect.isEmitChanges());
    }
}
