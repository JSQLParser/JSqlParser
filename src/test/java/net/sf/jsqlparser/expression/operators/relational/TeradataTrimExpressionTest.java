package net.sf.jsqlparser.expression.operators.relational;

import static org.junit.Assert.*;

import org.junit.Test;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.CastExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;

public class TeradataTrimExpressionTest
{

	@Test
	public void testPlainExpression()
	{
//		String sql = "TRIM(CAST(a AS NUMBER))";
		String sql = "TRIM(t.column)";
		try
		{
			Expression ex = CCJSqlParserUtil.parseExpression(sql);
			assertTrue(ex instanceof TeradataTrimExpression);
		}
		catch (JSQLParserException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testInSelectExpression() throws JSQLParserException
	{
		String sql = "SELECT trim(LEADING '-' from t.firstname) as col1 FROM table1 t";
		Select stmt = (Select) CCJSqlParserUtil.parse(sql);

		PlainSelect plain = (PlainSelect) stmt.getSelectBody();

		SelectExpressionItem si = (SelectExpressionItem) plain.getSelectItems().get(0);

		Expression ex = si.getExpression();

		assertTrue(ex instanceof TeradataTrimExpression);

		TeradataTrimExpression tte = (TeradataTrimExpression) ex;

		assertEquals("leading", tte.getDirection().toLowerCase());

		assertEquals("-",tte.getRemovalCharString());

	}

	@Test
	public void testTrimAsPlainFunction() throws JSQLParserException
	{
		String sql = "SELECT TRIM(t.column) as col1 FROM table1 t";
		Select stmt = (Select) CCJSqlParserUtil.parse(sql);

		PlainSelect plain = (PlainSelect) stmt.getSelectBody();

		SelectExpressionItem si = (SelectExpressionItem) plain.getSelectItems().get(0);

		Expression ex = si.getExpression();

		assertTrue(ex instanceof TeradataTrimExpression);

		TeradataTrimExpression tte = (TeradataTrimExpression) ex;

		Expression tse = tte.getTargetStringExpression();

		assertTrue(tse instanceof Column);

	}

	@Test
	public void testTrimWithSubstring() throws JSQLParserException
	{
		String sql = "SELECT TRIM(substr(t.column from 5 for 10)) as col1 FROM table1 t";
		Select stmt = (Select) CCJSqlParserUtil.parse(sql);

		PlainSelect plain = (PlainSelect) stmt.getSelectBody();

		SelectExpressionItem si = (SelectExpressionItem) plain.getSelectItems().get(0);

		Expression ex = si.getExpression();

		assertTrue(ex instanceof TeradataTrimExpression);

		TeradataTrimExpression tte = (TeradataTrimExpression) ex;

		Expression tse = tte.getTargetStringExpression();

		assertTrue(tse instanceof Function);

	}

	@Test
	public void testTrimBothWithCastFunction() throws JSQLParserException
	{
		String sql = "SELECT TRIM(both ' ' from cast('  string   ')) as col1 FROM table1 t";
		Select stmt = (Select) CCJSqlParserUtil.parse(sql);

		PlainSelect plain = (PlainSelect) stmt.getSelectBody();

		SelectExpressionItem si = (SelectExpressionItem) plain.getSelectItems().get(0);

		Expression ex = si.getExpression();

		assertTrue(ex instanceof TeradataTrimExpression);

		TeradataTrimExpression tte = (TeradataTrimExpression) ex;

		Expression tse = tte.getTargetStringExpression();

		assertTrue(tse instanceof Function);

	}

	@Test
	public void testTrimTrailingWithoutRemoveCharacter() throws JSQLParserException
	{
		String sql = "SELECT TRIM(trailing from cast('  string   ')) as col1 FROM table1 t";
		Select stmt = (Select) CCJSqlParserUtil.parse(sql);

		PlainSelect plain = (PlainSelect) stmt.getSelectBody();

		SelectExpressionItem si = (SelectExpressionItem) plain.getSelectItems().get(0);

		Expression ex = si.getExpression();

		assertTrue(ex instanceof TeradataTrimExpression);

		TeradataTrimExpression tte = (TeradataTrimExpression) ex;

		Expression tse = tte.getTargetStringExpression();

		assertTrue(tse instanceof Function);

	}


	@Test
	public void testTrimAroundCastFunction() throws JSQLParserException
	{
		String sql = "SELECT TRIM(CAST(CDSIRD.S_TX_CNTR_NO AS NUMBER FORMAT '#.##')) as col1 FROM table1 t";
		Select stmt = (Select) CCJSqlParserUtil.parse(sql);

		PlainSelect plain = (PlainSelect) stmt.getSelectBody();

		SelectExpressionItem si = (SelectExpressionItem) plain.getSelectItems().get(0);

		Expression ex = si.getExpression();

		assertTrue(ex instanceof TeradataTrimExpression);

		TeradataTrimExpression tte = (TeradataTrimExpression) ex;

		Expression tse = tte.getTargetStringExpression();

		assertTrue(tse instanceof CastExpression);

	}
}
