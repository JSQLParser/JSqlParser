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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DoubleValueTest {

    @Test
    public void testNullValue() {
        assertThrows(IllegalArgumentException.class, () -> {
            new DoubleValue(null);
        });
    }

    @Test
    public void testEmptyValue() {
        assertThrows(IllegalArgumentException.class, () -> {
            new DoubleValue("");
        });
    }

    @Test
    public void shouldSetStringValue() {
        final DoubleValue doubleValue = new DoubleValue("42");

        doubleValue.setValue(43D);

        assertEquals(43D, doubleValue.getValue());
        assertEquals("43.0", doubleValue.toString());
    }
}
