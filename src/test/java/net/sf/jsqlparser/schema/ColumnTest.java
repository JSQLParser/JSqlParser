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

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 * @author tw
 */
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

}
