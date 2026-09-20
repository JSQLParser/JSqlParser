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

import java.util.List;
import java.util.stream.Stream;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.CreateFunctionalStatement;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.create.function.CreateFunction;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.procedure.CreateProcedure;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class PostgreSqlRoutineBoundaryTest {
    private static final String FOLLOWING = "SELECT 1994 AS after_routine";

    static Stream<Arguments> quotedRoutines() {
        List<String> bodies = List.of(
                "'BEGIN PERFORM ''end; SELECT 9;''; END;'",
                "E'BEGIN\\nPERFORM 1;\\nEND;'",
                "E'BEGIN PERFORM \\'a;b\\'; END;'",
                "$$BEGIN PERFORM 'end;'; END;$$",
                "$Body$BEGIN PERFORM $inner$semi; $$ END$inner$; END;$Body$",
                "'BEGIN PERFORM 1;'\n' PERFORM 2; END;'",
                "E'BEGIN\\n'\n'PERFORM 1; END;'");
        return Stream.of("FUNCTION", "PROCEDURE").flatMap(kind -> bodies.stream().map(body -> {
            String returns = "FUNCTION".equals(kind) ? " RETURNS void" : "";
            return Arguments.of(kind + " " + body,
                    "CREATE OR REPLACE " + kind + " boundary_routine()" + returns
                            + " AS " + body + " LANGUAGE plpgsql",
                    body);
        }));
    }

    static Stream<Arguments> routineOptions() {
        String body = "'BEGIN PERFORM 1; END;'";
        return Stream.of(
                Arguments.of("language before body",
                        "CREATE FUNCTION boundary_routine() RETURNS void LANGUAGE 'plpgsql' AS "
                                + body,
                        body),
                Arguments.of("options after body",
                        "CREATE FUNCTION boundary_routine() RETURNS void AS " + body
                                + " LANGUAGE plpgsql VOLATILE COST 100 SET search_path TO public",
                        body),
                Arguments.of("comments around body",
                        "CREATE FUNCTION boundary_routine() RETURNS void AS /* END; */ " + body
                                + " -- next option\n LANGUAGE plpgsql /* ; SELECT 9; */",
                        body),
                Arguments.of("continued body with comments",
                        "CREATE FUNCTION boundary_routine() RETURNS void AS 'BEGIN PERFORM 1;'"
                                + " -- continuation\n ' END;' LANGUAGE plpgsql",
                        "'BEGIN PERFORM 1;'\n' END;'"),
                Arguments.of("SQL function",
                        "CREATE FUNCTION boundary_routine() RETURNS integer AS 'SELECT 42;' LANGUAGE sql",
                        "'SELECT 42;'"),
                Arguments.of("internal function",
                        "CREATE FUNCTION boundary_routine(integer) RETURNS integer"
                                + " AS 'int4abs' LANGUAGE internal IMMUTABLE STRICT",
                        "'int4abs'"),
                Arguments.of("parameter defaults",
                        "CREATE FUNCTION boundary_routine(x text DEFAULT $d$end; AS 'x'$d$,"
                                + " y integer DEFAULT CASE WHEN true THEN 1 ELSE 2 END)"
                                + " RETURNS void AS " + body + " LANGUAGE plpgsql",
                        body),
                Arguments.of("quoted names",
                        "CREATE FUNCTION public.\"end; routine\"(\"AS\" text DEFAULT 'END;')"
                                + " RETURNS void AS " + body + " LANGUAGE plpgsql",
                        body));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource({"quotedRoutines", "routineOptions"})
    void preservesQuotedRoutineAndFollowingStatement(String name, String declaration,
            String body) throws Exception {
        Statements statements = parse(declaration + "; " + FOLLOWING + ";");
        assertRoutineAndFollowing(statements, body);
        Class<?> expected = declaration.contains("PROCEDURE") ? CreateProcedure.class
                : CreateFunction.class;
        assertThat(statements.get(0)).isExactlyInstanceOf(expected);
        assertRoutineAndFollowing(parse(statements.toString()), body);
        StringBuilder deparsed = new StringBuilder();
        StatementDeParser deparser = new StatementDeParser(deparsed);
        for (Statement statement : statements) {
            statement.accept(deparser, null);
            deparsed.append(";\n");
        }
        assertRoutineAndFollowing(parse(deparsed.toString()), body);
    }

    @ParameterizedTest
    @ValueSource(strings = {"'BEGIN RETURN; END;'", "E'BEGIN\\nRETURN;\\nEND;'",
            "$$BEGIN RETURN; END;$$"})
    void supportsUnambiguousBodiesWithoutDialect(String body) throws Exception {
        String sql = "CREATE FUNCTION f() RETURNS void AS " + body
                + " LANGUAGE plpgsql; " + FOLLOWING + ";";
        assertRoutineAndFollowing(CCJSqlParserUtil.parseStatements(sql), body);
    }

    @ParameterizedTest
    @ValueSource(strings = {"'BEGIN RETURN; END;'", "$tag$BEGIN RETURN; END;$tag$"})
    void acceptsBodyAtEndOfInputWithoutFinalSemicolon(String body) throws Exception {
        Statements statements = parse("CREATE FUNCTION f() RETURNS void AS " + body
                + " LANGUAGE plpgsql");
        assertThat(statements).hasSize(1);
        assertThat(((CreateFunction) statements.get(0)).getFunctionDeclarationParts())
                .contains(body);
    }

    @Test
    void keepsConsecutiveFunctionsProceduresAndDdlSeparate() throws Exception {
        String sql = "CREATE FUNCTION f() RETURNS integer AS 'SELECT 1;' LANGUAGE sql;"
                + "CREATE PROCEDURE p() AS 'BEGIN NULL; END;' LANGUAGE plpgsql;"
                + "CREATE FUNCTION g() RETURNS integer AS $g$ SELECT 2; $g$ LANGUAGE sql;"
                + "CREATE TABLE after_routines(id integer); " + FOLLOWING + ";";
        Statements statements = parse(sql);
        assertThat(statements).hasSize(5);
        assertThat(statements.get(0)).isInstanceOf(CreateFunction.class);
        assertThat(statements.get(1)).isInstanceOf(CreateProcedure.class);
        assertThat(statements.get(2)).isInstanceOf(CreateFunction.class);
        assertThat(statements.get(3).toString()).startsWith("CREATE TABLE after_routines");
        assertThat(statements.get(4).toString()).isEqualTo(FOLLOWING);
        assertThat(parse(statements.toString())).hasSize(5);
    }

    @Test
    void preservesTwoLiteralExternalFunctionBody() throws Exception {
        Statements statements = parse("CREATE FUNCTION f(integer) RETURNS integer"
                + " AS 'library', 'symbol' LANGUAGE c; " + FOLLOWING + ";");
        assertRoutineAndFollowing(statements, "'library'");
        assertThat(((CreateFunction) statements.get(0)).getFunctionDeclarationParts())
                .containsSequence("'library'", ",", "'symbol'", "LANGUAGE", "c", ";");
    }

    @Test
    void preservesReportedScriptAndIndexIssue1994() throws Exception {
        // The issue deliberately omits part of the PL/pgSQL body. It stays opaque.
        String sql = "DROP FUNCTION IF EXISTS \"fin\".\"restore_fund_data\"(\"in_fund_id\" int8);\n"
                + "CREATE OR REPLACE FUNCTION \"fin\".\"restore_fund_data\"(\"in_fund_id\" int8)\n"
                + "RETURNS \"pg_catalog\".\"varchar\" AS $BODY$\nDECLARE\nmessage VARCHAR;\n"
                + "open rec_cur;\nclose proj_info_cur;\n********\nRETURN 'success';\nEND $BODY$\n"
                + "LANGUAGE plpgsql VOLATILE COST 100;\n"
                + "CREATE INDEX \"index_keyword\" ON \"inter\".\"inter_ti_rec\" USING btree "
                + "(\"keyword\" COLLATE \"pg_catalog\".\"default\" "
                + "\"pg_catalog\".\"text_ops\" ASC NULLS LAST);\n" + FOLLOWING + ";";
        Statements statements = parse(sql);
        assertThat(statements).hasSize(4);
        assertThat(statements.get(0)).isInstanceOf(Drop.class);
        CreateFunction function = (CreateFunction) statements.get(1);
        assertThat(function.getFunctionDeclarationParts()).anySatisfy(part -> assertThat(part)
                .startsWith("$BODY$").endsWith("$BODY$").contains("********", "RETURN 'success';"));
        CreateIndex index = (CreateIndex) statements.get(2);
        assertThat(index.getIndex().getColumns().get(0).getOperatorClass())
                .isEqualTo("\"pg_catalog\".\"text_ops\"");
        assertThat(statements.get(3).toString()).isEqualTo(FOLLOWING);
        assertThat(parse(statements.toString())).hasSize(4);
    }

    @ParameterizedTest
    @ValueSource(strings = {"'BEGIN RETURN;", "$body$BEGIN RETURN; END;$other$"})
    void rejectsUnterminatedBody(String body) {
        assertThrows(JSQLParserException.class, () -> parse("CREATE FUNCTION f() RETURNS void AS "
                + body + " LANGUAGE plpgsql; SELECT 1994;"));
    }

    @Test
    void reportsInvalidStatementAfterQuotedRoutine() {
        String routine = "CREATE FUNCTION f() RETURNS integer AS 'SELECT 1;' LANGUAGE sql; ";
        assertThrows(JSQLParserException.class, () -> parse(routine + "SELEC 1994;"));
    }

    @Test
    void escapePrefixDoesNotChangeOrdinaryStringHandling() throws Exception {
        String body = "e'BEGIN PERFORM \\'a;b\\'; END;'";
        Statements statements = parse("CREATE FUNCTION f() RETURNS void AS " + body
                + " LANGUAGE plpgsql; SELECT 'C:\\' AS path, 'after' AS marker;");
        assertThat(statements).hasSize(2);
        assertThat(((CreateFunction) statements.get(0)).getFunctionDeclarationParts())
                .contains(body);
        assertThat(statements.get(1).toString())
                .isEqualTo("SELECT 'C:\\' AS path, 'after' AS marker");
        assertThat(parse(statements.toString())).hasSize(2);
    }

    @Test
    void retainsLegacyUnquotedRoutineBodies() throws Exception {
        for (String sql : List.of("CREATE PROCEDURE p AS BEGIN NULL; END;",
                "CREATE PROCEDURE p() BEGIN SELECT 1 AS 'label'; SELECT 2; END;",
                "CREATE PROCEDURE p AS BEGIN SELECT $$text;$$; SELECT 2; END;",
                "CREATE FUNCTION f(x integer DEFAULT CASE WHEN 1 = 1 THEN 1 ELSE 2 END) "
                        + "RETURN integer AS BEGIN RETURN x; END;")) {
            Statements statements = CCJSqlParserUtil.parseStatements(sql + FOLLOWING + ";");
            assertThat(statements).hasSize(2);
            assertThat(statements.get(1).toString()).isEqualTo(FOLLOWING);
        }
    }

    private static Statements parse(String sql) throws JSQLParserException {
        return CCJSqlParserUtil.parseStatements(sql,
                parser -> parser.withDialect(Dialect.POSTGRESQL).withUnsupportedStatements(false));
    }

    private static void assertRoutineAndFollowing(Statements statements, String body) {
        assertThat(statements).hasSize(2);
        assertThat(statements.get(0)).isInstanceOf(CreateFunctionalStatement.class);
        CreateFunctionalStatement routine = (CreateFunctionalStatement) statements.get(0);
        assertThat(routine.getFunctionDeclarationParts()).contains(body)
                .doesNotContain("after_routine", "1994");
        assertThat(statements.get(1)).isInstanceOf(PlainSelect.class);
        assertThat(statements.get(1).toString()).isEqualTo(FOLLOWING);
    }
}
