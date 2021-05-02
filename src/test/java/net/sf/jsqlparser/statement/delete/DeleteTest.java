/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.delete;

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;

import static org.junit.Assert.assertEquals;

import java.io.StringReader;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.schema.Column;
import static net.sf.jsqlparser.test.TestUtils.assertOracleHintExists;
import org.junit.Test;

public class DeleteTest {

  private final CCJSqlParserManager parserManager = new CCJSqlParserManager();

  @Test
  public void testDelete() throws JSQLParserException {
    String statement = "DELETE FROM mytable WHERE mytable.col = 9";

    Delete delete = (Delete) parserManager.parse(new StringReader(statement));
    assertEquals("mytable", delete.getTable().getName());
    assertEquals(statement, "" + delete);
  }

  @Test
  public void testDeleteWhereProblem1() throws JSQLParserException {
    String stmt = "DELETE FROM tablename WHERE a = 1 AND b = 1";
    assertSqlCanBeParsedAndDeparsed(stmt);
  }

  @Test
  public void testDeleteWithLimit() throws JSQLParserException {
    String stmt = "DELETE FROM tablename WHERE a = 1 AND b = 1 LIMIT 5";
    Delete parsed = (Delete) assertSqlCanBeParsedAndDeparsed(stmt);
    AndExpression where = parsed.getWhere(AndExpression.class);
    EqualsTo left = where.getLeftExpression(EqualsTo.class);
    assertEquals("a", left.getLeftExpression(Column.class).getColumnName());
    EqualsTo right = where.getRightExpression(EqualsTo.class);
    assertEquals("b", right.getLeftExpression(Column.class).getColumnName());
    assertEquals(5, parsed.getLimit().getRowCount(LongValue.class).getValue());
  }

  @Test(expected = JSQLParserException.class)
  public void testDeleteDoesNotAllowLimitOffset() throws JSQLParserException {
    String statement = "DELETE FROM table1 WHERE A.cod_table = 'YYY' LIMIT 3,4";
    parserManager.parse(new StringReader(statement));
  }

  @Test
  public void testDeleteWithOrderBy() throws JSQLParserException {
    String stmt = "DELETE FROM tablename WHERE a = 1 AND b = 1 ORDER BY col";
    assertSqlCanBeParsedAndDeparsed(stmt);
  }

  @Test
  public void testDeleteWithOrderByAndLimit() throws JSQLParserException {
    String stmt = "DELETE FROM tablename WHERE a = 1 AND b = 1 ORDER BY col LIMIT 10";
    assertSqlCanBeParsedAndDeparsed(stmt);
  }

  @Test
  public void testDeleteFromTableUsingInnerJoinToAnotherTable() throws JSQLParserException {
    String stmt = "DELETE Table1 FROM Table1 INNER JOIN Table2 ON Table1.ID = Table2.ID";
    assertSqlCanBeParsedAndDeparsed(stmt);
  }

  @Test
  public void testDeleteFromTableUsingLeftJoinToAnotherTable() throws JSQLParserException {
    String stmt = "DELETE g FROM Table1 AS g LEFT JOIN Table2 ON Table1.ID = Table2.ID";
    assertSqlCanBeParsedAndDeparsed(stmt);
  }

  @Test
  public void testDeleteFromTableUsingInnerJoinToAnotherTableWithAlias()
      throws JSQLParserException {
    String stmt =
        "DELETE gc FROM guide_category AS gc LEFT JOIN guide AS g ON g.id_guide = gc.id_guide WHERE g.title IS NULL LIMIT 5";
    assertSqlCanBeParsedAndDeparsed(stmt);
  }

  @Test
  public void testDeleteMultiTableIssue878() throws JSQLParserException {
    assertSqlCanBeParsedAndDeparsed("DELETE table1, table2 FROM table1, table2");
  }

  @Test
  public void testOracleHint() throws JSQLParserException {
    String sql = "DELETE /*+ SOMEHINT */ FROM mytable WHERE mytable.col = 9";

    assertOracleHintExists(sql, true, "SOMEHINT");

    // @todo: add a testcase supposed to not finding a misplaced hint
  }

  @Test
  public void testWith() throws JSQLParserException {
    String statement =
        ""
            + "WITH a\n"
            + "     AS (SELECT 1 id_instrument_ref)\n"
            + "     , b\n"
            + "       AS (SELECT 1 id_instrument_ref)\n"
            + "DELETE FROM cfe.instrument_ref\n"
            + "WHERE  id_instrument_ref = (SELECT id_instrument_ref\n"
            + "                            FROM   a)";

    assertSqlCanBeParsedAndDeparsed(statement, true);
  }
}
