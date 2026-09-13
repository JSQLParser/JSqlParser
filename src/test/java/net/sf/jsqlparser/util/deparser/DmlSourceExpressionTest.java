/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.deparser;

import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.List;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.delete.Delete;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class DmlSourceExpressionTest {
    private static Statement parse(String sql) throws Exception {
        return CCJSqlParserUtil.parse(sql, parser -> {
            if (sql.startsWith("UPDATE t FROM")) {
                parser.withDialect(net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect.TERADATA);
            }
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "UPDATE t JOIN s ON s.a = 7 SET t.a = 8 WHERE t.b = 9",
            "UPDATE t SET a = 8 FROM (SELECT 7 AS a) s JOIN q ON s.a = q.a AND q.b = 9",
            "UPDATE t FROM s JOIN q ON q.a = 7 SET t.a = 8",
            "WITH c AS (SELECT 7 AS a), d AS (SELECT 8 AS b) UPDATE t SET a = 9 FROM c",
            "WITH c AS (DELETE FROM s WHERE a = 7 RETURNING a) UPDATE t SET a = 8",
            "WITH c AS (UPDATE s SET a = 7 RETURNING a) DELETE FROM t WHERE a = 8",
            "WITH c AS (INSERT INTO s (a) VALUES (7) RETURNING a) DELETE FROM t WHERE a = 8",
            "DELETE t FROM t JOIN s ON s.a = 7 WHERE t.b = 8",
            "DELETE FROM t USING (SELECT 7 AS a) s WHERE t.a = 8",
            "DELETE FROM t USING s, (SELECT 7 AS a) q WHERE t.a = 8"
    })
    void visitsEverySourceExpressionOnceAndLeavesTheAstUnchanged(String sql) throws Exception {
        Statement statement = parse(sql);
        String original = statement.toString();
        for (boolean direct : new boolean[] {false, true}) {
            StringBuilder output = new StringBuilder();
            List<Long> visited = new ArrayList<>();
            SelectDeParser select = new SelectDeParser();
            ExpressionDeParser expression = new ExpressionDeParser(select, output) {
                @Override
                public <S> StringBuilder visit(LongValue value, S context) {
                    visited.add(value.getValue());
                    return output.append(value.getValue() + 100);
                }
            };
            select.setExpressionVisitor(expression);
            select.setBuilder(output);
            if (direct && statement instanceof Update) {
                new UpdateDeParser(expression, output).deParse((Update) statement);
            } else if (direct) {
                new DeleteDeParser(expression, output).deParse((Delete) statement);
            } else {
                statement.accept(new StatementDeParser(expression, select, output), null);
            }
            String expected = sql.replace("7", "107").replace("8", "108").replace("9", "109");
            assertEquals(parse(expected).toString(),
                    parse(output.toString()).toString());
            for (long value : new long[] {7, 8, 9}) {
                assertEquals(sql.contains(Long.toString(value)) ? 1 : 0,
                        visited.stream().filter(v -> v == value).count());
            }
            assertEquals(original, statement.toString());
        }
    }

    @Test
    void usesTheConfiguredSelectDeParserForDmlSources() throws Exception {
        StringBuilder output = new StringBuilder();
        List<String> tables = new ArrayList<>();
        SelectDeParser selects = new SelectDeParser() {
            @Override
            public <S> StringBuilder visit(Table table, S context) {
                tables.add(table.getName());
                return super.visit(table, context);
            }
        };
        parse("WITH c AS (SELECT a FROM hidden) UPDATE t "
                + "SET a = 1 FROM source JOIN other ON source.a = other.a")
                .accept(new StatementDeParser(new ExpressionDeParser(), selects, output), null);
        assertEquals(List.of("hidden", "source", "other"), tables);
    }

    @Test
    void existingSettersAndGenericExpressionVisitorsCanRenderWithItems() throws Exception {
        StringBuilder output = new StringBuilder();
        ExpressionVisitorAdapter<StringBuilder> expressions = new ExpressionVisitorAdapter<>() {
            @Override
            public <S> StringBuilder visit(LongValue value, S context) {
                return output.append(value.getValue() + 100);
            }

            @Override
            public <S> StringBuilder visit(Column column, S context) {
                return output.append(column);
            }
        };
        UpdateDeParser deparser = new UpdateDeParser();
        deparser.setBuilder(output);
        deparser.setExpressionVisitor(expressions);
        deparser.deParse(
                (Update) parse("WITH c AS (SELECT 7) UPDATE t SET a = 8"));
        assertEquals("WITH c AS (SELECT 107) UPDATE t SET a = 108", output.toString());
    }
}
