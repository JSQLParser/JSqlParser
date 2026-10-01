/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.schema;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.PlainSelect;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static net.sf.jsqlparser.test.TestUtils.assertStatementCanBeDeparsedAs;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 *
 * @author tw
 */
@ResourceLock(Resources.SYSTEM_PROPERTIES)
public class ColumnTest {

    @Test
    public void testCheckNonFinalClass() {
        Column myColumn = new Column(null, "myColumn") {
            @Override
            public String toString() {
                return "anonymous class";
            }

        };
        assertEquals("anonymous class", myColumn.toString());
    }

    @Test
    public void testConstructorNameParts() {
        Column column = new Column(List.of("schema", "table", "column"));
        assertThat(column.getColumnName()).isEqualTo("column");

        Table table = column.getTable();
        assertThat(table.getNameParts()).containsExactly("table", "schema");
        assertThat(table.getNamePartDelimiters()).containsExactly(".");
    }

    @Test
    public void testConstructorNamePartsAndDelimiters() {
        Column column = new Column(List.of("a", "b", "c", "d"), List.of(":", ".", ":"));
        assertThat(column.getColumnName()).isEqualTo("d");

        Table table = column.getTable();
        assertThat(table.getNameParts()).containsExactly("c", "b", "a");
        assertThat(table.getNamePartDelimiters()).containsExactly(".", ":");
    }

    @ParameterizedTest
    @MethodSource("qualifiedQuotedNames")
    void testQualifiedQuotedNames(String name, List<String> tableParts, String columnName) {
        assertColumn(new Column(name), name, tableParts, columnName);
        Column column = new Column();
        column.setName(name, true);
        assertColumn(column, name, tableParts, columnName);
        assertColumn(new Column().withColumnName(name), name, tableParts, columnName);
    }

    @ParameterizedTest
    @MethodSource("separatedQuotedNames")
    void testQuotedNameParts(String name, List<String> tableParts, String columnName) {
        List<String> nameParts = new ArrayList<>(tableParts);
        Collections.reverse(nameParts);
        nameParts.add(columnName);
        assertColumn(new Column(nameParts), name, tableParts, columnName);
        assertColumn(new Column(nameParts, Collections.nCopies(nameParts.size() - 1, ".")),
                name, tableParts, columnName);
    }

    @ParameterizedTest
    @MethodSource("parsedQualifiedQuotedNames")
    void testParsedQualifiedQuotedNames(String name, List<String> tableParts, String columnName)
            throws JSQLParserException {
        String sql = "SELECT " + name + " FROM t1";
        PlainSelect select = (PlainSelect) assertSqlCanBeParsedAndDeparsed(sql, false,
                parser -> parser.withSquareBracketQuotation(true));
        assertColumn(select.getSelectItem(0).getExpression(Column.class), name, tableParts,
                columnName);
        PlainSelect reparsed = (PlainSelect) CCJSqlParserUtil.parse(select.toString(),
                parser -> parser.withSquareBracketQuotation(true));
        assertColumn(reparsed.getSelectItem(0).getExpression(Column.class), name, tableParts,
                columnName);
    }

    @ParameterizedTest
    @MethodSource("wholeQuotedNames")
    void testWholeQuotedDottedNames(String name, String expected, List<String> tableParts,
            String columnName) throws JSQLParserException {
        assertColumn(new Column(name), expected, tableParts, columnName);
        PlainSelect select = (PlainSelect) CCJSqlParserUtil.parse("SELECT " + name + " FROM t1",
                parser -> parser.withSquareBracketQuotation(true));
        assertColumn(select.getSelectItem(0).getExpression(Column.class), expected, tableParts,
                columnName);
        assertStatementCanBeDeparsedAs(select, "SELECT " + expected + " FROM t1");
    }

    @ParameterizedTest
    @ValueSource(strings = {"id", "`id`", "\"id\"", "[id]"})
    void testUnqualifiedNames(String name) {
        Column column = new Column(name);
        assertEquals(name, column.getColumnName());
        assertEquals(name, column.toString());
        assertEquals("id", column.getUnquotedColumnName());
        assertNull(column.getTable());
    }

    @ParameterizedTest
    @MethodSource("legacyQualifiedTableNames")
    void testLegacyQualifiedTableNames(String tableName, String expected,
            List<String> tableParts) {
        assertColumn(new Column(List.of(tableName, "id")), expected, tableParts, "id");
        assertColumn(new Column(List.of(tableName, "id"), List.of(".")), expected,
                tableParts, "id");
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void testBigQueryQualifiedTableColumn(boolean explicitDialect) throws JSQLParserException {
        PlainSelect select = (PlainSelect) CCJSqlParserUtil.parse(
                "SELECT `project.dataset.table`.id FROM `project.dataset.table`", parser -> {
                    if (explicitDialect) {
                        parser.withDialect(Dialect.BIGQUERY);
                    }
                });
        Column column = select.getSelectItem(0).getExpression(Column.class);
        assertColumn(column, "\"project\".\"dataset\".\"table\".id",
                List.of("\"table\"", "\"dataset\"", "\"project\""), "id");
        assertEquals("project", column.getUnquotedCatalogName());
        assertEquals("dataset", column.getUnquotedSchemaName());
        assertEquals("table", column.getUnquotedTableName());
        assertEquals(select.getFromItem(Table.class).getNameParts(),
                column.getTable().getNameParts());
        assertStatementCanBeDeparsedAs(select,
                "SELECT \"project\".\"dataset\".\"table\".id FROM \"project\".\"dataset\".\"table\"");
    }

    @ParameterizedTest
    @ValueSource(strings = {"t1.`id`", "`t1.id`", "\"t1.id\"", "[t1.id]", "s.t1.id"})
    void testExplicitlyDisabledSplitting(String name) {
        Column column = new Column();
        column.setName(name, false);
        assertEquals(name, column.getColumnName());
        assertEquals(name, column.toString());
        assertNull(column.getTable());
    }

    @ParameterizedTest
    @ValueSource(strings = {"false", "OFF", "0"})
    void testSystemPropertyDisablesSplitting(String value) {
        String previous = System.getProperty("SPLIT_NAMES_ON_DELIMITER");
        try {
            System.setProperty("SPLIT_NAMES_ON_DELIMITER", value);
            for (String name : List.of("t1.`id`", "`t1.id`", "\"t1.id\"", "[t1.id]", "s.t1.id")) {
                Column column = new Column(name);
                assertEquals(name, column.getColumnName());
                assertEquals(name, column.toString());
                assertNull(column.getTable());
            }
        } finally {
            if (previous == null) {
                System.clearProperty("SPLIT_NAMES_ON_DELIMITER");
            } else {
                System.setProperty("SPLIT_NAMES_ON_DELIMITER", previous);
            }
        }
    }

    private static void assertColumn(Column column, String name, List<String> tableParts,
            String columnName) {
        assertEquals(name, column.toString());
        assertEquals(name, column.getFullyQualifiedName());
        assertEquals(columnName, column.getColumnName());
        assertThat(column.getTable().getNameParts()).containsExactlyElementsOf(tableParts);
        assertEquals(tableParts.get(0), column.getTableName());
        assertEquals(tableParts.size() > 1 ? tableParts.get(1) : null, column.getSchemaName());
        assertEquals(tableParts.size() > 2 ? tableParts.get(2) : null, column.getCatalogName());
    }

    static Stream<Arguments> qualifiedQuotedNames() {
        return Stream.of(
                Arguments.of("t1.`id`", List.of("t1"), "`id`"),
                Arguments.of("t1.\"id\"", List.of("t1"), "\"id\""),
                Arguments.of("t1.[id]", List.of("t1"), "[id]"),
                Arguments.of("`t1`.id", List.of("`t1`"), "id"),
                Arguments.of("\"t1\".id", List.of("\"t1\""), "id"),
                Arguments.of("[t1].id", List.of("[t1]"), "id"),
                Arguments.of("`t1`.`id`", List.of("`t1`"), "`id`"),
                Arguments.of("\"t1\".\"id\"", List.of("\"t1\""), "\"id\""),
                Arguments.of("[t1].[id]", List.of("[t1]"), "[id]"),
                Arguments.of("s.t1.`id`", List.of("t1", "s"), "`id`"),
                Arguments.of("`s`.`t1`.`id`", List.of("`t1`", "`s`"), "`id`"),
                Arguments.of("\"s\".\"t1\".\"id\"", List.of("\"t1\"", "\"s\""), "\"id\""),
                Arguments.of("[s].[t1].[id]", List.of("[t1]", "[s]"), "[id]"),
                Arguments.of("[s].`t1`.\"id\"", List.of("`t1`", "[s]"), "\"id\""),
                Arguments.of("t1.`i.d`", List.of("t1"), "`i.d`"),
                Arguments.of("t1.`i``.d`", List.of("t1"), "`i``.d`"),
                Arguments.of("t1.\"i\"\".d\"", List.of("t1"), "\"i\"\".d\""),
                Arguments.of("t1.[i]].d]", List.of("t1"), "[i]].d]"),
                Arguments.of("`t.1`.id", List.of("`t.1`"), "id"),
                Arguments.of("`s.1`.`t.1`.`i.d`", List.of("`t.1`", "`s.1`"), "`i.d`"),
                Arguments.of("\"s.1\".\"t.1\".\"i.d\"", List.of("\"t.1\"", "\"s.1\""), "\"i.d\""),
                Arguments.of("[s.1].[t.1].[i.d]", List.of("[t.1]", "[s.1]"), "[i.d]"),
                Arguments.of("`t``.1`.`i``.d`", List.of("`t``.1`"), "`i``.d`"),
                Arguments.of("\"t\"\".1\".\"i\"\".d\"", List.of("\"t\"\".1\""), "\"i\"\".d\""),
                Arguments.of("[t]].1].[i]].d]", List.of("[t]].1]"), "[i]].d]"),
                Arguments.of("`t\".1`.\"i`.d\"", List.of("`t\".1`"), "\"i`.d\""),
                Arguments.of("t1.id", List.of("t1"), "id"),
                Arguments.of("s.t1.id", List.of("t1", "s"), "id"));
    }

    static Stream<Arguments> separatedQuotedNames() {
        // A single table token containing dots keeps the legacy BigQuery interpretation.
        return qualifiedQuotedNames()
                .filter(arguments -> {
                    List<?> tableParts = (List<?>) arguments.get()[1];
                    return tableParts.size() > 1 || !((String) tableParts.get(0)).contains(".");
                });
    }

    static Stream<Arguments> parsedQualifiedQuotedNames() {
        return separatedQuotedNames()
                .filter(arguments -> !((String) arguments.get()[0]).contains("]]"));
    }

    static Stream<Arguments> legacyQualifiedTableNames() {
        return Stream.of(
                Arguments.of("schema.table", "schema.table.id", List.of("table", "schema")),
                Arguments.of("`schema.table`", "\"schema\".\"table\".id",
                        List.of("\"table\"", "\"schema\"")),
                Arguments.of("`project.dataset.table`", "\"project\".\"dataset\".\"table\".id",
                        List.of("\"table\"", "\"dataset\"", "\"project\"")));
    }

    static Stream<Arguments> wholeQuotedNames() {
        return Stream.of(
                Arguments.of("`t1.id`", "\"t1\".\"id\"", List.of("\"t1\""), "\"id\""),
                Arguments.of("\"t1.id\"", "\"t1\".\"id\"", List.of("\"t1\""), "\"id\""),
                Arguments.of("[t1.id]", "\"t1\".\"id\"", List.of("\"t1\""), "\"id\""),
                Arguments.of("`s.t1.id`", "\"s\".\"t1\".\"id\"", List.of("\"t1\"", "\"s\""),
                        "\"id\""),
                Arguments.of("\"s.t1.id\"", "\"s\".\"t1\".\"id\"", List.of("\"t1\"", "\"s\""),
                        "\"id\""),
                Arguments.of("[s.t1.id]", "\"s\".\"t1\".\"id\"", List.of("\"t1\"", "\"s\""),
                        "\"id\""));
    }

}
