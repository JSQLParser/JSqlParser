/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.insert;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.select.OrderByElement;

/** SQL Server's SQL declaration preceding a separate bulk-load data stream. */
public class InsertBulk extends ASTNodeAccessImpl implements Statement {
    private Table table;
    private List<ColumnDefinition> columns = new ArrayList<>();
    private List<Option> options = new ArrayList<>();

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public List<ColumnDefinition> getColumns() {
        return columns;
    }

    public void setColumns(List<ColumnDefinition> columns) {
        this.columns = columns;
    }

    public List<Option> getOptions() {
        return options;
    }

    public void setOptions(List<Option> options) {
        this.options = options;
    }

    public void visitExpressions(Consumer<Expression> visitor) {
        for (Option option : options) {
            if (option.getValue() != null) {
                visitor.accept(option.getValue());
            }
            for (OrderByElement order : option.getOrderByElements()) {
                visitor.accept(order.getExpression());
            }
        }
    }

    public StringBuilder appendTo(StringBuilder builder, Consumer<Expression> expressions,
            Consumer<OrderByElement> ordering) {
        builder.append("INSERT BULK ").append(table).append(" (");
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(columns.get(i));
        }
        builder.append(')');
        if (!options.isEmpty()) {
            builder.append(" WITH (");
            for (int i = 0; i < options.size(); i++) {
                if (i > 0) {
                    builder.append(", ");
                }
                options.get(i).appendTo(builder, expressions, ordering);
            }
            builder.append(')');
        }
        return builder;
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> visitor, S context) {
        return visitor.visit(this, context);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        return appendTo(builder, value -> builder.append(value), order -> builder.append(order))
                .toString();
    }

    public static class Option {
        public enum Kind {
            CHECK_CONSTRAINTS, FIRE_TRIGGERS, KEEP_NULLS, TABLOCK, ALLOW_ENCRYPTED_VALUE_MODIFICATIONS, ROWS_PER_BATCH, ORDER
        }

        private Kind kind;
        private Expression value;
        private List<OrderByElement> orderByElements = new ArrayList<>();

        public Option(Kind kind) {
            this.kind = kind;
        }

        public Kind getKind() {
            return kind;
        }

        public void setKind(Kind kind) {
            this.kind = kind;
        }

        public Expression getValue() {
            return value;
        }

        public void setValue(Expression value) {
            this.value = value;
        }

        public List<OrderByElement> getOrderByElements() {
            return orderByElements;
        }

        public void setOrderByElements(List<OrderByElement> orderByElements) {
            this.orderByElements = orderByElements;
        }

        public StringBuilder appendTo(StringBuilder builder, Consumer<Expression> expressions,
                Consumer<OrderByElement> ordering) {
            builder.append(kind);
            if (value != null) {
                builder.append(" = ");
                expressions.accept(value);
            }
            if (!orderByElements.isEmpty()) {
                builder.append(" (");
                for (int i = 0; i < orderByElements.size(); i++) {
                    if (i > 0) {
                        builder.append(", ");
                    }
                    ordering.accept(orderByElements.get(i));
                }
                builder.append(')');
            }
            return builder;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            return appendTo(builder, value -> builder.append(value), order -> builder.append(order))
                    .toString();
        }
    }
}
