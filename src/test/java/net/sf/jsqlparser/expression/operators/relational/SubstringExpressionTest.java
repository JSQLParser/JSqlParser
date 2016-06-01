package net.sf.jsqlparser.expression.operators.relational;

import net.sf.jsqlparser.expression.Expression;
import org.junit.*;
import org.mockito.Mockito;

import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * unit tests of {@link SubstringExpression}
 *
 * @author ChrissW-R1
 */
public class SubstringExpressionTest {
	private static final String sourceVal = "foo";
	private static final int fromVal = 1;
	private static final int forVal = 10;

	private static SubstringExpression subStrExpr;
	
	@Before
	public void setUp() {
		subStrExpr = new SubstringExpression();

		Expression sourceExpr = Mockito.mock(Expression.class);
		Mockito.when(sourceExpr.toString()).thenReturn(sourceVal);
		subStrExpr.setSourceExpression(sourceExpr);

		Expression fromExpr = Mockito.mock(Expression.class);
		Mockito.when(fromExpr.toString()).thenReturn(Integer.toString(fromVal));
		subStrExpr.setFromExpression(fromExpr);

		Expression forExpr = Mockito.mock(Expression.class);
		Mockito.when(forExpr.toString()).thenReturn(Integer.toString(forVal));
		subStrExpr.setForExpression(forExpr);
	}

	/**
	 * Test of {@link SubstringExpression#toString()}
	 */
	@Test
	public void testToString() {
		String str = subStrExpr.toString();

		String regEx = "(?i)SUBSTRING\\([ \\r\\n\\t]*"
				+ sourceVal
				+ "[ \\r\\n\\t]+FROM[ \\r\\n\\t]+"
				+ fromVal
				+ "[ \\r\\n\\t]+FOR+[ \\r\\n\\t]+"
				+ forVal
				+ "[ \\r\\n\\t]*\\)";

		assertTrue(str.matches(regEx));
	}
}
