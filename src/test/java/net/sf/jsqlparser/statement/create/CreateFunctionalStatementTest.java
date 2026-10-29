/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2020 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create;

import static net.sf.jsqlparser.test.TestUtils.assertDeparse;
import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.MySqlDefiner;
import net.sf.jsqlparser.statement.CreateFunctionalStatement;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.create.event.CreateEvent;
import net.sf.jsqlparser.statement.create.view.CreateView;
import net.sf.jsqlparser.statement.create.trigger.CreateTrigger;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import net.sf.jsqlparser.statement.create.function.CreateFunction;
import net.sf.jsqlparser.statement.create.procedure.CreateProcedure;
import org.junit.jupiter.api.Test;

/**
 * Tests the behavior of {@link net.sf.jsqlparser.statement.CreateFunctionalStatement funtion
 * statements}
 */
public class CreateFunctionalStatementTest {

    @Test
    public void createFunctionMinimal() throws JSQLParserException {
        String statement = "CREATE FUNCTION foo RETURN 5; END;";
        assertSqlCanBeParsedAndDeparsed(statement);
        assertDeparse(
                new CreateFunction().addFunctionDeclarationParts("foo")
                        .addFunctionDeclarationParts(Arrays.asList("RETURN 5;", "END;")),
                statement);
    }

    @Test
    public void createFunctionLong() throws JSQLParserException {
        CreateFunction stm = (CreateFunction) CCJSqlParserUtil.parse(
                "CREATE FUNCTION fun(query_from_time date) RETURNS TABLE(foo double precision, bar double precision)\n"
                        + "    LANGUAGE plpgsql\n"
                        + "    AS $$\n"
                        + "      BEGIN\n"
                        + "       RETURN QUERY\n"
                        + "      WITH bla AS (\n"
                        + "        SELECT * from foo)\n"
                        + "      Select * from bla;\n"
                        + "      END;\n"
                        + "      $$;");
        assertThat(stm).isNotNull();
        assertThat(stm.formatDeclaration()).contains("fun ( query_from_time date )");
    }

    @Test
    public void createProcedureMinimal() throws JSQLParserException {
        String statement = "CREATE PROCEDURE foo AS BEGIN END;";
        assertSqlCanBeParsedAndDeparsed(statement);
        assertDeparse(
                new CreateProcedure().addFunctionDeclarationParts("foo", "AS")
                        .addFunctionDeclarationParts(Arrays.asList("BEGIN", "END;")),
                statement);
    }

    @Test
    public void createProcedureLong() throws JSQLParserException {
        CreateProcedure stm = (CreateProcedure) CCJSqlParserUtil
                .parse("CREATE PROCEDURE remove_emp (employee_id NUMBER) AS\n"
                        + "   tot_emps NUMBER;\n"
                        + "   BEGIN\n"
                        + "      DELETE FROM employees\n"
                        + "      WHERE employees.employee_id = remove_emp.employee_id;\n"
                        + "   tot_emps := tot_emps - 1;\n"
                        + "   END;");
        assertThat(stm).isNotNull();
        assertThat(stm.formatDeclaration()).contains("remove_emp ( employee_id NUMBER )");
    }

    @Test
    public void createOrReplaceFunctionMinimal() throws JSQLParserException {
        String statement = "CREATE OR REPLACE FUNCTION foo RETURN 5; END;";
        assertSqlCanBeParsedAndDeparsed(statement);
        final CreateFunction func = new CreateFunction()
                .addFunctionDeclarationParts("foo")
                .addFunctionDeclarationParts(Arrays.asList("RETURN 5;", "END;"));
        func.setOrReplace(true);
        assertDeparse(func, statement);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "CREATE DEFINER = CURRENT_USER FUNCTION f2 (s CHAR(20)) RETURNS CHAR(50) "
                    + "CHARSET utf8mb4 NO SQL RETURN CONCAT('Hello, ', s, '!')",
            "CREATE DEFINER = CURRENT_USER PROCEDURE p3 () SELECT 1",
            "CREATE DEFINER = `root`@`%` FUNCTION f3 () RETURNS INT RETURN 1",
            "CREATE DEFINER = 'admin'@'localhost' PROCEDURE p (IN a INT) BEGIN SELECT a; END",
            "CREATE OR REPLACE DEFINER = CURRENT_USER() PROCEDURE p () SELECT 1"})
    public void createRoutineWithMySqlDefiner(String sql) throws JSQLParserException {
        for (Dialect dialect : new Dialect[] {Dialect.MYSQL, Dialect.MARIADB, null}) {
            CreateFunctionalStatement statement =
                    (CreateFunctionalStatement) assertSqlCanBeParsedAndDeparsed(
                            sql, true, dialect == null ? parser -> {
                            } : parser -> parser.withDialect(dialect));
            assertThat(statement.getDefiner()).isNotNull();
            StringBuilder deparsed = new StringBuilder();
            statement.accept(new StatementDeParser(deparsed));
            assertThat(deparsed.toString()).isEqualTo(statement.toString());
            assertThat(CCJSqlParserUtil.parseStatements(sql + ";\nSELECT 1").getStatements())
                    .hasSize(2);
        }
    }

    @Test
    public void mySqlDefinerAccount() throws JSQLParserException {
        CreateFunction function = (CreateFunction) CCJSqlParserUtil.parse(
                "CREATE DEFINER='root'@'%' FUNCTION f3 () RETURNS INT RETURN 1");
        MySqlDefiner definer = function.getDefiner();
        assertThat(definer.getUser().getValue()).isEqualTo("root");
        assertThat(definer.getHost().getValue()).isEqualTo("%");
        assertThat(function.getFunctionDeclarationParts()).startsWith("f3", "(", ")");

        CreateProcedure procedure = (CreateProcedure) CCJSqlParserUtil
                .parse("CREATE DEFINER = CURRENT_USER PROCEDURE p3 () SELECT 1");
        assertThat(procedure.getDefiner().getUser().getValue()).isEqualTo("CURRENT_USER");
        assertThat(procedure.getDefiner().getHost()).isNull();

        procedure.setDefiner(new MySqlDefiner().withUser(new StringValue("app"))
                .withHost(new StringValue("%")));
        assertThat(procedure.toString())
                .isEqualTo("CREATE DEFINER = 'app'@'%' PROCEDURE p3 ( ) SELECT 1");
        procedure.setDefiner(null);
        assertThat(procedure.toString()).isEqualTo("CREATE PROCEDURE p3 ( ) SELECT 1");
    }

    @Test
    public void definerDispatchKeepsTriggersAndViews() throws JSQLParserException {
        assertThat(CCJSqlParserUtil.parse("CREATE DEFINER = CURRENT_USER TRIGGER t "
                + "BEFORE INSERT ON t1 FOR EACH ROW SET NEW.a = 1"))
                .isInstanceOf(CreateTrigger.class);
        assertThat(CCJSqlParserUtil.parse("CREATE DEFINER = CURRENT_USER VIEW v AS SELECT 1"))
                .isInstanceOf(CreateView.class);
        assertThat(CCJSqlParserUtil.parse("CREATE FUNCTION f() RETURNS int AS 'SELECT 1'"))
                .isInstanceOf(CreateFunction.class)
                .extracting(statement -> ((CreateFunction) statement).getDefiner()).isNull();
        assertThat(CCJSqlParserUtil.parse("CREATE DEFINER = x EVENT e "
                + "ON SCHEDULE EVERY 1 DAY DO SELECT 1")).isInstanceOf(CreateEvent.class);
    }

    @Test
    public void createFunctionWithPositionalParametersAcrossStatementsIssue2322()
            throws JSQLParserException {
        String sql = "create table if not exists test_table (\n"
                + "  id bigint not null\n"
                + ");\n"
                + "\n"
                + "create or replace function test_fn_1(\n"
                + "  target text,\n"
                + "  characters text\n"
                + ") returns boolean as $$\n"
                + "  select trim($2 from $1) <> $1\n"
                + "$$ language sql immutable;\n"
                + "\n"
                + "create or replace function test_fn_2(\n"
                + "  target text,\n"
                + "  characters text\n"
                + ") returns boolean as $$\n"
                + "  select position(repeat(first_char, 2) in translate(\n"
                + "    $1, $2, repeat(first_char, length($2))\n"
                + "  )) > 0\n"
                + "  from (values (left($2, 1))) params(first_char)\n"
                + "$$ language sql immutable;\n"
                + "\n"
                + "create table if not exists test_table_2 (\n"
                + "  id bigint not null\n"
                + ");";

        Statements statements = CCJSqlParserUtil.parseStatements(sql);

        assertThat(statements.getStatements()).hasSize(4);
        assertThat(statements.getStatements().get(1)).isInstanceOf(CreateFunction.class);
        assertThat(statements.getStatements().get(2)).isInstanceOf(CreateFunction.class);

        CreateFunction function1 = (CreateFunction) statements.getStatements().get(1);
        CreateFunction function2 = (CreateFunction) statements.getStatements().get(2);

        assertThat(function1.getFunctionDeclarationParts()).anySatisfy(
                token -> assertThat(token).startsWith("$$").endsWith("$$"));
        assertThat(function1.getFunctionDeclarationParts()).containsSequence("language", "sql",
                "immutable", ";");
        assertThat(String.join(" ", function1.getFunctionDeclarationParts()))
                .contains("test_fn_1")
                .contains("$2")
                .contains("$1")
                .doesNotContain("create or replace function test_fn_2");

        assertThat(function2.getFunctionDeclarationParts()).anySatisfy(
                token -> assertThat(token).startsWith("$$").endsWith("$$"));
        assertThat(function2.getFunctionDeclarationParts()).containsSequence("language", "sql",
                "immutable", ";");
        assertThat(String.join(" ", function2.getFunctionDeclarationParts()))
                .contains("test_fn_2")
                .contains("params")
                .doesNotContain("create table if not exists test_table_2");

        assertThat(function1.formatDeclaration()).contains("test_fn_1");
        assertThat(function1.formatDeclaration()).doesNotContain("test_fn_2");
        assertThat(function2.formatDeclaration()).contains("test_fn_2");
        assertThat(function2.formatDeclaration()).doesNotContain("test_table_2");
    }
}
