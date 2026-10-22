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

    /** The prefix of a PostgreSQL Unicode escape string, as in {@code U&'\0041'}. */
    private static final String UNICODE_ESCAPE_PREFIX = "U&";

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
        POSTGRESQL_ESCAPE_STRING,
        /**
         * PostgreSQL Unicode escapes of a {@code U&'...'} string: the escape character followed by
         * four hexadecimal digits or by {@code +} and six, and a doubled escape character. The
         * escape character is a backslash unless {@link #getUnicodeEscapeCharacter()} names another
         * one.
         */
        POSTGRESQL_UNICODE_ESCAPE
    }

    private String value = "";
    private String prefix = null;
    private String quoteStr = "'";
    private Escaping escaping = Escaping.STANDARD;
    private Character unicodeEscapeCharacter = null;

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

        if (readUnicodeEscapeString(escapedValue)) {
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
     * Reads {@code U&'body'} with an optional {@code UESCAPE 'c'} clause. Returns false, leaving
     * this value unchanged, when the text has another shape.
     */
    private boolean readUnicodeEscapeString(String text) {
        if (!text.regionMatches(true, 0, UNICODE_ESCAPE_PREFIX + "'", 0, 3)) {
            return false;
        }
        int close = 3;
        while (close < text.length()) {
            if (text.charAt(close) != '\'') {
                close++;
            } else if (close + 1 < text.length() && text.charAt(close + 1) == '\'') {
                close += 2;
            } else {
                break;
            }
        }
        if (close >= text.length()) {
            return false;
        }
        String clause = text.substring(close + 1).trim();
        Character escape = null;
        if (!clause.isEmpty()) {
            if (!clause.regionMatches(true, 0, "UESCAPE", 0, 7)) {
                return false;
            }
            String quoted = clause.substring(7).trim();
            if (quoted.length() != 3 || quoted.charAt(0) != '\'' || quoted.charAt(2) != '\'') {
                return false;
            }
            escape = quoted.charAt(1);
        }
        prefix = UNICODE_ESCAPE_PREFIX;
        value = text.substring(3, close);
        escaping = Escaping.POSTGRESQL_UNICODE_ESCAPE;
        unicodeEscapeCharacter = escape;
        return true;
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
     * Returns the escape character named by the {@code UESCAPE} clause of a PostgreSQL Unicode
     * escape string, or null when the literal has no such clause and the escape character is a
     * backslash.
     */
    public Character getUnicodeEscapeCharacter() {
        return unicodeEscapeCharacter;
    }

    /**
     * Sets the escape character rendered as {@code UESCAPE 'c'} after the literal; null removes the
     * clause. It applies to {@link Escaping#POSTGRESQL_UNICODE_ESCAPE} values, whose
     * {@link #getNotExcapedValue()} rejects a character that PostgreSQL rejects.
     */
    public void setUnicodeEscapeCharacter(Character unicodeEscapeCharacter) {
        this.unicodeEscapeCharacter = unicodeEscapeCharacter;
    }

    public StringValue withUnicodeEscapeCharacter(Character unicodeEscapeCharacter) {
        setUnicodeEscapeCharacter(unicodeEscapeCharacter);
        return this;
    }

    /**
     * Returns the literal's characters with its escapes resolved: a doubled quote character and,
     * depending on {@link #getEscaping()}, backslash or Unicode escape sequences. PostgreSQL octal
     * and hexadecimal escapes denote bytes, which are decoded as UTF-8. Dollar-quoted bodies are
     * returned unchanged.
     *
     * @throws IllegalArgumentException if a {@link Escaping#POSTGRESQL_UNICODE_ESCAPE} value has an
     *         escape that PostgreSQL rejects; the parser rejects such literals
     */
    public String getNotExcapedValue() {
        if (quoteStr != null && quoteStr.startsWith("$")) {
            return value;
        }
        char quote = "\"".equals(quoteStr) ? '"' : '\'';
        if (escaping == Escaping.POSTGRESQL_UNICODE_ESCAPE) {
            return unescapeUnicode(value, quote,
                    unicodeEscapeCharacter == null ? '\\' : unicodeEscapeCharacter);
        }
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

    /**
     * Resolves PostgreSQL Unicode escapes the way the server does: {@code escape} followed by four
     * hexadecimal digits, or by {@code +} and six, names a code point, and a UTF-16 surrogate pair
     * must be written as two consecutive escapes. A doubled escape or quote character stands for
     * itself.
     */
    private static String unescapeUnicode(String text, char quote, char escape) {
        checkUnicodeEscapeCharacter(escape);
        UnicodeUnescaped result = new UnicodeUnescaped(text.length());
        int i = 0;
        while (i < text.length()) {
            char c = text.charAt(i);
            boolean doubled = i + 1 < text.length() && text.charAt(i + 1) == c;
            if (c == escape && !doubled) {
                i = appendUnicodeEscape(result, text, i, escape);
            } else {
                result.append(c, i);
                i += doubled && (c == escape || c == quote) ? 2 : 1;
            }
        }
        return result.finish(text.length());
    }

    private static void checkUnicodeEscapeCharacter(char escape) {
        if (escape > 0x7F || isHexDigit(escape) || "+'\" \t\n\r\f\u000B".indexOf(escape) >= 0) {
            throw new IllegalArgumentException("invalid Unicode escape character '" + escape + "'");
        }
    }

    /**
     * Appends the code point of the escape at {@code position}, {@code escape} followed by four
     * hexadecimal digits or by {@code +} and six, and returns the next index.
     */
    private static int appendUnicodeEscape(UnicodeUnescaped result, String text, int position,
            char escape) {
        boolean plus = position + 1 < text.length() && text.charAt(position + 1) == '+';
        int start = plus ? position + 2 : position + 1;
        int end = start + (plus ? 6 : 4);
        int codePoint = parseHex(text, start, end);
        if (codePoint < 0) {
            throw new IllegalArgumentException("invalid Unicode escape at position " + position
                    + ": Unicode escapes must be " + escape + "XXXX or " + escape + "+XXXXXX");
        }
        if (codePoint == 0 || codePoint > Character.MAX_CODE_POINT) {
            throw new IllegalArgumentException(
                    "invalid Unicode escape value at position " + position);
        }
        result.appendCodePoint(codePoint, position);
        return end;
    }

    private static IllegalArgumentException invalidSurrogatePair(int position) {
        return new IllegalArgumentException(
                "invalid Unicode surrogate pair at position " + position);
    }

    private static boolean isHexDigit(char c) {
        return c >= '0' && c <= '9' || c >= 'a' && c <= 'f' || c >= 'A' && c <= 'F';
    }

    /** Returns the ASCII hexadecimal number spelled by text[start, end), or -1. */
    private static int parseHex(String text, int start, int end) {
        if (end > text.length()) {
            return -1;
        }
        int number = 0;
        for (int i = start; i < end; i++) {
            char c = text.charAt(i);
            if (!isHexDigit(c)) {
                return -1;
            }
            number = number * 16 + Character.digit(c, 16);
        }
        return number;
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

    /**
     * Collects resolved Unicode escapes. A UTF-16 surrogate pair must be written as two consecutive
     * escapes, as PostgreSQL requires.
     */
    private static final class UnicodeUnescaped {
        private final StringBuilder chars;
        private char highSurrogate;

        UnicodeUnescaped(int capacity) {
            chars = new StringBuilder(capacity);
        }

        void append(char c, int position) {
            requireNoPendingSurrogate(position);
            chars.append(c);
        }

        void appendCodePoint(int codePoint, int position) {
            if (codePoint >= Character.MIN_LOW_SURROGATE
                    && codePoint <= Character.MAX_LOW_SURROGATE) {
                if (highSurrogate == 0) {
                    throw invalidSurrogatePair(position);
                }
                chars.appendCodePoint(Character.toCodePoint(highSurrogate, (char) codePoint));
                highSurrogate = 0;
                return;
            }
            requireNoPendingSurrogate(position);
            if (codePoint >= Character.MIN_HIGH_SURROGATE
                    && codePoint <= Character.MAX_HIGH_SURROGATE) {
                highSurrogate = (char) codePoint;
            } else {
                chars.appendCodePoint(codePoint);
            }
        }

        String finish(int position) {
            requireNoPendingSurrogate(position);
            return chars.toString();
        }

        private void requireNoPendingSurrogate(int position) {
            if (highSurrogate != 0) {
                throw invalidSurrogatePair(position);
            }
        }
    }

    @Override
    public <T, S> T accept(ExpressionVisitor<T> expressionVisitor, S context) {
        return expressionVisitor.visit(this, context);
    }

    @Override
    public String toString() {
        String unicodeEscape = unicodeEscapeCharacter != null
                ? " UESCAPE '" + unicodeEscapeCharacter + "'"
                : "";
        return (prefix != null ? prefix : "") + quoteStr + value + quoteStr + unicodeEscape;
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
