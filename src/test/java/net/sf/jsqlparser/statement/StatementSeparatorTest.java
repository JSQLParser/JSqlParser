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

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StatementSeparatorTest {

    @Test
    void testDoubleNewLine() throws JSQLParserException {
        String sqlStr =
                "SELECT * FROM DUAL\n\n\nSELECT * FROM DUAL\n\n\n\nSELECT * FROM dual\n\n\n\n\nSELECT * FROM dual";
        Statements statements = CCJSqlParserUtil.parseStatements(sqlStr);
        Assertions.assertEquals(4, statements.size());
    }

    @Test
    void testNewLineSlash() throws JSQLParserException {
        String sqlStr =
                "SELECT * FROM DUAL\n\n\nSELECT * FROM DUAL\n/\nSELECT * FROM dual\n/\n\nSELECT * FROM dual";
        Statements statements = CCJSqlParserUtil.parseStatements(sqlStr);
        Assertions.assertEquals(4, statements.size());
    }

    @Test
    void testNewLineGo() throws JSQLParserException {
        String sqlStr =
                "SELECT * FROM DUAL\n\n\nSELECT * FROM DUAL\nGO\nSELECT * FROM dual\ngo\n\nSELECT * FROM dual\ngo";
        Statements statements = CCJSqlParserUtil.parseStatements(sqlStr);
        Assertions.assertEquals(4, statements.size());
    }

    @Test
    void testNewLineNotGoIssue() throws JSQLParserException {
        String sqlStr =
                "select name,\ngoods from test_table";
        Statements statements = CCJSqlParserUtil.parseStatements(sqlStr);
        Assertions.assertEquals(1, statements.size());
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
        Assertions.assertEquals(2, statements.size());
    }

    @Test
    void testSOQLIncludes() throws JSQLParserException {
        String sqlStr =
                "select name,\ngoods from test_table where option includes ('option1', 'option2')";
        Statement statement = TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
        System.out.println(statement);
    }

    @Test
    void testSOQLExcludes() throws JSQLParserException {
        String sqlStr =
                "select name,\ngoods from test_table where option excludes ('option1', 'option2')";
        Statement statement = TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
        System.out.println(statement);
    }
}
