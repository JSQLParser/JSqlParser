/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserConstants;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.ParseException;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class DdlParameterListTest {
    @ParameterizedTest
    @ValueSource(strings = {
            "CREATE VECTOR INDEX items_hnsw ON items (embedding) ORGANIZATION INMEMORY NEIGHBOR GRAPH DISTANCE COSINE WITH TARGET ACCURACY 95 PARAMETERS (TYPE HNSW,NEIGHBORS 32,EFCONSTRUCTION 500)",
            "CREATE VECTOR INDEX items_ivf ON items (embedding) ORGANIZATION NEIGHBOR PARTITIONS DISTANCE COSINE WITH TARGET ACCURACY 95 PARAMETERS (TYPE IVF,NEIGHBOR PARTITIONS 10)",
            "CREATE TABLE samples (id INT) STORAGE (INITIAL 65536,NEXT 131072)"})
    void ddlParametersKeepSeparateWordsAndNumbers(String sql) throws JSQLParserException {
        for (Dialect dialect : new Dialect[] {null, Dialect.ORACLE}) {
            Statement statement = CCJSqlParserUtil.parse(sql, parser -> {
                if (dialect != null) {
                    parser.withDialect(dialect);
                }
            });
            assertEquals(sql, statement.toString());
            StringBuilder output = new StringBuilder();
            statement.accept(new StatementDeParser(output), null);
            assertEquals(sql, output.toString());
            assertEquals(sql, CCJSqlParserUtil.parse(output.toString()).toString());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"()", "(1,2,3)", "(name=value,count=32)",
            "(TYPE HNSW,NEIGHBORS 32,EFCONSTRUCTION 500)",
            "(enabled TRUE,ratio 0.5,disabled FALSE)",
            "('keep  two spaces','comma,equals=','it''s fine')",
            "(\"Quoted Name\" 10,\"dotted.name\" 20)"})
    void preservesLiteralsQuotedNamesAndExplicitDelimiters(String list) throws ParseException {
        CCJSqlParser parser = CCJSqlParserUtil.newParser(list);
        assertEquals(list, parser.AList());
        assertEquals(CCJSqlParserConstants.EOF, parser.getNextToken().kind);
    }
}
