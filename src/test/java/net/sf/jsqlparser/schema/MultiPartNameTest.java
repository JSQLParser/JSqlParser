/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2025 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.schema;

import java.time.Duration;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.PlainSelect;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MultiPartNameTest {

    @Test
    void replaceBackticksWithDoubleQuotes() {
        Assertions.assertThat("\"starbake\".\"customers\"").isEqualToIgnoringCase(
                MultiPartName.replaceBackticksWithDoubleQuotes("`starbake`.`customers`"));
    }

    @Test
    void unquoteMatchesQuotePattern() {
        String[] names = {"", "a", "\"a\"", "[a]", "`a`", "\"a\".\"b\"", "\"\"\"\"", "\"]",
                "[\"a\"]", "a\"", "\"a", "]a[", "``a``", "\"a\"\"b\"", "[[a]]"};
        for (String name : names) {
            assertEquals(MultiPartName.LEADING_TRAILING_QUOTES_PATTERN.matcher(name).replaceAll(""),
                    MultiPartName.unquote(name), name);
            assertEquals(MultiPartName.LEADING_TRAILING_QUOTES_PATTERN.matcher(name).find(),
                    MultiPartName.isQuoted(name), name);
        }
        assertNull(MultiPartName.unquote(null));
        assertFalse(MultiPartName.isQuoted(null));
    }

    @Test
    void unquoteIsLinearOnRunsOfEscapedQuotes() {
        // a quoted identifier with a dot and many doubled quotes used to backtrack quadratically
        String quotes = "\"\"".repeat(40000);
        String sql = "SELECT * FROM \"a" + quotes + ".b\"";
        PlainSelect select = (PlainSelect) assertTimeoutPreemptively(Duration.ofSeconds(2),
                () -> CCJSqlParserUtil.parse(sql));
        assertNotNull(select.getFromItem());
        assertEquals("a" + quotes + ".b",
                assertTimeoutPreemptively(Duration.ofSeconds(2),
                        () -> MultiPartName.unquote("\"a" + quotes + ".b\"")));
    }
}
