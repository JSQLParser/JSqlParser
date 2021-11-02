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

import static net.sf.jsqlparser.test.TestUtils.*;

import static org.junit.Assert.assertEquals;

import java.io.StringReader;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import org.junit.Test;

public class DropTest {

    private final CCJSqlParserManager parserManager = new CCJSqlParserManager();

    @Test
    public void testDrop() throws JSQLParserException {
        String statement = "DROP TABLE mytab";
        Drop parsed = (Drop) parserManager.parse(new StringReader(statement));
        assertEquals("TABLE", parsed.getType());
        assertEquals("mytab", parsed.getName().getFullyQualifiedName());
        assertEquals(statement, "" + parsed);
        Drop created = new Drop().withType("TABLE").withName(new Table("mytab"));
        assertDeparse(created, statement);
        assertEqualsObjectTree(parsed, created);
    }

    @Test
    public void testDropIndex() throws JSQLParserException {
        String statement = "DROP INDEX myindex CASCADE";
        Drop parsed = (Drop) parserManager.parse(new StringReader(statement));
        assertEquals("INDEX", parsed.getType());
        assertEquals("myindex", parsed.getName().getFullyQualifiedName());
        assertEquals("CASCADE", parsed.getParameters().get(0));
        assertEquals(statement, "" + parsed);
        Drop created = new Drop().withType("INDEX").withName(new Table("myindex")).addParameters("CASCADE");
        assertDeparse(created, statement);
        assertEqualsObjectTree(parsed, created);
    }

    @Test
    public void testDrop2() throws JSQLParserException {
        Drop drop = (Drop) parserManager.parse(new StringReader("DROP TABLE \"testtable\""));
        assertEquals("TABLE", drop.getType());
        assertEquals("\"testtable\"", drop.getName().getFullyQualifiedName());
    }

    @Test
    public void testDropIfExists() throws JSQLParserException {
        String statement = "DROP TABLE IF EXISTS my_table";
        Statement parsed = assertSqlCanBeParsedAndDeparsed(statement);
        Drop created = new Drop().withType("TABLE").withIfExists(true).withName(new Table("my_table"));
        assertDeparse(created, statement);
        assertEqualsObjectTree(parsed, created);
    }
    
    @Test
    public void testDropRestrictIssue510() throws JSQLParserException {
        String statement = "DROP TABLE TABLE2 RESTRICT";
        Statement parsed = assertSqlCanBeParsedAndDeparsed(statement);
        Drop created = new Drop().withType("TABLE").withName(new Table("TABLE2")).addParameters(asList("RESTRICT"));
        assertDeparse(created, statement);
        assertEqualsObjectTree(parsed, created);
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

    @Test
    public void testDropSequence() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("DROP SEQUENCE mysequence");
    }

    @Test
    public void testOracleMultiColumnDrop() throws JSQLParserException {
        //assertSqlCanBeParsedAndDeparsed("ALTER TABLE foo DROP (bar, baz)");
        assertSqlCanBeParsedAndDeparsed("ALTER TABLE foo DROP (bar, baz) CASCADE");
    }
}
