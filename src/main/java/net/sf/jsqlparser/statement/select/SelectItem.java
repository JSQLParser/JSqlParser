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

import net.sf.jsqlparser.parser.ASTNodeAccess;

/**
 * Anything between "SELECT" and "FROM"<BR>
 * (that is, any column or expression etc to be retrieved with the query)
 */
public interface SelectItem extends ASTNodeAccess {

    void accept(SelectItemVisitor selectItemVisitor);
}
