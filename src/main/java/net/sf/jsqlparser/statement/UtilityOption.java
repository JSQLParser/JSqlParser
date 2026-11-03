/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

/**
 * One element {@code name [value]} of a PostgreSQL utility option list, such as
 * {@code VACUUM (VERBOSE, PARALLEL 2) t}. The same list follows {@code VACUUM}, {@code ANALYZE} and
 * {@code CLUSTER}.
 * <p>
 * The parser accepts every option that PostgreSQL's grammar accepts, as PostgreSQL does: names and
 * values are checked when the server runs the statement. {@link #getKind(Class)} tells whether a
 * statement knows the name.
 * <p>
 * The name and the value keep their spelling. {@link #getValueType()} tells how the value is
 * written, and {@link #getBooleanValue()}, {@link #getLongValue()} and {@link #getStringValue()}
 * read it.
 */
public class UtilityOption extends ASTNodeAccessImpl {

    /** How the value of an option is written. */
    public enum ValueType {
        /** {@code TRUE}, {@code FALSE}, {@code ON} or {@code OFF}. */
        BOOLEAN,
        /** An integer or decimal number, optionally signed, such as {@code 2}. */
        NUMBER,
        /** A string literal, such as {@code '256kB'}. */
        STRING,
        /** Another word, such as {@code AUTO}, or a double-quoted identifier. */
        WORD
    }

    private String name;
    private ValueType valueType;
    private String value;

    /** Creates an option without a value, such as {@code VERBOSE}. */
    public UtilityOption(String name) {
        setName(name);
    }

    /**
     * Creates an option with a value written as SQL text: a string value includes its quotes.
     */
    public UtilityOption(String name, ValueType valueType, String value) {
        setName(name);
        setValue(valueType, value);
    }

    /** {@code name TRUE} or {@code name FALSE}. */
    public static UtilityOption of(String name, boolean value) {
        return new UtilityOption(name, ValueType.BOOLEAN, value ? "TRUE" : "FALSE");
    }

    /** {@code name n}, such as {@code PARALLEL 2}. */
    public static UtilityOption of(String name, long value) {
        return new UtilityOption(name, ValueType.NUMBER, Long.toString(value));
    }

    /** {@code name 'text'}, quoting the text. */
    public static UtilityOption ofString(String name, String text) {
        return new UtilityOption(name, ValueType.STRING,
                "'" + Objects.requireNonNull(text, "text").replace("'", "''") + "'");
    }

    /** {@code name word}, such as {@code INDEX_CLEANUP AUTO}. */
    public static UtilityOption ofWord(String name, String word) {
        return new UtilityOption(name, isBooleanWord(word) ? ValueType.BOOLEAN : ValueType.WORD,
                word);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Objects.requireNonNull(name, "name");
    }

    /**
     * Whether this option has the given name, read as PostgreSQL reads it: an unquoted name ignores
     * case, a double-quoted one keeps it, and the unquoted {@code ANALYSE} is the option
     * {@code ANALYZE}.
     */
    public boolean isNamed(String name) {
        return name != null && canonicalName(this.name).equals(canonicalName(name));
    }

    /**
     * The constant of a statement's option kinds that this option names, such as
     * {@code getKind(VacuumStatement.OptionKind.class)}; null for a name that the statement does
     * not know, which PostgreSQL rejects when it runs the statement. As in PostgreSQL, a
     * double-quoted name matches only in lower case: {@code "verbose"} is {@code VERBOSE}, and
     * {@code "VERBOSE"} is unknown.
     */
    public <K extends Enum<K>> K getKind(Class<K> kinds) {
        return constantNamed(kinds, canonicalName(name));
    }

    public boolean hasValue() {
        return valueType != null;
    }

    /** The way the value is written, or null without a value. */
    public ValueType getValueType() {
        return valueType;
    }

    /** The value as written in SQL, with the quotes of a string; null without a value. */
    public String getValue() {
        return value;
    }

    /** Sets the value as written in SQL; both arguments are null to remove the value. */
    public void setValue(ValueType valueType, String value) {
        if ((valueType == null) != (value == null)) {
            throw new IllegalArgumentException("A value needs both its type and its text");
        }
        this.valueType = valueType;
        this.value = value;
    }

    public void removeValue() {
        setValue(null, null);
    }

    /**
     * The Boolean value as PostgreSQL reads it: {@code TRUE}/{@code ON} and {@code 1} are true,
     * {@code FALSE}/{@code OFF} and {@code 0} are false, also as a string. Null without a value or
     * for any other value. PostgreSQL enables a Boolean option written without a value.
     */
    public Boolean getBooleanValue() {
        if (valueType == null) {
            return null;
        }
        if (valueType == ValueType.NUMBER) {
            Long number = getLongValue();
            return number == null || number > 1 || number < 0 ? null : number == 1;
        }
        String text = getStringValue().toUpperCase(Locale.ROOT);
        if ("TRUE".equals(text) || "ON".equals(text)) {
            return Boolean.TRUE;
        }
        if ("FALSE".equals(text) || "OFF".equals(text)) {
            return Boolean.FALSE;
        }
        return null;
    }

    /**
     * The constant of the enum that the value spells, ignoring case, such as {@code AUTO}; null
     * without a value or for another value.
     */
    public <E extends Enum<E>> E getEnumValue(Class<E> type) {
        return valueType == null ? null
                : constantNamed(type, getStringValue().toLowerCase(Locale.ROOT));
    }

    /** The value of an integer number, or null for any other value. */
    public Long getLongValue() {
        if (valueType != ValueType.NUMBER) {
            return null;
        }
        try {
            return Long.valueOf(value.startsWith("+") ? value.substring(1) : value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /**
     * The text of the value: the characters of a string or a double-quoted identifier without its
     * quotes and escapes, or the value as written otherwise. Null without a value.
     */
    public String getStringValue() {
        if (valueType != ValueType.STRING) {
            return valueType == ValueType.WORD && isQuoted(value) ? unquoted(value) : value;
        }
        StringValue literal = new StringValue(value);
        if ("E".equalsIgnoreCase(literal.getPrefix())) {
            literal.setEscaping(StringValue.Escaping.POSTGRESQL_ESCAPE_STRING);
        }
        return literal.getNotExcapedValue();
    }

    public UtilityOption withName(String name) {
        setName(name);
        return this;
    }

    public UtilityOption withValue(ValueType valueType, String value) {
        setValue(valueType, value);
        return this;
    }

    @Override
    public StringBuilder appendTo(StringBuilder builder) {
        builder.append(name);
        if (valueType != null) {
            builder.append(' ').append(value);
        }
        return builder;
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }

    /**
     * Whether the word is one of the Boolean words {@code TRUE}, {@code FALSE}, {@code ON},
     * {@code OFF}.
     */
    public static boolean isBooleanWord(String word) {
        String text = Objects.requireNonNull(word, "word").toUpperCase(Locale.ROOT);
        return "TRUE".equals(text) || "FALSE".equals(text) || "ON".equals(text)
                || "OFF".equals(text);
    }

    /**
     * Appends the options of a statement after a space: in parentheses, or as bare keywords when
     * {@code parenthesized} is false and the unparenthesized form of the statement can express
     * them. Nothing is appended without options.
     *
     * @param keywords the options that the statement accepts without parentheses, in the order that
     *        it requires; each can appear once, without a value
     */
    public static StringBuilder appendOptions(StringBuilder builder, List<UtilityOption> options,
            boolean parenthesized, String... keywords) {
        if (options.isEmpty()) {
            return builder;
        }
        if (!parenthesized && !options.isEmpty() && isKeywordForm(options, keywords)) {
            for (String keyword : keywords) {
                for (UtilityOption option : options) {
                    if (option.isNamed(keyword)) {
                        builder.append(' ').append(option.getName());
                    }
                }
            }
            return builder;
        }
        builder.append(" (");
        for (int i = 0; i < options.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            options.get(i).appendTo(builder);
        }
        return builder.append(')');
    }

    /**
     * Whether the unparenthesized form of a statement can express the options: each is one of the
     * keywords, unquoted, without a value, and named once. True without options.
     *
     * @param keywords the options that the statement accepts without parentheses
     */
    public static boolean isKeywordForm(List<UtilityOption> options, String... keywords) {
        for (UtilityOption option : options) {
            if (option.hasValue() || isQuoted(option.name)) {
                return false;
            }
            int matches = 0;
            for (String keyword : keywords) {
                if (option.isNamed(keyword)) {
                    matches++;
                }
            }
            if (matches == 0 || options.stream().filter(other -> other.isNamed(option.name))
                    .count() > 1) {
                return false;
            }
        }
        return true;
    }

    /**
     * The name as PostgreSQL's grammar hands it on: a double-quoted name without its quotes, and an
     * unquoted one in lower case, with {@code analyse} spelled {@code analyze}.
     */
    private static String canonicalName(String name) {
        if (isQuoted(name)) {
            return unquoted(name);
        }
        String lower = name.toLowerCase(Locale.ROOT);
        return "analyse".equals(lower) ? "analyze" : lower;
    }

    private static boolean isQuoted(String text) {
        return text.length() > 1 && text.charAt(0) == '"' && text.charAt(text.length() - 1) == '"';
    }

    private static String unquoted(String text) {
        return text.substring(1, text.length() - 1).replace("\"\"", "\"");
    }

    private static <E extends Enum<E>> E constantNamed(Class<E> type, String name) {
        for (E constant : type.getEnumConstants()) {
            if (constant.name().toLowerCase(Locale.ROOT).equals(name)) {
                return constant;
            }
        }
        return null;
    }
}
