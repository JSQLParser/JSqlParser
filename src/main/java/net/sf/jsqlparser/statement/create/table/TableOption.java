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

/** A structured option following a {@code CREATE TABLE} definition. */
public class TableOption implements Serializable {

    public enum Kind {
        ENGINE, CHARACTER_SET, COLLATE, COMMENT, AUTO_INCREMENT, OTHER
    }

    private Kind kind = Kind.OTHER;
    private String name;
    private String value;
    private boolean useEquals;
    private List<String> tokens;

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
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
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
        if (value != null) {
            result.add(value);
        }
        return Collections.unmodifiableList(result);
    }

    public void setTokens(List<String> tokens) {
        this.tokens = tokens;
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
        return name + (value != null ? (useEquals ? " = " : " ") + value : "");
    }
}
