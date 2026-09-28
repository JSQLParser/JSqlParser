/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Stream;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserConstants;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.CreateFunctionalStatement;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.create.function.CreateFunction;
import net.sf.jsqlparser.statement.create.procedure.CreateProcedure;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class SqlRoutineBodyBoundaryTest {
    private static final String FOLLOWING = "SELECT 2718 AS after_routine";

    static Stream<Arguments> sqlRoutines() {
        Stream<Arguments> postgres = Stream.of(
                "CREATE FUNCTION f(x int) RETURNS int LANGUAGE SQL IMMUTABLE RETURN x + 1",
                "CREATE FUNCTION f(x int) RETURNS int LANGUAGE SQL RETURN "
                        + "CASE WHEN x IS NULL THEN 0 ELSE x END + CASE WHEN x > 0 THEN 1 ELSE 0 END",
                "CREATE FUNCTION f(x int) RETURNS boolean LANGUAGE SQL RETURN x IS NULL",
                "CREATE FUNCTION f(x begin) RETURNS begin LANGUAGE SQL RETURN x",
                "CREATE FUNCTION f(x public.begin) RETURNS public.begin LANGUAGE SQL RETURN x",
                "CREATE FUNCTION f() RETURNS int LANGUAGE SQL BEGIN ATOMIC "
                        + "SELECT t.end, t.case FROM t; SELECT 42; END",
                "CREATE FUNCTION f() RETURNS int LANGUAGE SQL RETURN (SELECT max(id) FROM hidden)",
                "CREATE FUNCTION f(x int) RETURNS int LANGUAGE SQL BEGIN ATOMIC "
                        + "SELECT CASE WHEN x > 0 THEN CASE WHEN x > 1 THEN x ELSE 1 END ELSE 0 END; "
                        + "SELECT x + 1; END",
                "CREATE PROCEDURE p() LANGUAGE SQL BEGIN ATOMIC "
                        + "INSERT INTO t VALUES (CASE WHEN true THEN 1 ELSE 0 END); SELECT 2; END",
                "CREATE FUNCTION f() RETURNS int SET search_path TO public "
                        + "AS 'SELECT 1;' LANGUAGE sql")
                .map(sql -> Arguments.of(Dialect.POSTGRESQL, sql));
        Stream<Arguments> mysql = Stream.of(
                "CREATE PROCEDURE p() SELECT 1",
                "CREATE PROCEDURE p() SET @result = IF(1, 2, 3)",
                "CREATE PROCEDURE p() INSERT INTO t VALUES (1)",
                "CREATE FUNCTION f(x int) RETURNS int DETERMINISTIC RETURN x + 1",
                "CREATE FUNCTION f(x int) RETURNS int RETURN CASE WHEN x IS NULL THEN 0 ELSE x END",
                "CREATE PROCEDURE p() BEGIN SELECT CASE WHEN 1 THEN IF(1, 2, 3) ELSE 0 END; "
                        + "BEGIN SELECT 'END; SELECT 0;'; END; SELECT 2; END",
                "CREATE PROCEDURE p() BEGIN IF (1) THEN SELECT 1; "
                        + "ELSEIF 2 THEN SELECT 2; ELSE SELECT 3; END IF; SELECT 4; END",
                "CREATE PROCEDURE p() BEGIN WHILE (1) DO IF (1) THEN SELECT 1; END IF; "
                        + "END WHILE; SELECT 2; END",
                "CREATE PROCEDURE p() BEGIN REPEAT SELECT 1; "
                        + "UNTIL CASE WHEN 1 THEN true ELSE false END END REPEAT; SELECT 2; END",
                "CREATE PROCEDURE p() BEGIN CASE 1 WHEN 1 THEN IF (1) THEN SELECT 1; END IF; "
                        + "ELSE SELECT 2; END CASE; SELECT 3; END",
                "CREATE PROCEDURE p() outer_block: BEGIN outer_loop: LOOP "
                        + "IF (1) THEN LEAVE outer_loop; END IF; END LOOP outer_loop; "
                        + "SELECT 2; END outer_block",
                "CREATE PROCEDURE p() IF (1) THEN SELECT 1; ELSE SELECT 2; END IF",
                "CREATE PROCEDURE p() CASE WHEN 1 THEN SELECT 1; ELSE SELECT 2; END CASE",
                "CREATE PROCEDURE p() BEGIN DECLARE CONTINUE HANDLER FOR NOT FOUND "
                        + "BEGIN SET @done = 1; END; SELECT 2; END",
                "CREATE PROCEDURE p() BEGIN DECLARE CONTINUE HANDLER FOR NOT FOUND "
                        + "IF @done IS NULL THEN SET @done = 1; END IF; SELECT 2; END",
                "CREATE PROCEDURE p() BEGIN DECLARE CONTINUE HANDLER FOR NOT FOUND "
                        + "SET @done = IF(1, 2, 3); SELECT 2; END")
                .map(sql -> Arguments.of(Dialect.MYSQL, sql));
        return Stream.concat(postgres, mysql);
    }

    @ParameterizedTest
    @MethodSource("sqlRoutines")
    void preservesRoutineBodyAndFollowingStatement(Dialect dialect, String sql) throws Exception {
        String script = sql + "; " + FOLLOWING + ";";
        Statements statements = parse(script, dialect);
        assertRoutineAndFollowing(statements, sql);
        assertRoutineAndFollowing(parser(script, dialect).Statements(), sql);
        assertRoutineAndFollowing(CCJSqlParserUtil.parseStatements(script), sql);
        assertRoutineAndFollowing(parse(statements.toString(), dialect), sql);

        StringBuilder deparsed = new StringBuilder();
        for (Statement statement : statements) {
            statement.accept(new StatementDeParser(deparsed), null);
            deparsed.append(";\n");
        }
        assertRoutineAndFollowing(parse(deparsed.toString(), dialect), sql);
        assertThat(parse(deparsed.toString(), dialect).get(0).toString())
                .isEqualTo(statements.get(0).toString());
        assertThat(CCJSqlParserUtil.parse(sql, p -> p.withDialect(dialect)).toString())
                .isEqualTo(statements.get(0).toString().replaceFirst(";$", ""));
    }

    @Test
    void directStatementParserLeavesFollowingStatementAvailable() throws Exception {
        CCJSqlParser parser = parser("CREATE FUNCTION f() RETURNS int LANGUAGE SQL RETURN 1; "
                + FOLLOWING + ";", Dialect.POSTGRESQL);
        assertThat(parser.Statement()).isInstanceOf(CreateFunction.class);
        assertThat(parser.getToken(1).kind).isEqualTo(CCJSqlParserConstants.K_SELECT);
        assertThat(parser.Statement().toString()).isEqualTo(FOLLOWING);
        assertThat(parser.getToken(1).kind).isEqualTo(CCJSqlParserConstants.EOF);
    }

    @Test
    void keepsAdjacentRoutinesAndDdlSeparate() throws Exception {
        String sql = "CREATE FUNCTION f() RETURNS int LANGUAGE SQL RETURN 1;"
                + "CREATE PROCEDURE p() LANGUAGE SQL BEGIN ATOMIC SELECT CASE WHEN true "
                + "THEN 1 ELSE 0 END; SELECT 2; END; CREATE TABLE after_routines(id int);"
                + FOLLOWING + ";";
        Statements statements = parse(sql, Dialect.POSTGRESQL);
        assertThat(statements).hasSize(4);
        assertThat(statements.get(0)).isExactlyInstanceOf(CreateFunction.class);
        assertThat(statements.get(1)).isExactlyInstanceOf(CreateProcedure.class);
        assertThat(statements.get(2).toString()).isEqualTo("CREATE TABLE after_routines (id int)");
        assertThat(statements.get(3).toString()).isEqualTo(FOLLOWING);
        assertThat(parse(statements.toString(), Dialect.POSTGRESQL)).hasSize(4);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "CREATE FUNCTION f() RETURNS int LANGUAGE SQL BEGIN ATOMIC SELECT 1;",
            "CREATE FUNCTION f() RETURNS int LANGUAGE SQL BEGIN ATOMIC "
                    + "SELECT CASE WHEN true THEN 1 END; SELECT 2;",
            "CREATE PROCEDURE p() BEGIN IF 1 THEN SELECT 1; END",
            "CREATE PROCEDURE p() BEGIN SELECT CASE WHEN 1 THEN 2; END"
    })
    void rejectsUnterminatedBlocks(String sql) {
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parseStatements(sql));
    }

    @Test
    void reportsInvalidFollowingStatementInsteadOfCapturingIt() {
        assertThrows(JSQLParserException.class,
                () -> parse("CREATE FUNCTION f() RETURNS int LANGUAGE SQL RETURN 1; SELEC 2;",
                        Dialect.POSTGRESQL));
        assertThrows(JSQLParserException.class,
                () -> parse("CREATE PROCEDURE p() SELECT 1; SELEC 2;", Dialect.MYSQL));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "CREATE PROCEDURE p AS v int; BEGIN NULL; END",
            "CREATE FUNCTION f(x int) RETURN int IS v int; "
                    + "BEGIN IF x > 0 THEN RETURN x; END IF; RETURN 0; END",
            "CREATE PROCEDURE p AS BEGIN FOR i IN 1 .. 3 LOOP NULL; END LOOP; NULL; END",
            "CREATE PROCEDURE p AS BEGIN WHILE 1 = 1 LOOP NULL; END LOOP; NULL; END"
    })
    void retainsOracleDeclarationAndBlockBoundaries(String sql) throws Exception {
        assertRoutineAndFollowing(CCJSqlParserUtil.parseStatements(sql + "; " + FOLLOWING + ";"),
                sql);
    }

    private static CCJSqlParser parser(String sql, Dialect dialect) {
        return CCJSqlParserUtil.newParser(sql).withDialect(dialect)
                .withUnsupportedStatements(false);
    }

    private static Statements parse(String sql, Dialect dialect) throws JSQLParserException {
        return CCJSqlParserUtil.parseStatements(sql,
                parser -> parser.withDialect(dialect).withUnsupportedStatements(false));
    }

    private static void assertRoutineAndFollowing(Statements statements, String source) {
        assertThat(statements).hasSize(2);
        assertThat(statements.get(0)).isInstanceOf(CreateFunctionalStatement.class);
        CreateFunctionalStatement routine = (CreateFunctionalStatement) statements.get(0);
        assertThat(routine.getFunctionDeclarationParts()).doesNotContain("after_routine", "2718");
        // The entire body must survive, including statements after an inner END.
        assertThat(routine.toString().replaceAll("\\s+", "").replaceFirst(";$", ""))
                .isEqualTo(source.replaceAll("\\s+", ""));
        assertThat(statements.get(1)).isExactlyInstanceOf(PlainSelect.class);
        assertThat(statements.get(1).toString()).isEqualTo(FOLLOWING);
    }
}
