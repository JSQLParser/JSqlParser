package net.sf.jsqlparser.test.create;

import java.io.StringReader;

import junit.framework.TestCase;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import static net.sf.jsqlparser.test.TestUtils.*;

/**
 * @author Raymond Augé
 */
public class CreateIndexTest extends TestCase {

	CCJSqlParserManager parserManager = new CCJSqlParserManager();

	public CreateIndexTest(String arg0) {
		super(arg0);
	}

	public void testCreateIndex() throws JSQLParserException {
		String statement =
			"CREATE INDEX myindex ON mytab (mycol, mycol2)";
		CreateIndex createIndex = (CreateIndex) parserManager.parse(new StringReader(statement));
		assertEquals(2, createIndex.getIndex().getColumnsNames().size());
		assertEquals("myindex", createIndex.getIndex().getName());
		assertNull(createIndex.getIndex().getType());
		assertEquals("mytab", createIndex.getTable().getWholeTableName());
		assertEquals("mycol", createIndex.getIndex().getColumnsNames().get(0));
		assertEquals(statement, ""+createIndex);
	}

	public void testCreateIndex2() throws JSQLParserException {
		String statement =
			"CREATE mytype INDEX myindex ON mytab (mycol, mycol2)";
		CreateIndex createIndex = (CreateIndex) parserManager.parse(new StringReader(statement));
		assertEquals(2, createIndex.getIndex().getColumnsNames().size());
		assertEquals("myindex", createIndex.getIndex().getName());
		assertEquals("mytype", createIndex.getIndex().getType());
		assertEquals("mytab", createIndex.getTable().getWholeTableName());
		assertEquals("mycol2", createIndex.getIndex().getColumnsNames().get(1));
		assertEquals(statement, ""+createIndex);
	}

	public void testCreateIndex3() throws JSQLParserException {
		String statement =
			"CREATE mytype INDEX myindex ON mytab (mycol ASC, mycol2, mycol3)";
		CreateIndex createIndex = (CreateIndex) parserManager.parse(new StringReader(statement));
		assertEquals(3, createIndex.getIndex().getColumnsNames().size());
		assertEquals("myindex", createIndex.getIndex().getName());
		assertEquals("mytype", createIndex.getIndex().getType());
		assertEquals("mytab", createIndex.getTable().getWholeTableName());
		assertEquals("mycol3", createIndex.getIndex().getColumnsNames().get(2));
	}

	public void testCreateIndex4() throws JSQLParserException {
		String statement =
			"CREATE mytype INDEX myindex ON mytab (mycol ASC, mycol2 (75), mycol3)";
		CreateIndex createIndex = (CreateIndex) parserManager.parse(new StringReader(statement));
		assertEquals(3, createIndex.getIndex().getColumnsNames().size());
		assertEquals("myindex", createIndex.getIndex().getName());
		assertEquals("mytype", createIndex.getIndex().getType());
		assertEquals("mytab", createIndex.getTable().getWholeTableName());
		assertEquals("mycol3", createIndex.getIndex().getColumnsNames().get(2));
	}

	public void testCreateIndex5() throws JSQLParserException {
		String statement =
			"CREATE mytype INDEX myindex ON mytab (mycol ASC, mycol2 (75), mycol3) mymodifiers";
		CreateIndex createIndex = (CreateIndex) parserManager.parse(new StringReader(statement));
		assertEquals(3, createIndex.getIndex().getColumnsNames().size());
		assertEquals("myindex", createIndex.getIndex().getName());
		assertEquals("mytype", createIndex.getIndex().getType());
		assertEquals("mytab", createIndex.getTable().getWholeTableName());
		assertEquals("mycol3", createIndex.getIndex().getColumnsNames().get(2));
	}

	public void testCreateIndex6() throws JSQLParserException {
		String stmt= "CREATE INDEX myindex ON mytab (mycol, mycol2)";
		assertSqlCanBeParsedAndDeparsed(stmt);		
	}
}
