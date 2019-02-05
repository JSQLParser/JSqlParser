/*
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2013 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.table;

import java.util.List;

import net.sf.jsqlparser.statement.select.PlainSelect;

/**
 * An index (unique, primary etc.) in a CREATE TABLE statement
 */
public class Index {

    private String type;
    private List<String> columnsNames;
    private String name;
    private List<String> idxSpec;

    /**
     * A list of strings of all the columns regarding this index
     */
    public List<String> getColumnsNames() {
        return columnsNames;
    }

    public String getName() {
        return name;
    }

    /**
     * The type of this index: "PRIMARY KEY", "UNIQUE", "INDEX"
     */
    public String getType() {
        return type;
    }

    public void setColumnsNames(List<String> list) {
        columnsNames = list;
    }

    public void setName(String string) {
        name = string;
    }

    public void setType(String string) {
        type = string;
    }

    public List<String> getIndexSpec() {
        return idxSpec;
    }

    public void setIndexSpec(List<String> idxSpec) {
        this.idxSpec = idxSpec;
    }

    @Override
    public String toString() {
        String idxSpecText = PlainSelect.getStringList(idxSpec, false, false);
        return type + (name != null ? " " + name : "") + " " + PlainSelect.
                getStringList(columnsNames, true, true) + (!"".equals(idxSpecText) ? " " + idxSpecText : "");
    }
}
