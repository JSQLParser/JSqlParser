/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.replace;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.Values;
import net.sf.jsqlparser.statement.upsert.Upsert;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ReplaceTest {
    @Test
    public void testReplaceSyntax1() throws JSQLParserException {
        String statement = "REPLACE mytable SET col1='as', col2=?, col3=565";
        Upsert upsert = (Upsert) TestUtils.assertSqlCanBeParsedAndDeparsed(statement, true);
        assertEquals("mytable", upsert.getTable().getName());
        assertEquals(3, upsert.getUpdateSets().size());
        assertEquals("col1", upsert.getUpdateSets().get(0).getColumns().get(0).getColumnName());
        assertEquals("col2", upsert.getUpdateSets().get(1).getColumns().get(0).getColumnName());
        assertEquals("col3", upsert.getUpdateSets().get(2).getColumns().get(0).getColumnName());
        assertEquals("'as'", upsert.getUpdateSets().get(0).getValues().get(0).toString());
        assertTrue(upsert.getUpdateSets().get(1).getValues().get(0) instanceof JdbcParameter);
        assertEquals(565L,
                ((LongValue) upsert.getUpdateSets().get(2).getValues().get(0)).getValue());
    }

    @Test
    public void testReplaceSyntax2() throws JSQLParserException {
        String statement = "REPLACE mytable (col1, col2, col3) VALUES ('as', ?, 565)";
        Upsert replace = (Upsert) TestUtils.assertSqlCanBeParsedAndDeparsed(statement, true);
        assertEquals("mytable", replace.getTable().getName());
        assertEquals(3, replace.getColumns().size());
        assertEquals("col1", ((Column) replace.getColumns().get(0)).getColumnName());
        assertEquals("col2", ((Column) replace.getColumns().get(1)).getColumnName());
        assertEquals("col3", ((Column) replace.getColumns().get(2)).getColumnName());

        ExpressionList expressions =
                (ExpressionList) ((Values) replace.getSelect()).getExpressions();
        assertEquals("as", ((StringValue) expressions.get(0)).getValue());
        assertTrue(expressions.get(1) instanceof JdbcParameter);
        assertEquals(565, ((LongValue) expressions.get(2)).getValue());
    }

    @Test
    public void testReplaceSyntax3() throws JSQLParserException {
        String statement = "REPLACE mytable (col1, col2, col3) SELECT * FROM mytable3";
        Upsert replace = (Upsert) TestUtils.assertSqlCanBeParsedAndDeparsed(statement, true);
        assertEquals("mytable", replace.getTable().getName());
        assertEquals(3, replace.getColumns().size());
        assertEquals("col1", ((Column) replace.getColumns().get(0)).getColumnName());
        assertEquals("col2", ((Column) replace.getColumns().get(1)).getColumnName());
        assertEquals("col3", ((Column) replace.getColumns().get(2)).getColumnName());
        assertNotNull(replace.getSelect());
    }

    @Test
    public void testProblemReplaceParseDeparse() throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(
                "REPLACE a_table (ID, A, B) SELECT A_ID, A, B FROM b_table", true);
    }

    @Test
    public void testProblemMissingIntoIssue389() throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(
                "REPLACE INTO mytable (key, data) VALUES (1, \"aaa\")", true);
    }

    @Test
    public void testMultipleValues() throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(
                "REPLACE INTO mytable (col1, col2, col3) VALUES (1, \"aaa\", now()), (2, \"bbb\", now())",
                true);
    }
}
