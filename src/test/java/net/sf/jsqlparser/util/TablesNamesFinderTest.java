package net.sf.jsqlparser.util;

import net.sf.jsqlparser.util.TablesNamesFinder;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import junit.framework.TestCase;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.replace.Replace;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.test.TestException;
import net.sf.jsqlparser.test.simpleparsing.CCJSqlParserManagerTest;

public class TablesNamesFinderTest extends TestCase {

	CCJSqlParserManager pm = new CCJSqlParserManager();

	public TablesNamesFinderTest(String arg0) {
		super(arg0);
	}

	public void testRUBiSTableList() throws Exception {
		runTestOnResource("/RUBiS-select-requests.txt");
	}

	public void testMoreComplexExamples() throws Exception {
		runTestOnResource("complex-select-requests.txt");
	}

	private void runTestOnResource(String resPath) throws Exception {
		BufferedReader in = new BufferedReader(new InputStreamReader(TablesNamesFinderTest.class.getResourceAsStream(resPath)));
		TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();

		try {
			int numSt = 1;
			while (true) {
				String line = getLine(in);
				if (line == null) {
					break;
				}

				if (line.length() == 0) {
					continue;
				}

				if (!line.equals("#begin")) {
					break;
				}
				line = getLine(in);
				StringBuilder buf = new StringBuilder(line);
				while (true) {
					line = getLine(in);
					if (line.equals("#end")) {
						break;
					}
					buf.append("\n");
					buf.append(line);
				}

				String query = buf.toString();
				if (!getLine(in).equals("true")) {
					continue;
				}

				String cols = getLine(in);
				String tables = getLine(in);
				String whereCols = getLine(in);
				String type = getLine(in);
				try {
					Select select = (Select) pm.parse(new StringReader(query));
					StringTokenizer tokenizer = new StringTokenizer(tables, " ");
					List tablesList = new ArrayList();
					while (tokenizer.hasMoreTokens()) {
						tablesList.add(tokenizer.nextToken());
					}

					String[] tablesArray = (String[]) tablesList.toArray(new String[tablesList.size()]);

					List<String> tableListRetr = tablesNamesFinder.getTableList(select);
					assertEquals("stm num:" + numSt, tablesArray.length, tableListRetr.size());

					for (int i = 0; i < tablesArray.length; i++) {
						assertEquals("stm num:" + numSt, tablesArray[i], tableListRetr.get(i));
					}
				} catch (Exception e) {
					throw new TestException("error at stm num: " + numSt, e);
				}
				numSt++;
			}
		} finally {
			if (in != null) {
				in.close();
			}
		}
	}

	public void testGetTableList() throws Exception {

		String sql = "SELECT * FROM MY_TABLE1, MY_TABLE2, (SELECT * FROM MY_TABLE3) LEFT OUTER JOIN MY_TABLE4 "
				+ " WHERE ID = (SELECT MAX(ID) FROM MY_TABLE5) AND ID2 IN (SELECT * FROM MY_TABLE6)";
		net.sf.jsqlparser.statement.Statement statement = pm.parse(new StringReader(sql));

		// now you should use a class that implements StatementVisitor to decide what to do
		// based on the kind of the statement, that is SELECT or INSERT etc. but here we are only
		// interested in SELECTS

		if (statement instanceof Select) {
			Select selectStatement = (Select) statement;
			TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
			List<String> tableList = tablesNamesFinder.getTableList(selectStatement);
			assertEquals(6, tableList.size());
			int i = 1;
			for (Iterator iter = tableList.iterator(); iter.hasNext(); i++) {
				String tableName = (String) iter.next();
				assertEquals("MY_TABLE" + i, tableName);
			}
		}

	}

	public void testGetTableListWithAlias() throws Exception {
		String sql = "SELECT * FROM MY_TABLE1 as ALIAS_TABLE1";
		net.sf.jsqlparser.statement.Statement statement = pm.parse(new StringReader(sql));

		Select selectStatement = (Select) statement;
		TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
		List<String> tableList = tablesNamesFinder.getTableList(selectStatement);
		assertEquals(1, tableList.size());
		assertEquals("MY_TABLE1", (String) tableList.get(0));
	}

	public void testGetTableListWithStmt() throws Exception {
		String sql = "WITH TESTSTMT as (SELECT * FROM MY_TABLE1 as ALIAS_TABLE1) SELECT * FROM TESTSTMT";
		net.sf.jsqlparser.statement.Statement statement = pm.parse(new StringReader(sql));

		Select selectStatement = (Select) statement;
		TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
		List<String> tableList = tablesNamesFinder.getTableList(selectStatement);
		assertEquals(1, tableList.size());
		assertEquals("MY_TABLE1", (String) tableList.get(0));
	}

	public void testGetTableListWithLateral() throws Exception {
		String sql = "SELECT * FROM MY_TABLE1, LATERAL(select a from MY_TABLE2) as AL";
		net.sf.jsqlparser.statement.Statement statement = pm.parse(new StringReader(sql));

		Select selectStatement = (Select) statement;
		TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
		List<String> tableList = tablesNamesFinder.getTableList(selectStatement);
		assertEquals(2, tableList.size());
		assertTrue(tableList.contains("MY_TABLE1"));
		assertTrue(tableList.contains("MY_TABLE2"));
	}

	public void testGetTableListFromDelete() throws Exception {
		String sql = "DELETE FROM MY_TABLE1 as AL WHERE a = (SELECT a from MY_TABLE2)";
		net.sf.jsqlparser.statement.Statement statement = pm.parse(new StringReader(sql));

		Delete deleteStatement = (Delete) statement;
		TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
		List<String> tableList = tablesNamesFinder.getTableList(deleteStatement);
		assertEquals(2, tableList.size());
		assertTrue(tableList.contains("MY_TABLE1"));
		assertTrue(tableList.contains("MY_TABLE2"));
	}

	public void testGetTableListFromInsert() throws Exception {
		String sql = "INSERT INTO MY_TABLE1 (a) VALUES ((SELECT a from MY_TABLE2 WHERE a = 1))";
		net.sf.jsqlparser.statement.Statement statement = pm.parse(new StringReader(sql));

		Insert insertStatement = (Insert) statement;
		TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
		List<String> tableList = tablesNamesFinder.getTableList(insertStatement);
		assertEquals(2, tableList.size());
		assertTrue(tableList.contains("MY_TABLE1"));
		assertTrue(tableList.contains("MY_TABLE2"));
	}
	
	public void testGetTableListFromInsertValues() throws Exception {
		String sql = "INSERT INTO MY_TABLE1 (a) VALUES (5)";
		net.sf.jsqlparser.statement.Statement statement = pm.parse(new StringReader(sql));

		Insert insertStatement = (Insert) statement;
		TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
		List<String> tableList = tablesNamesFinder.getTableList(insertStatement);
		assertEquals(1, tableList.size());
		assertTrue(tableList.contains("MY_TABLE1"));
	}

	public void testGetTableListFromReplace() throws Exception {
		String sql = "REPLACE INTO MY_TABLE1 (a) VALUES ((SELECT a from MY_TABLE2 WHERE a = 1))";
		net.sf.jsqlparser.statement.Statement statement = pm.parse(new StringReader(sql));

		Replace replaceStatement = (Replace) statement;
		TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
		List<String> tableList = tablesNamesFinder.getTableList(replaceStatement);
		assertEquals(2, tableList.size());
		assertTrue(tableList.contains("MY_TABLE1"));
		assertTrue(tableList.contains("MY_TABLE2"));
	}

	public void testGetTableListFromUpdate() throws Exception {
		String sql = "UPDATE MY_TABLE1 SET a = (SELECT a from MY_TABLE2 WHERE a = 1)";
		net.sf.jsqlparser.statement.Statement statement = pm.parse(new StringReader(sql));

		Update updateStatement = (Update) statement;
		TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
		List<String> tableList = tablesNamesFinder.getTableList(updateStatement);
		assertEquals(2, tableList.size());
		assertTrue(tableList.contains("MY_TABLE1"));
		assertTrue(tableList.contains("MY_TABLE2"));
	}
	
	public void testGetTableListFromUpdate2() throws Exception {
		String sql = "UPDATE MY_TABLE1 SET a = 5 WHERE 0 < (SELECT COUNT(b) FROM MY_TABLE3)";
		net.sf.jsqlparser.statement.Statement statement = pm.parse(new StringReader(sql));

		Update updateStatement = (Update) statement;
		TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
		List<String> tableList = tablesNamesFinder.getTableList(updateStatement);
		assertEquals(2, tableList.size());
		assertTrue(tableList.contains("MY_TABLE1"));
		assertTrue(tableList.contains("MY_TABLE3"));
	}
	
	public void testGetTableListFromUpdate3() throws Exception {
		String sql = "UPDATE MY_TABLE1 SET a = 5 FROM MY_TABLE1 INNER JOIN MY_TABLE2 on MY_TABLE1.C = MY_TABLE2.D WHERE 0 < (SELECT COUNT(b) FROM MY_TABLE3)";
		net.sf.jsqlparser.statement.Statement statement = pm.parse(new StringReader(sql));

		Update updateStatement = (Update) statement;
		TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
		List<String> tableList = tablesNamesFinder.getTableList(updateStatement);
		assertEquals(3, tableList.size());
		assertTrue(tableList.contains("MY_TABLE1"));
		assertTrue(tableList.contains("MY_TABLE2"));
		assertTrue(tableList.contains("MY_TABLE3"));
	}

	private String getLine(BufferedReader in) throws Exception {
		return CCJSqlParserManagerTest.getLine(in);
	}
}
