/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.event;

import net.sf.jsqlparser.statement.StatementVisitor;

/** MySQL {@code CREATE EVENT} statement. */
public class CreateEvent extends EventStatement {

    private boolean ifNotExists;

    public boolean isIfNotExists() {
        return ifNotExists;
    }

    public void setIfNotExists(boolean ifNotExists) {
        this.ifNotExists = ifNotExists;
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("CREATE EVENT ");
        if (ifNotExists) {
            builder.append("IF NOT EXISTS ");
        }
        builder.append(getEvent());
        appendScheduleAndCompletion(builder);
        appendStatusAndComment(builder);
        builder.append(" DO ").append(getBody());
        return builder.toString();
    }

    public CreateEvent withIfNotExists(boolean ifNotExists) {
        setIfNotExists(ifNotExists);
        return this;
    }
}
