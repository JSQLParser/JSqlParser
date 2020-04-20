/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.table;

import net.sf.jsqlparser.statement.select.PlainSelect;

public class NamedConstraint extends Index {

    @Override
    public String toString() {
        String idxSpecText = PlainSelect.getStringList(getIndexSpec(), false, false);
        return (getName() != null ? "CONSTRAINT " + getName() + " " : "") + getType() + " "
                + PlainSelect.getStringList(getColumnsNames(), true, true)
                + (!"".equals(idxSpecText) ? " " + idxSpecText : "");
    }
}
