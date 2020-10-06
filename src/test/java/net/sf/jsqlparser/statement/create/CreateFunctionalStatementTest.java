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
import org.junit.Test;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.create.function.CreateFunction;
import net.sf.jsqlparser.statement.create.procedure.CreateProcedure;

/**
 * Tests the behavior of {@link net.sf.jsqlparser.statement.CreateFunctionalStatement funtion statements}
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
        CreateFunction stm = (CreateFunction) CCJSqlParserUtil.parse("CREATE FUNCTION fun(query_from_time date) RETURNS TABLE(foo double precision, bar double precision)\n" +
                "    LANGUAGE plpgsql\n" +
                "    AS $$\n" +
                "      BEGIN\n" +
                "       RETURN QUERY\n" +
                "      WITH bla AS (\n" +
                "        SELECT * from foo)\n" +
                "      Select * from bla;\n" +
                "      END;\n" +
                "      $$;");
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
        CreateProcedure stm = (CreateProcedure) CCJSqlParserUtil.parse("CREATE PROCEDURE remove_emp (employee_id NUMBER) AS\n" +
                "   tot_emps NUMBER;\n" +
                "   BEGIN\n" +
                "      DELETE FROM employees\n" +
                "      WHERE employees.employee_id = remove_emp.employee_id;\n" +
                "   tot_emps := tot_emps - 1;\n" +
                "   END;");
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
}
