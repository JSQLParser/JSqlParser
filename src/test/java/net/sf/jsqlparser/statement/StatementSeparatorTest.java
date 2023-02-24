/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2023 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import net.sf.jsqlparser.*;
import net.sf.jsqlparser.parser.*;
import net.sf.jsqlparser.test.*;
import org.junit.jupiter.api.*;

public class StatementSeparatorTest {

    @Test
    void testDoubleNewLine() throws JSQLParserException {
        String sqlStr =
                "SELECT * FROM DUAL\n\n\nSELECT * FROM DUAL\n\n\n\nSELECT * FROM dual\n\n\n\n\nSELECT * FROM dual";
        Statements statements = CCJSqlParserUtil.parseStatements(sqlStr);
        Assertions.assertEquals(4, statements.getStatements().size());
    }

    @Test
    void testNewLineSlash() throws JSQLParserException {
        String sqlStr =
                "SELECT * FROM DUAL\n\n\nSELECT * FROM DUAL\n/\nSELECT * FROM dual\n/\n\nSELECT * FROM dual";
        Statements statements = CCJSqlParserUtil.parseStatements(sqlStr);
        Assertions.assertEquals(4, statements.getStatements().size());
    }

    @Test
    void testNewLineGo() throws JSQLParserException {
        String sqlStr =
                "SELECT * FROM DUAL\n\n\nSELECT * FROM DUAL\nGO\nSELECT * FROM dual\ngo\n\nSELECT * FROM dual\ngo";
        Statements statements = CCJSqlParserUtil.parseStatements(sqlStr);
        Assertions.assertEquals(4, statements.getStatements().size());
    }

    @Test
    void testOracleBlock() throws JSQLParserException {
        String sqlStr = "BEGIN\n" + "\n" + "SELECT * FROM TABLE;\n" + "\n" + "END\n" + "/\n";
        Statement statement = TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
        System.out.println(statement);
    }

    @Test
    void testMSSQLBlock() throws JSQLParserException {
        String sqlStr = "create view MyView1 as\n" + "select Id,Name from table1\n" + "go\n"
                + "create view MyView2 as\n" + "select Id,Name from table1\n" + "go";
        Statements statements = CCJSqlParserUtil.parseStatements(sqlStr);
        Assertions.assertEquals(2, statements.getStatements().size());
    }
}
