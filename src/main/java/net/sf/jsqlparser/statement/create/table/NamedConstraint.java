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

import java.util.Collection;
import java.util.List;
import net.sf.jsqlparser.statement.select.PlainSelect;

public class NamedConstraint extends Index {

    @Override
    public String toString() {
        String idxSpecText = PlainSelect.getStringList(getIndexSpec(), false, false);
        String head = getName() != null ? "CONSTRAINT " + getName() + " " : "";
        String tail = getType() + " " + PlainSelect.getStringList(getColumnsNames(), true, true) +
                (!"".equals(idxSpecText) ? " " + idxSpecText : "");
        return head + tail;
    }

    @Override
    public NamedConstraint withName(List<String> name) {
        return (NamedConstraint) super.withName(name);
    }

    @Override
    public NamedConstraint withName(String name) {
        return (NamedConstraint) super.withName(name);
    }

    @Override
    public NamedConstraint withType(String type) {
        return (NamedConstraint) super.withType(type);
    }

    @Override
    public NamedConstraint withUsing(String using) {
        return (NamedConstraint) super.withUsing(using);
    }

    @Override
    public NamedConstraint withColumnsNames(List<String> list) {
        return (NamedConstraint) super.withColumnsNames(list);
    }

    @Override
    public NamedConstraint withColumns(List<ColumnParams> columns) {
        return (NamedConstraint) super.withColumns(columns);
    }

    @Override
    public NamedConstraint addColumns(ColumnParams... functionDeclarationParts) {
        return (NamedConstraint) super.addColumns(functionDeclarationParts);
    }

    @Override
    public NamedConstraint addColumns(Collection<? extends ColumnParams> functionDeclarationParts) {
        return (NamedConstraint) super.addColumns(functionDeclarationParts);
    }

    @Override
    public NamedConstraint withIndexSpec(List<String> idxSpec) {
        return (NamedConstraint) super.withIndexSpec(idxSpec);
    }

}
