package net.sf.jsqlparser.statement.create;

import java.io.StringReader;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import static net.sf.jsqlparser.test.TestUtils.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

/**
 * @author Raymond Augé
 */
public class CreateIndexTest {

    private final CCJSqlParserManager parserManager = new CCJSqlParserManager();

    @Test
    public void testCreateIndex() throws JSQLParserException {
        String statement = "CREATE INDEX myindex ON mytab (mycol, mycol2)";
        CreateIndex createIndex = (CreateIndex) parserManager.parse(new StringReader(statement));
        assertEquals(2, createIndex.getIndex().getColumnsNames().size());
        assertEquals("myindex", createIndex.getIndex().getName());
        assertNull(createIndex.getIndex().getType());
        assertEquals("mytab", createIndex.getTable().getFullyQualifiedName());
        assertEquals("mycol", createIndex.getIndex().getColumnsNames().get(0));
        assertEquals(statement, "" + createIndex);
    }

    @Test
    public void testCreateIndex2() throws JSQLParserException {
        String statement = "CREATE mytype INDEX myindex ON mytab (mycol, mycol2)";
        CreateIndex createIndex = (CreateIndex) parserManager.parse(new StringReader(statement));
        assertEquals(2, createIndex.getIndex().getColumnsNames().size());
        assertEquals("myindex", createIndex.getIndex().getName());
        assertEquals("mytype", createIndex.getIndex().getType());
        assertEquals("mytab", createIndex.getTable().getFullyQualifiedName());
        assertEquals("mycol2", createIndex.getIndex().getColumnsNames().get(1));
        assertEquals(statement, "" + createIndex);
    }

    @Test
    public void testCreateIndex3() throws JSQLParserException {
        String statement = "CREATE mytype INDEX myindex ON mytab (mycol ASC, mycol2, mycol3)";
        CreateIndex createIndex = (CreateIndex) parserManager.parse(new StringReader(statement));
        assertEquals(3, createIndex.getIndex().getColumnsNames().size());
        assertEquals("myindex", createIndex.getIndex().getName());
        assertEquals("mytype", createIndex.getIndex().getType());
        assertEquals("mytab", createIndex.getTable().getFullyQualifiedName());
        assertEquals("mycol3", createIndex.getIndex().getColumnsNames().get(2));
    }

    @Test
    public void testCreateIndex4() throws JSQLParserException {
        String statement = "CREATE mytype INDEX myindex ON mytab (mycol ASC, mycol2 (75), mycol3)";
        CreateIndex createIndex = (CreateIndex) parserManager.parse(new StringReader(statement));
        assertEquals(3, createIndex.getIndex().getColumnsNames().size());
        assertEquals("myindex", createIndex.getIndex().getName());
        assertEquals("mytype", createIndex.getIndex().getType());
        assertEquals("mytab", createIndex.getTable().getFullyQualifiedName());
        assertEquals("mycol3", createIndex.getIndex().getColumnsNames().get(2));
    }

    @Test
    public void testCreateIndex5() throws JSQLParserException {
        String statement = "CREATE mytype INDEX myindex ON mytab (mycol ASC, mycol2 (75), mycol3) mymodifiers";
        CreateIndex createIndex = (CreateIndex) parserManager.parse(new StringReader(statement));
        assertEquals(3, createIndex.getIndex().getColumnsNames().size());
        assertEquals("myindex", createIndex.getIndex().getName());
        assertEquals("mytype", createIndex.getIndex().getType());
        assertEquals("mytab", createIndex.getTable().getFullyQualifiedName());
        assertEquals("mycol3", createIndex.getIndex().getColumnsNames().get(2));
    }

    @Test
    public void testCreateIndex6() throws JSQLParserException {
        String stmt = "CREATE INDEX myindex ON mytab (mycol, mycol2)";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }
}
