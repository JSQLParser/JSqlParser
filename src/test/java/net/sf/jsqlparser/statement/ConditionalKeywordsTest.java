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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;


import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;

/**
 *
 * @author <a href="mailto:andreas@manticore-projects.com">Andreas Reichel</a>
 */


public class ConditionalKeywordsTest {
    public final static Logger LOGGER = Logger.getLogger(ConditionalKeywordsTest.class.getName());

    public final static Stream<String> KEY_WORDS() {
        List<String> keywords = new ArrayList<>();
        try {
            try {
                keywords.addAll(ParserKeywordsUtils.getDefinedKeywords());
                for (String reserved: ParserKeywordsUtils.getReservedKeywords(
                        // get all PARSER RESTRICTED without the ALIAS RESTRICTED
                        ParserKeywordsUtils.RESTRICTED_JSQLPARSER
                        & ~ParserKeywordsUtils.RESTRICTED_ALIAS
                        )) {
                    keywords.remove(reserved);
                }
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Failed to generate the Keyword List", ex);
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Failed to generate the Keyword List", ex);
        }
        return keywords.stream();
    }

    @ParameterizedTest(name = "Keyword {0}")
    @MethodSource("KEY_WORDS")
    public void testRelObjectNameExt(String keyword) throws JSQLParserException {
        String sqlStr = String.format("SELECT %1$s.%1$s.%1$s AS \"%1$s\" from %1$s ORDER BY %1$s ",  keyword);
        LOGGER.fine(sqlStr);
        assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }
}
