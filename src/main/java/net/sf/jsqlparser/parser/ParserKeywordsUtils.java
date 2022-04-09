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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;

public class ParserKeywordsUtils {
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

    public static List<String> getDefinedKeywords() throws Exception {
        return CCJSqlParser.getDefinedKeywords();
    }

    public static List<String> getReservedKeywords(int restriction) {
        return CCJSqlParser.getReservedKeywords(restriction);
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

    public static void buildGrammarForRelObjectNameWithoutValue(File file) throws Exception {
        Pattern pattern = Pattern.compile("String\\W*RelObjectNameWithoutValue\\W*\\(\\W*\\)\\W*:\\s*\\{(?:[^}{]+|\\{(?:[^}{]+|\\{[^}{]*})*})*}\\s*\\{(?:[^}{]+|\\{(?:[^}{]+|\\{[^}{]*})*})*}", Pattern.MULTILINE);

        TreeSet<String> allKeywords = new TreeSet<>();
        allKeywords.addAll(CCJSqlParser.getDefinedKeywords());
        for (String reserved: CCJSqlParser.getReservedKeywords(RESTRICTED_JSQLPARSER)) {
            allKeywords.remove(reserved);
        }

        StringBuilder builder = new StringBuilder();
        builder.append("String RelObjectNameWithoutValue() :\n"
                       + "{    Token tk = null; }\n"
                       + "{\n"
                       + "    ( tk=<S_IDENTIFIER> | tk=<S_QUOTED_IDENTIFIER> |  tk=<K_DATE_LITERAL> | tk=<K_DATETIMELITERAL> | tk=<K_STRING_FUNCTION_NAME>\n"
                       + "      ");

        for (String keyword: allKeywords) {
            builder.append(" | tk=\"").append(keyword).append("\"");
        }

        builder.append(" )\n"
                       + "    { return tk.image; }\n"
                       + "}");

        replaceInFile(file, pattern, builder.toString());
    }

    public static void buildGrammarForRelObjectName(File file) throws Exception {
        // Pattern pattern = Pattern.compile("String\\W*RelObjectName\\W*\\(\\W*\\)\\W*:\\s*\\{(?:[^}{]+|\\{(?:[^}{]+|\\{[^}{]*})*})*}\\s*\\{(?:[^}{]+|\\{(?:[^}{]+|\\{[^}{]*})*})*}", Pattern.MULTILINE);
        TreeSet<String> allKeywords = new TreeSet<>();
        for (String reserved: CCJSqlParser.getReservedKeywords(RESTRICTED_ALIAS)) {
            allKeywords.add(reserved);
        }

        for (String reserved: CCJSqlParser.getReservedKeywords(RESTRICTED_JSQLPARSER & ~RESTRICTED_ALIAS)) {
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

    private static void replaceInFile(File file, Pattern pattern, String replacement) throws IOException {
        Path path = file.toPath();
        Charset charset = Charset.defaultCharset();

        String content = new String(Files.readAllBytes(path), charset);
        content = pattern.matcher(content).replaceAll(replacement);
        Files.write(file.toPath(), content.getBytes(charset));
    }
}
