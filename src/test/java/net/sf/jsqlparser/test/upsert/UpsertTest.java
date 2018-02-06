package net.sf.jsqlparser.test.upsert;

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.StringReader;

import org.junit.Test;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.upsert.Upsert;

public class UpsertTest {

    CCJSqlParserManager parserManager = new CCJSqlParserManager();

    @Test
    public void testUpsert() throws JSQLParserException {
        String statement ="UPSERT INTO TEST (NAME, ID) VALUES ('foo', 123)";
        Upsert upsert = (Upsert) parserManager.parse(new StringReader(statement));
        assertEquals("TEST", upsert.getTable().getName());
        assertTrue(upsert.isUseValues());
        assertEquals(2, upsert.getColumns().size());
        assertEquals("NAME", ((Column) upsert.getColumns().get(0)).getColumnName());
        assertEquals("ID", ((Column) upsert.getColumns().get(1)).getColumnName());
        assertEquals(2, ((ExpressionList) upsert.getItemsList()).getExpressions().size());
        assertEquals("foo",
                ((StringValue) ((ExpressionList) upsert.getItemsList()).getExpressions().get(0)).
                        getValue());
        assertEquals(123, ((LongValue) ((ExpressionList) upsert.getItemsList()).getExpressions().
                get(1)).getValue());
        assertFalse(upsert.isUseSelectBrackets());
        assertFalse(upsert.isUseDuplicate());
        assertEquals(statement, "" + upsert);
    }

    @Test
    public void testUpsertDuplicate() throws JSQLParserException {
        String statement="UPSERT INTO TEST (ID, COUNTER) VALUES (123, 0) ON DUPLICATE KEY UPDATE COUNTER = COUNTER + 1";
        Upsert upsert= (Upsert) parserManager.parse(new StringReader(statement));
        assertEquals("TEST", upsert.getTable().getName());
        assertEquals(2, upsert.getColumns().size());
        assertTrue(upsert.isUseValues());
        assertEquals("ID", ((Column) upsert.getColumns().get(0)).getColumnName());
        assertEquals("COUNTER", ((Column) upsert.getColumns().get(1)).getColumnName());
        assertEquals(2, ((ExpressionList) upsert.getItemsList()).getExpressions().size());
        assertEquals(123, ((LongValue) ((ExpressionList) upsert.getItemsList()).getExpressions().
                get(0)).getValue());
        assertEquals(0, ((LongValue) ((ExpressionList) upsert.getItemsList()).getExpressions().
                get(1)).getValue());
        assertEquals(1, upsert.getDuplicateUpdateColumns().size());
        assertEquals("COUNTER", ((Column) upsert.getDuplicateUpdateColumns().get(0)).getColumnName());
        assertEquals(1, upsert.getDuplicateUpdateExpressionList().size());
        assertEquals("COUNTER + 1", upsert.getDuplicateUpdateExpressionList().get(0).toString());
        assertFalse(upsert.isUseSelectBrackets());
        assertTrue(upsert.isUseDuplicate());
        assertEquals(statement, "" + upsert);
    }

    @Test
    public void testUpsertSelect() throws JSQLParserException {
        String statement="UPSERT INTO test.targetTable (col1, col2) SELECT * FROM test.sourceTable";
        Upsert upsert= (Upsert) parserManager.parse(new StringReader(statement));
        assertEquals("test.targetTable", upsert.getTable().getFullyQualifiedName());
        assertEquals(2, upsert.getColumns().size());
        assertFalse(upsert.isUseValues());
        assertEquals("col1", ((Column) upsert.getColumns().get(0)).getColumnName());
        assertEquals("col2", ((Column) upsert.getColumns().get(1)).getColumnName());
        assertNull(upsert.getItemsList());
        assertNotNull(upsert.getSelect());
        assertEquals("test.sourceTable",
                ((Table) ((PlainSelect) upsert.getSelect().getSelectBody()).getFromItem()).getFullyQualifiedName());
        assertFalse(upsert.isUseDuplicate());
        assertEquals(statement, "" + upsert);
    }

    @Test
    public void testUpsertN() throws JSQLParserException {
        String statement="UPSERT INTO TEST VALUES ('foo', 'bar', 3)";
        Upsert upsert= (Upsert) parserManager.parse(new StringReader(statement));
        assertEquals("TEST", upsert.getTable().getName());
        assertEquals(3, ((ExpressionList) upsert.getItemsList()).getExpressions().size());
        assertTrue(upsert.isUseValues());
        assertEquals("foo",
                ((StringValue) ((ExpressionList) upsert.getItemsList()).getExpressions().get(0)).
                        getValue());
        assertEquals("bar",
                ((StringValue) ((ExpressionList) upsert.getItemsList()).getExpressions().get(1)).
                        getValue());
        assertEquals(3, ((LongValue) ((ExpressionList) upsert.getItemsList()).getExpressions().
                get(2)).getValue());
        assertFalse(upsert.isUseSelectBrackets());
        assertFalse(upsert.isUseDuplicate());
        assertEquals(statement, "" + upsert);
    }
    
    @Test
    public void testUpsertMultiRowValue() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("UPSERT INTO mytable (col1, col2) VALUES (a, b), (d, e)");
    }

    @Test
    public void testUpsertMultiRowValueDifferent() throws JSQLParserException {
        try {
            assertSqlCanBeParsedAndDeparsed("UPSERT INTO mytable (col1, col2) VALUES (a, b), (d, e, c)");
        } catch (Exception e) {
            return;
        }
        fail("should not work");
    }

    @Test
    public void testSimpleUpsert() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("UPSERT INTO example (num, name, address, tel) VALUES (1, 'name', 'test ', '1234-1234')");
    }

    @Test
    public void testUpsertHasSelect() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("UPSERT INTO mytable (mycolumn) SELECT mycolumn FROM mytable");
        assertSqlCanBeParsedAndDeparsed("UPSERT INTO mytable (mycolumn) (SELECT mycolumn FROM mytable)");
    }

    @Test
    public void testUpsertWithSelect() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("UPSERT INTO mytable (mycolumn) WITH a AS (SELECT mycolumn FROM mytable) SELECT mycolumn FROM a");
        assertSqlCanBeParsedAndDeparsed("UPSERT INTO mytable (mycolumn) (WITH a AS (SELECT mycolumn FROM mytable) SELECT mycolumn FROM a)");
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
        assertSqlCanBeParsedAndDeparsed("UPSERT INTO Users0 (UserId, Key, Value) VALUES (51311, 'T_211', 18) ON DUPLICATE KEY UPDATE Value = 18");
    }
    
}
