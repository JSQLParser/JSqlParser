/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
    @Test
    public void testBlock3() throws JSQLParserException {
        Statements stmts = CCJSqlParserUtil.parseStatements("begin\nselect * from feature;\nend");
        Block block =(Block) stmts.getStatements().get(0);
        assertFalse(block.getStatements().getStatements().isEmpty());
    }
    @Test
    public void testBlockToStringIsNullSafe() throws JSQLParserException {
        Block block = new Block();
        block.setStatements(null);
        assertEquals("BEGIN\n"
                 + "END", block.toString());
    }

}
