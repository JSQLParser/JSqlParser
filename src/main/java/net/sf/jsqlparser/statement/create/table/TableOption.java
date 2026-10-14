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
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.schema.Table;

/**
 * A shared CREATE/ALTER TABLE option with either opaque tokens or structured properties. Setting
 * non-null tokens discards all structured properties. A structured setter discards the tokens;
 * their contents are never parsed or restored by a later setter.
 */
public class TableOption implements Serializable {

    private ForeignTableOptions foreignTableOptions;
    private Kind kind = Kind.OTHER;
    private String name;
    private String value;
    private boolean useEquals;
    private List<String> tokens;
    private List<Table> unionTables;
    private List<IndexOption> storageParameters;
    private ColumnOption.Storage tablespaceStorage;
    private OnCommitAction onCommitAction;
    private StringValue comment;

    public enum Kind {
        ENGINE, CHARACTER_SET, COLLATE, COMMENT, AUTO_INCREMENT, STATS_AUTO_RECALC, STATS_PERSISTENT, STATS_SAMPLE_PAGES, UNION, ENCRYPTION, PASSWORD, DATA_DIRECTORY, INDEX_DIRECTORY, SECONDARY_ENGINE, AUTOEXTEND_SIZE, INSERT_METHOD, PACK_KEYS, DELAY_KEY_WRITE, CHECKSUM, CONNECTION, COMPRESSION, STORAGE_PARAMETERS, WITHOUT_OIDS, ENGINE_ATTRIBUTE, SECONDARY_ENGINE_ATTRIBUTE, ROW_FORMAT, AVG_ROW_LENGTH, MAX_ROWS, MIN_ROWS, KEY_BLOCK_SIZE, TABLESPACE, FOREIGN_SERVER, ACCESS_METHOD, ON_COMMIT, OTHER
    }

    /**
     * The action of a temporary table's {@code ON COMMIT} clause. The DEFINITION actions belong to
     * Oracle private temporary tables.
     */
    public enum OnCommitAction {
        PRESERVE_ROWS, DELETE_ROWS, DROP, PRESERVE_DEFINITION, DROP_DEFINITION;

        /** Returns the SQL keywords, for example {@code DELETE ROWS}. */
        @Override
        public String toString() {
            return name().replace('_', ' ');
        }
    }

    public ForeignTableOptions getForeignTableOptions() {
        return foreignTableOptions;
    }

    /** Replaces the current contents with a structured PostgreSQL SERVER clause. */
    public void setForeignTableOptions(ForeignTableOptions options) {
        clearContents();
        kind = Kind.FOREIGN_SERVER;
        name = "SERVER";
        foreignTableOptions = options;
    }

    public static TableOption foreignServer(ForeignTableOptions options) {
        TableOption option = new TableOption();
        option.setForeignTableOptions(options);
        return option;
    }

    public ColumnOption.Storage getTablespaceStorage() {
        return tablespaceStorage;
    }

    /**
     * Sets the TABLESPACE storage modifier. A non-null modifier selects TABLESPACE and discards
     * another option's contents; an existing TABLESPACE keeps its name, value and equals spelling.
     */
    public void setTablespaceStorage(ColumnOption.Storage storage) {
        if (storage != null && storage != ColumnOption.Storage.DISK
                && storage != ColumnOption.Storage.MEMORY) {
            throw new IllegalArgumentException("TABLESPACE storage must be DISK or MEMORY");
        }
        if (storage != null && kind != Kind.TABLESPACE) {
            clearContents();
            kind = Kind.TABLESPACE;
            name = "TABLESPACE";
        }
        tokens = null;
        tablespaceStorage = storage;
    }

    /** Creates a table access method option such as PostgreSQL's {@code USING heap}. */
    public static TableOption accessMethod(String method) {
        return new TableOption(Kind.ACCESS_METHOD, "USING", method, false);
    }

    /** Returns the {@code ON COMMIT} action, or null if this option has none. */
    public OnCommitAction getOnCommitAction() {
        return onCommitAction;
    }

    /** Replaces the current contents with an {@code ON COMMIT} clause. */
    public void setOnCommitAction(OnCommitAction action) {
        clearContents();
        kind = Kind.ON_COMMIT;
        name = "ON COMMIT";
        onCommitAction = action;
    }

    /** Creates an {@code ON COMMIT} option, for example {@code ON COMMIT DROP}. */
    public static TableOption onCommit(OnCommitAction action) {
        TableOption option = new TableOption();
        option.setOnCommitAction(action);
        return option;
    }

    public List<IndexOption> getStorageParameters() {
        return storageParameters;
    }

    public void setStorageParameters(List<IndexOption> storageParameters) {
        clearContents();
        this.storageParameters = storageParameters;
        kind = Kind.STORAGE_PARAMETERS;
        name = "WITH";
    }

    /**
     * Returns the COMMENT string literal, or null for another kind, opaque tokens or a plain value
     * set with {@link #setValue(String)}. {@link StringValue#getNotExcapedValue()} decodes it.
     */
    public StringValue getComment() {
        return comment;
    }

    /**
     * Replaces the current contents with a COMMENT string literal and keeps the equals spelling; an
     * existing COMMENT also keeps its name. A null literal omits the value.
     */
    public void setComment(StringValue comment) {
        String commentName = kind == Kind.COMMENT && name != null ? name : "COMMENT";
        boolean equals = useEquals;
        clearContents();
        kind = Kind.COMMENT;
        name = commentName;
        useEquals = equals;
        this.comment = comment;
    }

    public TableOption withComment(StringValue comment) {
        setComment(comment);
        return this;
    }

    public static TableOption comment(StringValue comment) {
        TableOption option = new TableOption();
        option.setComment(comment);
        return option;
    }

    public void appendTo(StringBuilder builder,
            java.util.function.Consumer<net.sf.jsqlparser.expression.Expression> expressionPrinter) {
        if (foreignTableOptions != null) {
            foreignTableOptions.appendTo(builder, expressionPrinter);
        } else if (storageParameters != null) {
            builder.append(name).append(useEquals ? " = " : " ");
            IndexOption.appendListTo(builder, storageParameters, expressionPrinter);
        } else if (comment != null) {
            builder.append(name).append(useEquals ? " = " : " ");
            expressionPrinter.accept(comment);
        } else {
            builder.append(toString());
        }
    }


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
        tokens = null;
        this.kind = kind;
        if (kind != Kind.TABLESPACE) {
            tablespaceStorage = null;
        }
        if (kind != Kind.FOREIGN_SERVER) {
            foreignTableOptions = null;
        }
        if (kind != Kind.STORAGE_PARAMETERS) {
            storageParameters = null;
        }
        if (kind != Kind.UNION) {
            unionTables = null;
        }
        if (kind != Kind.ON_COMMIT) {
            onCommitAction = null;
        }
        if (kind != Kind.COMMENT && comment != null) {
            // keep the literal's SQL text, as for other scalar values
            value = comment.toString();
            comment = null;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        tokens = null;
        this.name = name;
    }

    public String getValue() {
        if (foreignTableOptions != null) {
            return foreignTableOptions.toString().substring("SERVER ".length());
        }
        if (storageParameters != null) {
            return PlainSelect.getStringList(storageParameters, true, true);
        }
        if (onCommitAction != null) {
            return onCommitAction.toString();
        }
        if (comment != null) {
            return comment.toString();
        }
        return unionTables == null ? value : PlainSelect.getStringList(unionTables, true, true);
    }

    /** Sets a plain value; it replaces a COMMENT literal without being parsed. */
    public void setValue(String value) {
        tokens = null;
        this.value = value;
        foreignTableOptions = null;
        unionTables = null;
        storageParameters = null;
        onCommitAction = null;
        comment = null;
    }

    /** Returns the mutable MERGE table sources, including an empty UNION list. */
    public List<Table> getUnionTables() {
        return unionTables;
    }

    /** Replaces raw option contents with structured UNION table references. */
    public void setUnionTables(List<Table> unionTables) {
        boolean equals = useEquals;
        clearContents();
        this.unionTables = unionTables;
        kind = Kind.UNION;
        name = "UNION";
        useEquals = equals;
    }

    public TableOption withUnionTables(List<Table> unionTables) {
        setUnionTables(unionTables);
        return this;
    }

    public boolean isUseEquals() {
        return useEquals;
    }

    public void setUseEquals(boolean useEquals) {
        tokens = null;
        this.useEquals = useEquals;
    }

    /**
     * Returns the original, mutable token list for an opaque option, or an unmodifiable snapshot of
     * the current structured properties. Editing tokens does not classify or parse them.
     */
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
        if (onCommitAction != null) {
            // one token per keyword, as in the legacy raw options
            Collections.addAll(result, renderedValue.split(" "));
        } else if (renderedValue != null) {
            result.add(renderedValue);
        }
        if (kind == Kind.TABLESPACE && tablespaceStorage != null) {
            result.add("STORAGE");
            result.add(tablespaceStorage.name());
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Replaces all structured properties with opaque tokens and {@link Kind#OTHER}. Null clears
     * opaque tokens without restoring discarded properties; it leaves structured options alone.
     */
    public void setTokens(List<String> tokens) {
        if (tokens != null) {
            clearContents();
        }
        this.tokens = tokens;
    }

    private void clearContents() {
        kind = Kind.OTHER;
        name = null;
        value = null;
        useEquals = false;
        tokens = null;
        tablespaceStorage = null;
        foreignTableOptions = null;
        unionTables = null;
        storageParameters = null;
        onCommitAction = null;
        comment = null;
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
        if (foreignTableOptions != null) {
            return foreignTableOptions.toString();
        }
        if (tokens != null) {
            return PlainSelect.getStringList(tokens, false, false);
        }
        String renderedValue = getValue();
        return name + (renderedValue != null ? (useEquals ? " = " : " ") + renderedValue : "")
                + (kind == Kind.TABLESPACE && tablespaceStorage != null
                        ? " STORAGE " + tablespaceStorage
                        : "");
    }
}
