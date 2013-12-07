package net.sf.jsqlparser.test.alter;


import junit.framework.TestCase;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.alter.Alter;

public class AlterTest extends TestCase {

	public AlterTest(String arg0) {
		super(arg0);
	}

	public void testAlterTableAddColumn() throws JSQLParserException {
		Statement stmt = CCJSqlParserUtil.parse("ALTER TABLE mytable ADD COLUMN mycolumn varchar (255)");
		assertTrue(stmt instanceof Alter);
		Alter alter = (Alter)stmt;
		assertEquals("mytable",alter.getTable().getWholeTableName());
		assertEquals("mycolumn", alter.getColumnName());
		assertEquals("varchar (255)", alter.getDataType().toString());
	}
}
