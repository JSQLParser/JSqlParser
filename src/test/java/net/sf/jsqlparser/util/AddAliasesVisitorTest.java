/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.select.Select;
import org.junit.jupiter.api.Test;

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AddAliasesVisitorTest {

    private final CCJSqlParserManager parserManager = new CCJSqlParserManager();

    /**
     * Test of visit method, of class AddAliasesVisitor.
     */
    @Test
    public void testVisit_PlainSelect() throws JSQLParserException {
        String sql = "select a,b,c from test";
        Select select = (Select) parserManager.parse(new StringReader(sql));
        final AddAliasesVisitor<Void> instance = new AddAliasesVisitor<>();
        select.accept(instance, null);

        assertEquals("SELECT a AS A1, b AS A2, c AS A3 FROM test", select.toString());
    }

    @Test
    public void testVisit_PlainSelect_duplicates() throws JSQLParserException {
        String sql = "select a,b as a1,c from test";
        Select select = (Select) parserManager.parse(new StringReader(sql));
        final AddAliasesVisitor<Void> instance = new AddAliasesVisitor<>();
        select.accept(instance, null);

        assertEquals("SELECT a AS A2, b AS a1, c AS A3 FROM test", select.toString());
    }

    @Test
    public void testVisit_PlainSelect_expression() throws JSQLParserException {
        String sql = "select 3+4 from test";
        Select select = (Select) parserManager.parse(new StringReader(sql));
        final AddAliasesVisitor<Void> instance = new AddAliasesVisitor<>();
        select.accept(instance, null);

        assertEquals("SELECT 3 + 4 AS A1 FROM test", select.toString());
    }

    /**
     * Test of visit method, of class AddAliasesVisitor.
     */
    @Test
    public void testVisit_SetOperationList() throws JSQLParserException {
        String sql = "select 3+4 from test union select 7+8 from test2";
        Select setOpList = (Select) parserManager.parse(new StringReader(sql));
        final AddAliasesVisitor<Void> instance = new AddAliasesVisitor<>();
        setOpList.accept(instance, null);

        assertEquals("SELECT 3 + 4 AS A1 FROM test UNION SELECT 7 + 8 AS A1 FROM test2",
                setOpList.toString());
    }
}
