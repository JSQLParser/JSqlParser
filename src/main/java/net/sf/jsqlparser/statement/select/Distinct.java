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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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

    public Distinct withOnSelectItems(List<SelectItem> onSelectItems) {
        this.setOnSelectItems(onSelectItems);
        return this;
    }

    public Distinct withUseUnique(boolean useUnique) {
        this.setUseUnique(useUnique);
        return this;
    }

    public Distinct addOnSelectItems(SelectItem... onSelectItems) {
        List<SelectItem> collection = Optional.ofNullable(getOnSelectItems()).orElseGet(ArrayList::new);
        Collections.addAll(collection, onSelectItems);
        return this.withOnSelectItems(collection);
    }

    public Distinct addOnSelectItems(Collection<? extends SelectItem> onSelectItems) {
        List<SelectItem> collection = Optional.ofNullable(getOnSelectItems()).orElseGet(ArrayList::new);
        collection.addAll(onSelectItems);
        return this.withOnSelectItems(collection);
    }
}
