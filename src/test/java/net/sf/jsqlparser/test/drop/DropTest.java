package net.sf.jsqlparser.test.drop;

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.junit.Assert.assertEquals;

import java.io.StringReader;

import org.junit.Test;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.drop.Drop;

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
}
