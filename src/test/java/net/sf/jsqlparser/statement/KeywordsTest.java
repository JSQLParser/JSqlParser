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

import java.io.File;
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
public class KeywordsTest {
    public final static Logger LOGGER = Logger.getLogger(KeywordsTest.class.getName());

    public static Stream<String> keyWords() {
        File file = new File("src/main/jjtree/net/sf/jsqlparser/parser/JSqlParserCC.jjt");
        List<String> keywords = new ArrayList<>();
        try {
            keywords.addAll( ParserKeywordsUtils.getAllKeywordsUsingRegex(file) );
            for (String reserved: ParserKeywordsUtils.getReservedKeywords(ParserKeywordsUtils.RESTRICTED_JSQLPARSER)) {
                keywords.remove(reserved);
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Failed to generate the Keyword List", ex);
        }
        return keywords.stream();
    }

    @ParameterizedTest(name = "Keyword {0}")
    @MethodSource("keyWords")
    public void testRelObjectNameWithoutValue(String keyword) throws JSQLParserException {
        String sqlStr = String.format("SELECT %1$s.%1$s AS %1$s from %1$s.%1$s AS %1$s",  keyword);
        assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

}
