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

import net.sf.jsqlparser.Model;

public interface Statement extends Model {
    <T, S> T accept(StatementVisitor<T> statementVisitor, S context);

    default void accept(StatementVisitor<?> statementVisitor) {
        accept(statementVisitor, null);
    }
}
