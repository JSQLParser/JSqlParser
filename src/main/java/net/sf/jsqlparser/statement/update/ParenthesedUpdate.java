/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2023 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.update;

import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.statement.ParenthesedStatement;
import net.sf.jsqlparser.statement.StatementVisitor;

public class ParenthesedUpdate extends Update implements ParenthesedStatement {

    Alias alias;
    Update update;

    public ParenthesedUpdate() {}

    @Override
    public Alias getAlias() {
        return alias;
    }

    @Override
    public void setAlias(Alias alias) {
        this.alias = alias;
    }

    public ParenthesedUpdate withAlias(Alias alias) {
        this.setAlias(alias);
        return this;
    }

    public Update getUpdate() {
        return update;
    }

    public void setUpdate(Update update) {
        this.update = update;
    }

    public ParenthesedUpdate withUpdate(Update update) {
        setUpdate(update);
        return this;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("(").append(update).append(")");
        if (alias != null) {
            builder.append(alias);
        }
        return builder.toString();
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }
}
