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

import net.sf.jsqlparser.statement.create.domain.CreateDomain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.function.Consumer;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.alter.AlterRelation;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.trigger.CreateTrigger;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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
    void testFormerlyUnsupportedAlterIndex() throws JSQLParserException {
        String sqlStr =
                "ALTER INDEX idx_t_fa RENAME TO idx_t_fb";
        Statement statement = TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
        assertInstanceOf(AlterRelation.class, statement);
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
        assertInstanceOf(CreateTrigger.class, statement);

        sqlStr =
                "create domain TNOTIFICATION_ACTION as ENUM ('ADD', 'CHANGE', 'DEL')";
        statement = TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
        assertInstanceOf(CreateDomain.class, statement);
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

    @ParameterizedTest
    @ValueSource(strings = {
            "CREATE LANGUAGE plperl",
            "CREATE OR REPLACE TRUSTED PROCEDURAL LANGUAGE plsample HANDLER plsample_call_handler",
            "CREATE OPERATOR CLASS gist__int_ops DEFAULT FOR TYPE _int4 USING gist AS OPERATOR 3 &&",
            "CREATE OPERATOR FAMILY myfam USING btree",
            "CREATE DEFAULT CONVERSION myconv FOR 'LATIN1' TO 'UTF8' FROM latin1_to_utf8",
            "CREATE TRANSFORM FOR hstore LANGUAGE plpython3u (FROM SQL WITH FUNCTION "
                    + "hstore_to_plpython(internal), TO SQL WITH FUNCTION plpython_to_hstore(internal))",
            "CREATE OR REPLACE AGGREGATE myavg(float8) (SFUNC = float8_accum, STYPE = float8[])"})
    void unknownCreateFallsBackToUnsupportedStatement(String sql) throws JSQLParserException {
        for (Consumer<CCJSqlParser> dialect : Arrays.<Consumer<CCJSqlParser>>asList(parser -> {
        }, parser -> parser.withDialect(Dialect.POSTGRESQL))) {
            Statement statement = CCJSqlParserUtil.parse(sql, dialect);

            assertInstanceOf(UnsupportedStatement.class, statement);
            // the captured tokens render with single spaces; OR REPLACE is kept
            assertEquals(sql.replaceAll("\\s+", ""), statement.toString().replaceAll("\\s+", ""));
        }
    }

    @Test
    void unknownCreateDoesNotStopAScript() throws JSQLParserException {
        Statements statements = CCJSqlParserUtil.parseStatements(
                "CREATE TABLE a (x int); CREATE LANGUAGE plperl; CREATE INDEX i ON a (x);",
                parser -> parser.withDialect(Dialect.POSTGRESQL));

        assertEquals(3, statements.size());
        assertInstanceOf(CreateTable.class, statements.get(0));
        assertInstanceOf(UnsupportedStatement.class, statements.get(1));
        assertInstanceOf(CreateIndex.class, statements.get(2));
    }

    @Test
    void testRedshiftSetStatementIssue1708() throws JSQLParserException {
        Statement st = TestUtils.assertSqlCanBeParsedAndDeparsed(
                "SET x TO y;", true, parser -> parser.withUnsupportedStatements());
        SetStatement set = assertInstanceOf(SetStatement.class, st);
        assertEquals(SetStatement.AssignmentOperator.TO, set.getAssignmentOperator());
    }
}
