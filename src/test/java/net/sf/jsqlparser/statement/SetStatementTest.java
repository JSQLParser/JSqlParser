/*
 * Copyright (C) 2015 JSQLParser.
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

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;

import java.io.StringReader;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;

/**
 *
 * @author toben
 */
public class SetStatementTest {

    CCJSqlParserManager parserManager = new CCJSqlParserManager();

    public SetStatementTest() {}

    @BeforeClass
    public static void setUpClass() {}

    @AfterClass
    public static void tearDownClass() {}

    @Before
    public void setUp() {}

    @After
    public void tearDown() {}

    @Test
    public void testSimpleSet() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SET statement_timeout = 0");
    }

    @Test
    public void testIssue373() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SET deferred_name_resolution true");
    }

    @Test
    public void testIssue373_2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SET tester 5");
    }

    @Test
    public void testSetQueryBandTeradata() throws JSQLParserException {
        // make sure it is parseable (dont want to deparse atm)
        parserManager.parse(new StringReader("SET QUERY_BAND = 'UTILITYNAME=MULTLOAD;' UPDATE FOR SESSION"));
    }
}
