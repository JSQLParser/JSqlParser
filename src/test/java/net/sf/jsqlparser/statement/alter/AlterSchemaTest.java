/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.alter;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.alter.schema.AlterSchema;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import net.sf.jsqlparser.util.validation.Validation;
import net.sf.jsqlparser.util.validation.feature.FeaturesAllowed;
import net.sf.jsqlparser.util.validation.feature.PostgresqlVersion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class AlterSchemaTest {
    @ParameterizedTest
    @ValueSource(strings = {"ALTER SCHEMA analytics RENAME TO reporting",
            "ALTER SCHEMA \"a.b\" RENAME TO \"next.schema\"",
            "ALTER SCHEMA s OWNER TO app", "ALTER SCHEMA s OWNER TO CURRENT_USER",
            "ALTER SCHEMA s OWNER TO CURRENT_ROLE", "ALTER SCHEMA s OWNER TO SESSION_USER",
            "ALTER SCHEMA \"Case\" OWNER TO \"role.with.dot\""})
    void parsesAndRendersSchemaActions(String sql) throws Exception {
        AlterSchema schema = parse(sql);
        assertEquals(sql, schema.toString());
        assertTrue(new TablesNamesFinder().getTables(schema).isEmpty());
        roundTrip(schema);
        assertTrue(new Validation(List.of(PostgresqlVersion.V14), sql).validate().isEmpty());
    }

    @Test
    void supportsAstMutationAndVisitorDispatch() throws Exception {
        AlterSchema schema = parse("ALTER SCHEMA s RENAME TO renamed");
        assertEquals(AlterSchema.Action.RENAME, schema.getAction());
        assertEquals("renamed", schema.getNewName());
        schema.setSchemaName("\"schema.name\"");
        schema.setNewName("replacement");
        roundTrip(schema);
        schema.setAction(AlterSchema.Action.OWNER);
        schema.setOwner("CURRENT_ROLE");
        assertEquals("ALTER SCHEMA \"schema.name\" OWNER TO CURRENT_ROLE", schema.toString());
        roundTrip(schema);
        Object context = new Object();
        assertEquals("visited", schema.accept(new StatementVisitorAdapter<String>() {
            @Override
            public <S> String visit(AlterSchema statement, S supplied) {
                assertSame(schema, statement);
                assertSame(context, supplied);
                return "visited";
            }
        }, context));
    }

    @Test
    void respectsStatementBoundariesAndValidationCapabilities() throws Exception {
        Statements statements = CCJSqlParserUtil.parseStatements(
                "ALTER SCHEMA s OWNER TO CURRENT_USER; SELECT 1;");
        assertEquals(2, statements.size());
        assertInstanceOf(AlterSchema.class, statements.get(0));
        assertFalse(new Validation(List.of(FeaturesAllowed.SELECT),
                "ALTER SCHEMA s RENAME TO r").validate().isEmpty());
        assertTrue(new Validation(List.of(new FeaturesAllowed().add(Feature.alterSchema)),
                "ALTER SCHEMA s RENAME TO r").validate().isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"ALTER SCHEMA s RENAME", "ALTER SCHEMA s OWNER TO",
            "ALTER SCHEMA s RENAME TO 'not an identifier'", "ALTER SCHEMA s OWNER TO a.b",
            "ALTER SCHEMA s OWNER TO a, b", "ALTER SCHEMA a.b RENAME TO c"})
    void rejectsIncompleteActionsAndQualifiedSchemaOrRoleNames(String sql) {
        assertThrows(JSQLParserException.class, () -> parse(sql));
    }

    private static AlterSchema parse(String sql) throws JSQLParserException {
        return (AlterSchema) CCJSqlParserUtil.parse(sql, p -> p.withDialect(Dialect.POSTGRESQL));
    }

    private static void roundTrip(AlterSchema schema) throws Exception {
        StringBuilder out = new StringBuilder();
        schema.accept(new StatementDeParser(out), null);
        assertEquals(schema.toString(), out.toString());
        assertEquals(schema.toString(), parse(out.toString()).toString());
    }
}
