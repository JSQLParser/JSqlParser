/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2022 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

public class UnsupportedStatementTest {
    @Test
    public void testSingleUnsupportedStatement() throws JSQLParserException {
        String sqlStr = "this is an unsupported statement";

        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true,
                parser -> parser.withUnsupportedStatements(true));

        Assertions.assertThrowsExactly(JSQLParserException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                CCJSqlParserUtil.parse(sqlStr, parser -> parser.withUnsupportedStatements(false));
            }
        });
    }

    // This test does not work since the first statement MUST be a regular statement
    // for the current grammar to work
    @Test
    @Disabled
    public void testUnsupportedStatementsFirstInBlock() throws JSQLParserException {
        String sqlStr = "This is an unsupported statement; Select * from dual; Select * from dual;";

        Statements statements = CCJSqlParserUtil.parseStatements(sqlStr,
                parser -> parser.withUnsupportedStatements(true));
        Assertions.assertEquals(3, statements.size());
        Assertions.assertInstanceOf(UnsupportedStatement.class, statements.get(0));
        Assertions.assertInstanceOf(Select.class, statements.get(1));
        Assertions.assertInstanceOf(Select.class, statements.get(2));

        Assertions.assertThrowsExactly(JSQLParserException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                CCJSqlParserUtil.parseStatements(sqlStr,
                        parser -> parser.withUnsupportedStatements(false));
            }
        });
    }

    @Test
    public void testUnsupportedStatementsMiddleInBlock() throws JSQLParserException {
        String sqlStr = "Select * from dual; This is an unsupported statement; Select * from dual;";

        Statements statements = CCJSqlParserUtil.parseStatements(sqlStr,
                parser -> parser.withUnsupportedStatements(true).withErrorRecovery(true));
        Assertions.assertEquals(3, statements.size());

        Assertions.assertInstanceOf(Select.class, statements.get(0));
        Assertions.assertInstanceOf(UnsupportedStatement.class, statements.get(1));
        Assertions.assertInstanceOf(Select.class, statements.get(2));

        Assertions.assertThrowsExactly(JSQLParserException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                CCJSqlParserUtil.parseStatements(sqlStr,
                        parser -> parser.withUnsupportedStatements(false));
            }
        });
    }

    @Test
    public void testTwoUnsupportedStatementsMiddleInBlock() throws JSQLParserException {
        String sqlStr =
                "Select * from dual; This is an unsupported statement; Some more rubbish; Select * from dual;";

        Statements statements = CCJSqlParserUtil.parseStatements(sqlStr,
                parser -> parser.withUnsupportedStatements(true).withErrorRecovery(true));
        Assertions.assertEquals(4, statements.size());

        Assertions.assertInstanceOf(Select.class, statements.get(0));
        Assertions.assertInstanceOf(UnsupportedStatement.class, statements.get(1));
        Assertions.assertInstanceOf(UnsupportedStatement.class, statements.get(2));
        Assertions.assertInstanceOf(Select.class, statements.get(3));

        Assertions.assertThrowsExactly(JSQLParserException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                CCJSqlParserUtil.parseStatements(sqlStr,
                        parser -> parser.withUnsupportedStatements(false));
            }
        });
    }

    @Test
    public void testCaptureRestIssue1993() throws JSQLParserException {
        String sqlStr = "Select 1; ALTER TABLE \"inter\".\"inter_user_rec\" \n"
                + "  OWNER TO \"postgres\"; select 2; select 3;";
        Statements statements = CCJSqlParserUtil.parseStatements(sqlStr,
                parser -> parser.withErrorRecovery(false));
        Assertions.assertEquals(4, statements.size());
    }

    @Test
    void testAlter() throws JSQLParserException {
        String sqlStr =
                "ALTER INDEX idx_t_fa RENAME TO idx_t_fb";
        Statement statement = TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
        assertInstanceOf(UnsupportedStatement.class, statement);
    }

    @Test
    void testRefresh() throws JSQLParserException {
        String sqlStr = "REFRESH MATERIALIZED VIEW CONCURRENTLY my_view WITH NO DATA";
        Statements statement = CCJSqlParserUtil.parseStatements(sqlStr);
        assertTrue(statement.get(0) instanceof UnsupportedStatement);
    }

    @Test
    void testCreate() throws JSQLParserException {
        String sqlStr =
                "create trigger stud_marks before INSERT on Student for each row set Student.total = Student.subj1 + Student.subj2, Student.per = Student.total * 60 / 100";
        Statement statement = TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
        assertTrue(statement instanceof UnsupportedStatement);

        sqlStr =
                "create domain TNOTIFICATION_ACTION as ENUM ('ADD', 'CHANGE', 'DEL')";
        statement = TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
        assertTrue(statement instanceof UnsupportedStatement);
    }

    @Test
    void testFunctions() throws JSQLParserException {
        String sqlStr =
                "CREATE OR REPLACE FUNCTION func_example(foo integer)\n"
                        + "RETURNS integer AS $$\n"
                        + "BEGIN\n"
                        + "  RETURN foo + 1;\n"
                        + "END\n"
                        + "$$ LANGUAGE plpgsql;\n"
                        + "\n"
                        + "CREATE OR REPLACE FUNCTION func_example2(IN foo integer, OUT bar integer)\n"
                        + "AS $$\n"
                        + "BEGIN\n"
                        + "    SELECT foo + 1 INTO bar;\n"
                        + "END\n"
                        + "$$ LANGUAGE plpgsql;";

        Statements statements = CCJSqlParserUtil.parseStatements(sqlStr);
        assertEquals(2, statements.size());
    }

    @Test
    void testSQLServerSetStatementIssue1984() throws JSQLParserException {
        String sqlStr = "SET IDENTITY_INSERT tb_inter_d2v_transfer on";
        Statements statements = CCJSqlParserUtil.parseStatements(sqlStr,
                parser -> parser.withUnsupportedStatements(true));
        assertEquals(1, statements.size());
        assertInstanceOf(UnsupportedStatement.class, statements.get(0));

        TestUtils.assertStatementCanBeDeparsedAs(statements.get(0), sqlStr, true);

        Statement statement = CCJSqlParserUtil.parse(sqlStr,
                parser -> parser.withUnsupportedStatements(true));
        assertInstanceOf(UnsupportedStatement.class, statement);

        TestUtils.assertStatementCanBeDeparsedAs(statement, sqlStr, true);
    }

    @Test
    void testInformixSetStatementIssue1945() throws JSQLParserException {
        String sqlStr = "set isolation to dirty read;";
        Statement statement = CCJSqlParserUtil.parse(sqlStr,
                parser -> parser.withUnsupportedStatements(true));
        assertInstanceOf(UnsupportedStatement.class, statement);
        TestUtils.assertStatementCanBeDeparsedAs(statement, sqlStr, true);

        TestUtils.assertSqlCanBeParsedAndDeparsed(
                "set isolation to dirty read;", true, parser -> parser.withUnsupportedStatements());
    }

    @Test
    void testRedshiftSetStatementIssue1708() throws JSQLParserException {
        Statement st = TestUtils.assertSqlCanBeParsedAndDeparsed(
                "SET x TO y;", true, parser -> parser.withUnsupportedStatements());
        assertInstanceOf(UnsupportedStatement.class, st);
    }
}
