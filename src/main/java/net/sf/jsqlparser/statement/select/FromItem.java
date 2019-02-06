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

import net.sf.jsqlparser.expression.Alias;

public interface FromItem {

    void accept(FromItemVisitor fromItemVisitor);

    Alias getAlias();

    void setAlias(Alias alias);

    Pivot getPivot();

    void setPivot(Pivot pivot);

}
