package net.sf.jsqlparser.test.truncate;

import static org.junit.Assert.assertEquals;

import java.io.StringReader;

import org.junit.Test;

import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.truncate.Truncate;

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
    }
}
