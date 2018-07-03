/*
 * Copyright (C) 2017 JSQLParser.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package net.sf.jsqlparser.expression;

import java.math.BigInteger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * 
 * @author tw
 */
public class LongValueTest {

    public LongValueTest() {}

    @BeforeClass
    public static void setUpClass() {}

    @AfterClass
    public static void tearDownClass() {}

    @Before
    public void setUp() {}

    @After
    public void tearDown() {}

    @Test
    public void testSimpleNumber() {
        LongValue value = new LongValue("123");

        assertEquals("123", value.getStringValue());
        assertEquals(123L, value.getValue());
        assertEquals(new BigInteger("123"), value.getBigIntegerValue());
    }

    @Test
    public void testLargeNumber() {
        final String largeNumber = "20161114000000035001";
        LongValue value = new LongValue(largeNumber);

        assertEquals(largeNumber, value.getStringValue());
        try {
            value.getValue();
            fail("should not work");
        } catch (Exception e) {
            //expected to fail
        }
        assertEquals(new BigInteger(largeNumber), value.getBigIntegerValue());
    }
}
