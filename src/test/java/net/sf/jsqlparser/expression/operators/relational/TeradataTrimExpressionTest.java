package net.sf.jsqlparser.expression.operators.relational;

import org.junit.Assert;
import org.junit.Test;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;

public class TeradataTrimExpressionTest
{

	@Test
	public void testInSelectExpression() throws JSQLParserException
	{
		String sql = "SELECT trim(LEADING '-' from t.firstname) as col1 FROM table1 t";
		Select stmt = (Select) CCJSqlParserUtil.parse(sql);

		PlainSelect plain = (PlainSelect) stmt.getSelectBody();

		SelectExpressionItem si = (SelectExpressionItem) plain.getSelectItems().get(0);

		Expression ex = si.getExpression();

		Assert.assertTrue(ex instanceof TeradataTrimExpression);

		TeradataTrimExpression tte = (TeradataTrimExpression) ex;

		Assert.assertEquals(TeradataTrimExpression.Direction.LEADING, tte.getDirection());

		Assert.assertTrue(tte.getRemovalCharExpression() instanceof StringValue);

	}
}
