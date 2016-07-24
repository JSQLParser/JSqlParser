/*
 * Copyright (C) 2014 JSQLParser.
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

package net.sf.jsqlparser.statement.execute;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.statement.StatementVisitor;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static net.sf.jsqlparser.test.TestUtils.*;

/**
 *
 * @author toben
 */
public class ExecuteTest {
    
    public ExecuteTest() {
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

    /**
     * Test of accept method, of class Execute.
     * @throws net.sf.jsqlparser.JSQLParserException
     */
    @Test
    public void testAccept() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("EXECUTE myproc 'a', 2, 'b'");
    }
}
