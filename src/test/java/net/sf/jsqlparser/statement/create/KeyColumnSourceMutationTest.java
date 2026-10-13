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

import java.util.List;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.create.table.*;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class KeyColumnSourceMutationTest {
    @ParameterizedTest
    @ValueSource(strings = {"INDEX ix (id DESC)", "PRIMARY KEY (id)", "UNIQUE (id DESC)",
            "FOREIGN KEY (id) REFERENCES parent (id)", "EXCLUDE USING btree (id WITH =)"})
    void clearedNamesCanBeRepopulatedThroughBothAppendApisAndTheLiveList(String declaration)
            throws Exception {
        for (boolean nullClear : new boolean[] {true, false}) {
            for (boolean collectionAppend : new boolean[] {false, true}) {
                CreateTable table = (CreateTable) CCJSqlParserUtil.parse(
                        "CREATE TABLE t (id INT, " + declaration + ")");
                KeyColumnSource key = (KeyColumnSource) table.getTableElements().get(1);
                KeyElement element = key.getColumns().get(0);
                String expected = table.toString();

                key.setColumnsNames(nullClear ? null : List.of());
                assertNotNull(key.getColumns());
                assertTrue(key.getColumns().isEmpty());
                assertTrue(key.getColumnsNames().isEmpty());
                append(key, element, collectionAppend);
                assertSame(element, key.getColumns().get(0));
                assertSql(table, expected);

                // Clearing and editing through the shared structured API must also work.
                key.setColumnsNames(null);
                key.getColumns().add(element);
                assertSql(table, expected);
            }
        }
    }

    private static void append(KeyColumnSource key, KeyElement element, boolean collection) {
        if (key instanceof Index) {
            if (collection) {
                ((Index) key).addColumns(List.of(element));
            } else {
                ((Index) key).addColumns(element);
            }
        } else if (key instanceof KeyConstraint) {
            if (collection) {
                ((KeyConstraint) key).addColumns(List.of(element));
            } else {
                ((KeyConstraint) key).addColumns(element);
            }
        } else if (key instanceof ForeignKeyConstraint) {
            if (collection) {
                ((ForeignKeyConstraint) key).addColumns(List.of(element));
            } else {
                ((ForeignKeyConstraint) key).addColumns(element);
            }
        } else if (key instanceof ExcludeConstraint) {
            if (collection) {
                ((ExcludeConstraint) key).addColumns(List.of(element));
            } else {
                ((ExcludeConstraint) key).addColumns(element);
            }
        } else {
            fail("Unexpected key owner: " + key.getClass());
        }
    }

    private static void assertSql(CreateTable table, String expected) throws Exception {
        assertEquals(expected, table.toString());
        StringBuilder output = new StringBuilder();
        table.accept(new StatementDeParser(output), null);
        assertEquals(expected, output.toString());
        assertEquals(expected, CCJSqlParserUtil.parse(output.toString()).toString());
    }
}
