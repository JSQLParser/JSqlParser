/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2025 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.schema;

import net.sf.jsqlparser.expression.Expression;

import java.util.Collection;
import java.util.Objects;

public class Partition {
    protected Column column;
    protected Expression value;

    public Partition() {

    }

    public Partition(Column column, Expression value) {
        this.column = column;
        this.value = value;
    }

    public static StringBuilder appendPartitionsTo(StringBuilder builder, Collection<Partition> partitions) {
        int j = 0;
        for (Partition partition : partitions) {
            partition.appendTo(builder, j);
            j++;
        }
        return builder;
    }

    public Column getColumn() {
        return column;
    }

    public void setColumn(Column column) {
        this.column = Objects.requireNonNull(column);
    }

    public Expression getValue() {
        return value;
    }

    public void setValue(Expression value) {
        this.value = Objects.requireNonNull(value);
    }


    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPath"})
    StringBuilder appendTo(StringBuilder builder, int j) {
        if (j > 0) {
            builder.append(", ");
        }
        builder.append(column.getColumnName());
        if (value != null) {
            builder.append(" = ").append(value);
        }
        return builder;
    }
}
