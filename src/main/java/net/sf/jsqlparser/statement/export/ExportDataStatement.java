/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.export;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.select.Select;

import java.util.function.Consumer;

/**
 * BigQuery's {@code EXPORT DATA [WITH CONNECTION connection] OPTIONS (option_list) AS query}.
 *
 * @see <a href=
 *      "https://cloud.google.com/bigquery/docs/reference/standard-sql/export-statements">Export
 *      statements</a>
 */
public class ExportDataStatement implements Statement {
    private String connectionName;
    private ExpressionList<Expression> options;
    private Select select;

    public String getConnectionName() {
        return connectionName;
    }

    public void setConnectionName(String connectionName) {
        this.connectionName = connectionName;
    }

    public ExportDataStatement withConnectionName(String connectionName) {
        setConnectionName(connectionName);
        return this;
    }

    public ExpressionList<Expression> getOptions() {
        return options;
    }

    public void setOptions(ExpressionList<Expression> options) {
        this.options = options;
    }

    public ExportDataStatement withOptions(ExpressionList<Expression> options) {
        setOptions(options);
        return this;
    }

    public Select getSelect() {
        return select;
    }

    public void setSelect(Select select) {
        this.select = select;
    }

    public ExportDataStatement withSelect(Select select) {
        setSelect(select);
        return this;
    }

    public StringBuilder appendTo(StringBuilder builder) {
        return appendTo(builder, builder::append, builder::append);
    }

    /** Renders options and the query through the caller's expression and select visitors. */
    public StringBuilder appendTo(StringBuilder builder, Consumer<Expression> expressionRenderer,
            Consumer<Select> selectRenderer) {
        builder.append("EXPORT DATA");
        if (connectionName != null) {
            builder.append(" WITH CONNECTION ").append(connectionName);
        }
        if (options != null) {
            builder.append(" OPTIONS (");
            for (int i = 0; i < options.size(); i++) {
                if (i > 0) {
                    builder.append(", ");
                }
                expressionRenderer.accept(options.get(i));
            }
            builder.append(")");
        }
        builder.append(" AS ");
        selectRenderer.accept(select);
        return builder;
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }
}
