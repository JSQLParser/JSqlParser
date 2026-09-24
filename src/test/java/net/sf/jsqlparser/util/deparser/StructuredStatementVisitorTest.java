/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.deparser;

import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StructuredStatementVisitorTest {
    @ParameterizedTest
    @ValueSource(strings = {
            "CREATE MACRO m(x, y := 7) AS x + y + 8",
            "CREATE MACRO m() AS TABLE WITH c AS (SELECT 7 AS id) SELECT id FROM c UNION ALL SELECT 8",
            "ASSERT (1 = 1) AS 'expected'",
            "ASSERT EXISTS (SELECT 7 FROM users WHERE id = 8)",
            "EXPORT DATA OPTIONS(uri='gs://example/*.csv',format='CSV') AS SELECT 7 FROM users",
            "EXPORT DATA OPTIONS(uri='gs://example/*.csv',format='CSV') AS WITH c AS (SELECT 7 AS id) SELECT id FROM c UNION ALL SELECT 8"
    })
    void defaultDeparserMatchesStatementOutput(String sql) throws Exception {
        Statement statement = CCJSqlParserUtil.parse(sql);
        StringBuilder output = new StringBuilder();
        statement.accept(new StatementDeParser(output), null);
        assertEquals(statement.toString(), output.toString());
        assertEquals(output.toString(), CCJSqlParserUtil.parse(output.toString()).toString());
    }

    @Test
    void visitsMacroDefaultsAndScalarBody() throws Exception {
        assertVisited("CREATE MACRO m(x, y := 7) AS x + y + 8", List.of("7", "8"),
                "CREATE MACRO m (x, y := 107) AS x + y + 108");
    }

    @Test
    void visitsMacroQueriesAndCtes() throws Exception {
        assertVisited(
                "CREATE MACRO m() AS TABLE WITH c AS (SELECT 7 AS id) SELECT id FROM c UNION ALL SELECT 8",
                List.of("7", "8"),
                "CREATE MACRO m () AS TABLE WITH c AS (SELECT 107 AS id) SELECT id FROM c UNION ALL SELECT 108");
    }

    @Test
    void visitsAssertionAndDescription() throws Exception {
        assertVisited("ASSERT (1 = 1) AS 'expected'", List.of("1", "1", "expected"),
                "ASSERT (101 = 101) AS 'changed'");
    }

    @Test
    void visitsExportOptionsAndQuery() throws Exception {
        assertVisited(
                "EXPORT DATA OPTIONS(uri='gs://example/*.csv',format='CSV') AS SELECT 7 FROM users WHERE id = 8",
                List.of("gs://example/*.csv", "CSV", "7", "8"),
                "EXPORT DATA OPTIONS (uri = 'changed', format = 'changed') AS SELECT 107 FROM users WHERE id = 108");
    }

    private void assertVisited(String sql, List<String> expectedVisits, String expectedOutput)
            throws Exception {
        List<String> visited = new ArrayList<>();
        ExpressionDeParser expressions = new ExpressionDeParser() {
            @Override
            public <S> StringBuilder visit(LongValue value, S context) {
                visited.add(value.getStringValue());
                return getBuilder().append(value.getValue() + 100);
            }

            @Override
            public <S> StringBuilder visit(StringValue value, S context) {
                visited.add(value.getValue());
                return getBuilder().append("'changed'");
            }
        };
        StringBuilder output = new StringBuilder();
        Statement statement = CCJSqlParserUtil.parse(sql);
        statement.accept(new StatementDeParser(expressions, new SelectDeParser(), output),
                "context");
        assertEquals(expectedVisits, visited);
        assertEquals(expectedOutput, output.toString());
        assertEquals(expectedOutput, CCJSqlParserUtil.parse(output.toString()).toString());
    }
}
