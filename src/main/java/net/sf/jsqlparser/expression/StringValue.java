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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

/**
 * A string as in 'example_string'
 */
public final class StringValue extends ASTNodeAccessImpl implements Expression {

    public static final List<String> ALLOWED_PREFIXES =
            Arrays.asList("N", "U", "E", "R", "B", "RB", "_utf8", "Q");
    private String value = "";
    private String prefix = null;
    private String quoteStr = "'";

    public StringValue() {
        // empty constructor
    }

    public StringValue(String escapedValue) {
        // removing "'" at the start and at the end
        if (escapedValue.length() >= 2 && escapedValue.startsWith("'")
                && escapedValue.endsWith("'")) {
            value = escapedValue.substring(1, escapedValue.length() - 1);
            return;
        } else if (escapedValue.length() >= 2 && escapedValue.startsWith("\"")
                && escapedValue.endsWith("\"")) {
            // double quoted String Literals (Feature.allowDoubleQuotedStrings)
            value = escapedValue.substring(1, escapedValue.length() - 1);
            quoteStr = "\"";
            return;
        }

        String delimiter = getDollarQuoteDelimiter(escapedValue);
        if (delimiter != null && escapedValue.length() >= 2 * delimiter.length()
                && escapedValue.endsWith(delimiter)) {
            quoteStr = delimiter;
            value = escapedValue.substring(delimiter.length(),
                    escapedValue.length() - delimiter.length());
            return;
        }

        if (escapedValue.length() > 2) {
            for (String p : ALLOWED_PREFIXES) {
                if (escapedValue.length() > p.length()
                        && escapedValue.substring(0, p.length()).equalsIgnoreCase(p)
                        && escapedValue.charAt(p.length()) == '\'') {
                    this.prefix = p;
                    value = escapedValue.substring(p.length() + 1, escapedValue.length() - 1);
                    return;
                }
            }
        }

        value = escapedValue;
    }

    /**
     * Returns the opening PostgreSQL dollar-quote delimiter, or null if there is none. A tag
     * follows unquoted identifier rules, excluding dollar signs. This method does not require the
     * closing delimiter or inspect the body.
     */
    public static String getDollarQuoteDelimiter(String text) {
        if (text == null || text.length() < 2 || text.charAt(0) != '$') {
            return null;
        }
        int end = text.indexOf('$', 1);
        if (end < 0) {
            return null;
        }
        for (int i = 1; i < end;) {
            int character = text.codePointAt(i);
            boolean valid =
                    i == 1 ? Character.isUnicodeIdentifierStart(character) || character == '_'
                            : Character.isUnicodeIdentifierPart(character);
            if (!valid) {
                return null;
            }
            i += Character.charCount(character);
        }
        return text.substring(0, end + 1);
    }

    public String getValue() {
        return value;
    }

    public void setValue(String string) {
        value = string;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getQuoteStr() {
        return quoteStr;
    }

    public StringValue setQuoteStr(String quoteStr) {
        this.quoteStr = quoteStr;
        return this;
    }

    public String getNotExcapedValue() {
        if (quoteStr != null && quoteStr.startsWith("$")) {
            return value;
        }
        StringBuilder buffer = new StringBuilder(value.length());
        int from = 0;
        int index;
        while ((index = value.indexOf("''", from)) != -1) {
            buffer.append(value, from, index + 1);
            from = index + 2;
        }
        buffer.append(value, from, value.length());
        return buffer.toString();
    }

    @Override
    public <T, S> T accept(ExpressionVisitor<T> expressionVisitor, S context) {
        return expressionVisitor.visit(this, context);
    }

    @Override
    public String toString() {
        return (prefix != null ? prefix : "") + quoteStr + value + quoteStr;
    }

    public StringValue withPrefix(String prefix) {
        this.setPrefix(prefix);
        return this;
    }

    public StringValue withValue(String value) {
        this.setValue(value);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        StringValue that = (StringValue) o;
        return Objects.equals(value, that.value) && Objects.equals(prefix, that.prefix);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, prefix);
    }
}
