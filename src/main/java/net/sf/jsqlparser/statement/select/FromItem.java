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

import net.sf.jsqlparser.expression.Alias;

/**
 * An item in a "SELECT [...] FROM item1" statement. (for example a table or a sub-select)
 */
public interface FromItem {

    void accept(FromItemVisitor fromItemVisitor);

    Alias getAlias();

    void setAlias(Alias alias);

    Pivot getPivot();

    void setPivot(Pivot pivot);

}
