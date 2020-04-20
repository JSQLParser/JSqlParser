/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.update;

import java.io.StringReader;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.schema.Column;
import static net.sf.jsqlparser.test.TestUtils.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class UpdateTest {

    private static CCJSqlParserManager parserManager = new CCJSqlParserManager();

    @Test
    public void testUpdate() throws JSQLParserException {
        String statement = "UPDATE mytable set col1='as', col2=?, col3=565 Where o >= 3";
        Update update = (Update) parserManager.parse(new StringReader(statement));
        assertEquals("mytable", update.getTable().toString());
        assertEquals(3, update.getColumns().size());
        assertEquals("col1", ((Column) update.getColumns().get(0)).getColumnName());
        assertEquals("col2", ((Column) update.getColumns().get(1)).getColumnName());
        assertEquals("col3", ((Column) update.getColumns().get(2)).getColumnName());
        assertEquals("as", ((StringValue) update.getExpressions().get(0)).getValue());
        assertTrue(update.getExpressions().get(1) instanceof JdbcParameter);
        assertEquals(565, ((LongValue) update.getExpressions().get(2)).getValue());

        assertTrue(update.getWhere() instanceof GreaterThanEquals);
    }

    @Test
    public void testUpdateWAlias() throws JSQLParserException {
        String statement = "UPDATE table1 A SET A.columna = 'XXX' WHERE A.cod_table = 'YYY'";
        Update update = (Update) parserManager.parse(new StringReader(statement));
    }

    @Test
    public void testUpdateWithDeparser() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("UPDATE table1 AS A SET A.columna = 'XXX' WHERE A.cod_table = 'YYY'");
    }

    @Test
    public void testUpdateWithFrom() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("UPDATE table1 SET columna = 5 FROM table1 LEFT JOIN table2 ON col1 = col2");
    }

    @Test
    public void testUpdateMultiTable() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "UPDATE T1, T2 SET T1.C2 = T2.C2, T2.C3 = 'UPDATED' WHERE T1.C1 = T2.C1 AND T1.C2 < 10");
    }

    @Test
    public void testUpdateWithSelect() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("UPDATE NATION SET (N_NATIONKEY) = (SELECT ? FROM SYSIBM.SYSDUMMY1)");
    }

    @Test
    public void testUpdateWithSelect2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("UPDATE mytable SET (col1, col2, col3) = (SELECT a, b, c FROM mytable2)");
    }

    @Test
    public void testUpdateIssue167_SingleQuotes() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "UPDATE tablename SET NAME = 'Customer 2', ADDRESS = 'Address \\' ddad2', AUTH_KEY = 'samplekey' WHERE ID = 2");
    }

    @Test
    public void testUpdateWithLimit() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("UPDATE tablename SET col = 'thing' WHERE id = 1 LIMIT 10");
    }

    @Test
    public void testUpdateWithOrderBy() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("UPDATE tablename SET col = 'thing' WHERE id = 1 ORDER BY col");
    }

    @Test
    public void testUpdateWithOrderByAndLimit() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("UPDATE tablename SET col = 'thing' WHERE id = 1 ORDER BY col LIMIT 10");
    }

    @Test
    public void testUpdateWithReturningAll() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "UPDATE tablename SET col = 'thing' WHERE id = 1 ORDER BY col LIMIT 10 RETURNING *");
        assertSqlCanBeParsedAndDeparsed("UPDATE tablename SET col = 'thing' WHERE id = 1 RETURNING *");
    }

    @Test
    public void testUpdateWithReturningList() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "UPDATE tablename SET col = 'thing' WHERE id = 1 ORDER BY col LIMIT 10 RETURNING col_1, col_2, col_3");
        assertSqlCanBeParsedAndDeparsed(
                "UPDATE tablename SET col = 'thing' WHERE id = 1 RETURNING col_1, col_2, col_3");
        assertSqlCanBeParsedAndDeparsed(
                "UPDATE tablename SET col = 'thing' WHERE id = 1 ORDER BY col LIMIT 10 RETURNING col_1 AS Bar, col_2 AS Baz, col_3 AS Foo");
        assertSqlCanBeParsedAndDeparsed(
                "UPDATE tablename SET col = 'thing' WHERE id = 1 RETURNING col_1 AS Bar, col_2 AS Baz, col_3 AS Foo");
        assertSqlCanBeParsedAndDeparsed(
                "UPDATE tablename SET col = 'thing' WHERE id = 1 RETURNING ABS(col_1) AS Bar, ABS(col_2), col_3 AS Foo");
    }

    @Test(expected = JSQLParserException.class)
    public void testUpdateDoesNotAllowLimitOffset() throws JSQLParserException {
        String statement = "UPDATE table1 A SET A.columna = 'XXX' WHERE A.cod_table = 'YYY' LIMIT 3,4";
        parserManager.parse(new StringReader(statement));
    }

    @Test
    public void testUpdateWithFunctions() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("UPDATE tablename SET col = SUBSTRING(col2, 1, 2)");
    }

    @Test
    public void testUpdateIssue508LeftShift() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("UPDATE user SET num = 1 << 1 WHERE id = 1");
    }

    @Test
    public void testUpdateIssue338() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("UPDATE mytable SET status = (status & ~1)");
    }

    @Test
    public void testUpdateIssue338_1() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("UPDATE mytable SET status = (status & 1)");
    }

    @Test
    public void testUpdateIssue338_2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("UPDATE mytable SET status = (status + 1)");
    }

    @Test
    public void testUpdateIssue826() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("update message_topic inner join message_topic_config on\n"
                + " message_topic.id=message_topic_config.topic_id \n" + "set message_topic_config.enable_flag='N', \n"
                + "message_topic_config.updated_by='test', \n" + "message_topic_config.update_at='2019-07-16' \n"
                + "where message_topic.name='test' \n" + "AND message_topic_config.enable_flag='Y'", true);
    }

    @Test
    public void testUpdateIssue750() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("update a,(select * from c) b set a.id=b.id where a.id=b.id", true);
    }
}
