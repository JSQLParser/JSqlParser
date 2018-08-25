/*
 * Copyright (C) 2018 JSQLParser.
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
package net.sf.jsqlparser.statement;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Tobias Warneke (t.warneke@gmx.net)
 */
public class BlockTest {

    public BlockTest() {
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
     * Test of getStatements method, of class Block.
     */
    @Test
    public void testGetStatements() throws JSQLParserException {
        Statements stmts = CCJSqlParserUtil.parseStatements("begin\nselect * from feature;\nend");
        assertEquals("BEGIN\n"
                + "SELECT * FROM feature;\n"
                + "END;\n", stmts.toString());
    }

    @Test
    public void testBlock2() throws JSQLParserException {
        Statements stmts = CCJSqlParserUtil.parseStatements("begin\n"
                + "update table1 set a = 'xx' where b = 'condition1';\n"
                + "update table1 set a = 'xx' where b = 'condition2';\n"
                + "end;");
        assertEquals("BEGIN\n"
                + "UPDATE table1 SET a = 'xx' WHERE b = 'condition1';\n"
                + "UPDATE table1 SET a = 'xx' WHERE b = 'condition2';\n"
                + "END;\n"
                + "", stmts.toString());

    }
}
