package net.sf.jsqlparser.test.upsert;

import static org.junit.Assert.*;

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
        assertEquals(2, upsert.getColumns().size());
        assertEquals("NAME", ((Column) upsert.getColumns().get(0)).getColumnName());
        assertEquals("ID", ((Column) upsert.getColumns().get(1)).getColumnName());
        assertEquals(2, ((ExpressionList) upsert.getItemsList()).getExpressions().size());
        assertEquals("foo",
                ((StringValue) ((ExpressionList) upsert.getItemsList()).getExpressions().get(0)).
                        getValue());
        assertEquals(123, ((LongValue) ((ExpressionList) upsert.getItemsList()).getExpressions().
                get(1)).getValue());
        assertEquals(statement, "" + upsert);
    }

    @Test
    public void testUpsertDuplicate() throws JSQLParserException {
        String statement="UPSERT INTO TEST (ID, COUNTER) VALUES (123, 0) ON DUPLICATE KEY UPDATE COUNTER = COUNTER + 1";
        Upsert upsert= (Upsert) parserManager.parse(new StringReader(statement));
        assertEquals("TEST", upsert.getTable().getName());
        assertEquals(2, upsert.getColumns().size());
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
        assertEquals(statement, "" + upsert);
    }

    @Test
    public void testUpsertSelect() throws JSQLParserException {
        String statement="UPSERT INTO test.targetTable (col1, col2) SELECT * FROM test.sourceTable";
        Upsert upsert= (Upsert) parserManager.parse(new StringReader(statement));
        assertEquals("test.targetTable", upsert.getTable().getFullyQualifiedName());
        assertEquals(2, upsert.getColumns().size());
        assertEquals("col1", ((Column) upsert.getColumns().get(0)).getColumnName());
        assertEquals("col2", ((Column) upsert.getColumns().get(1)).getColumnName());
        assertNull(upsert.getItemsList());
        assertNotNull(upsert.getSelect());
        assertEquals("test.sourceTable",
                ((Table) ((PlainSelect) upsert.getSelect().getSelectBody()).getFromItem()).getFullyQualifiedName());
        assertEquals(statement, "" + upsert);
    }

    @Test
    public void testUpsertN() throws JSQLParserException {
        String statement="UPSERT INTO TEST VALUES ('foo', 'bar', 3)";
        Upsert upsert= (Upsert) parserManager.parse(new StringReader(statement));
        assertEquals("TEST", upsert.getTable().getName());
        assertEquals(3, ((ExpressionList) upsert.getItemsList()).getExpressions().size());
        assertEquals("foo",
                ((StringValue) ((ExpressionList) upsert.getItemsList()).getExpressions().get(0)).
                        getValue());
        assertEquals("bar",
                ((StringValue) ((ExpressionList) upsert.getItemsList()).getExpressions().get(1)).
                        getValue());
        assertEquals(3, ((LongValue) ((ExpressionList) upsert.getItemsList()).getExpressions().
                get(2)).getValue());
        assertEquals(statement, "" + upsert);
    }
    
}
