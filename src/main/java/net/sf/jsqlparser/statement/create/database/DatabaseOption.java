/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.database;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** A structured option shared by MySQL {@code CREATE DATABASE} and {@code CREATE SCHEMA}. */
public class DatabaseOption implements Serializable {

    public enum Kind {
        CHARACTER_SET, COLLATE, ENCRYPTION, OTHER
    }

    private Kind kind = Kind.OTHER;
    private String name;
    private String value;
    private boolean useDefault;
    private boolean useEquals;
    private List<String> rawTokens;

    public DatabaseOption() {}

    public DatabaseOption(Kind kind, String name, String value, boolean useDefault,
            boolean useEquals) {
        this.kind = kind;
        this.name = name;
        this.value = value;
        this.useDefault = useDefault;
        this.useEquals = useEquals;
    }

    public static DatabaseOption raw(List<String> tokens) {
        DatabaseOption option = new DatabaseOption();
        option.setRawTokens(tokens);
        return option;
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

    public boolean isUseDefault() {
        return useDefault;
    }

    public void setUseDefault(boolean useDefault) {
        this.useDefault = useDefault;
    }

    public boolean isUseEquals() {
        return useEquals;
    }

    public void setUseEquals(boolean useEquals) {
        this.useEquals = useEquals;
    }

    public List<String> getRawTokens() {
        return rawTokens;
    }

    public void setRawTokens(List<String> rawTokens) {
        this.rawTokens = rawTokens;
    }

    /** Returns this option in the legacy flat-token representation. */
    public List<String> getTokens() {
        if (rawTokens != null) {
            return Collections.unmodifiableList(rawTokens);
        }
        List<String> tokens = new ArrayList<>();
        if (useDefault) {
            tokens.add("DEFAULT");
        }
        Collections.addAll(tokens, name.split(" "));
        if (useEquals) {
            tokens.add("=");
        }
        tokens.add(value);
        return tokens;
    }

    @Override
    public String toString() {
        return String.join(" ", getTokens());
    }

    public DatabaseOption withKind(Kind kind) {
        setKind(kind);
        return this;
    }

    public DatabaseOption withName(String name) {
        setName(name);
        return this;
    }

    public DatabaseOption withValue(String value) {
        setValue(value);
        return this;
    }

    public DatabaseOption withUseDefault(boolean useDefault) {
        setUseDefault(useDefault);
        return this;
    }

    public DatabaseOption withUseEquals(boolean useEquals) {
        setUseEquals(useEquals);
        return this;
    }
}
