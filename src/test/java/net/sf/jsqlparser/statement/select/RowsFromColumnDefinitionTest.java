/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class RowsFromColumnDefinitionTest {

    // Accepted/rejected fixtures were checked against PostgreSQL 18.6.
    static Stream<String> validSql() throws IOException {
        return fixture("rows-from-valid.sql");
    }

    static Stream<String> invalidSql() throws IOException {
        return fixture("rows-from-invalid.sql");
    }

    private static Stream<String> fixture(String name) throws IOException {
        try (InputStream input = RowsFromColumnDefinitionTest.class.getResourceAsStream(name)) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8).lines()
                    .filter(line -> !line.isBlank());
        }
    }

    @ParameterizedTest
    @MethodSource("validSql")
    void preservesDefinitionsAndOuterAliasesThroughBothRenderers(String sql)
            throws JSQLParserException {
        PlainSelect select = parse(sql);
        StringBuilder visitor = new StringBuilder();
        select.accept(new StatementDeParser(visitor));
        for (String rendered : List.of(select.toString(), visitor.toString())) {
            PlainSelect reparsed = parse(rendered);
            assertEquals(select.toString(), reparsed.toString());
            assertEquals(select.getFromItem().getClass(), reparsed.getFromItem().getClass());
        }
    }

    @ParameterizedTest
    @MethodSource("invalidSql")
    void rejectsTableConstraintsAndMalformedColumnLists(String sql) {
        assertThrows(JSQLParserException.class, () -> parse(sql));
    }

    @Test
    void eachFunctionOwnsItsDefinitionsIndependentlyOfTheOuterAlias()
            throws JSQLParserException {
        PlainSelect select = parse("SELECT * FROM ROWS FROM ("
                + "json_to_record('{}') AS (amount numeric(10, 2), label text COLLATE \"C\"), "
                + "generate_series(1, 2), json_to_record('{}') AS (items integer[])) "
                + "WITH ORDINALITY AS r(total, name, n, data, ord)");
        TableFunction table = (TableFunction) select.getFromItem();
        assertTrue(table.isRowsFrom());
        assertEquals(3, table.getRowsFromFunctions().size());
        assertEquals("ORDINALITY", table.getWithClause());
        assertEquals(5, table.getAlias().getAliasColumns().size());
        List<ColumnDefinition> first = table.getFunctions().get(0).getResultColumnDefinitions();
        assertEquals("amount", first.get(0).getColumnName());
        assertEquals(10, first.get(0).getColDataType().getPrecision());
        assertEquals(2, first.get(0).getColDataType().getScale());
        assertEquals("\"C\"", first.get(1).getColumnOptions().get(0).getCollation());
        assertNull(table.getFunctions().get(1).getResultColumnDefinitions());
        assertEquals(1, table.getFunctions().get(2).getResultColumnDefinitions().get(0)
                .getColDataType().getArrayData().size());

        first.get(0).setColumnName("changed");
        first.get(0).setColDataType(new ColDataType("bigint"));
        TableFunction reparsed = (TableFunction) parse(select.toString()).getFromItem();
        assertEquals("changed", reparsed.getFunctions().get(0).getResultColumnDefinitions()
                .get(0).getColumnName());
        assertEquals("bigint", reparsed.getFunctions().get(0).getResultColumnDefinitions()
                .get(0).getColDataType().getDataType());
    }

    @Test
    void legacyFunctionListEditsKeepDefinitionsAttachedToTheirFunction()
            throws JSQLParserException {
        TableFunction table = (TableFunction) parse("SELECT * FROM ROWS FROM ("
                + "json_to_record('{}') AS (a integer), generate_series(1, 2))").getFromItem();
        Function record = table.getRowsFromFunctions().remove(0);
        table.getRowsFromFunctions().add(record);
        TableFunction reparsed = (TableFunction) parse("SELECT * FROM " + table).getFromItem();
        assertNull(reparsed.getFunctions().get(0).getResultColumnDefinitions());
        assertNotNull(reparsed.getFunctions().get(1).getResultColumnDefinitions());
    }

    @Test
    void tableFunctionRendererVisitsFunctionArguments() throws JSQLParserException {
        PlainSelect select = parse("SELECT * FROM ROWS FROM (json_to_record('{}') AS (a integer), "
                + "generate_series(1, 2)) WITH ORDINALITY AS r(a, n, ord)");
        String original = select.toString();
        StringBuilder sql = new StringBuilder();
        ExpressionDeParser expressions = new ExpressionDeParser() {
            @Override
            public <S> StringBuilder visit(LongValue value, S context) {
                return getBuilder().append(value.getValue() + 10);
            }
        };
        select.accept(new StatementDeParser(expressions, new SelectDeParser(), sql));
        assertTrue(sql.toString().contains("generate_series(11, 12)"));
        assertTrue(sql.toString().contains("AS (a integer)"));
        assertEquals(original, select.toString());
        assertEquals(sql.toString(), parse(sql.toString()).toString());
    }

    @Test
    void tableDiscoveryStillVisitsSubqueriesInRecordFunctionArguments()
            throws JSQLParserException {
        String sql = "SELECT * FROM ROWS FROM ("
                + "json_to_record((SELECT payload FROM events)) AS (a integer))";
        assertEquals(Set.of("events"), TablesNamesFinder.findTables(sql));
    }

    private static PlainSelect parse(String sql) throws JSQLParserException {
        return (PlainSelect) CCJSqlParserUtil.parse(sql, p -> p.withDialect(Dialect.POSTGRESQL));
    }
}
