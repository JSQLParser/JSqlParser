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

import net.sf.jsqlparser.statement.values.ValuesStatement;

public interface SelectVisitor {

    default void visit(PlainSelect plainSelect) { // default implementation ignored
    }

    default void visit(SetOperationList setOpList) { // default implementation ignored
    }

    default void visit(WithItem withItem) { // default implementation ignored
    }

    default void visit(ValuesStatement aThis) { // default implementation ignored
    }
}
