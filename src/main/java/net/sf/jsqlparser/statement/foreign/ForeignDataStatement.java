/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.foreign;

import java.util.List;
import java.util.function.Consumer;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.table.ForeignDataOption;

/** Shared structured options for PostgreSQL foreign-data DDL. */
public abstract class ForeignDataStatement implements Statement {
    private List<ForeignDataOption> options;

    public List<ForeignDataOption> getOptions() {
        return options;
    }

    public void setOptions(List<ForeignDataOption> options) {
        this.options = options;
    }

    protected void appendOptionsTo(StringBuilder sql, Consumer<Expression> printer) {
        if (options != null) {
            sql.append(' ');
            ForeignDataOption.appendOptionsTo(sql, options, printer);
        }
    }

    public void visitExpressions(Consumer<Expression> visitor) {
        ForeignDataOption.visitExpressions(options, visitor);
    }

    protected static void visit(Expression expression, Consumer<Expression> visitor) {
        if (expression != null) {
            visitor.accept(expression);
        }
    }

    public abstract StringBuilder appendTo(StringBuilder sql, Consumer<Expression> printer);

    @Override
    public String toString() {
        StringBuilder sql = new StringBuilder();
        return appendTo(sql, sql::append).toString();
    }
}
