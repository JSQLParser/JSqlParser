package net.sf.jsqlparser.test.insert;

import java.io.StringReader;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.PlainSelect;
import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.junit.Assert.*;

import org.junit.Test;

public class InsertTest {

	CCJSqlParserManager parserManager = new CCJSqlParserManager();

    @Test
	public void testRegularInsert() throws JSQLParserException {
		String statement = "INSERT INTO mytable (col1, col2, col3) VALUES (?, 'sadfsd', 234)";
		Insert insert = (Insert) parserManager.parse(new StringReader(statement));
		assertEquals("mytable", insert.getTable().getName());
		assertEquals(3, insert.getColumns().size());
		assertEquals("col1", ((Column) insert.getColumns().get(0)).getColumnName());
		assertEquals("col2", ((Column) insert.getColumns().get(1)).getColumnName());
		assertEquals("col3", ((Column) insert.getColumns().get(2)).getColumnName());
		assertEquals(3, ((ExpressionList) insert.getItemsList()).getExpressions().size());
		assertTrue(((ExpressionList) insert.getItemsList()).getExpressions().get(0) instanceof JdbcParameter);
		assertEquals("sadfsd",
				((StringValue) ((ExpressionList) insert.getItemsList()).getExpressions().get(1)).getValue());
		assertEquals(234, ((LongValue) ((ExpressionList) insert.getItemsList()).getExpressions().get(2)).getValue());
		assertEquals(statement, "" + insert);

		statement = "INSERT INTO myschema.mytable VALUES (?, ?, 2.3)";
		insert = (Insert) parserManager.parse(new StringReader(statement));
		assertEquals("myschema.mytable", insert.getTable().getFullyQualifiedName());
		assertEquals(3, ((ExpressionList) insert.getItemsList()).getExpressions().size());
		assertTrue(((ExpressionList) insert.getItemsList()).getExpressions().get(0) instanceof JdbcParameter);
		assertEquals(2.3, ((DoubleValue) ((ExpressionList) insert.getItemsList()).getExpressions().get(2)).getValue(),
				0.0);
		assertEquals(statement, "" + insert);

	}

    @Test
	public void testInsertWithKeywordValue() throws JSQLParserException {
		String statement = "INSERT INTO mytable (col1) VALUE ('val1')";
		Insert insert = (Insert) parserManager.parse(new StringReader(statement));
		assertEquals("mytable", insert.getTable().getName());
		assertEquals(1, insert.getColumns().size());
		assertEquals("col1", ((Column) insert.getColumns().get(0)).getColumnName());
		assertEquals("val1",
				((StringValue) ((ExpressionList) insert.getItemsList()).getExpressions().get(0)).getValue());
		assertEquals("INSERT INTO mytable (col1) VALUES ('val1')", insert.toString());
	}

    @Test
	public void testInsertFromSelect() throws JSQLParserException {
		String statement = "INSERT INTO mytable (col1, col2, col3) SELECT * FROM mytable2";
		Insert insert = (Insert) parserManager.parse(new StringReader(statement));
		assertEquals("mytable", insert.getTable().getName());
		assertEquals(3, insert.getColumns().size());
		assertEquals("col1", ((Column) insert.getColumns().get(0)).getColumnName());
		assertEquals("col2", ((Column) insert.getColumns().get(1)).getColumnName());
		assertEquals("col3", ((Column) insert.getColumns().get(2)).getColumnName());
		assertNull(insert.getItemsList());
        assertNotNull(insert.getSelect());
		assertEquals("mytable2",
				((Table) ((PlainSelect)insert.getSelect().getSelectBody()).getFromItem()).getName());

		// toString uses brakets
		String statementToString = "INSERT INTO mytable (col1, col2, col3) SELECT * FROM mytable2";
		assertEquals(statementToString, "" + insert);
	}

    @Test
	public void testInsertMultiRowValue() throws JSQLParserException {
		assertSqlCanBeParsedAndDeparsed("INSERT INTO mytable (col1, col2) VALUES (a, b), (d, e)");
	}

    @Test
	public void testInsertMultiRowValueDifferent() throws JSQLParserException {
		try {
			assertSqlCanBeParsedAndDeparsed("INSERT INTO mytable (col1, col2) VALUES (a, b), (d, e, c)");
		} catch (Exception e) {
			return;
		}

		fail("should not work");
	}

    @Test
	public void testSimpleInsert() throws JSQLParserException {
		assertSqlCanBeParsedAndDeparsed("INSERT INTO example (num, name, address, tel) VALUES (1, 'name', 'test ', '1234-1234')");
	}
    
    @Test
    public void testInsertWithReturning() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("INSERT INTO mytable (mycolumn) VALUES ('1') RETURNING id");
    }
    
    @Test
    public void testInsertWithReturning2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("INSERT INTO mytable (mycolumn) VALUES ('1') RETURNING *");
    }
    
    @Test
    public void testInsertWithReturning3() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("INSERT INTO mytable (mycolumn) VALUES ('1') RETURNING id AS a1, id2 AS a2");
    }
    
    @Test
    public void testInsertSelect() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("INSERT INTO mytable (mycolumn) SELECT mycolumn FROM mytable");
        assertSqlCanBeParsedAndDeparsed("INSERT INTO mytable (mycolumn) (SELECT mycolumn FROM mytable)");
    }
    
    @Test
    public void testInsertWithSelect() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("INSERT INTO mytable (mycolumn) WITH a AS (SELECT mycolumn FROM mytable) SELECT mycolumn FROM a");
        assertSqlCanBeParsedAndDeparsed("INSERT INTO mytable (mycolumn) (WITH a AS (SELECT mycolumn FROM mytable) SELECT mycolumn FROM a)");
    }
    
    @Test
    public void testInsertWithKeywords() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("INSERT INTO kvPair (value, key) VALUES (?, ?)");
    }
    
	@Test
	public void testHexValues() throws JSQLParserException {
		assertSqlCanBeParsedAndDeparsed("INSERT INTO TABLE2 VALUES ('1', \"DSDD\", x'EFBFBDC7AB')");
	}
	
	@Test
	public void testHexValues2() throws JSQLParserException {
		assertSqlCanBeParsedAndDeparsed("INSERT INTO TABLE2 VALUES ('1', \"DSDD\", 0xEFBFBDC7AB)");
	}
	
	@Test
	public void testHexValues3() throws JSQLParserException {
		assertSqlCanBeParsedAndDeparsed("INSERT INTO TABLE2 VALUES ('1', \"DSDD\", 0xabcde)");
	}

	@Test
	public void testDuplicateKey() throws JSQLParserException {
		assertSqlCanBeParsedAndDeparsed("INSERT INTO Users0 (UserId, Key, Value) VALUES (51311, 'T_211', 18) ON DUPLICATE KEY UPDATE Value = 18");
	}

    @Test
    public void testModifierIgnore() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("INSERT IGNORE INTO `AoQiSurvey_FlashVersion_Single` VALUES (302215163, 'WIN 16,0,0,235')");
    }

    @Test
    public void testModifierPriority1() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("INSERT DELAYED INTO kvPair (value, key) VALUES (?, ?)");
    }
    
    @Test
    public void testModifierPriority2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("INSERT LOW_PRIORITY INTO kvPair (value, key) VALUES (?, ?)");
    }
    
    @Test
    public void testModifierPriority3() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("INSERT HIGH_PRIORITY INTO kvPair (value, key) VALUES (?, ?)");
    }
    
    @Test
    public void testIssue223() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("INSERT INTO user VALUES (2001, '\\'Clark\\'', 'Kent')");
    }
    
    @Test
    public void testKeywordPrecisionIssue363() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("INSERT INTO test (user_id, precision) VALUES (1, '111')");
    }
}
