/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.accessmethod;

import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

/** A PostgreSQL table or index access method, whose handler names a function, not a table. */
public class CreateAccessMethod implements Statement {
    public enum Type {
        TABLE, INDEX
    }

    private String name;
    private Type type;
    private String handler;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getHandler() {
        return handler;
    }

    public void setHandler(String handler) {
        this.handler = handler;
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> visitor, S context) {
        return visitor.visit(this, context);
    }

    public StringBuilder appendTo(StringBuilder builder) {
        return builder.append("CREATE ACCESS METHOD ").append(name)
                .append(" TYPE ").append(type).append(" HANDLER ").append(handler);
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }
}
