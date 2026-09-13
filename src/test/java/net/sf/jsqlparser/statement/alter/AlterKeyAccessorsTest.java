/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.alter;

import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.List;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.create.table.Index;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class AlterKeyAccessorsTest {
    private List<String> columns(AlterExpression action, boolean primary) {
        return primary ? action.getPkColumns() : action.getUkColumns();
    }

    private void setColumns(AlterExpression action, boolean primary, List<String> columns) {
        if (primary) {
            action.setPkColumns(columns);
        } else {
            action.setUkColumns(columns);
        }
    }

    private void assertDeparsed(Alter statement, String expected) {
        assertEquals(expected, statement.toString());
        StringBuilder output = new StringBuilder();
        statement.accept(new StatementDeParser(output), null);
        assertEquals(expected, output.toString());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void readsCurrentIndexAndAppliesSettersAndFluentAdditions(boolean primary) throws Exception {
        String prefix = "ALTER TABLE t ADD " + (primary ? "PRIMARY KEY" : "UNIQUE");
        Alter statement = (Alter) CCJSqlParserUtil.parse(prefix + " (a)");
        AlterExpression action = statement.getAlterExpressions().get(0);
        Index originalIndex = action.getIndex();
        originalIndex.setColumnsNames(List.of("b"));
        List<String> snapshot = columns(action, primary);
        assertEquals(List.of("b"), snapshot);
        snapshot.set(0, "c");
        assertEquals(List.of("b"), columns(action, primary));
        assertDeparsed(statement, prefix + " (b)");
        setColumns(action, primary, List.of("c"));
        assertDeparsed(statement, prefix + " (c)");
        if (primary) {
            action.addPkColumns("d").addPkColumns(List.of("e"));
        } else {
            action.addUkColumns("d").addUkColumns(List.of("e"));
        }
        assertEquals(List.of("c", "d", "e"), columns(action, primary));
        assertSame(originalIndex, action.getIndex());
        assertDeparsed(statement, prefix + " (c, d, e)");
        setColumns(action, primary, null);
        assertTrue(columns(action, primary).isEmpty());
        assertTrue(originalIndex.getColumns().isEmpty());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void replacingWithBareNamesDropsElementOptionsButKeepsIndexMetadata(boolean primary)
            throws Exception {
        String prefix = "ALTER TABLE t ADD CONSTRAINT key_t "
                + (primary ? "PRIMARY KEY" : "UNIQUE");
        Alter statement = (Alter) CCJSqlParserUtil.parse(prefix + " (a DESC, b) DEFERRABLE");
        AlterExpression action = statement.getAlterExpressions().get(0);
        Index index = action.getIndex();
        Index.ColumnParams first = index.getColumns().get(0);
        assertEquals("a DESC", first.toString());
        setColumns(action, primary, List.of("b", "a"));
        assertEquals(List.of("b", "a"), columns(action, primary));
        assertNull(index.getColumns().get(1).getSortOrder());
        assertNull(index.getColumns().get(1).getParams());
        assertNotSame(first, index.getColumns().get(1));
        assertSame(index, action.getIndex());
        assertEquals("key_t", index.getName());
        assertDeparsed(statement, prefix + " (b, a) DEFERRABLE");
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void settingAnUnchangedSnapshotPreservesSqlButRebuildsPlainElements(boolean primary)
            throws Exception {
        String prefix = "ALTER TABLE t ADD " + (primary ? "PRIMARY KEY" : "UNIQUE");
        Alter statement = (Alter) CCJSqlParserUtil.parse(prefix + " (a DESC, b)");
        AlterExpression action = statement.getAlterExpressions().get(0);
        Index index = action.getIndex();
        Index.ColumnParams expression = new Index.ColumnParams(new Column("a"))
                .withExpressionParenthesized(false)
                .withSortOrder(Index.ColumnParams.SortOrder.DESC);
        index.getColumns().set(0, expression);
        setColumns(action, primary, new ArrayList<>(columns(action, primary)));
        Index.ColumnParams replacement = index.getColumns().get(0);
        assertNotSame(expression, replacement);
        assertEquals("a DESC", replacement.getColumnName());
        assertNull(replacement.getExpression());
        assertNull(replacement.getSortOrder());
        assertDeparsed(statement, prefix + " (a DESC, b)");
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void structuredReorderingRetainsElementIdentityAndOptions(boolean primary) throws Exception {
        String prefix = "ALTER TABLE t ADD " + (primary ? "PRIMARY KEY" : "UNIQUE");
        Alter statement = (Alter) CCJSqlParserUtil.parse(prefix + " (a DESC, b)");
        AlterExpression action = statement.getAlterExpressions().get(0);
        Index index = action.getIndex();
        Index.ColumnParams first = index.getColumns().get(0);
        Index.ColumnParams second = index.getColumns().get(1);
        index.setColumns(List.of(second, first));
        assertSame(first, index.getColumns().get(1));
        assertSame(second, index.getColumns().get(0));
        assertEquals(Index.ColumnParams.SortOrder.DESC, first.getSortOrder());
        assertEquals(List.of("b", "a DESC"), columns(action, primary));
        assertDeparsed(statement, prefix + " (b, a DESC)");
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void supportsAnIndexWithoutColumnsAndFluentPopulation(boolean primary) {
        Index index = new Index().withType(primary ? "PRIMARY KEY" : "UNIQUE");
        AlterExpression action = new AlterExpression().withOperation(AlterOperation.ADD)
                .withIndex(index);
        assertTrue(index.getColumnsNames().isEmpty());
        assertTrue(columns(action, primary).isEmpty());
        index.getColumnsNames().add("detached");
        assertNull(index.getColumns());
        if (primary) {
            action.addPkColumns("a");
        } else {
            action.addUkColumns("a");
        }
        assertEquals(List.of("a"), columns(action, primary));
        index.setColumns(null);
        assertTrue(columns(action, primary).isEmpty());
        setColumns(action, primary, List.of("b"));
        assertEquals(List.of("b"), index.getColumnsNames());
    }

    @Test
    void retainsLegacyOnlyConstructionAndIgnoresUnrelatedIndexes() {
        AlterExpression action = new AlterExpression().withOperation(AlterOperation.ADD)
                .withPkColumns(new ArrayList<>(List.of("a")));
        action.getPkColumns().add("b");
        assertEquals("ADD PRIMARY KEY (a, b)", action.toString());
        Index unrelated = new Index().withType("INDEX").withColumnsNames(List.of("other"));
        action.setIndex(unrelated);
        action.setPkColumns(List.of("pk"));
        action.setUkColumns(List.of("uk"));
        assertEquals(List.of("pk"), action.getPkColumns());
        assertEquals(List.of("uk"), action.getUkColumns());
        assertEquals(List.of("other"), unrelated.getColumnsNames());
    }
}
