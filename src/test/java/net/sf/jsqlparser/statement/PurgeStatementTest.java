/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2021 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */

package net.sf.jsqlparser.statement;

import java.util.List;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.test.TestUtils;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.validation.ValidationTestAsserts;
import net.sf.jsqlparser.util.validation.feature.DatabaseType;
import net.sf.jsqlparser.util.validation.validator.ExpressionValidator;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 *
 * @author <a href="mailto:andreas@manticore-projects.com">Andreas Reichel</a>
 */
public class PurgeStatementTest {

    @Test
    public void testStatement() throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed("PURGE TABLE testtable", true);
        TestUtils.assertSqlCanBeParsedAndDeparsed("PURGE TABLE cfe.testtable", true);
        TestUtils.assertSqlCanBeParsedAndDeparsed("PURGE INDEX testtable_idx1", true);
        TestUtils.assertSqlCanBeParsedAndDeparsed("PURGE INDEX cfe.testtable_idx1", true);
        TestUtils.assertSqlCanBeParsedAndDeparsed("PURGE RECYCLEBIN", true);
        TestUtils.assertSqlCanBeParsedAndDeparsed("PURGE DBA_RECYCLEBIN", true);
        TestUtils.assertSqlCanBeParsedAndDeparsed("PURGE TABLESPACE my_table_space", true);
        TestUtils.assertSqlCanBeParsedAndDeparsed("PURGE TABLESPACE my_table_space USER cfe", true);
    }
    
   /**
   * This test will trigger the method {@link StatementVisitorAdaptor#visit() Visit Method} in the
   * StatementVisitorAdaptor needed for the Code Coverage.
   * 
   * @throws net.sf.jsqlparser.JSQLParserException
   */
  @Test
  public void testStatementVisitorAdaptor() throws JSQLParserException {
    String sqlStr = "PURGE TABLE testtable";

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
    String sqlStr = "PURGE TABLE testtable";

    Statement statement = CCJSqlParserUtil.parse(sqlStr);
    List<String> tables = new TablesNamesFinder().getTableList(statement);
    assertEquals(1, tables.size());
    assertTrue(tables.contains("testtable"));
  }

  /**
   * This test will trigger the method {@link ExpressionValidator#visit() Visit Method} in the
   * ExpressionValidator needed for the Code Coverage.
   * 
   * @throws net.sf.jsqlparser.JSQLParserException
   */
  @Test
  public void testValidator() throws JSQLParserException {
    String sqlStr = "PURGE TABLE testtable";

    ValidationTestAsserts.validateNoErrors(sqlStr, 1, DatabaseType.ORACLE);
  }

  @Test
  public void testObjectAccess() throws JSQLParserException {
      String sqlStr = "PURGE TABLESPACE my_table_space USER cfe";
      PurgeStatement purgeStatement = (PurgeStatement) CCJSqlParserUtil.parse(sqlStr);
      purgeStatement.setUserName("common");
       
      assertEquals(PurgeObjectType.TABLESPACE, purgeStatement.getPurgeObjectType());
      assertEquals("my_table_space", purgeStatement.getObject());
      assertEquals("common", purgeStatement.getUserName());
    }
}
