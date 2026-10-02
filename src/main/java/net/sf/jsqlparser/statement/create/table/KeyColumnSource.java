/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.sf.jsqlparser.expression.Expression;

/** Shared key access without making a constraint an index. */
public interface KeyColumnSource {
    List<KeyElement> getColumns();

    void setColumns(List<KeyElement> columns);

    /** Mutable rendered snapshot; use getColumns() for structured edits. */
    default List<String> getColumnsNames() {
        return getColumns() == null ? new ArrayList<>()
                : getColumns().stream()
                        .map(KeyElement::toString).collect(Collectors.toList());
    }

    /** Replaces keys with opaque names, without parsing or retaining expression options. */
    default void setColumnsNames(List<String> names) {
        setColumns(names == null ? Collections.emptyList()
                : names.stream()
                        .map(KeyElement::new).collect(Collectors.toList()));
    }

    default void appendColumnsTo(StringBuilder sql, Consumer<Expression> expressionPrinter) {
        sql.append('(');
        for (int i = 0; i < getColumns().size(); i++) {
            if (i > 0) {
                sql.append(", ");
            }
            getColumns().get(i).appendTo(sql, expressionPrinter);
        }
        sql.append(')');
    }
}
