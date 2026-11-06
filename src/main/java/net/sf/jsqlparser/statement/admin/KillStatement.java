/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.admin;

import java.util.function.Consumer;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

/**
 * MySQL {@code KILL [CONNECTION | QUERY] processlist_id}, which ends a connection or the statement
 * it runs. MySQL takes any expression for the identifier, such as {@code @id} or {@code 1 + 2}.
 *
 * @see <a href="https://dev.mysql.com/doc/refman/8.4/en/kill.html">MySQL KILL</a>
 */
public class KillStatement extends ASTNodeAccessImpl implements Statement {
    private Kind kind;
    private Expression processlistId;

    /** What KILL ends. Without either keyword, KILL ends the connection. */
    public enum Kind {
        CONNECTION, QUERY
    }

    public KillStatement() {
        // empty constructor
    }

    public KillStatement(Kind kind, Expression processlistId) {
        this.kind = kind;
        this.processlistId = processlistId;
    }

    /** Returns {@code CONNECTION} or {@code QUERY}, or null when neither keyword was given. */
    public Kind getKind() {
        return kind;
    }

    public void setKind(Kind kind) {
        this.kind = kind;
    }

    public KillStatement withKind(Kind kind) {
        setKind(kind);
        return this;
    }

    public Expression getProcesslistId() {
        return processlistId;
    }

    public void setProcesslistId(Expression processlistId) {
        this.processlistId = processlistId;
    }

    public KillStatement withProcesslistId(Expression processlistId) {
        setProcesslistId(processlistId);
        return this;
    }

    /** Shares the statement syntax with deparsers, which render the identifier expression. */
    public StringBuilder appendTo(StringBuilder builder, Consumer<Expression> expressionRenderer) {
        builder.append("KILL ");
        if (kind != null) {
            builder.append(kind).append(' ');
        }
        expressionRenderer.accept(processlistId);
        return builder;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        return appendTo(builder, builder::append).toString();
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }
}
