/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.sf.jsqlparser.statement.create.table.ColDataType;

public class Alias implements Serializable {

    private String name;
    private boolean useAs = true;
    private List<AliasColumn> aliasColumns;

    public Alias(String name) {
        this.name = name;
    }

    public Alias(String name, boolean useAs) {
        this.name = name;
        this.useAs = useAs;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isUseAs() {
        return useAs;
    }

    public void setUseAs(boolean useAs) {
        this.useAs = useAs;
    }

    public List<AliasColumn> getAliasColumns() {
        return aliasColumns;
    }

    public void setAliasColumns(List<AliasColumn> aliasColumns) {
        this.aliasColumns = aliasColumns;
    }

    @Override
    public String toString() {
        String alias = (useAs ? " AS " : " ") + name;

        if (aliasColumns != null && !aliasColumns.isEmpty()) {
            String ac = "";
            for (AliasColumn col : aliasColumns) {
                if (ac.length() > 0) {
                    ac += ", ";
                }
                ac += col.name;
                if (col.colDataType != null) {
                    ac += " " + col.colDataType.toString();
                }
            }
            alias += "(" + ac + ")";
        }

        return alias;
    }

    public Alias withName(String name) {
        this.setName(name);
        return this;
    }

    public Alias withUseAs(boolean useAs) {
        this.setUseAs(useAs);
        return this;
    }

    public Alias withAliasColumns(List<AliasColumn> aliasColumns) {
        this.setAliasColumns(aliasColumns);
        return this;
    }

    public Alias addAliasColumns(AliasColumn... aliasColumns) {
        List<AliasColumn> collection = Optional.ofNullable(getAliasColumns()).orElseGet(ArrayList::new);
        Collections.addAll(collection, aliasColumns);
        return this.withAliasColumns(collection);
    }

    public Alias addAliasColumns(Collection<? extends AliasColumn> aliasColumns) {
        List<AliasColumn> collection = Optional.ofNullable(getAliasColumns()).orElseGet(ArrayList::new);
        collection.addAll(aliasColumns);
        return this.withAliasColumns(collection);
    }

    public static class AliasColumn implements Serializable {

        public final String name;
        public final ColDataType colDataType;

        public AliasColumn(String name, ColDataType colDataType) {
            Objects.requireNonNull(name);
            this.name = name;
            this.colDataType = colDataType;
        }

        public AliasColumn(String name) {
            this(name, null);
        }
    }
}
