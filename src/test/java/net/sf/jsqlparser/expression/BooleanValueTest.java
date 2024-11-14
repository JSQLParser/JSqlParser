/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2024 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author tw
 */
public class BooleanValueTest {

    @Test
    public void testTrueValue() {
        BooleanValue value = new BooleanValue("true");

        assertTrue(value.getValue());
    }

    @Test
    public void testFalseValue() {
        BooleanValue value = new BooleanValue("false");

        assertFalse(value.getValue());
    }

    @Test
    public void testWrongValueAsFalseLargeNumber() {
        BooleanValue value = new BooleanValue("test");

        assertFalse(value.getValue());
    }

    @Test
    public void testNullStringValue() {
        BooleanValue value = new BooleanValue(null);

        assertFalse(value.getValue());
    }
}
