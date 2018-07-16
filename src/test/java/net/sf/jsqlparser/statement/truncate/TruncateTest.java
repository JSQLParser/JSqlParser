package net.sf.jsqlparser.statement.truncate;

import java.io.StringReader;

import static net.sf.jsqlparser.test.TestUtils.*;
import net.sf.jsqlparser.*;

import net.sf.jsqlparser.parser.CCJSqlParserManager;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class TruncateTest {

    private CCJSqlParserManager parserManager = new CCJSqlParserManager();

    @Test
    public void testTruncate() throws Exception {
        String statement = "TRUncATE TABLE myschema.mytab";
        Truncate truncate = (Truncate) parserManager.parse(new StringReader(statement));
        assertEquals("myschema", truncate.getTable().getSchemaName());
        assertEquals("myschema.mytab", truncate.getTable().getFullyQualifiedName());
        assertEquals(statement.toUpperCase(), truncate.toString().toUpperCase());

        statement = "TRUncATE   TABLE    mytab";
        String toStringStatement = "TRUncATE TABLE mytab";
        truncate = (Truncate) parserManager.parse(new StringReader(statement));
        assertEquals("mytab", truncate.getTable().getName());
        assertEquals(toStringStatement.toUpperCase(), truncate.toString().toUpperCase());

        statement = "TRUNCATE TABLE mytab CASCADE";
        truncate = (Truncate) parserManager.parse(new StringReader(statement));
        assertEquals(statement, truncate.toString());
    }

    @Test
    public void testTruncateDeparse() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("TRUNCATE TABLE foo");
    }

    @Test
    public void testTruncateCascadeDeparse() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("TRUNCATE TABLE foo CASCADE");
    }

}
