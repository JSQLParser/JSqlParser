/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2021 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */

package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Test;

/**
 *
 * @author <a href="mailto:andreas@manticore-projects.com">Andreas Reichel</a>
 */
public class CastExpressionTest {
  @Test
  public void testCastToRowConstructorIssue1267() throws JSQLParserException {
    TestUtils.assertExpressionCanBeParsedAndDeparsed("CAST(ROW(dataid, value, calcMark) AS ROW(datapointid CHAR, value CHAR, calcMark CHAR))", true);
    TestUtils.assertExpressionCanBeParsedAndDeparsed("CAST(ROW(dataid, value, calcMark) AS testcol)", true);
  }
}
