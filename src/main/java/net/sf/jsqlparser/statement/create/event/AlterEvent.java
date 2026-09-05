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

import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.StatementVisitor;

/** MySQL {@code ALTER EVENT} statement. */
public class AlterEvent extends EventStatement {

    private Table renameTo;

    public Table getRenameTo() {
        return renameTo;
    }

    public void setRenameTo(Table renameTo) {
        this.renameTo = renameTo;
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("ALTER EVENT ").append(getEvent());
        appendScheduleAndCompletion(builder);
        if (renameTo != null) {
            builder.append(" RENAME TO ").append(renameTo);
        }
        appendStatusAndComment(builder);
        if (getBody() != null) {
            builder.append(" DO ").append(getBody());
        }
        return builder.toString();
    }

    public AlterEvent withRenameTo(Table renameTo) {
        setRenameTo(renameTo);
        return this;
    }
}
