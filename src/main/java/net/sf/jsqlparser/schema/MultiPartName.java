/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.schema;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface MultiPartName {
    Pattern LEADING_TRAILING_QUOTES_PATTERN = Pattern.compile("^[\"\\[`]+|[\"\\]`]+$");
    Pattern BACKTICK_PATTERN = Pattern.compile("`([^`]*)`");

    /**
     * Removes leading and trailing quotes from a SQL quoted identifier
     *
     * @param quotedIdentifier the quoted identifier
     * @return the pure identifier without quotes
     */
    static String unquote(String quotedIdentifier) {
        if (quotedIdentifier == null) {
            return null;
        }
        // same result as LEADING_TRAILING_QUOTES_PATTERN.matcher(..).replaceAll(""), but linear:
        // the regex retries its trailing alternative at every offset of a run of quotes
        int start = 0;
        int end = quotedIdentifier.length();
        while (start < end && isLeadingQuote(quotedIdentifier.charAt(start))) {
            start++;
        }
        while (end > start && isTrailingQuote(quotedIdentifier.charAt(end - 1))) {
            end--;
        }
        return quotedIdentifier.substring(start, end);
    }

    static boolean isQuoted(String identifier) {
        return identifier != null && !identifier.isEmpty()
                && (isLeadingQuote(identifier.charAt(0))
                        || isTrailingQuote(identifier.charAt(identifier.length() - 1)));
    }

    private static boolean isLeadingQuote(char c) {
        return c == '"' || c == '[' || c == '`';
    }

    private static boolean isTrailingQuote(char c) {
        return c == '"' || c == ']' || c == '`';
    }

    String getFullyQualifiedName();

    String getUnquotedName();


    static String replaceBackticksWithDoubleQuotes(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        Matcher matcher = BACKTICK_PATTERN.matcher(input);
        StringBuilder sb = new StringBuilder();
        int lastEnd = 0;

        while (matcher.find()) {
            sb.append(input, lastEnd, matcher.start()); // text before match
            sb.append('"').append(matcher.group(1)).append('"'); // replace with double quotes
            lastEnd = matcher.end();
        }

        sb.append(input.substring(lastEnd)); // append remaining text
        return sb.toString();
    }


}
