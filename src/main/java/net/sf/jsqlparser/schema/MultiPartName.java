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

import java.util.regex.Pattern;

public interface MultiPartName {
    Pattern LEADING_TRAILING_QUOTES_PATTERN = Pattern.compile("^[\"\\[`]+|[\"\\]`]+$");

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
        return LEADING_TRAILING_QUOTES_PATTERN.matcher(identifier).find();
    }

    String getFullyQualifiedName();

    String getUnquotedName();
}
