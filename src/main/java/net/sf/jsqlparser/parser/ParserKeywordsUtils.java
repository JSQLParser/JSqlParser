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

import org.javacc.jjtree.JJTree;
import org.javacc.parser.JavaCCGlobals;
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InvalidClassException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParserKeywordsUtils {
    public final static CharsetEncoder CHARSET_ENCODER = StandardCharsets.US_ASCII.newEncoder();

    public final static int RESTRICTED_FUNCTION = 1;
    public final static int RESTRICTED_SCHEMA = 2;
    public final static int RESTRICTED_TABLE = 4;
    public final static int RESTRICTED_COLUMN = 8;
    public final static int RESTRICTED_EXPRESSION = 16;
    public final static int RESTRICTED_ALIAS = 32;
    public final static int RESTRICTED_SQL2016 = 64;

    public final static int RESTRICTED_JSQLPARSER =  128
            | RESTRICTED_FUNCTION
            | RESTRICTED_SCHEMA
            | RESTRICTED_TABLE
            | RESTRICTED_COLUMN
            | RESTRICTED_EXPRESSION
            | RESTRICTED_ALIAS
            | RESTRICTED_SQL2016;


    @SuppressWarnings({"PMD.ExcessiveMethodLength"})
    public static List<String> getReservedKeywords(int restriction) {
        // Classification follows http://www.h2database.com/html/advanced.html#keywords
        Object[][] ALL_RESERVED_KEYWORDS = {
                { "ABSENT", RESTRICTED_JSQLPARSER }
                ,  { "ALL" , RESTRICTED_SQL2016 }
                ,  { "AND" , RESTRICTED_SQL2016 }
                ,  { "ANY" , RESTRICTED_JSQLPARSER }
                ,  { "AS" , RESTRICTED_SQL2016 }
                ,  { "BETWEEN" , RESTRICTED_SQL2016 }
                ,  { "BOTH" , RESTRICTED_SQL2016 }
                ,  { "CASEWHEN" , RESTRICTED_ALIAS }
                ,  { "CHECK" , RESTRICTED_SQL2016 }
                ,  { "CONNECT" , RESTRICTED_ALIAS }
                ,  { "CONNECT_BY_ROOT" , RESTRICTED_JSQLPARSER }
                ,  { "CONSTRAINT" , RESTRICTED_SQL2016 }
                ,  { "CREATE" , RESTRICTED_ALIAS }
                ,  { "CROSS" , RESTRICTED_SQL2016 }
                ,  { "CURRENT" , RESTRICTED_JSQLPARSER }
                ,  { "DISTINCT" , RESTRICTED_SQL2016 }
                ,  { "DOUBLE" , RESTRICTED_ALIAS }
                ,  { "ELSE" , RESTRICTED_JSQLPARSER }
                ,  { "EXCEPT" , RESTRICTED_SQL2016 }
                ,  { "EXISTS" , RESTRICTED_SQL2016 }
                ,  { "FETCH" , RESTRICTED_SQL2016 }
                ,  { "FOR" , RESTRICTED_SQL2016 }
                ,  { "FORCE" , RESTRICTED_SQL2016 }
                ,  { "FOREIGN" , RESTRICTED_SQL2016 }
                ,  { "FROM" , RESTRICTED_SQL2016 }
                ,  { "FULL", RESTRICTED_SQL2016 }
                ,  { "GROUP", RESTRICTED_SQL2016 }
                ,  { "GROUPING" , RESTRICTED_ALIAS }
                ,  { "HAVING" , RESTRICTED_SQL2016 }
                ,  { "IF" , RESTRICTED_SQL2016 }
                ,  { "IIF" , RESTRICTED_ALIAS }
                ,  { "IGNORE" , RESTRICTED_ALIAS }
                ,  { "ILIKE" , RESTRICTED_SQL2016 }
                ,  { "IN" , RESTRICTED_SQL2016 }
                ,  { "INNER" , RESTRICTED_SQL2016 }
                ,  { "INTERSECT" , RESTRICTED_SQL2016 }
                ,  { "INTERVAL", RESTRICTED_SQL2016 }
                ,  { "INTO" , RESTRICTED_JSQLPARSER }
                ,  { "IS" , RESTRICTED_SQL2016 }
                ,  { "JOIN" , RESTRICTED_JSQLPARSER }
                ,  { "LATERAL" , RESTRICTED_SQL2016 }
                ,  { "LEFT", RESTRICTED_SQL2016 }
                ,  { "LIKE" , RESTRICTED_SQL2016 }
                ,  { "LIMIT" , RESTRICTED_SQL2016 }
                ,  { "MINUS" , RESTRICTED_SQL2016 }
                ,  { "NATURAL" , RESTRICTED_SQL2016 }
                ,  { "NOCYCLE" , RESTRICTED_JSQLPARSER }
                ,  { "NOT", RESTRICTED_SQL2016 }
                ,  { "NULL" , RESTRICTED_SQL2016 }
                ,  { "OFFSET" , RESTRICTED_SQL2016 }
                ,  { "ON" , RESTRICTED_SQL2016 }
                ,  { "ONLY" , RESTRICTED_JSQLPARSER }
                ,  { "OPTIMIZE" , RESTRICTED_ALIAS }
                ,  { "OR" , RESTRICTED_SQL2016 }
                ,  { "ORDER" , RESTRICTED_SQL2016 }
                ,  { "OUTER" , RESTRICTED_JSQLPARSER }
                ,  { "OUTPUT" , RESTRICTED_JSQLPARSER }
                ,  { "OPTIMIZE ", RESTRICTED_JSQLPARSER }
                ,  { "PIVOT" , RESTRICTED_JSQLPARSER }
                ,  { "PROCEDURE" , RESTRICTED_ALIAS }
                ,  { "PUBLIC", RESTRICTED_ALIAS }
                ,  { "RECURSIVE" , RESTRICTED_SQL2016 }
                ,  { "REGEXP" , RESTRICTED_SQL2016 }
                ,  { "RETURNING" , RESTRICTED_JSQLPARSER }
                ,  { "RIGHT" , RESTRICTED_SQL2016 }
                ,  { "SEL" , RESTRICTED_ALIAS }
                ,  { "SELECT" , RESTRICTED_ALIAS }
                ,  { "SEMI" , RESTRICTED_JSQLPARSER }
                ,  { "SET" , RESTRICTED_JSQLPARSER }
                ,  { "SOME" , RESTRICTED_JSQLPARSER }
                ,  { "START" , RESTRICTED_JSQLPARSER }
                ,  { "TABLES" , RESTRICTED_ALIAS }
                ,  { "TOP" , RESTRICTED_SQL2016 }
                ,  { "TRAILING", RESTRICTED_SQL2016 }
                ,  { "UNBOUNDED" , RESTRICTED_JSQLPARSER }
                ,  { "UNION" , RESTRICTED_SQL2016 }
                ,  { "UNIQUE" , RESTRICTED_SQL2016 }
                ,  { "UNPIVOT" , RESTRICTED_JSQLPARSER }
                ,  { "USE" , RESTRICTED_JSQLPARSER }
                ,  { "USING" , RESTRICTED_SQL2016 }
                ,  { "SQL_CACHE" , RESTRICTED_JSQLPARSER }
                ,  { "SQL_CALC_FOUND_ROWS" , RESTRICTED_JSQLPARSER }
                ,  { "SQL_NO_CACHE" , RESTRICTED_JSQLPARSER }
                ,  { "STRAIGHT_JOIN" , RESTRICTED_JSQLPARSER }
                ,  { "VALUE", RESTRICTED_JSQLPARSER }
                ,  { "VALUES" , RESTRICTED_SQL2016 }
                ,  { "VARYING" , RESTRICTED_JSQLPARSER }
                ,  { "WHEN" , RESTRICTED_SQL2016 }
                ,  { "WHERE" , RESTRICTED_SQL2016 }
                ,  { "WINDOW" , RESTRICTED_SQL2016 }
                ,  { "WITH" , RESTRICTED_SQL2016 }
                ,  { "XOR", RESTRICTED_JSQLPARSER }
                ,  { "XMLSERIALIZE" , RESTRICTED_JSQLPARSER }

                // add keywords from the composite token definitions:
                // tk=<K_DATE_LITERAL> | tk=<K_DATETIMELITERAL> | tk=<K_STRING_FUNCTION_NAME>
                // we will use the composite tokens instead, which are always hit first before the simple keywords
                // @todo: figure out a way to remove these composite tokens, as they do more harm than good
                , { "SEL", RESTRICTED_JSQLPARSER }
                , { "SELECT", RESTRICTED_JSQLPARSER }

                , { "DATE", RESTRICTED_JSQLPARSER }
                , { "TIME" , RESTRICTED_JSQLPARSER }
                , { "TIMESTAMP", RESTRICTED_JSQLPARSER }

                , { "YEAR", RESTRICTED_JSQLPARSER }
                , { "MONTH", RESTRICTED_JSQLPARSER }
                , { "DAY", RESTRICTED_JSQLPARSER }
                , { "HOUR", RESTRICTED_JSQLPARSER }
                , { "MINUTE" , RESTRICTED_JSQLPARSER }
                , { "SECOND", RESTRICTED_JSQLPARSER }

                , { "SUBSTR", RESTRICTED_JSQLPARSER }
                , { "SUBSTRING", RESTRICTED_JSQLPARSER }
                , { "TRIM", RESTRICTED_JSQLPARSER }
                , { "POSITION", RESTRICTED_JSQLPARSER }
                , { "OVERLAY", RESTRICTED_JSQLPARSER }

                , { "NEXTVAL", RESTRICTED_JSQLPARSER }

                , { "AUTO", RESTRICTED_JSQLPARSER }
                , { "REFRESH", RESTRICTED_JSQLPARSER }
                , { "YES", RESTRICTED_JSQLPARSER }
                , { "NO", RESTRICTED_JSQLPARSER }

                //@todo: Object Names should not start with Hex-Prefix, we shall not find that Token
                , { "0x", RESTRICTED_JSQLPARSER }
        };

        ArrayList<String> keywords = new ArrayList<>();
        for (Object[] data : ALL_RESERVED_KEYWORDS) {
            int value = (int) data[1];

            // test if bit is not set
            if ( (value & restriction) == restriction
                    || (restriction & value) == value ) {
                keywords.add((String) data[0]);
            }
        }

        return keywords;
    }

    public static void main(String[] args) throws Exception {
        if (args.length<1) {
            throw new IllegalArgumentException("No filename provided as parameters ARGS[0]");
        }

        File file = new File(args[0]);
        if (file.exists() && file.canRead()) {
            buildGrammarForRelObjectName(file);
            buildGrammarForRelObjectNameWithoutValue(file);
        } else {
            throw new FileNotFoundException("Can't read file " + args[0]);
        }
    }

    public static TreeSet<String> getAllKeywordsUsingRegex(File file) throws IOException {
        Pattern tokenBlockPattern = Pattern.compile("TOKEN\\s*:\\s*(?:/\\*.*\\*/*)\\n\\{(?:[^\\}\\{]+|\\{(?:[^\\}\\{]+|\\{[^\\}\\{]*\\})*\\})*\\}", Pattern.MULTILINE);
        Pattern tokenStringValuePattern = Pattern.compile("\\\"(\\w{2,})\\\"", Pattern.MULTILINE);

        TreeSet<String> allKeywords = new TreeSet<>();

        Path path = file.toPath();
        Charset charset = Charset.defaultCharset();
        String content = new String(Files.readAllBytes(path), charset);

        Matcher tokenBlockmatcher = tokenBlockPattern.matcher(content);
        while (tokenBlockmatcher.find()) {
            String tokenBlock = tokenBlockmatcher.group(0);
            Matcher tokenStringValueMatcher= tokenStringValuePattern.matcher(tokenBlock);
            while (tokenStringValueMatcher.find()) {
                String tokenValue=tokenStringValueMatcher.group(1);
                // test if pure US-ASCII
                if (CHARSET_ENCODER.canEncode(tokenValue) && tokenValue.matches("[A-Za-z]+")) {
                    allKeywords.add(tokenValue);
                }
            }
        }
        return allKeywords;
    }

    private static void addTokenImage(TreeSet<String> allKeywords, RStringLiteral literal) {
        if (CHARSET_ENCODER.canEncode(literal.image) && literal.image.matches("[A-Za-z]+")) {
            allKeywords.add(literal.image);
        }
    }

    @SuppressWarnings({"PMD.EmptyIfStmt", "PMD.CyclomaticComplexity"})
    private static void addTokenImage(TreeSet<String> allKeywords, Object o) throws Exception {
        if (o instanceof RStringLiteral) {
            RStringLiteral literal = (RStringLiteral) o;
            addTokenImage(allKeywords, literal);
        } else  if (o instanceof RChoice) {
            RChoice choice = (RChoice) o;
            addTokenImage(allKeywords, choice);
        } else if (o instanceof RSequence) {
            RSequence sequence1 = (RSequence) o;
            addTokenImage(allKeywords, sequence1);
        } else if (o  instanceof ROneOrMore) {
            ROneOrMore oneOrMore = (ROneOrMore) o ;
            addTokenImage(allKeywords, oneOrMore);
        } else if (o  instanceof RZeroOrMore) {
            RZeroOrMore zeroOrMore = (RZeroOrMore) o ;
            addTokenImage(allKeywords, zeroOrMore);
        } else if (o  instanceof RZeroOrOne) {
            RZeroOrOne zeroOrOne = (RZeroOrOne) o ;
            addTokenImage(allKeywords, zeroOrOne);
        }  else if (o  instanceof RJustName) {
            RJustName zeroOrOne = (RJustName) o ;
            addTokenImage(allKeywords, zeroOrOne);
        } else if (o  instanceof RCharacterList) {
           // do nothing, we are not interested in those
        } else {
            throw new InvalidClassException("Unknown Type: " + o.getClass().getName() + " " + o.toString());
        }
    }

    private static void addTokenImage(TreeSet<String> allKeywords, RSequence sequence) throws Exception {
        for (Object o: sequence.units) {
            addTokenImage(allKeywords, o);
        }
    }

    private static void addTokenImage(TreeSet<String> allKeywords, ROneOrMore oneOrMore) {
        for (Token token: oneOrMore.lhsTokens) {
            if (CHARSET_ENCODER.canEncode(token.image)) {
                allKeywords.add(token.image);
            }
        }
    }

    private static void addTokenImage(TreeSet<String> allKeywords, RZeroOrMore oneOrMore) {
        for (Token token: oneOrMore.lhsTokens) {
            if (CHARSET_ENCODER.canEncode(token.image)) {
                allKeywords.add(token.image);
            }
        }
    }

    private static void addTokenImage(TreeSet<String> allKeywords, RZeroOrOne oneOrMore) {
        for (Token token: oneOrMore.lhsTokens) {
            if (CHARSET_ENCODER.canEncode(token.image)) {
                allKeywords.add(token.image);
            }
        }
    }

    private static void addTokenImage(TreeSet<String> allKeywords, RJustName oneOrMore) {
        for (Token token: oneOrMore.lhsTokens) {
            if (CHARSET_ENCODER.canEncode(token.image)) {
                allKeywords.add(token.image);
            }
        }
    }

    private static void addTokenImage(TreeSet<String> allKeywords, RChoice choice) throws  Exception {
        for (Object o: choice.getChoices()) {
            addTokenImage(allKeywords, o);
        }
    }

    public static TreeSet<String> getAllKeywordsUsingJavaCC(File file) throws Exception {
        TreeSet<String> allKeywords = new TreeSet<>();

        Path jjtGrammar = file.toPath();
        Path jjGrammarOutputDir = Files.createTempDirectory("jjgrammer");

        new JJTree().main(new String[]{
                "-JDK_VERSION=1.8",
                "-OUTPUT_DIRECTORY=" + jjGrammarOutputDir.toString(),
                jjtGrammar.toString()
        });
        Path jjGrammarFile = jjGrammarOutputDir.resolve("JSqlParserCC.jj");

        JavaCCParser parser = new JavaCCParser(new java.io.FileInputStream(jjGrammarFile.toFile()));
        parser.javacc_input();

        // needed for filling JavaCCGlobals
        Semanticize.start();

        // read all the Token and get the String image
        for (Map.Entry<Integer, RegularExpression> item : JavaCCGlobals.rexps_of_tokens.entrySet()) {
            addTokenImage(allKeywords, item.getValue());
        }

        //clean up
        if (jjGrammarOutputDir.toFile().exists()) {
            jjGrammarOutputDir.toFile().delete();
        }

        return allKeywords;
    }

    public static void buildGrammarForRelObjectNameWithoutValue(File file) throws Exception {
        Pattern methodBlockPattern = Pattern.compile("String\\W*RelObjectNameWithoutValue\\W*\\(\\W*\\)\\W*:\\s*\\{(?:[^}{]+|\\{(?:[^}{]+|\\{[^}{]*})*})*}\\s*\\{(?:[^}{]+|\\{(?:[^}{]+|\\{[^}{]*})*})*}", Pattern.MULTILINE);

        TreeSet<String> allKeywords = getAllKeywords(file);

        for (String reserved: getReservedKeywords(RESTRICTED_JSQLPARSER)) {
            allKeywords.remove(reserved);
        }

        StringBuilder builder = new StringBuilder();
        builder.append("String RelObjectNameWithoutValue() :\n"
                       + "{    Token tk = null; }\n"
                       + "{\n"
                       //@todo: find a way to avoid those hardcoded compound tokens
                       + "    ( tk=<S_IDENTIFIER> | tk=<S_QUOTED_IDENTIFIER> |  tk=<K_DATE_LITERAL> | tk=<K_DATETIMELITERAL> | tk=<K_STRING_FUNCTION_NAME> | tk=<K_ISOLATION> | tk=<K_TIME_KEY_EXPR> \n"
                       + "      ");

        for (String keyword: allKeywords) {
            builder.append(" | tk=\"").append(keyword).append("\"");
        }

        builder.append(" )\n"
                       + "    { return tk.image; }\n"
                       + "}");

        replaceInFile(file, methodBlockPattern, builder.toString());
    }

    public static void buildGrammarForRelObjectName(File file) throws Exception {
        // Pattern pattern = Pattern.compile("String\\W*RelObjectName\\W*\\(\\W*\\)\\W*:\\s*\\{(?:[^}{]+|\\{(?:[^}{]+|\\{[^}{]*})*})*}\\s*\\{(?:[^}{]+|\\{(?:[^}{]+|\\{[^}{]*})*})*}", Pattern.MULTILINE);
        TreeSet<String> allKeywords = new TreeSet<>();
        for (String reserved: getReservedKeywords(RESTRICTED_ALIAS)) {
            allKeywords.add(reserved);
        }

        for (String reserved: getReservedKeywords(RESTRICTED_JSQLPARSER & ~RESTRICTED_ALIAS)) {
            allKeywords.remove(reserved);
        }

        StringBuilder builder = new StringBuilder();
        builder.append("String RelObjectName() :\n"
                       + "{    Token tk = null; String result = null; }\n"
                       + "{\n"
                       + "    (result = RelObjectNameWithoutValue()\n"
                       + "      ");

        for (String keyword: allKeywords) {
            builder.append(" | tk=\"").append(keyword).append("\"");
        }

        builder.append(" )\n"
                       + "    { return tk!=null ? tk.image : result; }\n"
                       + "}");

        // @todo: Needs fine-tuning, we are not replacing this part yet
        // replaceInFile(file, pattern, builder.toString());
    }

    public static TreeSet<String> getAllKeywords(File file) throws Exception {
        return getAllKeywordsUsingJavaCC(file);
    }

    private static void replaceInFile(File file, Pattern pattern, String replacement) throws IOException {
        Path path = file.toPath();
        Charset charset = Charset.defaultCharset();

        String content = new String(Files.readAllBytes(path), charset);
        content = pattern.matcher(content).replaceAll(replacement);
        Files.write(file.toPath(), content.getBytes(charset));
    }
}
