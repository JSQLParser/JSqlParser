/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

import java.util.List;

public class Distinct {

    private List<SelectItem> onSelectItems;
    private boolean useUnique = false;

    public Distinct() {
    }

    public Distinct(boolean useUnique) {
        this.useUnique = useUnique;
    }

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
