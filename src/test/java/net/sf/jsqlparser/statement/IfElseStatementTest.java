/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import java.util.Arrays;
import java.util.List;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.NotExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.test.TestUtils;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.validation.Validation;
import net.sf.jsqlparser.util.validation.ValidationError;
import net.sf.jsqlparser.util.validation.ValidationTestAsserts;
import net.sf.jsqlparser.util.validation.feature.DatabaseType;
import net.sf.jsqlparser.util.validation.feature.FeaturesAllowed;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 *
 * @author <a href="mailto:andreas@manticore-projects.com">Andreas Reichel</a>
 */

public class IfElseStatementTest {
  @Test
  public void testSimpleIfElseStatement() throws Exception {
    TestUtils.assertSqlCanBeParsedAndDeparsed(
        "IF OBJECT_ID('tOrigin', 'U') IS NOT NULL DROP TABLE tOrigin", true);
    TestUtils.assertSqlCanBeParsedAndDeparsed(
        "IF OBJECT_ID('tOrigin', 'U') IS NOT NULL DROP TABLE tOrigin;", true);
    TestUtils.assertSqlCanBeParsedAndDeparsed(
        "IF OBJECT_ID('tOrigin', 'U') IS NOT NULL DROP TABLE tOrigin; ELSE CREATE TABLE tOrigin (ID VARCHAR(40));",
        true);
  }

  @Test
  public void testIfElseStatements1() throws Exception {
    String sqlStr =
        "IF OBJECT_ID('tOrigin', 'U') IS NOT NULL DROP TABLE tOrigin1; ELSE CREATE TABLE tOrigin1 (ID VARCHAR (40));\n"
            + "IF OBJECT_ID('tOrigin', 'U') IS NOT NULL DROP TABLE tOrigin2; ELSE CREATE TABLE tOrigin2 (ID VARCHAR (40));\n"
            + "IF OBJECT_ID('tOrigin', 'U') IS NOT NULL DROP TABLE tOrigin3; ELSE CREATE TABLE tOrigin3 (ID VARCHAR (40));\n";

    Statements result = CCJSqlParserUtil.parseStatements(sqlStr);
    assertEquals(sqlStr, result.toString());
  }

  @Test
  public void testIfElseStatements2() throws Exception {
    String sqlStr = "IF OBJECT_ID('tOrigin', 'U') IS NOT NULL DROP TABLE tOrigin1;\n"
        + "CREATE TABLE tOrigin2 (ID VARCHAR (40));\n"
        + "IF OBJECT_ID('tOrigin', 'U') IS NOT NULL DROP TABLE tOrigin3; ELSE CREATE TABLE tOrigin3 (ID VARCHAR (40));\n";

    Statements result = CCJSqlParserUtil.parseStatements(sqlStr);
    assertEquals(sqlStr, result.toString());
  }

  @Test
  public void testObjectBuilder() throws JSQLParserException {
    Statement ifStatement = CCJSqlParserUtil.parse("SELECT * from dual");
    Statement elseStatement = CCJSqlParserUtil.parse("SELECT * from dual");

    IfElseStatement ifElseStatement = new IfElseStatement(new NotExpression(), ifStatement);
    ifElseStatement.setUsingSemicolonForIfStatement(true);

    ifElseStatement.setElseStatement(elseStatement);
    ifElseStatement.setUsingSemicolonForElseStatement(true);

    assertEquals(ifElseStatement.isUsingSemicolonForIfStatement(),
        ifElseStatement.isUsingSemicolonForElseStatement());
    assertEquals(ifElseStatement.getIfStatement().toString(),
        ifElseStatement.getElseStatement().toString());

    assertNotNull(ifElseStatement.getCondition());
  }

  @Test
  public void testValidation() {
    String sqlStr = "IF OBJECT_ID('tOrigin', 'U') IS NOT NULL DROP TABLE tOrigin1;";
    List<ValidationError> errors =
        Validation.validate(Arrays.asList(DatabaseType.SQLSERVER, FeaturesAllowed.DROP), sqlStr);
    ValidationTestAsserts.assertErrorsSize(errors, 0);
  }

  @Test
  public void testTableNames() throws JSQLParserException {
    String sql = "IF OBJECT_ID('tOrigin', 'U') IS NOT NULL DROP TABLE tOrigin1;";
    Statement stmt = CCJSqlParserUtil.parse(sql);
    TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
    List<String> tableList = tablesNamesFinder.getTableList(stmt);
    assertEquals(1, tableList.size());
    assertTrue(tableList.contains("tOrigin1"));
  }
}
