/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Statements extends ArrayList<Statement> implements Serializable {

    @Deprecated
    public List<Statement> getStatements() {
        return this;
    }

    @Deprecated
    public void setStatements(List<Statement> statements) {
        this.clear();
        this.addAll(statements);
    }

    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }

    public <E extends Statement> E get(Class<E> type, int index) {
        return type.cast(get(index));
    }

    /**
     * Appends each statement using the supplied renderer and the list's separators. Blocks and
     * IF/ELSE statements retain their own semicolon settings. Null entries produced by error
     * recovery retain the legacy "null;" placeholder without invoking the renderer.
     */
    public StringBuilder appendTo(StringBuilder builder, Consumer<Statement> statementPrinter) {
        for (Statement stmt : this) {
            if (stmt == null) {
                builder.append("null");
            } else {
                statementPrinter.accept(stmt);
            }
            // IfElseStatements and Blocks control the Semicolons by themselves
            if (!(stmt instanceof IfElseStatement || stmt instanceof Block)) {
                builder.append(';');
            }
            builder.append('\n');
        }
        return builder;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        return appendTo(builder, builder::append).toString();
    }
}
