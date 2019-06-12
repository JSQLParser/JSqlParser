/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
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
    public void setUsing(String string) {
        using = string;
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

    public String getUsing() {
        return using;
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
