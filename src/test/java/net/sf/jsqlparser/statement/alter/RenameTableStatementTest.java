/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2021 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */

package net.sf.jsqlparser.statement.alter;

import java.util.List;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
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
    
    sqlStr = "RENAME TABLE old_table TO backup_table, new_table TO old_table";
    TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    
    sqlStr = "RENAME TABLE IF EXISTS old_table WAIT 20 TO backup_table, new_table TO old_table";
    TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    
    sqlStr = "RENAME TABLE IF EXISTS old_table NOWAIT TO backup_table, new_table TO old_table";
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
  
  @Test
  public void testObjectAccess() {
      Table oldTable = new Table("oldTableName");
      Table newTable = new Table("newTableName");
      
      RenameTableStatement renameTableStatement = new RenameTableStatement(oldTable, newTable);
      renameTableStatement.withUsingTableKeyword(true).setUsingTableKeyword(false);
      renameTableStatement.withUsingIfExistsKeyword(true).setUsingIfExistsKeyword(false);
      renameTableStatement.withWaitDirective("NOWAIT").setWaitDirective("WAIT 20");
      
      Assert.assertFalse(renameTableStatement.isTableNamesEmpty());
      Assert.assertTrue(renameTableStatement.getTableNamesSize()>0);
      Assert.assertFalse(renameTableStatement.isUsingTableKeyword());
      Assert.assertFalse(renameTableStatement.isUsingIfExistsKeyword());
      Assert.assertEquals("WAIT 20", renameTableStatement.getWaitDirective());
    }
}
