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

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    public void testUnsupportedStatementsFirstInBlock() throws JSQLParserException {
        String sqlStr = "This is an unsupported statement; Select * from dual; Select * from dual;";

        Statements statements = CCJSqlParserUtil.parseStatements(sqlStr,
                parser -> parser.withUnsupportedStatements(true));
        Assertions.assertEquals(3, statements.getStatements().size());
        Assertions.assertInstanceOf(UnsupportedStatement.class, statements.getStatements().get(0));
        Assertions.assertInstanceOf(Select.class, statements.getStatements().get(1));
        Assertions.assertInstanceOf(Select.class, statements.getStatements().get(2));

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
                parser -> parser.withUnsupportedStatements(true));
        Assertions.assertEquals(3, statements.getStatements().size());

        Assertions.assertInstanceOf(Select.class, statements.getStatements().get(0));
        Assertions.assertInstanceOf(UnsupportedStatement.class, statements.getStatements().get(1));
        Assertions.assertInstanceOf(Select.class, statements.getStatements().get(2));

        // This will not fail, but always return the Unsupported Statements
        // Since we can't LOOKAHEAD in the Statements() production

        // Assertions.assertThrowsExactly(JSQLParserException.class, new Executable() {
        // @Override
        // public void execute() throws Throwable {
        // CCJSqlParserUtil.parseStatements(sqlStr, parser ->
        // parser.withUnsupportedStatements(false) );
        // }
        // });
    }

    @Test
    void testAlter() throws JSQLParserException {
        String sqlStr =
                "ALTER INDEX idx_t_fa RENAME TO idx_t_fb";
        Statement statement = TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
        Assertions.assertTrue(statement instanceof UnsupportedStatement);
    }

    @Test
    void testCreate() throws JSQLParserException {
        String sqlStr =
                "create trigger stud_marks before INSERT on Student for each row set Student.total = Student.subj1 + Student.subj2, Student.per = Student.total * 60 / 100";
        Statement statement = TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
        Assertions.assertTrue(statement instanceof UnsupportedStatement);

        sqlStr =
                "create domain TNOTIFICATION_ACTION as ENUM ('ADD', 'CHANGE', 'DEL')";
        statement = TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
        assertTrue(statement instanceof UnsupportedStatement);
    }
}
