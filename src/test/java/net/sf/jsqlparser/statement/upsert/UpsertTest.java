/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.upsert;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.PlainSelect;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.StringReader;

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class UpsertTest {

    CCJSqlParserManager parserManager = new CCJSqlParserManager();

    @Test
    public void testUpsert() throws JSQLParserException {
        String statement = "UPSERT INTO TEST (NAME, ID) VALUES ('foo', 123)";
        Upsert upsert = (Upsert) parserManager.parse(new StringReader(statement));
        assertEquals("TEST", upsert.getTable().getName());
        assertEquals(2, upsert.getColumns().size());
        assertEquals("NAME", upsert.getColumns().get(0).getColumnName());
        assertEquals("ID", upsert.getColumns().get(1).getColumnName());

        ExpressionList expressions = upsert.getValues().getExpressions();
        assertEquals(2, expressions.size());
        assertEquals("foo", ((StringValue) expressions.get(0)).getValue());
        assertEquals(123, ((LongValue) expressions.get(1)).getValue());
        assertFalse(upsert.isUseDuplicate());
        assertEquals(statement, "" + upsert);
    }

    @Test
    public void testUpsertDuplicate() throws JSQLParserException {
        String statement =
                "UPSERT INTO TEST (ID, COUNTER) VALUES (123, 0) ON DUPLICATE KEY UPDATE COUNTER = COUNTER + 1";
        Upsert upsert = (Upsert) parserManager.parse(new StringReader(statement));
        assertEquals("TEST", upsert.getTable().getName());
        assertEquals(2, upsert.getColumns().size());
        assertEquals("ID", upsert.getColumns().get(0).getColumnName());
        assertEquals("COUNTER", upsert.getColumns().get(1).getColumnName());

        ExpressionList<?> expressions = upsert.getValues().getExpressions();
        assertEquals(2, expressions.size());
        assertEquals(123, ((LongValue) expressions.get(0)).getValue());
        assertEquals(0, ((LongValue) expressions.get(1)).getValue());
        assertEquals(1, upsert.getDuplicateUpdateColumns().size());
        assertEquals("COUNTER", upsert.getDuplicateUpdateColumns().get(0).getColumnName());
        assertEquals(1, upsert.getDuplicateUpdateExpressionList().size());
        assertEquals("COUNTER + 1", upsert.getDuplicateUpdateExpressionList().get(0).toString());
        assertTrue(upsert.isUseDuplicate());
        assertEquals(statement, "" + upsert);
    }

    @Test
    public void testUpsertSelect() throws JSQLParserException {
        String statement =
                "UPSERT INTO test.targetTable (col1, col2) SELECT * FROM test.sourceTable";
        Upsert upsert = (Upsert) parserManager.parse(new StringReader(statement));
        assertEquals("test.targetTable", upsert.getTable().getFullyQualifiedName());
        assertEquals(2, upsert.getColumns().size());
        assertEquals("col1", upsert.getColumns().get(0).getColumnName());
        assertEquals("col2", upsert.getColumns().get(1).getColumnName());
        assertNull(upsert.getExpressions());
        assertNotNull(upsert.getSelect());
        assertEquals("test.sourceTable",
                ((Table) ((PlainSelect) upsert.getSelect()).getFromItem()).getFullyQualifiedName());
        assertFalse(upsert.isUseDuplicate());
        assertEquals(statement, "" + upsert);
    }

    @Test
    public void testUpsertN() throws JSQLParserException {
        String statement = "UPSERT INTO TEST VALUES ('foo', 'bar', 3)";
        Upsert upsert = (Upsert) parserManager.parse(new StringReader(statement));
        assertEquals("TEST", upsert.getTable().getName());

        ExpressionList expressions = upsert.getValues().getExpressions();
        assertEquals(3, expressions.size());
        assertEquals("foo", ((StringValue) expressions.get(0)).getValue());
        assertEquals("bar", ((StringValue) expressions.get(1)).getValue());
        assertEquals(3, ((LongValue) expressions.get(2)).getValue());
        assertFalse(upsert.isUseDuplicate());
        assertEquals(statement, "" + upsert);
    }

    @Test
    public void testUpsertMultiRowValue() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("UPSERT INTO mytable (col1, col2) VALUES (a, b), (d, e)");
    }

    @Test
    @Disabled
    /* not the job of the parser to validate this, it even may be valid eventually */
    public void testUpsertMultiRowValueDifferent() throws JSQLParserException {
        try {
            assertSqlCanBeParsedAndDeparsed(
                    "UPSERT INTO mytable (col1, col2) VALUES (a, b), (d, e, c)");
        } catch (Exception e) {
            return;
        }
        fail("should not work");
    }

    @Test
    public void testSimpleUpsert() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "UPSERT INTO example (num, name, address, tel) VALUES (1, 'name', 'test ', '1234-1234')");
    }

    @Test
    public void testUpsertHasSelect() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "UPSERT INTO mytable (mycolumn) SELECT mycolumn FROM mytable");
        assertSqlCanBeParsedAndDeparsed(
                "UPSERT INTO mytable (mycolumn) (SELECT mycolumn FROM mytable)");
    }

    @Test
    public void testUpsertWithSelect() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "UPSERT INTO mytable (mycolumn) WITH a AS (SELECT mycolumn FROM mytable) SELECT mycolumn FROM a");
        assertSqlCanBeParsedAndDeparsed(
                "UPSERT INTO mytable (mycolumn) (WITH a AS (SELECT mycolumn FROM mytable) SELECT mycolumn FROM a)");
    }

    @Test
    public void testUpsertWithKeywords() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("UPSERT INTO kvPair (value, key) VALUES (?, ?)");
    }

    @Test
    public void testHexValues() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("UPSERT INTO TABLE2 VALUES ('1', \"DSDD\", x'EFBFBDC7AB')");
    }

    @Test
    public void testHexValues2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("UPSERT INTO TABLE2 VALUES ('1', \"DSDD\", 0xEFBFBDC7AB)");
    }

    @Test
    public void testHexValues3() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("UPSERT INTO TABLE2 VALUES ('1', \"DSDD\", 0xabcde)");
    }

    @Test
    public void testDuplicateKey() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "UPSERT INTO Users0 (UserId, Key, Value) VALUES (51311, 'T_211', 18) ON DUPLICATE KEY UPDATE Value = 18");
    }

}
