/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

import net.sf.jsqlparser.schema.Table;

public interface FromItemVisitor {

    default void visit(Table tableName) { // default implementation ignored
    }

    default void visit(SubSelect subSelect) { // default implementation ignored
    }

    default void visit(SubJoin subjoin) { // default implementation ignored
    }

    default void visit(LateralSubSelect lateralSubSelect) { // default implementation ignored
    }

    default void visit(ValuesList valuesList) { // default implementation ignored
    }

    default void visit(TableFunction tableFunction) { // default implementation ignored
    }

    default void visit(ParenthesisFromItem aThis) { // default implementation ignored
    }
}
