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

import java.util.List;
import net.sf.jsqlparser.statement.select.PlainSelect;

public class NamedConstraint extends Index {

    @Override
    public String toString() {
        String idxSpecText = PlainSelect.getStringList(getIndexSpec(), false, false);
        return (getName() != null ? "CONSTRAINT " + getName() + " " : "")
                + getType() + " " + PlainSelect.getStringList(getColumnsNames(), true, true) + (!"".
                equals(idxSpecText) ? " " + idxSpecText : "");
    }

    @Override()
    public NamedConstraint name(List<String> name) {
        return (NamedConstraint) super.name(name);
    }

    @Override()
    public NamedConstraint type(String type) {
        return (NamedConstraint) super.type(type);
    }

    @Override()
    public NamedConstraint using(String using) {
        return (NamedConstraint) super.using(using);
    }
}
