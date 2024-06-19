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

public interface FromItemVisitor<T> {

    T visit(Table tableName);

    T visit(ParenthesedSelect selectBody);

    T visit(LateralSubSelect lateralSubSelect);

    T visit(TableFunction tableFunction);

    T visit(ParenthesedFromItem aThis);

    T visit(Values values);
}
