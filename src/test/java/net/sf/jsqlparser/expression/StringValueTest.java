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

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author toben
 */
public class StringValueTest {

    public StringValueTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

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
}
