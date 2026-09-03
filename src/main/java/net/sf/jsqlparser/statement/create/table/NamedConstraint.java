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

    private String indexName;
    private boolean useConstraintKeyword;

    /**
     * Returns the optional index name declared after the constraint type. This is distinct from
     * {@link #getName()}, which represents the optional constraint symbol.
     *
     * @return the index name, or {@code null} when it was omitted
     */
    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public boolean isUseConstraintKeyword() {
        return useConstraintKeyword;
    }

    public void setUseConstraintKeyword(boolean useConstraintKeyword) {
        this.useConstraintKeyword = useConstraintKeyword;
    }

    @Override
    public String toString() {
        String idxSpecText = PlainSelect.getStringList(getIndexSpec(), false, false);
        String head = useConstraintKeyword || getName() != null
                ? "CONSTRAINT" + (getName() != null ? " " + getName() : "") + " "
                : "";
        String tail = getType()
                + (indexName != null ? " " + indexName : "")
                + (getUsing() != null ? " USING " + getUsing() : "")
                + " " + PlainSelect.getStringList(getColumnsNames(), true, true) +
                (!"".equals(idxSpecText) ? " " + idxSpecText : "");
        return head + tail;
    }

    public NamedConstraint withIndexName(String indexName) {
        setIndexName(indexName);
        return this;
    }

    public NamedConstraint withUseConstraintKeyword(boolean useConstraintKeyword) {
        setUseConstraintKeyword(useConstraintKeyword);
        return this;
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

    @Override
    public NamedConstraint withIndexKeyword(String indexKeyword) {
        return (NamedConstraint) super.withIndexKeyword(indexKeyword);
    }
}
