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

/**
 * One of the parts of a "WITH" clause of a "SELECT" statement
 */
public class WithItem implements SelectBody {

    private String name;
    private List<SelectItem> withItemList;
    private SelectBody selectBody;
    private boolean recursive;

    /**
     * The name of this WITH item (for example, "myWITH" in "WITH myWITH AS
     * (SELECT A,B,C))"
     *
     * @return the name of this WITH
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isRecursive() {
        return recursive;
    }

    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }

    /**
     * The {@link SelectBody} of this WITH item is the part after the "AS"
     * keyword
     *
     * @return {@link SelectBody} of this WITH item
     */
    public SelectBody getSelectBody() {
        return selectBody;
    }

    public void setSelectBody(SelectBody selectBody) {
        this.selectBody = selectBody;
    }

    /**
     * The {@link SelectItem}s in this WITH (for example the A,B,C in "WITH
     * mywith (A,B,C) AS ...")
     *
     * @return a list of {@link SelectItem}s
     */
    public List<SelectItem> getWithItemList() {
        return withItemList;
    }

    public void setWithItemList(List<SelectItem> withItemList) {
        this.withItemList = withItemList;
    }

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
