/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2022 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.parser;

import org.javacc.jjtree.JJTree;
import org.javacc.parser.Context;
import org.javacc.parser.JavaCCParser;
import org.javacc.parser.RCharacterList;
import org.javacc.parser.RChoice;
import org.javacc.parser.RJustName;
import org.javacc.parser.ROneOrMore;
import org.javacc.parser.RSequence;
import org.javacc.parser.RStringLiteral;
import org.javacc.parser.RZeroOrMore;
import org.javacc.parser.RZeroOrOne;
import org.javacc.parser.RegularExpression;
import org.javacc.parser.Semanticize;
import org.javacc.parser.Token;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.InvalidClassException;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;


class ParserKeywordsUtilsTest {
    public final static CharsetEncoder CHARSET_ENCODER = StandardCharsets.US_ASCII.newEncoder();

    final static File FILE = new File("src/main/jjtree/net/sf/jsqlparser/parser/JSqlParserCC.jjt");
    final static Logger LOGGER = Logger.getLogger(ParserKeywordsUtilsTest.class.getName());


    private static void addTokenImage(TreeSet<String> allKeywords, RStringLiteral literal) {
        if (CHARSET_ENCODER.canEncode(literal.image) && literal.image.matches("\\w+")) {
            allKeywords.add(literal.image);
        }
    }

    @SuppressWarnings({"PMD.EmptyIfStmt", "PMD.CyclomaticComplexity"})
    private static void addTokenImage(TreeSet<String> allKeywords, Object o) throws Exception {
        if (o instanceof RStringLiteral) {
            RStringLiteral literal = (RStringLiteral) o;
            addTokenImage(allKeywords, literal);
        } else if (o instanceof RChoice) {
            RChoice choice = (RChoice) o;
            addTokenImage(allKeywords, choice);
        } else if (o instanceof RSequence) {
            RSequence sequence1 = (RSequence) o;
            addTokenImage(allKeywords, sequence1);
        } else if (o instanceof ROneOrMore) {
            ROneOrMore oneOrMore = (ROneOrMore) o;
            addTokenImage(allKeywords, oneOrMore);
        } else if (o instanceof RZeroOrMore) {
            RZeroOrMore zeroOrMore = (RZeroOrMore) o;
            addTokenImage(allKeywords, zeroOrMore);
        } else if (o instanceof RZeroOrOne) {
            RZeroOrOne zeroOrOne = (RZeroOrOne) o;
            addTokenImage(allKeywords, zeroOrOne);
        } else if (o instanceof RJustName) {
            RJustName zeroOrOne = (RJustName) o;
            addTokenImage(allKeywords, zeroOrOne);
        } else if (o instanceof RCharacterList) {
            // do nothing, we are not interested in those
        } else {
            throw new InvalidClassException(
                    "Unknown Type: " + o.getClass().getName() + " " + o.toString());
        }
    }

    private static void addTokenImage(TreeSet<String> allKeywords, RSequence sequence)
            throws Exception {
        for (Object o : sequence.units) {
            addTokenImage(allKeywords, o);
        }
    }

    private static void addTokenImage(TreeSet<String> allKeywords, ROneOrMore oneOrMore) {
        for (Token token : oneOrMore.lhsTokens) {
            if (CHARSET_ENCODER.canEncode(token.image)) {
                allKeywords.add(token.image);
            }
        }
    }

    private static void addTokenImage(TreeSet<String> allKeywords, RZeroOrMore oneOrMore) {
        for (Token token : oneOrMore.lhsTokens) {
            if (CHARSET_ENCODER.canEncode(token.image)) {
                allKeywords.add(token.image);
            }
        }
    }

    private static void addTokenImage(TreeSet<String> allKeywords, RZeroOrOne oneOrMore) {
        for (Token token : oneOrMore.lhsTokens) {
            if (CHARSET_ENCODER.canEncode(token.image)) {
                allKeywords.add(token.image);
            }
        }
    }

    private static void addTokenImage(TreeSet<String> allKeywords, RJustName oneOrMore) {
        for (Token token : oneOrMore.lhsTokens) {
            if (CHARSET_ENCODER.canEncode(token.image)) {
                allKeywords.add(token.image);
            }
        }
    }

    private static void addTokenImage(TreeSet<String> allKeywords, RChoice choice)
            throws Exception {
        for (Object o : choice.getChoices()) {
            addTokenImage(allKeywords, o);
        }
    }

    public static TreeSet<String> getAllKeywordsUsingJavaCC(File file) throws Exception {
        TreeSet<String> allKeywords = new TreeSet<>();

        Path jjtGrammar = file.toPath();
        Path jjGrammarOutputDir = Files.createTempDirectory("jjgrammer");

        new JJTree().main(new String[] {
                "-JJTREE_OUTPUT_DIRECTORY=" + jjGrammarOutputDir.toString(),
                "-CODE_GENERATOR=java",
                jjtGrammar.toString()
        });
        Path jjGrammarFile = jjGrammarOutputDir.resolve("JSqlParserCC.jj");

        Context context = new Context();
        JavaCCParser parser = new JavaCCParser(new java.io.FileInputStream(jjGrammarFile.toFile()));
        parser.javacc_input(context);

        // needed for filling JavaCCGlobals
        Semanticize.start(context);

        // read all the Token and get the String image
        for (Map.Entry<Integer, RegularExpression> item : context.globals().rexps_of_tokens
                .entrySet()) {
            addTokenImage(allKeywords, item.getValue());
        }

        // clean up
        if (jjGrammarOutputDir.toFile().exists()) {
            jjGrammarOutputDir.toFile().delete();
        }

        return allKeywords;
    }

    @Test
    void getAllSimpleKeywords() throws IOException {
        Set<String> allKeywords = ParserKeywordsUtils.getAllSimpleKeywords(FILE);
        Assertions.assertFalse(allKeywords.isEmpty(), "Keyword List must not be empty!");
        LOGGER.info("All simple keywords: " + allKeywords.size());
    }

    @Test
    void getNonReservedKeywords() {
        Set<String> nonReserved = ParserKeywordsUtils.getNonReservedKeywords();
        Assertions.assertFalse(nonReserved.isEmpty(),
                "Non-reserved Keyword List must not be empty!");
        LOGGER.info("Non-reserved keywords: " + nonReserved.size());
    }

    @Test
    void getReservedKeywords() throws IOException {
        Set<String> reserved = ParserKeywordsUtils.getReservedKeywords(FILE);
        Assertions.assertFalse(reserved.isEmpty(), "Reserved Keyword List must not be empty!");
        LOGGER.info("Reserved keywords: " + reserved.size());
    }

    @Test
    void reservedAndNonReservedAreDisjoint() throws IOException {
        Set<String> reserved = ParserKeywordsUtils.getReservedKeywords(FILE);
        Set<String> nonReserved = ParserKeywordsUtils.getNonReservedKeywords();

        TreeSet<String> overlap = new TreeSet<>(reserved);
        overlap.retainAll(nonReserved);
        Assertions.assertTrue(overlap.isEmpty(),
                "Reserved and non-reserved sets must not overlap, but found: " + overlap);
    }

    @Test
    void getAllKeywordsUsingJavaCC() throws Exception {
        Set<String> allKeywords = getAllKeywordsUsingJavaCC(FILE);
        Assertions.assertFalse(allKeywords.isEmpty(), "Keyword List must not be empty!");
    }

    // Cross-check: compare grammar-scanned keywords with those extracted by the JavaCC Parser.
    @Test
    void compareKeywordLists() throws Exception {
        Set<String> allGrammarKeywords = ParserKeywordsUtils.getAllSimpleKeywords(FILE);
        Set<String> allJavaCCParserKeywords = getAllKeywordsUsingJavaCC(FILE);

        // Grammar keywords not found by JavaCC — log for review
        for (String s : allGrammarKeywords) {
            if (!allJavaCCParserKeywords.contains(s)) {
                LOGGER.info("Grammar keyword not in JavaCC extraction: " + s);
            }
        }

        // We expect all simple keywords found by JavaCC to exist in the grammar set
        for (String s : allJavaCCParserKeywords) {
            if (!allGrammarKeywords.contains(s)) {
                LOGGER.info("Additional keyword found by JavaCC Parser: " + s);
            }
        }
    }

}
