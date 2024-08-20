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

import net.sf.jsqlparser.expression.Alias;

public interface ParenthesedStatement extends Statement {

    <T, S> T accept(StatementVisitor<T> statementVisitor, S context);

    default void accept(StatementVisitor<?> statementVisitor) {
        this.accept(statementVisitor, null);
    }

    Alias getAlias();

    void setAlias(Alias alias);

    default ParenthesedStatement withAlias(Alias alias) {
        setAlias(alias);
        return this;
    }

}
