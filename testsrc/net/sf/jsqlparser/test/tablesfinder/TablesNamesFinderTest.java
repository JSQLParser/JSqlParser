package net.sf.jsqlparser.test.tablesfinder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import junit.framework.TestCase;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.test.TestException;
import net.sf.jsqlparser.test.simpleparsing.CCJSqlParserManagerTest;

public class TablesNamesFinderTest extends TestCase {
	CCJSqlParserManager pm = new CCJSqlParserManager();

	public TablesNamesFinderTest(String arg0) {
		super(arg0);
	}

	public static void main(String[] args) {
		junit.swingui.TestRunner.run(TablesNamesFinderTest.class);
	}

	public void testRUBiSTableList() throws Exception {

		BufferedReader in = new BufferedReader(new FileReader("testfiles" + File.separator
				+ "RUBiS-select-requests.txt"));
		TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();

		try {
			int numSt = 1;
			while (true) {
				String line = getLine(in);
				if (line == null) {
					break;
				}

				if (line.length() == 0)
					continue;

				if (!line.equals("#begin"))
					break;
				line = getLine(in);
				StringBuffer buf = new StringBuffer(line);
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

					List tableListRetr = tablesNamesFinder.getTableList(select);
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
			if (in != null)
				in.close();
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
			List tableList = tablesNamesFinder.getTableList(selectStatement);
			assertEquals(6, tableList.size());
			int i = 1;
			for (Iterator iter = tableList.iterator(); iter.hasNext(); i++) {
				String tableName = (String) iter.next();
				assertEquals("MY_TABLE" + i, tableName);
			}
		}

	}

	private String getLine(BufferedReader in) throws Exception {
		return CCJSqlParserManagerTest.getLine(in);
	}

}
