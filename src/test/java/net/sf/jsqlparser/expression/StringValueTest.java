/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.sf.jsqlparser.*;
import net.sf.jsqlparser.test.*;
import org.junit.jupiter.api.Test;

/**
 *
 * @author toben
 */
public class StringValueTest {

    @Test
    public void testGetValue() {
        StringValue instance = new StringValue("'*'");
        String expResult = "*";
        String result = instance.getValue();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetValue2_issue329() {
        StringValue instance = new StringValue("*");
        String expResult = "*";
        String result = instance.getValue();
        assertEquals(expResult, result);
    }

    /**
     * Test of getNotExcapedValue method, of class StringValue.
     */
    @Test
    public void testGetNotExcapedValue() {
        StringValue instance = new StringValue("'*''*'");
        String expResult = "*'*";
        String result = instance.getNotExcapedValue();
        assertEquals(expResult, result);
    }

    @Test
    public void testPrefixes() {
        checkStringValue("E'test'", "test", "E");
        checkStringValue("'test'", "test", null);

    }

    private void checkStringValue(String original, String expectedValue, String expectedPrefix) {
        StringValue v = new StringValue(original);
        assertEquals(expectedValue, v.getValue());
        assertEquals(expectedPrefix, v.getPrefix());
    }

    @Test
    public void testIssue1566EmptyStringValue() {
        StringValue v = new StringValue("'");
        assertEquals("'", v.getValue());
    }

    @Test
    public void testOracleAlternativeQuoting() throws JSQLParserException {
        String sqlStr = "COMMENT ON COLUMN EMP.NAME IS q'{Na'm\\e}'";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        sqlStr = "COMMENT ON COLUMN EMP.NAME IS q'(Na'm\\e)'";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        sqlStr = "COMMENT ON COLUMN EMP.NAME IS q'[Na'm\\e]'";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        sqlStr = "COMMENT ON COLUMN EMP.NAME IS q''Na'm\\e]''";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        sqlStr = "select q'{Its good!}' from dual";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        sqlStr = "select q'{It's good!}' from dual";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }
}
