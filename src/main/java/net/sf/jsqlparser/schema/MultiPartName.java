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
        return quotedIdentifier != null
                ? LEADING_TRAILING_QUOTES_PATTERN.matcher(quotedIdentifier).replaceAll("")
                : null;
    }

    static boolean isQuoted(String identifier) {
        return identifier!=null && LEADING_TRAILING_QUOTES_PATTERN.matcher(identifier).find();
    }

    String getFullyQualifiedName();

    String getUnquotedName();

    static String replaceBackticksWithDoubleQuotes(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        Matcher matcher = BACKTICK_PATTERN.matcher(input);
        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            // Replace each backtick-quoted part with double-quoted equivalent
            String content = matcher.group(1);
            matcher.appendReplacement(sb, "\"" + Matcher.quoteReplacement(content) + "\"");
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

}
