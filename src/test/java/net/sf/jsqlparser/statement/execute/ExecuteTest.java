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
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import static net.sf.jsqlparser.test.TestUtils.*;

/**
 *
 * @author toben
 */
public class ExecuteTest {

    public ExecuteTest() {}

    @BeforeClass
    public static void setUpClass() {}

    @AfterClass
    public static void tearDownClass() {}

    @Before
    public void setUp() {}

    @After
    public void tearDown() {}

    /**
     * Test of accept method, of class Execute.
     *
     * @throws net.sf.jsqlparser.JSQLParserException
     */
    @Test
    public void testAcceptExecute() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("EXECUTE myproc 'a', 2, 'b'");
    }

    @Test
    public void testAcceptExec() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("EXEC myproc 'a', 2, 'b'");
    }

    @Test
    public void testAcceptCall() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CALL myproc 'a', 2, 'b'");
    }

    @Test
    public void testCallTeradataSimple() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("call abc(152)", true);
        assertSqlCanBeParsedAndDeparsed("call abc(:a ,'fdf', ?, ?)", true);
        assertSqlCanBeParsedAndDeparsed("CALL spAccount(test.value_expression(3, 4), outp1)");
        assertSqlCanBeParsedAndDeparsed("CALL myintz(CAST('32767' AS INTEGER))");
        assertSqlCanBeParsedAndDeparsed("CALL spSample2(p1, 20 * 2, 30 + 40)");
        assertSqlCanBeParsedAndDeparsed("CALL spSample2(:AppVar1, 3 + :AppVar2, 3 + 4 + :AppVar3)");
    }

    @Test
    public void testCallTeradataAs() throws JSQLParserException {

        assertSqlCanBeParsedAndDeparsed("CALL spSample1(1, CAST(((2 + 4) * 2) AS FORMAT 'ZZZ9'), 3)");
        assertSqlCanBeParsedAndDeparsed(
            "CALL spSample2(CAST(CAST(p1 AS CHARACTER (10)) AS TITLE 'OutputValue'), CAST(20 AS SMALLINT), 30 + 40)");
        assertSqlCanBeParsedAndDeparsed("CALL spSample1(p1, :a, 3)");
        assertSqlCanBeParsedAndDeparsed(
            "CALL spSample1(CAST(p1 AS NAMED AA TITLE 'OUT VALUE'), CAST(((20 * 2) + 3) AS TITLE 'INOUT VALUE'), 1)");
        assertSqlCanBeParsedAndDeparsed(
            "CALL spSample2(:AppVar1, :AppVar1 + :AppVar2, CAST(:AppVar3 AS FORMAT 'Z,ZZ9'))");
    }

    @Ignore("TBD")
    @Test
    public void testCallTeradataRETURNS() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
            "CALL myXSP1(10, RESULT_B RETURNS INTEGER, RESULT_C, RESULT_D RETURNS STYLE t1.int_col,  RESULT_E, RESULT_F RETURNS INTEGER)");
    }
}
