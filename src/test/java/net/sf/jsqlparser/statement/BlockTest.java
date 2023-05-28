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
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * @author Tobias Warneke (t.warneke@gmx.net)
 */
public class BlockTest {

    /**
     * Test of getStatements method, of class Block.
     */
    @Test
    public void testGetStatements() throws JSQLParserException {
        String sqlStr = "begin\n" + "select * from feature;\n" + "end;";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    public void testBlock2() throws JSQLParserException {
        String sqlStr = "begin\n" + "update table1 set a = 'xx' where b = 'condition1';\n" + "update table1 set a = 'xx' where b = 'condition2';\n" + "end;";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    public void testBlock3() throws JSQLParserException {
        Statements stmts = CCJSqlParserUtil.parseStatements("begin\nselect * from feature;\nend");
        Block block = (Block) stmts.getStatements().get(0);
        assertFalse(block.getStatements().getStatements().isEmpty());
    }

    @Test
    public void testBlockToStringIsNullSafe() throws JSQLParserException {
        Block block = new Block();
        block.setStatements(null);
        assertEquals("BEGIN\n" + "END", block.toString());
    }

    @Test
    public void testIfElseBlock() throws JSQLParserException {
        String sqlStr = "if (a=b) begin\n" + "update table1 set a = 'xx' where b = 'condition1';\n" + "update table1 set a = 'xx' where b = 'condition2';\n" + "end";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
        String sqlStr2 = "if (a=b) begin\n" + "update table1 set a = 'xx' where b = 'condition1';\n" + "update table1 set a = 'xx' where b = 'condition2';\n" + "end;\n" + "else begin\n" + "update table1 set a = 'xx' where b = 'condition1';\n" + "update table1 set a = 'xx' where b = 'condition2';\n" + "end;";
        Statements statements = CCJSqlParserUtil.parseStatements(sqlStr2);
        for (Statement stm : statements.getStatements()) {
            TestUtils.assertDeparse(stm, sqlStr2, true);
        }
    }
}
