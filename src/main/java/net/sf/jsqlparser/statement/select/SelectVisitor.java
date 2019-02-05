/*
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2013 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

import net.sf.jsqlparser.statement.values.ValuesStatement;

public interface SelectVisitor {

    void visit(PlainSelect plainSelect);

    void visit(SetOperationList setOpList);

    void visit(WithItem withItem);

    void visit(ValuesStatement aThis);
}
