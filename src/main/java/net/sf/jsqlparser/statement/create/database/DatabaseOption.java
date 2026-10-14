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

/**
 * A structured option shared by MySQL {@code CREATE/ALTER DATABASE} and
 * {@code CREATE/ALTER SCHEMA}, and by PostgreSQL {@code CREATE/ALTER DATABASE}. The name keeps the
 * written spelling, for example {@code CHARSET} for {@link Kind#CHARACTER_SET}. Values keep their
 * SQL text, including string quotes and signs. Unrecognized option text is an {@link Kind#OTHER}
 * option with raw tokens.
 */
public class DatabaseOption implements Serializable {

    /**
     * The option. MySQL uses {@code CHARACTER_SET} to {@code READ_ONLY}, PostgreSQL uses
     * {@code OWNER} to {@code OID}, and {@code OTHER} holds raw tokens.
     */
    public enum Kind {
        CHARACTER_SET, COLLATE, ENCRYPTION, READ_ONLY, OWNER, TEMPLATE, ENCODING, STRATEGY, LOCALE, LC_COLLATE, LC_CTYPE, BUILTIN_LOCALE, ICU_LOCALE, ICU_RULES, LOCALE_PROVIDER, COLLATION_VERSION, TABLESPACE, ALLOW_CONNECTIONS, CONNECTION_LIMIT, IS_TEMPLATE, OID, OTHER
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
