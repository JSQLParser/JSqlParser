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

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
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

    /**
     * How a backslash in the literal body was read. The parser records the escaping of the
     * configuration that lexed the literal; constructed values default to {@link #STANDARD}.
     */
    public enum Escaping {
        /** Only a doubled quote character is an escape, as in standard SQL. */
        STANDARD,
        /** MySQL/MariaDB backslash escapes, for example {@code \n}, {@code \'} and {@code \\}. */
        MYSQL_BACKSLASH,
        /** PostgreSQL escape string escapes, for example {@code E'\n'} and {@code E'\x41'}. */
        POSTGRESQL_ESCAPE_STRING
    }

    private String value = "";
    private String prefix = null;
    private String quoteStr = "'";
    private Escaping escaping = Escaping.STANDARD;

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

    public Escaping getEscaping() {
        return escaping;
    }

    public void setEscaping(Escaping escaping) {
        this.escaping = escaping == null ? Escaping.STANDARD : escaping;
    }

    public StringValue withEscaping(Escaping escaping) {
        setEscaping(escaping);
        return this;
    }

    /**
     * Returns the literal's characters with its escapes resolved: a doubled quote character and,
     * depending on {@link #getEscaping()}, backslash escape sequences. PostgreSQL octal and
     * hexadecimal escapes denote bytes, which are decoded as UTF-8. Dollar-quoted bodies are
     * returned unchanged.
     */
    public String getNotExcapedValue() {
        if (quoteStr != null && quoteStr.startsWith("$")) {
            return value;
        }
        char quote = "\"".equals(quoteStr) ? '"' : '\'';
        Unescaped result = new Unescaped(value.length());
        int i = 0;
        while (i < value.length()) {
            char c = value.charAt(i);
            if (c == '\\' && escaping != Escaping.STANDARD && i + 1 < value.length()) {
                i = escaping == Escaping.MYSQL_BACKSLASH
                        ? appendMySqlEscape(result, value.charAt(i + 1), i + 1)
                        : appendPostgresEscape(result, value, i + 1);
            } else if (c == quote && i + 1 < value.length() && value.charAt(i + 1) == quote) {
                result.append(quote);
                i += 2;
            } else {
                result.append(c);
                i++;
            }
        }
        return result.toString();
    }

    /** Appends the MySQL escape {@code c} at {@code index} and returns the index after it. */
    private static int appendMySqlEscape(Unescaped result, char c, int index) {
        switch (c) {
            case '0':
                result.append('\0');
                break;
            case 'b':
                result.append('\b');
                break;
            case 'n':
                result.append('\n');
                break;
            case 'r':
                result.append('\r');
                break;
            case 't':
                result.append('\t');
                break;
            case 'Z':
                result.append((char) 0x1A);
                break;
            case '%':
            case '_':
                // kept with the backslash, so that LIKE patterns still match them literally
                result.append('\\');
                result.append(c);
                break;
            default:
                result.append(c);
        }
        return index + 1;
    }

    /** Appends the PostgreSQL escape string escape at {@code index}; returns the next index. */
    private static int appendPostgresEscape(Unescaped result, String text, int index) {
        char c = text.charAt(index);
        switch (c) {
            case 'b':
                result.append('\b');
                return index + 1;
            case 'f':
                result.append('\f');
                return index + 1;
            case 'n':
                result.append('\n');
                return index + 1;
            case 'r':
                result.append('\r');
                return index + 1;
            case 't':
                result.append('\t');
                return index + 1;
            case 'x':
                return appendNumber(result, text, index + 1, 16, 1, 2, c, true);
            case 'u':
                return appendNumber(result, text, index + 1, 16, 4, 4, c, false);
            case 'U':
                return appendNumber(result, text, index + 1, 16, 8, 8, c, false);
            default:
                if (c >= '0' && c <= '7') {
                    return appendNumber(result, text, index, 8, 1, 3, c, true);
                }
                result.append(c);
                return index + 1;
        }
    }

    /**
     * Appends the byte or code point spelled by {@code min} to {@code max} digits at {@code start},
     * or {@code fallback} when fewer digits follow, and returns the next index.
     */
    private static int appendNumber(Unescaped result, String text, int start, int radix,
            int min, int max, char fallback, boolean isByte) {
        int end = start;
        while (end < text.length() && end - start < max
                && Character.digit(text.charAt(end), radix) >= 0) {
            end++;
        }
        if (end - start < min) {
            result.append(fallback);
            return start;
        }
        int number = Integer.parseInt(text.substring(start, end), radix);
        if (isByte) {
            result.appendByte(number);
        } else {
            result.appendCodePoint(number);
        }
        return end;
    }

    /** Collects unescaped characters; consecutive escaped bytes are decoded as UTF-8. */
    private static final class Unescaped {
        private final StringBuilder chars;
        private final ByteArrayOutputStream bytes = new ByteArrayOutputStream();

        Unescaped(int capacity) {
            chars = new StringBuilder(capacity);
        }

        void append(char c) {
            flushBytes();
            chars.append(c);
        }

        void appendCodePoint(int codePoint) {
            flushBytes();
            chars.appendCodePoint(codePoint);
        }

        void appendByte(int value) {
            bytes.write(value);
        }

        private void flushBytes() {
            if (bytes.size() > 0) {
                chars.append(new String(bytes.toByteArray(), StandardCharsets.UTF_8));
                bytes.reset();
            }
        }

        @Override
        public String toString() {
            flushBytes();
            return chars.toString();
        }
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
