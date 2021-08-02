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
public class OracleNamedFunctionParameterTest {

  /**
   * This test will parse and deparse the statement and assures the functional coverage by
   * JSQLParser.
     * @throws net.sf.jsqlparser.JSQLParserException
   */
  @Test
  public void testExpression() throws JSQLParserException {
    String sqlStr =
        "select r.*, test.numeric_function ( p_1 => r.param1, p_2 => r.param2 ) as resultaat2";

    TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    
    sqlStr = 
      "exec dbms_stats.gather_schema_stats(\n" +
        "      ownname          => 'COMMON', \n" +
        "      estimate_percent => dbms_stats.auto_sample_size, \n" +
        "      method_opt       => 'for all columns size auto', \n" +
        "      degree           => DBMS_STATS.DEFAULT_DEGREE,\n" +
        "      cascade          => DBMS_STATS.AUTO_CASCADE,\n" +
        "      options          => 'GATHER AUTO'\n" +
        "   )";
    TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
  }

  /**
   * This test will trigger the method {@link ExpressionVisitorAdaptor#visit() Visit Method} in the
   * ExpressionVisitorAdaptor needed for the Code Coverage.
     * @throws net.sf.jsqlparser.JSQLParserException
   */
  @Test
  public void testExpressionVisitorAdaptor() throws JSQLParserException {
    String sqlStr =
        "select r.*, test.numeric_function ( p_1 => r.param1, p_2 => r.param2 ) as resultaat2";

    CCJSqlParserUtil.parse(sqlStr).accept(new StatementVisitorAdapter());

    // alternatively, for the Expression only
    CCJSqlParserUtil.parseExpression("p_1 => r.param1").accept(new ExpressionVisitorAdapter());
  }


  /**
   * This test will trigger the method {@link TableNamesFinder#visit() Visit Method} in the
   * TableNamesFinder needed for the Code Coverage.
     * @throws net.sf.jsqlparser.JSQLParserException
   */
  @Test
  public void testTableNamesFinder() throws JSQLParserException {
    String sqlStr =
        "select r.*, test.numeric_function ( p_1 => r.param1, p_2 => r.param2 ) as resultaat2 from test_table";

    Statement statement = CCJSqlParserUtil.parse(sqlStr);
    List<String> tables = new TablesNamesFinder().getTableList(statement);
    Assert.assertEquals(1, tables.size());
    Assert.assertTrue(tables.contains("test_table"));
  }

  /**
   * This test will trigger the method {@link ExpressionValidator#visit() Visit Method} in the
   * ExpressionValidator needed for the Code Coverage.
     * @throws net.sf.jsqlparser.JSQLParserException
   */
  @Test
  public void testValidator() throws JSQLParserException {
    String sqlStr =
        "select r.*, test.numeric_function ( p_1 => r.param1, p_2 => r.param2 ) as resultaat2";

    ValidationTestAsserts.validateNoErrors(sqlStr, 1, DatabaseType.ORACLE);
  }
}
