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

public interface SelectVisitor<T> {

    T visit(ParenthesedSelect parenthesedSelect);

    T visit(PlainSelect plainSelect);

    T visit(SetOperationList setOpList);

    T visit(WithItem withItem);

    T visit(Values aThis);

    T visit(LateralSubSelect lateralSubSelect);

    T visit(TableStatement tableStatement);
}
