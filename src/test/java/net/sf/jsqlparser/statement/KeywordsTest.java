/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2021 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.ParserKeywordsUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;

/**
 * Verifies that all non-reserved keywords can be used as unquoted identifiers (schema, table,
 * column, alias, function names).
 *
 * @author <a href="mailto:andreas@manticore-projects.com">Andreas Reichel</a>
 */
public class KeywordsTest {

    public static Stream<String> nonReservedKeywords() {
        return ParserKeywordsUtils.getNonReservedKeywords().stream();
    }

    @ParameterizedTest(name = "Keyword {0}")
    @MethodSource("nonReservedKeywords")
    public void testRelObjectNameWithoutValue(String keyword) throws JSQLParserException {
        String sqlStr = String.format("SELECT %1$s.%1$s AS %1$s from %1$s.%1$s AS %1$s", keyword);
        assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @ParameterizedTest(name = "Keyword {0}")
    @MethodSource("nonReservedKeywords")
    public void testRelObjectNameExt(String keyword) throws JSQLParserException {
        String sqlStr = String.format(
                "SELECT %1$s.%1$s.%1$s \"%1$s\" from %1$s \"%1$s\" ORDER BY %1$s ", keyword);
        assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    public void testCombinedTokenKeywords() throws JSQLParserException {
        String sqlStr = "SELECT current_date(3)";
        assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }
}
