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

public interface SelectVisitor {

    void visit(ParenthesedSelect parenthesedSelect);

    void visit(PlainSelect plainSelect);

    void visit(SetOperationList setOpList);

    void visit(WithItem withItem);

    void visit(Values aThis);

    void visit(LateralSubSelect lateralSubSelect);

    void visit(TableStatement tableStatement);
}
