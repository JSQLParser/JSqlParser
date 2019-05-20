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

public class Index {

    private String type;
    private String using;
    private List<String> columnsNames;
    private String name;
    private List<String> idxSpec;

    public List<String> getColumnsNames() {
        return columnsNames;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    /**
     * In postgresql, the index type (Btree, GIST, etc.) is indicated
     * with a USING clause.
     * Please note that:
     *  Oracle - the type might be BITMAP, indicating a bitmap kind of index
     *  MySQL - the type might be FULLTEXT or SPATIAL
    */
    public String getUsing() {
        return using;
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

    public void setUsing(String string) {
        using = string;
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
