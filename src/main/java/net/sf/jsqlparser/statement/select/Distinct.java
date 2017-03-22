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
 * A DISTINCT [ON (expression, ...)] clause
 */
public class Distinct {

    private List<SelectItem> onSelectItems;
    private boolean useUnique = false;

    public Distinct() {
    }

    public Distinct(boolean useUnique) {
        this.useUnique = useUnique;
    }

    /**
     * A list of {@link SelectItem}s expressions, as in "select DISTINCT ON (a,b,c) a,b FROM..."
     *
     * @return a list of {@link SelectItem}s expressions
     */
    public List<SelectItem> getOnSelectItems() {
        return onSelectItems;
    }

    public void setOnSelectItems(List<SelectItem> list) {
        onSelectItems = list;
    }

    public boolean isUseUnique() {
        return useUnique;
    }

    public void setUseUnique(boolean useUnique) {
        this.useUnique = useUnique;
    }

    @Override
    public String toString() {
        String sql = useUnique ? "UNIQUE" : "DISTINCT";

        if (onSelectItems != null && !onSelectItems.isEmpty()) {
            sql += " ON (" + PlainSelect.getStringList(onSelectItems) + ")";
        }

        return sql;
    }
}
