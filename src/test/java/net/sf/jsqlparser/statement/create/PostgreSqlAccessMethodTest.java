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
import java.util.List;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.create.accessmethod.CreateAccessMethod;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import net.sf.jsqlparser.util.validation.Validation;
import net.sf.jsqlparser.util.validation.feature.FeaturesAllowed;
import net.sf.jsqlparser.util.validation.feature.PostgresqlVersion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PostgreSqlAccessMethodTest {
    @ParameterizedTest
    @ValueSource(strings = {"TABLE", "INDEX"})
    void preservesAccessMethodKindAndDoesNotInventTableReferences(String type)
            throws JSQLParserException {
        String sql = "CREATE ACCESS METHOD am TYPE " + type + " HANDLER public.handlerfn";
        CreateAccessMethod statement = assertInstanceOf(CreateAccessMethod.class,
                CCJSqlParserUtil.parse(sql));
        assertEquals("am", statement.getName());
        assertEquals(CreateAccessMethod.Type.valueOf(type), statement.getType());
        assertEquals("public.handlerfn", statement.getHandler());
        assertEquals(sql, statement.toString());
        assertTrue(new TablesNamesFinder().getTables(statement).isEmpty());
        assertTrue(statement.getFeatures().modifiesSchema());
        assertFalse(statement.getFeatures().returnsResultSet());
        StringBuilder output = new StringBuilder();
        statement.accept(new StatementDeParser(output));
        assertEquals(sql, output.toString());
        assertInstanceOf(CreateAccessMethod.class, CCJSqlParserUtil.parse(output.toString()));
        assertEquals(2, CCJSqlParserUtil.parseStatements(sql + "; SELECT 1").size());
        assertTrue(new Validation(List.of(FeaturesAllowed.DDL), sql).validate().isEmpty());
        assertTrue(new Validation(List.of(PostgresqlVersion.V12), sql).validate().isEmpty());
        assertFalse(new Validation(List.of(FeaturesAllowed.SELECT), sql).validate().isEmpty());
        assertEquals(type.equals("INDEX"),
                new Validation(List.of(PostgresqlVersion.V10), sql).validate().isEmpty());
    }

    @Test
    void quotedNamesRemainMutableAndVisitorsReceiveContext() throws JSQLParserException {
        CreateAccessMethod statement = assertInstanceOf(CreateAccessMethod.class,
                CCJSqlParserUtil.parse(
                        "CREATE ACCESS METHOD \"my method\" TYPE TABLE HANDLER \"my schema\".\"my handler\""));
        assertEquals("\"my method\"", statement.getName());
        assertEquals("\"my schema\".\"my handler\"", statement.getHandler());
        String actual = statement.accept(new StatementVisitorAdapter<String>() {
            @Override
            public <S> String visit(CreateAccessMethod method, S context) {
                return context + ":" + method.getName();
            }
        }, "visited");
        assertEquals("visited:\"my method\"", actual);
        statement.setName("other_method");
        statement.setType(CreateAccessMethod.Type.INDEX);
        statement.setHandler("other_schema.handler");
        StringBuilder output = new StringBuilder();
        statement.accept(new StatementDeParser(output));
        assertEquals("CREATE ACCESS METHOD other_method TYPE INDEX HANDLER other_schema.handler",
                output.toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"CREATE ACCESS METHOD am TYPE TABLE",
            "CREATE ACCESS METHOD am TYPE VIEW HANDLER fn",
            "CREATE OR REPLACE ACCESS METHOD am TYPE TABLE HANDLER fn",
            "CREATE ACCESS METHOD am TYPE TABLE HANDLER fn()"})
    void rejectsIncompleteOrInvalidAccessMethodDefinitions(String sql) {
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql));
    }
}
