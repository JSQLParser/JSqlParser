/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.drop;

import java.io.StringReader;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import static net.sf.jsqlparser.test.TestUtils.*;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class DropTest {

    private final CCJSqlParserManager parserManager = new CCJSqlParserManager();

    @Test
    public void testDrop() throws JSQLParserException {
        String statement = "DROP TABLE mytab";
        Drop drop = (Drop) parserManager.parse(new StringReader(statement));
        assertEquals("TABLE", drop.getType());
        assertEquals("mytab", drop.getName().getFullyQualifiedName());
        assertEquals(statement, "" + drop);

        statement = "DROP INDEX myindex CASCADE";
        drop = (Drop) parserManager.parse(new StringReader(statement));
        assertEquals("INDEX", drop.getType());
        assertEquals("myindex", drop.getName().getFullyQualifiedName());
        assertEquals("CASCADE", drop.getParameters().get(0));
        assertEquals(statement, "" + drop);
    }

    @Test
    public void testDrop2() throws JSQLParserException {
        Drop drop = (Drop) parserManager.parse(new StringReader("DROP TABLE \"testtable\""));
        assertEquals("TABLE", drop.getType());
        assertEquals("\"testtable\"", drop.getName().getFullyQualifiedName());
    }

    @Test
    public void testDropIfExists() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("DROP TABLE IF EXISTS my_table");
    }

    @Test
    public void testDropRestrictIssue510() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("DROP TABLE TABLE2 RESTRICT");
    }

    @Test
    public void testDropViewIssue545() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("DROP VIEW myview");
    }

    @Test
    public void testDropViewIssue545_2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("DROP VIEW IF EXISTS myview");
    }

    @Test
    public void testDropSchemaIssue855() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("DROP SCHEMA myschema");
    }
}
