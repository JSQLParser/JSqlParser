package net.sf.jsqlparser.expression;

import org.junit.Assert;
import org.junit.Test;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;

public class TeradataCastExpressionTest
{

	@Test
	public void testFormatNumber() throws JSQLParserException
	{
		String sql = "SELECT cast(t.firstname as NUMBER FORMAT '#.000') as col1 FROM table1 t";
		Select stmt = (Select) CCJSqlParserUtil.parse(sql);

		PlainSelect plain = (PlainSelect) stmt.getSelectBody();

		SelectExpressionItem si = (SelectExpressionItem) plain.getSelectItems().get(0);

		Expression ex = si.getExpression();

		Assert.assertTrue(ex instanceof TeradataCastExpression);

		TeradataCastExpression tte = (TeradataCastExpression) ex;

		Assert.assertEquals("'#.000'", tte.getFormatExpression().toString());

	}
}
