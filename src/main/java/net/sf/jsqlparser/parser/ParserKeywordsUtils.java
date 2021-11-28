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

import java.util.*;

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

    public final static void main(String[] args) {
        try {
            System.out.println( buildGrammarForRelObjectNameWithoutValue() );
            System.out.println( buildGrammarForRelObjectName() );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String buildGrammarForRelObjectNameWithoutValue() throws Exception {
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

        return builder.toString();
    }

    public static String buildGrammarForRelObjectName() throws Exception {
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

        return builder.toString();
    }
}
