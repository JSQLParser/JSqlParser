package net.sf.jsqlparser.test.select;

import junit.framework.*;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.*;
import static net.sf.jsqlparser.test.TestUtils.*;

public class SubstringTest extends TestCase {
    public SubstringTest(String arg0) {
        super(arg0);
    }
    
    public void testInWhere() throws Exception {
        final String query = "SELECT foo FROM bar WHERE SUBSTRING(foo FROM 1 FOR 10) LIKE 'A%'";
        assertSqlCanBeParsedAndDeparsed(query);
    }
    
    public void testInOrderBy() throws Exception {
        final String query = "SELECT foo FROM bar ORDER BY SUBSTRING(foo FROM 12 FOR 2)";
        assertSqlCanBeParsedAndDeparsed(query);
    }

    public void testOnlyFrom() throws Exception {
        final String query = "SELECT SUBSTRING(foo FROM 5) AS sub FROM bar";
        assertSqlCanBeParsedAndDeparsed(query);
    }

    public void testOnlyFor() throws Exception {
        final String query = "SELECT SUBSTRING(foo FOR 2) AS sub FROM bar";
        assertSqlCanBeParsedAndDeparsed(query);
    }
    
    public void testWithString() throws Exception {
        final String query = "SELECT SUBSTRING('free text to split' FROM 6 FOR 4) AS foo FROM bar";
        assertSqlCanBeParsedAndDeparsed(query);
    }

    public void testWithToSyntax() throws Exception {
        final String query = "SELECT SUBSTRING(foo FROM 6 TO 7) AS sub FROM bar";

        try {
        	assertSqlCanBeParsedAndDeparsed(query);

        	fail("Function SUBSTRING must reject wrong syntax with '... FROM ... TO ...'");
        } catch (JSQLParserException e) {
        }
    }
    
    public void testCommaSyntax() throws Exception {
        final String query = "SELECT SUBSTRING(foo, 6, 4) AS sub FROM bar";
        assertSqlCanBeParsedAndDeparsed(query);
    }
}
