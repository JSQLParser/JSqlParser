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

import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.List;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.alter.AlterForeignDataWrapper;
import net.sf.jsqlparser.statement.alter.AlterServer;
import net.sf.jsqlparser.statement.alter.AlterUserMapping;
import net.sf.jsqlparser.statement.create.fdw.CreateForeignDataWrapper;
import net.sf.jsqlparser.statement.create.server.CreateServer;
import net.sf.jsqlparser.statement.create.table.ForeignDataOption;
import net.sf.jsqlparser.statement.create.usermapping.CreateUserMapping;
import net.sf.jsqlparser.statement.foreign.ForeignDataStatement;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import net.sf.jsqlparser.util.validation.Validation;
import net.sf.jsqlparser.util.validation.feature.FeaturesAllowed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PostgreSqlForeignDataObjectsTest {
    @ParameterizedTest
    @ValueSource(strings = {"CREATE FOREIGN DATA WRAPPER w",
            "CREATE FOREIGN DATA WRAPPER w NO HANDLER NO VALIDATOR OPTIONS(x 'y')",
            "CREATE FOREIGN DATA WRAPPER w HANDLER public.handler VALIDATOR public.validator",
            "CREATE FOREIGN DATA WRAPPER w VALIDATOR public.validator HANDLER public.handler",
            "ALTER FOREIGN DATA WRAPPER w OPTIONS(ADD x 'y')",
            "ALTER FOREIGN DATA WRAPPER w NO HANDLER NO VALIDATOR",
            "ALTER FOREIGN DATA WRAPPER w HANDLER public.handler OPTIONS(SET x 'z',DROP y)",
            "ALTER FOREIGN DATA WRAPPER w OWNER TO CURRENT_USER",
            "ALTER FOREIGN DATA WRAPPER w RENAME TO w2",
            "CREATE SERVER s FOREIGN DATA WRAPPER w OPTIONS(host 'localhost')",
            "CREATE SERVER IF NOT EXISTS s TYPE 'postgres' VERSION '18' FOREIGN DATA WRAPPER w",
            "CREATE SERVER s VERSION NULL FOREIGN DATA WRAPPER w",
            "ALTER SERVER s OPTIONS(SET host '127.0.0.1')", "ALTER SERVER s VERSION NULL",
            "ALTER SERVER s VERSION '18' OPTIONS(ADD port '5432',DROP host)",
            "ALTER SERVER s OWNER TO CURRENT_ROLE", "ALTER SERVER s RENAME TO s2",
            "CREATE USER MAPPING FOR CURRENT_USER SERVER s OPTIONS(user 'u')",
            "CREATE USER MAPPING IF NOT EXISTS FOR PUBLIC SERVER s",
            "CREATE USER MAPPING FOR USER SERVER s OPTIONS(user 'u',password 'a,b')",
            "CREATE USER MAPPING FOR CURRENT_ROLE SERVER s",
            "CREATE USER MAPPING FOR \"user name\" SERVER \"server name\"",
            "ALTER USER MAPPING FOR CURRENT_USER SERVER s OPTIONS(SET \"user\" 'u2')",
            "ALTER USER MAPPING FOR PUBLIC SERVER s OPTIONS(DROP password,ADD user 'u')"})
    void foreignObjectsShareOptionsAndKeepBoundaries(String sql) throws JSQLParserException {
        Statement statement = CCJSqlParserUtil.parse(sql);
        assertInstanceOf(ForeignDataStatement.class, statement);
        assertTrue(new TablesNamesFinder().getTables(statement).isEmpty());
        roundTrip(statement);
        assertEquals(2, CCJSqlParserUtil.parseStatements(sql + "; SELECT 1").size());
        assertTrue(new Validation(List.of(FeaturesAllowed.DDL), sql).validate().isEmpty());
        assertFalse(new Validation(List.of(FeaturesAllowed.SELECT), sql).validate().isEmpty());
    }

    @Test
    void omittedAndRemovedHandlersStayDistinct() throws JSQLParserException {
        CreateForeignDataWrapper create = (CreateForeignDataWrapper) CCJSqlParserUtil.parse(
                "CREATE FOREIGN DATA WRAPPER w");
        assertFalse(create.getFunctions().isHandlerSpecified());
        create.getFunctions().setHandler(null);
        assertTrue(create.getFunctions().isHandlerSpecified());
        assertEquals("CREATE FOREIGN DATA WRAPPER w NO HANDLER", create.toString());
        create.getFunctions().setHandler("public.handler");
        roundTrip(create);
        create.getFunctions().clearHandler();
        assertEquals("CREATE FOREIGN DATA WRAPPER w", create.toString());
        AlterForeignDataWrapper alter = (AlterForeignDataWrapper) CCJSqlParserUtil.parse(
                "ALTER FOREIGN DATA WRAPPER w OPTIONS(SET x 'y')");
        alter.getOptions().get(0).setValue(new StringValue("changed"));
        assertTrue(alter.toString().contains("SET x 'changed'"));
        roundTrip(alter);
    }

    @Test
    void serverLiteralsAndOptionsAreVisitedAndMutable() throws JSQLParserException {
        CreateServer create = (CreateServer) CCJSqlParserUtil.parse(
                "CREATE SERVER s TYPE 'pg' VERSION '18' FOREIGN DATA WRAPPER w OPTIONS(host 'local')");
        List<Expression> visited = new ArrayList<>();
        create.visitExpressions(visited::add);
        assertEquals(3, visited.size());
        create.setVersion(new NullValue());
        create.setForeignDataWrapper("other_wrapper");
        create.getOptions().get(0).setValue(new StringValue("remote"));
        assertTrue(create.toString().contains("VERSION NULL FOREIGN DATA WRAPPER other_wrapper"));
        roundTrip(create);
        AlterServer alter = (AlterServer) CCJSqlParserUtil.parse("ALTER SERVER s VERSION NULL");
        assertInstanceOf(NullValue.class, alter.getVersion());
        alter.setAction(AlterServer.Action.RENAME);
        alter.setNewName("new_server");
        visited.clear();
        alter.visitExpressions(visited::add);
        assertTrue(visited.isEmpty());
        assertEquals("ALTER SERVER s RENAME TO new_server", alter.toString());
    }

    @Test
    void userMappingOptionsUseExistingForeignDataNodes() throws JSQLParserException {
        CreateUserMapping create = (CreateUserMapping) CCJSqlParserUtil.parse(
                "CREATE USER MAPPING FOR CURRENT_USER SERVER s OPTIONS(user 'u')");
        assertNull(create.getOptions().get(0).getAction());
        AlterUserMapping alter = (AlterUserMapping) CCJSqlParserUtil.parse(
                "ALTER USER MAPPING FOR CURRENT_USER SERVER s OPTIONS(SET user 'u2')");
        assertEquals(ForeignDataOption.Action.SET, alter.getOptions().get(0).getAction());
        alter.getOptions().get(0).setAction(ForeignDataOption.Action.DROP);
        assertNull(alter.getOptions().get(0).getValue());
        List<Expression> expressions = new ArrayList<>();
        alter.visitExpressions(expressions::add);
        assertTrue(expressions.isEmpty());
        roundTrip(alter);
        assertEquals("s", create.accept(new StatementVisitorAdapter<String>() {
            @Override
            public <S> String visit(CreateUserMapping statement, S context) {
                return statement.getServer();
            }
        }, null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"CREATE OR REPLACE SERVER s FOREIGN DATA WRAPPER w",
            "CREATE OR REPLACE FOREIGN DATA WRAPPER w", "CREATE SERVER s OPTIONS(x 'y')",
            "CREATE FOREIGN DATA WRAPPER w HANDLER h HANDLER h2",
            "CREATE FOREIGN DATA WRAPPER w NO VALIDATOR VALIDATOR v",
            "ALTER FOREIGN DATA WRAPPER w", "ALTER SERVER s",
            "ALTER USER MAPPING FOR USER SERVER s",
            "CREATE USER MAPPING FOR USER SERVER s OPTIONS(SET x 'y')",
            "CREATE SERVER s FOREIGN DATA WRAPPER w OPTIONS(DROP x)",
            "ALTER SERVER s OPTIONS(SET x)", "ALTER SERVER s OPTIONS(DROP x 'y')",
            "ALTER SERVER s OPTIONS()", "ALTER SERVER s OPTIONS(host = 'localhost')",
            "CREATE SERVER s TYPE NULL FOREIGN DATA WRAPPER w", "ALTER SERVER s VERSION 18",
            "ALTER SERVER s RENAME TO s2 OPTIONS(x 'y')"})
    void rejectsWrongObjectAndOptionForms(String sql) {
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql));
    }

    private static void roundTrip(Statement statement) throws JSQLParserException {
        StringBuilder output = new StringBuilder();
        statement.accept(new StatementDeParser(output));
        assertEquals(statement.toString(), output.toString());
        assertEquals(output.toString(), CCJSqlParserUtil.parse(output.toString()).toString());
    }
}
