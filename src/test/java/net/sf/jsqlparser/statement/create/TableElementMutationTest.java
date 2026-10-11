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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.LikeClause;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;

class TableElementMutationTest {
    private static final String SQL = "CREATE TABLE t (LIKE parent INCLUDING DEFAULTS, "
            + "a INT, CONSTRAINT c CHECK (a > 0), b INT)";

    @Test
    void legacyListEditsUpdateOrderedElements() throws JSQLParserException {
        CreateTable table = parse();
        table.getColumnDefinitions().remove(1);
        table.getTableConstraints().clear();
        table.getColumnDefinitions().add(0, column("first"));
        table.getColumnDefinitions().set(1, column("last"));
        assertSql(table, "CREATE TABLE t (LIKE parent INCLUDING DEFAULTS, first INT, last INT)");
        assertEquals(3, table.getTableElements().size());
        assertTrue(new TablesNamesFinder().getTables(table).contains("parent"));
    }

    @Test
    void orderedListEditsRemainVisibleThroughExistingViews() throws JSQLParserException {
        CreateTable table = parse();
        List<ColumnDefinition> columns = table.getColumnDefinitions();
        table.getTableElements().remove(1);
        assertEquals("b", columns.get(0).getColumnName());
        table.getTableElements().add(column("extra"));
        assertEquals(2, columns.size());
        columns.clear();
        assertEquals(2, table.getTableElements().size());
        assertEquals(1, table.getTableConstraints().size());
        assertEquals(1, table.getTableElements(LikeClause.class).size());
    }

    @Test
    void replacingColumnsPreservesOtherElementsAndTheirPositions() throws JSQLParserException {
        CreateTable table = parse();
        table.setColumnDefinitions(new ArrayList<>(table.getColumnDefinitions()));
        assertSql(table, SQL);
        table.setColumnDefinitions(Collections.singletonList(column("replacement")));
        assertSql(table, "CREATE TABLE t (LIKE parent INCLUDING DEFAULTS, replacement INT, "
                + "CONSTRAINT c CHECK (a > 0))");
        table.setTableConstraints(null);
        table.setColumnDefinitions(null);
        assertSql(table, "CREATE TABLE t (LIKE parent INCLUDING DEFAULTS)");
    }

    @Test
    void fluentAddersCanPassTheirOwnViewsToSetters() throws JSQLParserException {
        CreateTable table = parse();
        table.addColumnDefinitions(column("extra"));
        assertEquals(3, table.getColumnDefinitions().size());
        assertEquals(1, table.getTableElements(LikeClause.class).size());
        table.setTableConstraints(table.getTableConstraints());
        assertEquals(1, table.getTableConstraints().size());
        assertSql(table, SQL.substring(0, SQL.length() - 1) + ", extra INT)");
    }

    @Test
    void filteredListHonorsIndexBounds() throws JSQLParserException {
        List<ColumnDefinition> columns = parse().getColumnDefinitions();
        assertThrows(IndexOutOfBoundsException.class, () -> columns.get(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> columns.get(2));
        assertThrows(IndexOutOfBoundsException.class, () -> columns.add(3, column("bad")));
    }

    @Test
    void clearingAllElementsKeepsBothRenderersConsistent() throws JSQLParserException {
        CreateTable table = parse();
        table.getTableElements().clear();
        assertSql(table, "CREATE TABLE t ()");
        assertTrue(table.getColumnDefinitions().isEmpty());
        assertTrue(table.getTableConstraints().isEmpty());
    }

    private static CreateTable parse() throws JSQLParserException {
        return (CreateTable) CCJSqlParserUtil.parse(SQL);
    }

    private static ColumnDefinition column(String name) {
        return new ColumnDefinition(name, new ColDataType("INT"));
    }

    private static void assertSql(CreateTable table, String expected) {
        assertEquals(expected, table.toString());
        StringBuilder buffer = new StringBuilder();
        table.accept(new StatementDeParser(buffer), null);
        assertEquals(expected, buffer.toString());
    }
}
