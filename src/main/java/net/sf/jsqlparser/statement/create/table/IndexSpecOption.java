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
import java.util.Objects;
import net.sf.jsqlparser.expression.StringValue;

/**
 * An option written after the key list of an index or key definition. MySQL's index options
 * (KEY_BLOCK_SIZE, USING index type, WITH PARSER, COMMENT, VISIBLE/INVISIBLE, ENGINE_ATTRIBUTE and
 * SECONDARY_ENGINE_ATTRIBUTE) are typed; other accepted tokens are retained as opaque
 * {@link Kind#OTHER} options. The kind is fixed; replace the node to change it.
 */
public class IndexSpecOption implements Serializable {

    public enum Kind {
        KEY_BLOCK_SIZE, INDEX_TYPE, PARSER, COMMENT, VISIBILITY, ENGINE_ATTRIBUTE, SECONDARY_ENGINE_ATTRIBUTE, OTHER
    }

    private final Kind kind;
    private String value;
    private StringValue comment;
    private boolean useEquals;
    private boolean visible;
    private List<String> tokens;

    private IndexSpecOption(Kind kind) {
        this.kind = kind;
    }

    /**
     * Creates a valued option. {@code value} keeps its SQL spelling: a number for KEY_BLOCK_SIZE,
     * an index type or parser name, or a quoted string literal for COMMENT and the engine
     * attributes.
     */
    public static IndexSpecOption of(Kind kind, String value) {
        return of(kind, value, false);
    }

    /** Like {@link #of(Kind, String)}; {@code useEquals} requires a kind that accepts '='. */
    public static IndexSpecOption of(Kind kind, String value, boolean useEquals) {
        IndexSpecOption option = new IndexSpecOption(Objects.requireNonNull(kind, "kind"));
        option.setValue(value);
        option.setUseEquals(useEquals);
        return option;
    }

    /**
     * Creates a COMMENT option. The literal keeps its quotes and escaping, so
     * {@link StringValue#getNotExcapedValue()} returns the stored comment text.
     */
    public static IndexSpecOption comment(StringValue comment) {
        IndexSpecOption option = new IndexSpecOption(Kind.COMMENT);
        option.setComment(comment);
        return option;
    }

    public static IndexSpecOption visibility(boolean visible) {
        IndexSpecOption option = new IndexSpecOption(Kind.VISIBILITY);
        option.visible = visible;
        return option;
    }

    /** Creates an opaque option from a copy of {@code tokens}; the tokens are never parsed. */
    public static IndexSpecOption raw(List<String> tokens) {
        IndexSpecOption option = new IndexSpecOption(Kind.OTHER);
        option.setTokens(tokens);
        return option;
    }

    public static IndexSpecOption raw(String... tokens) {
        return raw(Arrays.asList(tokens));
    }

    public Kind getKind() {
        return kind;
    }

    /**
     * The SQL spelling of the option value, or null for VISIBILITY and opaque options. For COMMENT,
     * this is the literal's SQL text, such as {@code 'it\'s'}; use {@link #getComment()} for its
     * decoded text.
     */
    public String getValue() {
        return kind == Kind.COMMENT ? comment.toString() : value;
    }

    /**
     * Replaces the value with its SQL spelling. A COMMENT value is read as a string literal and
     * keeps the escaping of the literal it replaces.
     */
    public void setValue(String value) {
        if (kind == Kind.VISIBILITY || kind == Kind.OTHER) {
            throw new IllegalStateException(kind + " options have no value");
        }
        Objects.requireNonNull(value, "value");
        if (kind == Kind.COMMENT) {
            setComment(new StringValue(value)
                    .withEscaping(comment == null ? null : comment.getEscaping()));
        } else {
            this.value = value;
        }
    }

    /** The literal of a COMMENT option, otherwise null. */
    public StringValue getComment() {
        return comment;
    }

    public void setComment(StringValue comment) {
        if (kind != Kind.COMMENT) {
            throw new IllegalStateException(kind + " options have no comment");
        }
        this.comment = Objects.requireNonNull(comment, "comment");
    }

    public boolean isUseEquals() {
        return useEquals;
    }

    /**
     * Selects the optional '=' of KEY_BLOCK_SIZE, ENGINE_ATTRIBUTE and SECONDARY_ENGINE_ATTRIBUTE.
     */
    public void setUseEquals(boolean useEquals) {
        if (useEquals && kind != Kind.KEY_BLOCK_SIZE && kind != Kind.ENGINE_ATTRIBUTE
                && kind != Kind.SECONDARY_ENGINE_ATTRIBUTE) {
            throw new IllegalStateException(kind + " options do not use '='");
        }
        this.useEquals = useEquals;
    }

    /** VISIBLE (true) or INVISIBLE (false) for a VISIBILITY option, otherwise null. */
    public Boolean getVisible() {
        return kind == Kind.VISIBILITY ? visible : null;
    }

    public void setVisible(boolean visible) {
        if (kind != Kind.VISIBILITY) {
            throw new IllegalStateException(kind + " options have no visibility");
        }
        this.visible = visible;
    }

    /**
     * Returns an unmodifiable, detached token snapshot: the keywords, an optional '=' and the value
     * of a typed option, or a copy of an opaque option's tokens.
     */
    public List<String> getTokens() {
        if (kind == Kind.OTHER) {
            return Collections.unmodifiableList(new ArrayList<>(tokens));
        }
        List<String> result = new ArrayList<>(Arrays.asList(keyword().split(" ")));
        if (useEquals) {
            result.add("=");
        }
        if (getValue() != null) {
            result.add(getValue());
        }
        return Collections.unmodifiableList(result);
    }

    /** Replaces the tokens of an opaque option with a copy; typed options reject raw tokens. */
    public void setTokens(List<String> tokens) {
        if (kind != Kind.OTHER) {
            throw new IllegalStateException(
                    "Typed " + kind + " options cannot be replaced with raw tokens");
        }
        this.tokens = new ArrayList<>(Objects.requireNonNull(tokens, "tokens"));
    }

    private String keyword() {
        switch (kind) {
            case INDEX_TYPE:
                return "USING";
            case PARSER:
                return "WITH PARSER";
            case VISIBILITY:
                return visible ? "VISIBLE" : "INVISIBLE";
            default:
                return kind.name();
        }
    }

    @Override
    public String toString() {
        return String.join(" ", getTokens());
    }
}
