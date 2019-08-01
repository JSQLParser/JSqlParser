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

public interface PivotVisitor {

    default void visit(Pivot pivot) { // default implementation ignored
    }

    default void visit(PivotXml pivot) { // default implementation ignored
    }

}
