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
package net.sf.jsqlparser.statement.select;

import java.util.List;

import lombok.Data;

/**
 * One of the parts of a "WITH" clause of a "SELECT" statement
 */
@Data
public class WithItem implements SelectBody {

    /**
     * The name of this WITH item (for example, "myWITH" in "WITH myWITH AS (SELECT A,B,C))"
     */
    private String name;
    /**
     * The {@link SelectItem}s in this WITH (for example the A,B,C in "WITH mywith (A,B,C) AS ...")
     */
    private List<SelectItem> withItemList;
    /**
     * The {@link SelectBody} of this WITH item is the part after the "AS" keyword
     */
    private SelectBody selectBody;
    private boolean recursive;

    @Override
    public String toString() {
        return (recursive ? "RECURSIVE " : "") + name + ((withItemList != null) ? " " + PlainSelect.getStringList(withItemList, true, true) : "")
            + " AS (" + selectBody + ")";
    }

    @Override
    public void accept(SelectVisitor visitor) {
        visitor.visit(this);
    }
}
