package net.sf.jsqlparser.parser;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

class ParserKeywordsUtilsTest {
    final static File FILE = new File("src/main/jjtree/net/sf/jsqlparser/parser/JSqlParserCC.jjt");
    final static Logger LOGGER = Logger.getLogger(ParserKeywordsUtilsTest.class.getName());

    @Test
    void main() {
    }

    @Test
    void getAllKeywords() throws IOException {
        Set<String> allKeywords =  ParserKeywordsUtils.getAllKeywordsUsingRegex(FILE);
        assertFalse( allKeywords.isEmpty(), "Keyword List must not be empty!" );
    }

    @Test
    void getAllKeywordsUsingJavaCC() throws Exception {
        Set<String> allKeywords =  ParserKeywordsUtils.getAllKeywordsUsingJavaCC(FILE);
        assertFalse( allKeywords.isEmpty(), "Keyword List must not be empty!" );
    }

    // Test, if all Tokens found per RegEx are also found from the JavaCCParser
    @Test
    void compareKeywordLists() throws Exception {
        Set<String> allRegexKeywords =  ParserKeywordsUtils.getAllKeywordsUsingRegex(FILE);
        Set<String> allJavaCCParserKeywords =  ParserKeywordsUtils.getAllKeywordsUsingJavaCC(FILE);

        // Exceptions, which should not have been found from the RegEx
        List<String> exceptions = Arrays.asList("0x");

        // We expect all Keywords from the Regex to be found by the JavaCC Parser
        for (String s:allRegexKeywords) {
            Assertions.assertTrue(
                    exceptions.contains(s) || allJavaCCParserKeywords.contains(s)
                    , "The Keywords from JavaCC do not contain Keyword: " + s);
        }

        // The JavaCC Parser finds some more valid Keywords (where no explicit Token has been defined
        for (String s:allJavaCCParserKeywords) {
            if ( ! (exceptions.contains(s) || allRegexKeywords.contains(s)) ) {
                LOGGER.fine ("Found Additional Keywords from Parser: " + s);
            }
        }
    }
}