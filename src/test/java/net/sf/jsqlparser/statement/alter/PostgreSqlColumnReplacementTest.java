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

import java.util.ArrayList;
import java.util.List;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.util.TableDefinitionTraversal;
import net.sf.jsqlparser.util.deparser.AlterDeParser;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PostgreSqlColumnReplacementTest {
    @ParameterizedTest
    @ValueSource(strings = {"BIGINT", "BIGINT USING id::bigint",
            "VARCHAR(20) COLLATE \"C\" USING id::text"})
    void sharesTypeChangePrefixes(String type) throws JSQLParserException {
        for (String column : new String[] {"COLUMN ", ""}) {
            for (String prefix : new String[] {"TYPE ", "SET DATA TYPE "}) {
                String sql = "ALTER TABLE t ALTER " + column + "id " + prefix + type;
                Alter table = (Alter) parse(sql);
                AlterExpression.ColumnDataType definition = table.getAlterExpressions().get(0)
                        .getColDataTypeList().get(0);
                assertTrue(definition.isWithType());
                assertEquals(prefix.startsWith("SET"), definition.isUseSetData());
                assertNotEquals("SET", definition.getColDataType().getDataType());
                if (type.contains("USING")) {
                    assertNotNull(definition.getUsingExpression());
                }
                roundTrip(table);
                assertEquals(2, CCJSqlParserUtil.parseStatements(sql + "; SELECT 1").size());
                definition.setUseSetData(!definition.isUseSetData());
                roundTrip(table);
            }
        }
        for (String prefix : new String[] {"TYPE ", "SET DATA TYPE "}) {
            roundTrip(parse("ALTER TYPE row_type ALTER ATTRIBUTE id " + prefix + "BIGINT"));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"id + 2", "COALESCE(id, 0) * 2", "CASE WHEN id > 0 THEN id ELSE 0 END"})
    void generatedExpressionIsAnActionAndNotADataType(String expression)
            throws JSQLParserException {
        for (String column : new String[] {"COLUMN ", ""}) {
            String sql = "ALTER TABLE t ALTER " + column + "g SET EXPRESSION AS (" + expression
                    + "), ADD COLUMN extra INT";
            for (Statement statement : List.of(parse(sql), CCJSqlParserUtil.parse(sql))) {
                Alter table = (Alter) statement;
                RelationAlterAction action = assertInstanceOf(RelationAlterAction.class,
                        table.getAlterExpressions().get(0));
                assertEquals(RelationAlterAction.ColumnAction.SET_EXPRESSION,
                        action.getColumnAction());
                assertEquals("g", action.getColumnName());
                assertNull(action.getColDataTypeList());
                assertNotNull(action.getGenerationExpression());
                List<Expression> expressions = new ArrayList<>();
                TableDefinitionTraversal.visit(action, expressions::add, ignored -> {
                });
                assertEquals(List.of(action.getGenerationExpression()), expressions);
                action.setGenerationExpression(new LongValue(42));
                assertTrue(table.toString().contains("SET EXPRESSION AS (42)"));
                roundTrip(table);
            }
            assertEquals(2, CCJSqlParserUtil.parseStatements(sql + "; SELECT 1").size());
        }
    }

    @Test
    void replacementUsesTheExpressionDeparser() throws JSQLParserException {
        Alter table = (Alter) parse("ALTER TABLE t ALTER COLUMN g SET EXPRESSION AS (1)");
        StringBuilder output = new StringBuilder();
        ExpressionDeParser expressions = new ExpressionDeParser() {
            @Override
            public <S> StringBuilder visit(LongValue value, S context) {
                return getBuilder().append(2);
            }
        };
        expressions.setBuilder(output);
        new AlterDeParser(output, expressions).deParse(table);
        assertEquals("ALTER TABLE t ALTER COLUMN g SET EXPRESSION AS (2)", output.toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"ALTER TABLE t ALTER COLUMN id SET DATA TYPE",
            "ALTER TABLE t ALTER COLUMN id SET DATA BIGINT",
            "ALTER TABLE t ALTER COLUMN g SET EXPRESSION AS ()",
            "ALTER TABLE t ALTER COLUMN g SET EXPRESSION AS id + 2",
            "ALTER TABLE t ALTER COLUMN g SET EXPRESSION AS (id+2) STORED",
            "ALTER VIEW v ALTER COLUMN g SET EXPRESSION AS (id+2)",
            "ALTER INDEX ix ALTER COLUMN 1 SET EXPRESSION AS (id+2)"})
    void rejectsIncompleteOrWrongContextActions(String sql) {
        assertThrows(JSQLParserException.class, () -> parse(sql));
    }

    private static Statement parse(String sql) throws JSQLParserException {
        return CCJSqlParserUtil.parse(sql, parser -> parser.withDialect(Dialect.POSTGRESQL));
    }

    private static void roundTrip(Statement statement) throws JSQLParserException {
        StringBuilder output = new StringBuilder();
        statement.accept(new StatementDeParser(output));
        assertEquals(statement.toString(), output.toString());
        assertEquals(output.toString(), parse(output.toString()).toString());
    }
}
