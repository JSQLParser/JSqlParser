/*
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2013 JSQLParser
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

import lombok.Data;
import net.sf.jsqlparser.statement.select.PlainSelect;

/**
 * An index (unique, primary etc.) in a CREATE TABLE statement
 */
@Data
public class Index {

    /**
     * The type of this index: "PRIMARY KEY", "UNIQUE", "INDEX"
     */
    private String type;
    /**
     * A list of strings of all the columns regarding this index
     */
    private List<String> columnsNames;
    private String name;
    private List<String> indexSpec;

    @Override
    public String toString() {
        String idxSpecText = PlainSelect.getStringList(indexSpec, false, false);
        return type + (name != null ? " " + name : "") + " " + PlainSelect.getStringList(columnsNames, true, true) + (!"".equals(idxSpecText) ? " " + idxSpecText : "");
    }
}
