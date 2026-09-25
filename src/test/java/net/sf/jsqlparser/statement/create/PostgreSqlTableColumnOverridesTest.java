/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create;

import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.List;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.ColumnOption;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PostgreSqlTableColumnOverridesTest {
    @ParameterizedTest
    @ValueSource(strings = {"row_type", "public.row_type", "\"Type.Schema\".\"Row.Type\""})
    void keepsTypeNameSeparateFromFirstColumn(String type) throws JSQLParserException {
        for (String withOptions : new String[] {"", " WITH OPTIONS"}) {
            CreateTable table = parse("CREATE TABLE t OF " + type + " (a" + withOptions
                    + " DEFAULT 7 NOT NULL, b, CHECK (a > 0))");
            assertEquals(type, table.getOfType().getDataType());
            assertNull(table.getOfType().getArgumentsStringList());
            assertEquals(2, table.getColumnDefinitions().size());
            ColumnDefinition first = table.getColumnDefinitions().get(0);
            assertNull(first.getColDataType());
            assertEquals(!withOptions.isEmpty(), first.isWithOptions());
            assertEquals("b", table.getColumnDefinitions().get(1).getColumnName());
            assertEquals(ColumnOption.Kind.DEFAULT, first.getColumnOptions().get(0).getKind());
            roundTrip(table);
            first.getColumnOptions().get(0).setDefaultExpression(new LongValue(9));
            assertTrue(table.toString().contains("DEFAULT 9 NOT NULL"));
            roundTrip(table);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"DEFAULT", "FOR VALUES IN (1, 2)",
            "FOR VALUES FROM (0) TO (10)", "FOR VALUES WITH (MODULUS 4, REMAINDER 0)"})
    void sharesOverridesForPartitionChildren(String bound) throws JSQLParserException {
        for (String options : new String[] {"", " WITH OPTIONS"}) {
            CreateTable table = parse("CREATE TABLE child PARTITION OF public.parent (a" + options
                    + " DEFAULT 7 NOT NULL, b, CHECK (a > 0)) " + bound);
            assertNotNull(table.getPartitionBound());
            assertNull(table.getColumnDefinitions().get(0).getColDataType());
            assertNull(table.getColumns());
            roundTrip(table);
            List<Long> values = new ArrayList<>();
            ExpressionDeParser expressions = new ExpressionDeParser() {
                @Override
                public <S> StringBuilder visit(LongValue value, S context) {
                    values.add(value.getValue());
                    return getBuilder().append(value.getValue());
                }
            };
            StringBuilder out = new StringBuilder();
            table.accept(new StatementDeParser(expressions, new SelectDeParser(), out));
            assertEquals(table.toString(), out.toString());
            assertEquals(1, values.stream().filter(v -> v == 7).count());
        }
    }

    @Test
    void singleBareOverrideAndOrdinaryTypeParametersRemainDistinct() throws JSQLParserException {
        assertNotNull(parse("CREATE TABLE t OF row_type (a)").getColumnDefinitions());
        assertNotNull(
                parse("CREATE TABLE child PARTITION OF parent (a) DEFAULT").getColumnDefinitions());
        assertEquals("numeric (10, 2)", parse("CREATE TABLE t (a numeric(10, 2))")
                .getColumnDefinitions().get(0).getColDataType().toString());
        assertEquals(List.of("a"), parse("CREATE TABLE t (a) AS SELECT 1").getColumns());
        assertEquals(2, CCJSqlParserUtil
                .parseStatements("CREATE TABLE t OF row_type (a DEFAULT 1); SELECT 1").size());
    }

    private static CreateTable parse(String sql) throws JSQLParserException {
        return (CreateTable) CCJSqlParserUtil.parse(sql, p -> p.withDialect(Dialect.POSTGRESQL));
    }

    private static void roundTrip(CreateTable table) throws JSQLParserException {
        StringBuilder out = new StringBuilder();
        table.accept(new StatementDeParser(out));
        assertEquals(table.toString(), out.toString());
        assertEquals(out.toString(), parse(out.toString()).toString());
    }
}
