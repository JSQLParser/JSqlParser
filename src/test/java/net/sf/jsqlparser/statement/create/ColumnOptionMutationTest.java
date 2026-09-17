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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.ColumnOption;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.ForeignKeyReference;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;

class ColumnOptionMutationTest {
    @Test
    void clearingIdentityDoesNotRestoreRawSpecifications() throws JSQLParserException {
        CreateTable table = parse("id INT GENERATED ALWAYS AS IDENTITY");
        ColumnDefinition column = table.getColumnDefinitions().get(0);
        column.getColumnOptions().clear();
        assertTrue(column.getColumnSpecs().isEmpty());
        assertSql(table, "CREATE TABLE t (id INT)");
    }

    @Test
    void legacyAppendPreservesReferenceAndTraversal() throws JSQLParserException {
        CreateTable table = parse("id INT REFERENCES parent(id)");
        ColumnDefinition column = table.getColumnDefinitions().get(0);
        ForeignKeyReference reference = column.getForeignKeyReference();
        column.addColumnSpecs("NOT", "NULL");
        column.addColumnSpecs(Arrays.asList("DEFAULT", "1"));
        assertSame(reference, column.getForeignKeyReference());
        assertTrue(new TablesNamesFinder().getTables(table).contains("parent"));
        assertSql(table, "CREATE TABLE t (id INT REFERENCES parent(id) NOT NULL DEFAULT 1)");
    }

    @Test
    void addingStructuredOptionPreservesExistingRawSpecifications() throws JSQLParserException {
        CreateTable table = parse("id INT");
        ColumnDefinition column = table.getColumnDefinitions().get(0);
        column.setColumnSpecs(Arrays.asList("NOT", "NULL"));
        assertNull(column.getColumnOptions());
        ForeignKeyReference reference = parse("id INT REFERENCES parent(id)")
                .getColumnDefinitions().get(0).getForeignKeyReference();
        column.addColumnOptions(ColumnOption.reference(reference));
        assertSql(table, "CREATE TABLE t (id INT NOT NULL REFERENCES parent(id))");
        assertTrue(new TablesNamesFinder().getTables(table).contains("parent"));
    }

    @Test
    void replacingOptionsWithNullClearsPreviousSpecifications() throws JSQLParserException {
        CreateTable table = parse("id INT GENERATED ALWAYS AS IDENTITY");
        ColumnDefinition column = table.getColumnDefinitions().get(0);
        column.setColumnOptions(null);
        assertNull(column.getColumnSpecs());
        assertSql(table, "CREATE TABLE t (id INT)");
    }

    @Test
    void legacySetterStillReplacesStructuredOptions() throws JSQLParserException {
        CreateTable table = parse("id INT REFERENCES parent(id)");
        ColumnDefinition column = table.getColumnDefinitions().get(0);
        column.setColumnSpecs(Arrays.asList("NOT", "NULL"));
        assertNull(column.getForeignKeyReference());
        assertSql(table, "CREATE TABLE t (id INT NOT NULL)");
    }

    private static CreateTable parse(String definition) throws JSQLParserException {
        return (CreateTable) CCJSqlParserUtil.parse("CREATE TABLE t (" + definition + ")");
    }

    private static void assertSql(CreateTable table, String expected) throws JSQLParserException {
        assertEquals(expected, table.toString());
        StringBuilder buffer = new StringBuilder();
        table.accept(new StatementDeParser(buffer), null);
        assertEquals(expected, buffer.toString());
        assertEquals(expected, CCJSqlParserUtil.parse(expected).toString());
    }
}
