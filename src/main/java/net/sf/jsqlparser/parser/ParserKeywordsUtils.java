/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2021 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.parser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utilities for querying the parser's reserved and non-reserved keyword sets.
 *
 * <p>
 * <b>Non-reserved keywords</b> are derived from the generated {@link CCJSqlParserConstants} token
 * table using the {@code MIN_NON_RESERVED_WORD} / {@code MAX_NON_RESERVED_WORD} sentinels.
 *
 * <p>
 * <b>Reserved keywords</b> are determined by scanning the Grammar file for all simple string token
 * definitions ({@code <K_NAME: "VALUE">}) and subtracting the non-reserved set.
 */
public class ParserKeywordsUtils {

    private static final CharsetEncoder ASCII_ENCODER = StandardCharsets.US_ASCII.newEncoder();

    /** Matches a pure keyword image: word characters, at least two characters, pure US-ASCII. */
    private static final Pattern KEYWORD_PATTERN = Pattern.compile("[A-Za-z_][A-Za-z_0-9]+");

    /**
     * Matches simple token definitions in the grammar: {@code <K_NAME: "VALUE">}. Group 1 captures
     * the string value. Only matches definitions where the value is a plain quoted string —
     * compound regex tokens like {@code <K_DATETIMELITERAL: ...>} won't match.
     */
    private static final Pattern SIMPLE_TOKEN_PATTERN =
            Pattern.compile("<K_\\w+:\\s*\"(\\w+)\"\\s*>", Pattern.MULTILINE);

    private ParserKeywordsUtils() {
        // utility class
    }

    /**
     * Returns the set of <b>non-reserved</b> keywords, i.e.&nbsp;tokens whose kind lies between
     * {@code MIN_NON_RESERVED_WORD} and {@code MAX_NON_RESERVED_WORD}. These keywords can be used
     * as unquoted identifiers.
     */
    public static TreeSet<String> getNonReservedKeywords() {
        TreeSet<String> keywords = new TreeSet<>();
        String[] images = CCJSqlParserConstants.tokenImage;

        for (int kind = CCJSqlParserConstants.MIN_NON_RESERVED_WORD
                + 1; kind < CCJSqlParserConstants.MAX_NON_RESERVED_WORD; kind++) {
            String image = extractKeyword(images[kind]);
            if (image != null && isKeywordImage(image)) {
                keywords.add(image);
            }
        }
        return keywords;
    }

    /**
     * Returns the set of <b>reserved</b> keywords by scanning the Grammar file for all simple
     * string token definitions and subtracting the non-reserved keywords.
     *
     * @param grammarFile the {@code .jjt} grammar file
     * @return reserved keyword strings
     * @throws IOException if reading the grammar file fails
     */
    public static TreeSet<String> getReservedKeywords(File grammarFile) throws IOException {
        TreeSet<String> allSimple = getAllSimpleKeywords(grammarFile);
        allSimple.removeAll(getNonReservedKeywords());
        return allSimple;
    }

    /**
     * Returns all simple string keywords defined in the grammar file. Scans for
     * {@code <K_NAME: "VALUE">} patterns and collects the string values.
     *
     * @param grammarFile the {@code .jjt} grammar file
     * @return all simple keyword strings
     * @throws IOException if reading the grammar file fails
     */
    public static TreeSet<String> getAllSimpleKeywords(File grammarFile) throws IOException {
        TreeSet<String> keywords = new TreeSet<>();
        String content = Files.readString(grammarFile.toPath(), StandardCharsets.UTF_8);

        Matcher matcher = SIMPLE_TOKEN_PATTERN.matcher(content);
        while (matcher.find()) {
            String value = matcher.group(1);
            if (isKeywordImage(value) && ASCII_ENCODER.canEncode(value)) {
                keywords.add(value);
            }
        }
        return keywords;
    }

    /**
     * Checks whether the given token kind is a non-reserved keyword that can be used as an unquoted
     * identifier.
     */
    public static boolean isNonReservedKeyword(int tokenKind) {
        return tokenKind > CCJSqlParserConstants.MIN_NON_RESERVED_WORD
                && tokenKind < CCJSqlParserConstants.MAX_NON_RESERVED_WORD;
    }

    /**
     * Writes a reStructuredText documentation file listing all reserved keywords.
     *
     * @param grammarFile the {@code .jjt} grammar file
     * @param rstFile the output {@code .rst} file to write
     * @throws IOException if reading or writing fails
     */
    public static void writeKeywordsDocumentationFile(File grammarFile, File rstFile)
            throws IOException {
        TreeSet<String> reserved = getReservedKeywords(grammarFile);

        StringBuilder builder = new StringBuilder();
        builder.append("***********************\n");
        builder.append("Reserved Keywords\n");
        builder.append("***********************\n");
        builder.append("\n");

        builder.append(
                "The following Keywords are **reserved** in JSQLParser-|JSQLPARSER_VERSION| and must not be used for **Naming Objects**: \n");
        builder.append("\n");

        builder.append("+---------------------------+\n");
        builder.append("| **Keyword**               |\n");
        builder.append("+---------------------------+\n");

        for (String keyword : reserved) {
            builder.append("| ").append(rightPadding(keyword, ' ', 25)).append(" |\n");
            builder.append("+---------------------------+\n");
        }

        try (FileWriter fileWriter = new FileWriter(rstFile)) {
            fileWriter.append(builder);
            fileWriter.flush();
        }
    }

    public static String rightPadding(String input, char ch, int length) {
        return String.format("%" + (-length) + "s", input).replace(' ', ch);
    }

    /**
     * Entry point for the {@code updateKeywords} Gradle/Maven task.
     *
     * <p>
     * Usage: {@code java net.sf.jsqlparser.parser.ParserKeywordsUtils <grammar.jjt> <keywords.rst>}
     *
     * @param args {@code args[0]}: path to the grammar file, {@code args[1]}: path to the output
     *        RST file
     * @throws Exception if reading or writing fails
     */
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            throw new IllegalArgumentException(
                    "Usage: ParserKeywordsUtils <grammar.jjt> <keywords.rst>");
        }

        File grammarFile = new File(args[0]);
        if (!grammarFile.canRead()) {
            throw new IOException("Cannot read grammar file: " + grammarFile);
        }

        File rstFile = new File(args[1]);
        rstFile.getParentFile().mkdirs();
        writeKeywordsDocumentationFile(grammarFile, rstFile);

        System.out.println("Reserved keywords: " + getReservedKeywords(grammarFile).size());
        System.out.println("Non-reserved keywords: " + getNonReservedKeywords().size());
        System.out.println("Written to: " + rstFile.getAbsolutePath());
    }

    /**
     * Extracts the keyword string from a {@code tokenImage} entry.
     *
     * <p>
     * JavaCC renders inline BNF token declarations as {@code <K_ACTION>} in {@code tokenImage}.
     * Stripping the {@code K_} prefix and angle brackets yields the keyword string.
     *
     * @return the keyword string, or {@code null} if the entry is not a {@code K_} token
     */
    private static String extractKeyword(String tokenImage) {
        if (tokenImage == null || tokenImage.length() < 5) {
            return null;
        }

        // Format: <K_ACTION> → ACTION
        if (tokenImage.charAt(0) == '<'
                && tokenImage.charAt(tokenImage.length() - 1) == '>'
                && tokenImage.startsWith("<K_")) {
            return tokenImage.substring(3, tokenImage.length() - 1);
        }

        // Format: "ACTION" (TOKEN block with quoted string)
        if (tokenImage.charAt(0) == '"'
                && tokenImage.charAt(tokenImage.length() - 1) == '"') {
            return tokenImage.substring(1, tokenImage.length() - 1);
        }

        return null;
    }

    /**
     * Returns {@code true} if the image looks like a SQL keyword: alphabetic start, word characters
     * only, at least 2 characters, pure US-ASCII.
     */
    private static boolean isKeywordImage(String image) {
        return KEYWORD_PATTERN.matcher(image).matches()
                && ASCII_ENCODER.canEncode(image);
    }
}
