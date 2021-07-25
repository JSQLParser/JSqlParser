/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2021 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
/*
 * Copyright (C) 2021 JSQLParser.
 *
 * This library is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301 USA
 */

package net.sf.jsqlparser.statement.alter;

import java.util.List;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.test.TestUtils;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.validation.ValidationTestAsserts;
import net.sf.jsqlparser.util.validation.feature.DatabaseType;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author <a href="mailto:andreas@manticore-projects.com">Andreas Reichel</a>
 */
public class RenameTableStatementTest {
  /**
   * This test will parse and deparse the statement and assures the functional coverage by
   * JSQLParser.
   * 
   * @throws net.sf.jsqlparser.JSQLParserException
   */
  @Test
  public void testStatement() throws JSQLParserException {
    String sqlStr = "RENAME oldTableName TO newTableName";

    TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
  }

  /**
   * This test will trigger the method {@link StatementVisitorAdaptor#visit() Visit Method} in the
   * StatementVisitorAdaptor needed for the Code Coverage.
   * 
   * @throws net.sf.jsqlparser.JSQLParserException
   */
  @Test
  public void testStatementVisitorAdaptor() throws JSQLParserException {
    String sqlStr = "RENAME oldTableName TO newTableName";

    CCJSqlParserUtil.parse(sqlStr).accept(new StatementVisitorAdapter());
  }

  /**
   * This test will trigger the method {@link TableNamesFinder#visit() Visit Method} in the
   * TableNamesFinder needed for the Code Coverage.
   * 
   * @throws net.sf.jsqlparser.JSQLParserException
   */
  @Test
  public void testTableNamesFinder() throws JSQLParserException {
    String sqlStr = "RENAME oldTableName TO newTableName";

    Statement statement = CCJSqlParserUtil.parse(sqlStr);
    List<String> tables = new TablesNamesFinder().getTableList(statement);
    Assert.assertEquals(2, tables.size());
    Assert.assertTrue(tables.contains("oldTableName"));
    Assert.assertTrue(tables.contains("newTableName"));
  }

  /**
   * This test will trigger the method {@link ExpressionValidator#visit() Visit Method} in the
   * ExpressionValidator needed for the Code Coverage.
   * 
   * @throws net.sf.jsqlparser.JSQLParserException
   */
  @Test
  public void testValidator() throws JSQLParserException {
    String sqlStr = "RENAME oldTableName TO newTableName";

    ValidationTestAsserts.validateNoErrors(sqlStr, 1, DatabaseType.ORACLE);
  }
}
