/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.resourcegroup;

import java.util.Objects;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

/**
 * MySQL {@code SET RESOURCE GROUP group_name [FOR thread_id [, thread_id] ...]}, which assigns the
 * named threads, or the current thread, to a resource group. A thread ID is the number as written:
 * a {@code LongValue}, or a {@code HexValue} for {@code 0x1F} and {@code X'1F'}. The commas are
 * optional in MySQL and are always written here.
 *
 * @see <a href="https://dev.mysql.com/doc/refman/8.4/en/set-resource-group.html">MySQL SET RESOURCE
 *      GROUP Statement</a>
 */
public class SetResourceGroup extends ASTNodeAccessImpl implements Statement {
    private String name;
    private ExpressionList<Expression> threadIds;

    public SetResourceGroup() {
        // the parser fills the statement in
    }

    public SetResourceGroup(String name) {
        setName(name);
    }

    public String getName() {
        return name;
    }

    public final void setName(String name) {
        this.name = Objects.requireNonNull(name, "name");
    }

    /** The thread IDs after FOR, or null when the statement assigns the current thread. */
    public ExpressionList<Expression> getThreadIds() {
        return threadIds;
    }

    public void setThreadIds(ExpressionList<Expression> threadIds) {
        this.threadIds = threadIds;
    }

    public SetResourceGroup withThreadIds(ExpressionList<Expression> threadIds) {
        setThreadIds(threadIds);
        return this;
    }

    public SetResourceGroup addThreadIds(Expression... threadIds) {
        if (this.threadIds == null) {
            this.threadIds = new ExpressionList<>();
        }
        for (Expression threadId : threadIds) {
            this.threadIds.add(threadId);
        }
        return this;
    }

    public StringBuilder appendTo(StringBuilder builder) {
        builder.append("SET RESOURCE GROUP ").append(name);
        if (threadIds != null && !threadIds.isEmpty()) {
            for (int i = 0; i < threadIds.size(); i++) {
                builder.append(i == 0 ? " FOR " : ", ").append(threadIds.get(i));
            }
        }
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
