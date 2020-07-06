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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.PlainSelect;

public class ForeignKeyIndex extends NamedConstraint {

    private Table table;
    private List<String> referencedColumnNames;
    private String onDeleteReferenceOption;
    private String onUpdateReferenceOption;

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public List<String> getReferencedColumnNames() {
        return referencedColumnNames;
    }

    public void setReferencedColumnNames(List<String> referencedColumnNames) {
        this.referencedColumnNames = referencedColumnNames;
    }

    public String getOnDeleteReferenceOption() {
        return onDeleteReferenceOption;
    }

    public void setOnDeleteReferenceOption(String onDeleteReferenceOption) {
        this.onDeleteReferenceOption = onDeleteReferenceOption;
    }

    public String getOnUpdateReferenceOption() {
        return onUpdateReferenceOption;
    }

    public void setOnUpdateReferenceOption(String onUpdateReferenceOption) {
        this.onUpdateReferenceOption = onUpdateReferenceOption;
    }

    @Override
    public String toString() {
        String referenceOptions = "";
        if (onDeleteReferenceOption != null) {
            referenceOptions += " ON DELETE " + onDeleteReferenceOption;
        }
        if (onUpdateReferenceOption != null) {
            referenceOptions += " ON UPDATE " + onUpdateReferenceOption;
        }
        return super.toString()
                + " REFERENCES " + table + PlainSelect.
                        getStringList(getReferencedColumnNames(), true, true)
                + referenceOptions;
    }

    public ForeignKeyIndex withTable(Table table) {
        this.setTable(table);
        return this;
    }

    public ForeignKeyIndex withReferencedColumnNames(List<String> referencedColumnNames) {
        this.setReferencedColumnNames(referencedColumnNames);
        return this;
    }

    public ForeignKeyIndex withOnDeleteReferenceOption(String onDeleteReferenceOption) {
        this.setOnDeleteReferenceOption(onDeleteReferenceOption);
        return this;
    }

    public ForeignKeyIndex withOnUpdateReferenceOption(String onUpdateReferenceOption) {
        this.setOnUpdateReferenceOption(onUpdateReferenceOption);
        return this;
    }

    public ForeignKeyIndex addReferencedColumnNames(String... referencedColumnNames) {
        List<String> collection = Optional.ofNullable(getReferencedColumnNames()).orElseGet(ArrayList::new);
        Collections.addAll(collection, referencedColumnNames);
        return this.withReferencedColumnNames(collection);
    }

    public ForeignKeyIndex addReferencedColumnNames(Collection<String> referencedColumnNames) {
        List<String> collection = Optional.ofNullable(getReferencedColumnNames()).orElseGet(ArrayList::new);
        collection.addAll(referencedColumnNames);
        return this.withReferencedColumnNames(collection);
    }

    @Override()
    public ForeignKeyIndex withType(String type) {
        return (ForeignKeyIndex) super.withType(type);
    }

    @Override()
    public ForeignKeyIndex withUsing(String using) {
        return (ForeignKeyIndex) super.withUsing(using);
    }

    @Override()
    public ForeignKeyIndex withName(List<String> name) {
        return (ForeignKeyIndex) super.withName(name);
    }

}
