/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.alter;

import java.util.function.Consumer;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

/**
 * PostgreSQL ALTER LARGE OBJECT oid OWNER TO role. The numeric OID retains its sign and spelling.
 */
public class AlterLargeObject extends ASTNodeAccessImpl implements Statement {
    private Expression largeObjectId;
    private String owner;

    public Expression getLargeObjectId() {
        return largeObjectId;
    }

    public void setLargeObjectId(Expression largeObjectId) {
        this.largeObjectId = largeObjectId;
    }

    public AlterLargeObject withLargeObjectId(Expression largeObjectId) {
        setLargeObjectId(largeObjectId);
        return this;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public AlterLargeObject withOwner(String owner) {
        setOwner(owner);
        return this;
    }

    public void visitExpressions(Consumer<Expression> visitor) {
        if (largeObjectId != null) {
            visitor.accept(largeObjectId);
        }
    }

    public StringBuilder appendTo(StringBuilder sql, Consumer<Expression> expressionRenderer) {
        sql.append("ALTER LARGE OBJECT ");
        expressionRenderer.accept(largeObjectId);
        return sql.append(" OWNER TO ").append(owner);
    }

    public StringBuilder appendTo(StringBuilder sql) {
        return appendTo(sql, expression -> sql.append(expression));
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> visitor, S context) {
        return visitor.visit(this, context);
    }
}
