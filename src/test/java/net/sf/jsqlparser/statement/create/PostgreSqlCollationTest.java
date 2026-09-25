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
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.alter.AlterCollation;
import net.sf.jsqlparser.statement.create.collation.CreateCollation;
import net.sf.jsqlparser.statement.create.table.Index;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import net.sf.jsqlparser.util.validation.Validation;
import net.sf.jsqlparser.util.validation.feature.FeaturesAllowed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PostgreSqlCollationTest {
    @ParameterizedTest
    @ValueSource(strings = {"CREATE COLLATION c FROM \"C\"",
            "CREATE COLLATION IF NOT EXISTS app.c FROM pg_catalog.\"C\"",
            "CREATE COLLATION c (locale='C')",
            "CREATE COLLATION c (locale='C',deterministic)",
            "CREATE COLLATION c (provider=builtin,locale='C.UTF-8')",
            "CREATE COLLATION c (lc_collate='C',lc_ctype='C',provider=libc)",
            "CREATE COLLATION c (provider=icu,locale='und',deterministic=false,rules='&V << w',version='1')",
            "ALTER COLLATION app.c REFRESH VERSION", "ALTER COLLATION app.c RENAME TO c2",
            "ALTER COLLATION c OWNER TO CURRENT_USER", "ALTER COLLATION c SET SCHEMA app"})
    void structuredDefinitionsAndChangesRoundTrip(String sql) throws JSQLParserException {
        Statement statement = CCJSqlParserUtil.parse(sql);
        assertTrue(statement instanceof CreateCollation || statement instanceof AlterCollation);
        assertTrue(new TablesNamesFinder().getTables(statement).isEmpty());
        roundTrip(statement);
        assertEquals(2, CCJSqlParserUtil.parseStatements(sql + "; SELECT 1").size());
        assertTrue(new Validation(List.of(FeaturesAllowed.DDL), sql).validate().isEmpty());
    }

    @Test
    void switchingBetweenCopyAndOptionsClearsInactiveState() throws JSQLParserException {
        CreateCollation statement =
                (CreateCollation) CCJSqlParserUtil.parse("CREATE COLLATION c FROM \"C\"");
        statement.setOptions(
                new ArrayList<>(List.of(new Index.Option("locale", new StringValue("C"), true))));
        assertNull(statement.getSourceCollation());
        statement.getOptions().get(0).setValue(new StringValue("C.UTF-8"));
        List<Expression> expressions = new ArrayList<>();
        statement.visitExpressions(expressions::add);
        assertEquals(1, expressions.size());
        roundTrip(statement);
        statement.setSourceCollation("pg_catalog.\"C\"");
        assertNull(statement.getOptions());
        assertEquals("CREATE COLLATION c FROM pg_catalog.\"C\"", statement.toString());
        expressions.clear();
        statement.visitExpressions(expressions::add);
        assertTrue(expressions.isEmpty());
        roundTrip(statement);
    }

    @ParameterizedTest
    @ValueSource(strings = {"CREATE COLLATION c", "CREATE COLLATION c ()",
            "CREATE COLLATION c (locale=)", "CREATE COLLATION c (locale 'C')",
            "CREATE OR REPLACE COLLATION c FROM \"C\"", "ALTER COLLATION c REFRESH",
            "ALTER COLLATION c RENAME TO c2, SET SCHEMA app"})
    void rejectsIncompleteDefinitions(String sql) {
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql));
    }

    private static void roundTrip(Statement statement) throws JSQLParserException {
        StringBuilder output = new StringBuilder();
        statement.accept(new StatementDeParser(output));
        assertEquals(statement.toString(), output.toString());
        assertEquals(output.toString(), CCJSqlParserUtil.parse(output.toString()).toString());
    }
}
