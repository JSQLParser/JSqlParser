/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2023 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.insert;

import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.statement.ParenthesedStatement;
import net.sf.jsqlparser.statement.StatementVisitor;

public class ParenthesedInsert extends Insert implements ParenthesedStatement {
    Alias alias;
    Insert insert;

    public ParenthesedInsert() {}

    @Override
    public Alias getAlias() {
        return alias;
    }

    @Override
    public void setAlias(Alias alias) {
        this.alias = alias;
    }

    public ParenthesedInsert withAlias(Alias alias) {
        this.setAlias(alias);
        return this;
    }

    public Insert getInsert() {
        return insert;
    }

    public void setInsert(Insert insert) {
        this.insert = insert;
    }

    public ParenthesedInsert withInsert(Insert insert) {
        setInsert(insert);
        return this;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("(").append(insert).append(")");
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
