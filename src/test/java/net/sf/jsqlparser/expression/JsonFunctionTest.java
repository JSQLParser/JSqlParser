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
import org.junit.Test;

/**
 *
 * @author <a href="mailto:andreas@manticore-projects.com">Andreas Reichel</a>
 */
public class JsonFunctionTest {
  @Test
  public void testObjectAgg() throws JSQLParserException {
    TestUtils.assertSqlCanBeParsedAndDeparsed(
        "SELECT JSON_OBJECTAGG( KEY foo VALUE bar) FROM dual ", true);
    TestUtils.assertSqlCanBeParsedAndDeparsed("SELECT JSON_OBJECTAGG( foo:bar) FROM dual ", true);
    TestUtils.assertSqlCanBeParsedAndDeparsed(
        "SELECT JSON_OBJECTAGG( foo:bar FORMAT JSON) FROM dual ", true);
    TestUtils.assertSqlCanBeParsedAndDeparsed(
        "SELECT JSON_OBJECTAGG( KEY foo VALUE bar NULL ON NULL) FROM dual ", true);
    TestUtils.assertSqlCanBeParsedAndDeparsed(
        "SELECT JSON_OBJECTAGG( KEY foo VALUE bar ABSENT ON NULL) FROM dual ", true);
    TestUtils.assertSqlCanBeParsedAndDeparsed(
        "SELECT JSON_OBJECTAGG( KEY foo VALUE bar WITH UNIQUE KEYS) FROM dual ", true);
    TestUtils.assertSqlCanBeParsedAndDeparsed(
        "SELECT JSON_OBJECTAGG( KEY foo VALUE bar WITHOUT UNIQUE KEYS) FROM dual ", true);
    TestUtils.assertSqlCanBeParsedAndDeparsed(
        "SELECT JSON_OBJECTAGG( KEY foo VALUE bar NULL ON NULL WITH UNIQUE KEYS ) FROM dual ",
        true);

    TestUtils.assertSqlCanBeParsedAndDeparsed(
        "SELECT JSON_OBJECTAGG( KEY foo VALUE bar NULL ON NULL WITH UNIQUE KEYS ) FILTER( WHERE name = 'Raj' ) FROM dual ",
        true);
    TestUtils.assertSqlCanBeParsedAndDeparsed(
        "SELECT JSON_OBJECTAGG( KEY foo VALUE bar NULL ON NULL WITH UNIQUE KEYS ) OVER( PARTITION BY name ) FROM dual ",
        true);
    TestUtils.assertSqlCanBeParsedAndDeparsed(
        "SELECT JSON_OBJECTAGG( KEY foo VALUE bar NULL ON NULL WITH UNIQUE KEYS ) FILTER( WHERE name = 'Raj' ) OVER( PARTITION BY name ) FROM dual ",
        true);
  }

  @Test
  public void testArrayAgg() throws JSQLParserException {
    TestUtils.assertSqlCanBeParsedAndDeparsed("SELECT JSON_ARRAYAGG( a ) FROM dual ", true);
    TestUtils.assertSqlCanBeParsedAndDeparsed("SELECT JSON_ARRAYAGG( a ORDER BY a ) FROM dual ",
        true);
    TestUtils.assertSqlCanBeParsedAndDeparsed("SELECT JSON_ARRAYAGG( a NULL ON NULL ) FROM dual ",
        true);
    TestUtils.assertSqlCanBeParsedAndDeparsed("SELECT JSON_ARRAYAGG( a FORMAT JSON ) FROM dual ",
        true);
    TestUtils.assertSqlCanBeParsedAndDeparsed(
        "SELECT JSON_ARRAYAGG( a FORMAT JSON NULL ON NULL ) FROM dual ", true);
    TestUtils.assertSqlCanBeParsedAndDeparsed(
        "SELECT JSON_ARRAYAGG( a FORMAT JSON ABSENT ON NULL ) FROM dual ", true);
    TestUtils.assertSqlCanBeParsedAndDeparsed(
        "SELECT JSON_ARRAYAGG( a FORMAT JSON ABSENT ON NULL ) FILTER( WHERE name = 'Raj' ) FROM dual ",
        true);
    TestUtils.assertSqlCanBeParsedAndDeparsed(
        "SELECT JSON_ARRAYAGG( a FORMAT JSON ABSENT ON NULL ) OVER( PARTITION BY name )  FROM dual ",
        true);
    TestUtils.assertSqlCanBeParsedAndDeparsed(
        "SELECT JSON_ARRAYAGG( a FORMAT JSON ABSENT ON NULL ) FILTER( WHERE name = 'Raj' ) OVER( PARTITION BY name ) FROM dual ",
        true);

    TestUtils.assertSqlCanBeParsedAndDeparsed(
        "SELECT json_arrayagg(json_array(\"v0\") order by \"t\".\"v0\") FROM dual ", true);
  }

  @Test
  public void testObject() throws JSQLParserException {
    TestUtils.assertSqlCanBeParsedAndDeparsed(
        "SELECT JSON_OBJECT( KEY foo VALUE bar, KEY foo VALUE bar) FROM dual ", true);
    TestUtils.assertSqlCanBeParsedAndDeparsed("SELECT JSON_OBJECT( foo:bar, foo:bar) FROM dual ",
        true);
    TestUtils.assertSqlCanBeParsedAndDeparsed(
        "SELECT JSON_OBJECT( foo:bar, foo:bar FORMAT JSON) FROM dual ", true);
    TestUtils.assertSqlCanBeParsedAndDeparsed(
        "SELECT JSON_OBJECT( KEY foo VALUE bar, foo:bar FORMAT JSON, foo:bar NULL ON NULL) FROM dual ",
        true);
    TestUtils.assertSqlCanBeParsedAndDeparsed(
        "SELECT JSON_OBJECT( KEY foo VALUE bar FORMAT JSON, foo:bar, foo:bar ABSENT ON NULL) FROM dual ",
        true);

    TestUtils.assertExpressionCanBeParsedAndDeparsed("json_object(null on null)", true);
    
    TestUtils.assertExpressionCanBeParsedAndDeparsed("json_object(absent on null)", true);
    
    TestUtils.assertExpressionCanBeParsedAndDeparsed("json_object()", true);
  }

  @Test
  public void testArray() throws JSQLParserException {
    TestUtils.assertSqlCanBeParsedAndDeparsed(
        "SELECT JSON_ARRAY( (SELECT * from dual) ) FROM dual ", true);
    TestUtils.assertSqlCanBeParsedAndDeparsed("SELECT JSON_ARRAY( 1, 2, 3 ) FROM dual ", true);
    TestUtils.assertSqlCanBeParsedAndDeparsed("SELECT JSON_ARRAY( \"v0\" ) FROM dual ", true);
  }
  
  @Test
  public void testArrayWithNullExpressions() throws JSQLParserException {
    TestUtils.assertExpressionCanBeParsedAndDeparsed("JSON_ARRAY( 1, 2, 3 )", true);
    TestUtils.assertExpressionCanBeParsedAndDeparsed("json_array(null on null)", true);
    TestUtils.assertExpressionCanBeParsedAndDeparsed("json_array(null null on null)", true);
    TestUtils.assertExpressionCanBeParsedAndDeparsed("json_array(null, null null on null)", true);
    TestUtils.assertExpressionCanBeParsedAndDeparsed("json_array()", true);
  }

  @Test
  public void testIssue1260() throws JSQLParserException {
    TestUtils.assertSqlCanBeParsedAndDeparsed("select \n" + "  cast((\n" + "    select coalesce(\n"
        + "      json_arrayagg(json_array(\"v0\") order by \"t\".\"v0\"),\n"
        + "      json_array(null on null)\n" + "    )\n" + "    from (\n"
        + "      select 2 \"v0\"\n" + "      union\n" + "      select 4 \"ID\"\n" + "    ) \"t\"\n"
        + "  ) as text)", true);
  }
}
