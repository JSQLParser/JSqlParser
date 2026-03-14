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
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statements;
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
