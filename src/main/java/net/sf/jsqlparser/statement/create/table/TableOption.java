/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.table;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.schema.Table;

/** A structured option following a {@code CREATE TABLE} definition. */
public class TableOption implements Serializable {

    public enum Kind {
        ENGINE, CHARACTER_SET, COLLATE, COMMENT, AUTO_INCREMENT, STATS_AUTO_RECALC, STATS_PERSISTENT, STATS_SAMPLE_PAGES, UNION, ENCRYPTION, PASSWORD, DATA_DIRECTORY, INDEX_DIRECTORY, OTHER
    }

    private Kind kind = Kind.OTHER;
    private String name;
    private String value;
    private boolean useEquals;
    private List<String> tokens;
    private List<Table> unionTables;

    public TableOption() {}

    public TableOption(Kind kind, String name, String value, boolean useEquals) {
        this.kind = kind;
        this.name = name;
        this.value = value;
        this.useEquals = useEquals;
    }

    public static TableOption raw(List<String> tokens) {
        TableOption option = new TableOption();
        option.setTokens(tokens);
        return option;
    }

    public static TableOption raw(String... tokens) {
        return raw(Arrays.asList(tokens));
    }

    public Kind getKind() {
        return kind;
    }

    public void setKind(Kind kind) {
        this.kind = kind;
        if (kind != Kind.UNION) {
            unionTables = null;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return unionTables == null ? value : PlainSelect.getStringList(unionTables, true, true);
    }

    public void setValue(String value) {
        this.value = value;
        unionTables = null;
    }

    /** Returns the mutable MERGE table sources, including an empty UNION list. */
    public List<Table> getUnionTables() {
        return unionTables;
    }

    /** Replaces raw option contents with structured UNION table references. */
    public void setUnionTables(List<Table> unionTables) {
        this.unionTables = unionTables;
        kind = Kind.UNION;
        name = "UNION";
        value = null;
        tokens = null;
    }

    public TableOption withUnionTables(List<Table> unionTables) {
        setUnionTables(unionTables);
        return this;
    }

    public boolean isUseEquals() {
        return useEquals;
    }

    public void setUseEquals(boolean useEquals) {
        this.useEquals = useEquals;
    }

    /** Returns the original token groups used by the legacy table-options API. */
    public List<String> getTokens() {
        if (tokens != null) {
            return tokens;
        }
        List<String> result = new ArrayList<>();
        if (name != null) {
            Collections.addAll(result, name.trim().split("\\s+"));
        }
        if (useEquals) {
            result.add("=");
        }
        String renderedValue = getValue();
        if (renderedValue != null) {
            result.add(renderedValue);
        }
        return Collections.unmodifiableList(result);
    }

    public void setTokens(List<String> tokens) {
        this.tokens = tokens;
        if (tokens != null) {
            unionTables = null;
        }
    }

    public TableOption withKind(Kind kind) {
        setKind(kind);
        return this;
    }

    public TableOption withName(String name) {
        setName(name);
        return this;
    }

    public TableOption withValue(String value) {
        setValue(value);
        return this;
    }

    public TableOption withUseEquals(boolean useEquals) {
        setUseEquals(useEquals);
        return this;
    }

    @Override
    public String toString() {
        if (tokens != null) {
            return PlainSelect.getStringList(tokens, false, false);
        }
        String renderedValue = getValue();
        return name + (renderedValue != null ? (useEquals ? " = " : " ") + renderedValue : "");
    }
}
