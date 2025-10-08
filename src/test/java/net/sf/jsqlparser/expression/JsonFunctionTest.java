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
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author <a href="mailto:andreas@manticore-projects.com">Andreas Reichel</a>
 */
public class JsonFunctionTest {

    @Test
    public void testObjectAgg() throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(
                "SELECT JSON_OBJECTAGG( KEY foo VALUE bar) FROM dual ", true);
        TestUtils.assertSqlCanBeParsedAndDeparsed("SELECT JSON_OBJECTAGG( foo:bar) FROM dual ",
                true);
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
    public void testObjectBuilder() throws JSQLParserException {
        JsonFunction f = new JsonFunction();
        f.setType(JsonFunctionType.OBJECT);

        JsonKeyValuePair keyValuePair1 = new JsonKeyValuePair("foo", "bar", false, false);
        keyValuePair1.setUsingKeyKeyword(true);
        keyValuePair1.setUsingValueKeyword(true);
        f.add(keyValuePair1.withUsingFormatJson(true));

        JsonKeyValuePair keyValuePair2 = new JsonKeyValuePair("foo", "bar", false, false)
                .withUsingKeyKeyword(true).withUsingValueKeyword(true).withUsingFormatJson(false);

        // this should work because we compare based on KEY only
        assertEquals(keyValuePair1, keyValuePair2);

        // this must fail because all the properties are considered
        Assertions.assertNotEquals(keyValuePair1.toString(), keyValuePair2.toString());

        JsonKeyValuePair keyValuePair3 = new JsonKeyValuePair("foo", "bar", false, false)
                .withUsingKeyKeyword(false).withUsingValueKeyword(false).withUsingFormatJson(false);
        assertNotNull(keyValuePair3);
        assertEquals(keyValuePair3, keyValuePair3);
        Assertions.assertNotEquals(keyValuePair3, f);

        Assertions.assertTrue(keyValuePair3.hashCode() != 0);

        f.add(keyValuePair2);
    }

    @Test
    public void testArrayBuilder() throws JSQLParserException {
        JsonFunction f = new JsonFunction();
        f.setType(JsonFunctionType.ARRAY);

        JsonFunctionExpression expression1 = new JsonFunctionExpression(new NullValue());
        expression1.setUsingFormatJson(true);

        JsonFunctionExpression expression2 =
                new JsonFunctionExpression(new NullValue()).withUsingFormatJson(
                        true);

        assertEquals(expression1.toString(), expression2.toString());

        f.add(expression1);
        f.add(expression2);
    }

    @Test
    public void testArrayAgg() throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed("SELECT JSON_ARRAYAGG( a ) FROM dual ", true);
        TestUtils.assertSqlCanBeParsedAndDeparsed("SELECT JSON_ARRAYAGG( a ORDER BY a ) FROM dual ",
                true);
        TestUtils.assertSqlCanBeParsedAndDeparsed(
                "SELECT JSON_ARRAYAGG( a NULL ON NULL ) FROM dual ",
                true);
        TestUtils.assertSqlCanBeParsedAndDeparsed(
                "SELECT JSON_ARRAYAGG( a FORMAT JSON ) FROM dual ",
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
                "WITH Items AS (SELECT 'hello' AS key, 'world' AS value)\n" +
                        "SELECT JSON_OBJECT(key, value) AS json_data FROM Items",
                true);
        TestUtils.assertSqlCanBeParsedAndDeparsed(
                "SELECT JSON_OBJECT( KEY 'foo' VALUE bar, KEY 'foo' VALUE bar) FROM dual ", true);
        TestUtils.assertSqlCanBeParsedAndDeparsed(
                "SELECT JSON_OBJECT( 'foo' : bar, 'foo' : bar) FROM dual ",
                true);
        TestUtils.assertSqlCanBeParsedAndDeparsed(
                "SELECT JSON_OBJECT( 'foo':bar, 'foo':bar FORMAT JSON) FROM dual ", true);
        TestUtils.assertSqlCanBeParsedAndDeparsed(
                "SELECT JSON_OBJECT( KEY 'foo' VALUE bar, 'foo':bar FORMAT JSON, 'foo':bar NULL ON NULL) FROM dual ",
                true);
        TestUtils.assertSqlCanBeParsedAndDeparsed(
                "SELECT JSON_OBJECT( KEY 'foo' VALUE bar FORMAT JSON, 'foo':bar, 'foo':bar ABSENT ON NULL) FROM dual ",
                true);

        TestUtils.assertSqlCanBeParsedAndDeparsed(
                "SELECT JSON_OBJECT( KEY 'foo' VALUE bar FORMAT JSON, 'foo':bar, 'foo':bar ABSENT ON NULL WITH UNIQUE KEYS) FROM dual ",
                true);

        TestUtils.assertSqlCanBeParsedAndDeparsed(
                "SELECT JSON_OBJECT( KEY 'foo' VALUE bar FORMAT JSON, 'foo':bar, 'foo':bar ABSENT ON NULL WITHOUT UNIQUE KEYS) FROM dual ",
                true);

        TestUtils.assertExpressionCanBeParsedAndDeparsed("json_object(null on null)", true);

        TestUtils.assertExpressionCanBeParsedAndDeparsed("json_object(absent on null)", true);

        TestUtils.assertExpressionCanBeParsedAndDeparsed("json_object()", true);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            // AllColumns
            "SELECT JSON_OBJECT(*) FROM employees",
            "SELECT JSON_OBJECT(* ABSENT ON NULL) FROM employees",

            // AllTableColumns
            "SELECT JSON_OBJECT(e.*) FROM employees e",
            "SELECT JSON_OBJECT(e.*, d.* NULL ON NULL) FROM employees e, departments d",
            "SELECT JSON_OBJECT(e.* WITH UNIQUE KEYS) FROM employees e",

            // STRICT Keyword
            "SELECT JSON_OBJECT( 'foo':bar, 'fob':baz FORMAT JSON STRICT ) FROM dual",
            "SELECT JSON_OBJECT( 'foo':bar, 'fob':baz NULL ON NULL STRICT WITH UNIQUE KEYS) FROM dual"
    })
    void testObjectOracle(String sqlStr) throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            // BigQuery EXCEPT/REPLACE are not allowed here
            "SELECT JSON_OBJECT(* EXCEPT(first_name)) FROM employees",
            "SELECT JSON_OBJECT(* EXCLUDE(first_name)) FROM employees",
            "SELECT JSON_OBJECT(* REPLACE(\"first_name\" AS first_name)) FROM employees"
    })
    void testInvalidObjectOracle(String sqlStr) {
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sqlStr));
    }

    @Test
    public void testObjectWithExpression() throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(
                "SELECT JSON_OBJECT( KEY 'foo' VALUE cast( bar AS VARCHAR(40)), KEY 'foo' VALUE bar) FROM dual ",
                true);

        TestUtils.assertSqlCanBeParsedAndDeparsed(
                "SELECT JSON_ARRAYAGG(obj) FROM (SELECT trt.relevance_id,JSON_OBJECT('id',CAST(trt.id AS CHAR),'taskName',trt.task_name,'openStatus',trt.open_status,'taskSort',trt.task_sort) as obj FROM tb_review_task trt ORDER BY trt.task_sort ASC)",
                true);
    }

    @Test
    public void testObjectIssue1504() throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(
                "SELECT JSON_OBJECT(key 'person' value tp.account) obj", true);

        TestUtils.assertSqlCanBeParsedAndDeparsed(
                "SELECT JSON_OBJECT(key 'person' value tp.account, key 'person' value tp.account) obj",
                true);

        TestUtils.assertSqlCanBeParsedAndDeparsed(
                "SELECT JSON_OBJECT( 'person' : tp.account) obj", true);

        TestUtils.assertSqlCanBeParsedAndDeparsed(
                "SELECT JSON_OBJECT( 'person' : tp.account, 'person' : tp.account) obj", true);

        TestUtils.assertSqlCanBeParsedAndDeparsed(
                "SELECT JSON_OBJECT( 'person' : '1', 'person' : '2') obj", true);

        TestUtils.assertSqlCanBeParsedAndDeparsed(
                "SELECT JSON_OBJECT( 'person' VALUE tp.person, 'account' VALUE tp.account) obj",
                true);
    }

    @Test
    public void testObjectMySQL() throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(
                "SELECT JSON_OBJECT('person', tp.person, 'account', tp.account) obj", true);
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
        TestUtils.assertExpressionCanBeParsedAndDeparsed("json_array(null, null null on null)",
                true);
        TestUtils.assertExpressionCanBeParsedAndDeparsed("json_array()", true);
    }

    @Test
    public void testIssue1260() throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(
                "select \n" + "  cast((\n" + "    select coalesce(\n"
                        + "      json_arrayagg(json_array(\"v0\") order by \"t\".\"v0\"),\n"
                        + "      json_array(null on null)\n" + "    )\n" + "    from (\n"
                        + "      select 2 \"v0\"\n" + "      union\n" + "      select 4 \"ID\"\n"
                        + "    ) \"t\"\n"
                        + "  ) as text)",
                true);

        TestUtils.assertExpressionCanBeParsedAndDeparsed(
                "listagg( json_object(key 'v0' value \"v0\"), ',' )", true);

        TestUtils.assertSqlCanBeParsedAndDeparsed("select (\n"
                + "  select coalesce(\n"
                + "    cast(('[' || listagg(\n"
                + "      json_object(key 'v0' value \"v0\"),\n"
                + "      ','\n"
                + "    ) || ']') as varchar(32672)),\n"
                + "    json_array()\n"
                + "  )\n"
                + "  from (\n"
                + "    select cast(null as timestamp) \"v0\"\n"
                + "    from SYSIBM.DUAL\n"
                + "    union all\n"
                + "    select timestamp '2000-03-15 10:15:00.0' \"a\"\n"
                + "    from SYSIBM.DUAL\n"
                + "  ) \"t\"\n"
                + ")\n"
                + "from SYSIBM.DUAL", true);
    }

    @Test
    public void testIssue1371() throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed("SELECT json_object('{a, 1, b, 2}')", true);
        TestUtils.assertSqlCanBeParsedAndDeparsed("SELECT json_object('{{a, 1}, {b, 2}}')", true);
        TestUtils.assertSqlCanBeParsedAndDeparsed("SELECT json_object('{a, b}', '{1,2 }')", true);
    }

    @Test
    public void testJavaMethods() throws JSQLParserException {
        String expressionStr =
                "JSON_OBJECT( KEY 'foo' VALUE bar FORMAT JSON, 'foo':bar, 'foo':bar ABSENT ON NULL WITHOUT UNIQUE KEYS)";
        JsonFunction jsonFunction = (JsonFunction) CCJSqlParserUtil.parseExpression(expressionStr);

        assertEquals(JsonFunctionType.OBJECT, jsonFunction.getType());
        Assertions.assertNotEquals(jsonFunction.withType(JsonFunctionType.POSTGRES_OBJECT),
                jsonFunction.getType());

        assertEquals(3, jsonFunction.getKeyValuePairs().size());
        assertEquals(new JsonKeyValuePair("'foo'", "bar", true, true),
                jsonFunction.getKeyValuePair(0));

        jsonFunction.setOnNullType(JsonAggregateOnNullType.NULL);
        assertEquals(JsonAggregateOnNullType.ABSENT,
                jsonFunction.withOnNullType(JsonAggregateOnNullType.ABSENT).getOnNullType());

        jsonFunction.setUniqueKeysType(JsonAggregateUniqueKeysType.WITH);
        assertEquals(JsonAggregateUniqueKeysType.WITH, jsonFunction
                .withUniqueKeysType(JsonAggregateUniqueKeysType.WITH).getUniqueKeysType());
    }

    @Test
    void testJavaMethodsStrict() throws JSQLParserException {
        String expressionStr = "JSON_OBJECT( 'foo':bar, 'fob':baz FORMAT JSON STRICT )";
        JsonFunction jsonFunction = (JsonFunction) CCJSqlParserUtil.parseExpression(expressionStr);

        assertTrue(jsonFunction.isStrict());

        jsonFunction.withStrict(false);

        assertEquals("JSON_OBJECT( 'foo':bar, 'fob':baz FORMAT JSON ) ", jsonFunction.toString());

    }

    @Test
    void testJavaMethodsAllColumns() throws JSQLParserException {
        String expressionStr = "JSON_OBJECT(* NULL ON NULL)";
        JsonFunction jsonFunction = (JsonFunction) CCJSqlParserUtil.parseExpression(expressionStr);

        assertEquals(1, jsonFunction.getKeyValuePairs().size());
        JsonKeyValuePair kv = jsonFunction.getKeyValuePair(0);
        assertNotNull(kv);

        assertNull(kv.getValue());
        assertInstanceOf(AllColumns.class, kv.getKey());
    }

    @Test
    void testJavaMethodsAllTableColumns() throws JSQLParserException {
        String expressionStr = "JSON_OBJECT(a.*, b.* NULL ON NULL)";
        JsonFunction jsonFunction = (JsonFunction) CCJSqlParserUtil.parseExpression(expressionStr);

        assertEquals(2, jsonFunction.getKeyValuePairs().size());

        JsonKeyValuePair kv1 = jsonFunction.getKeyValuePair(0);
        assertNotNull(kv1);
        assertInstanceOf(AllTableColumns.class, kv1.getKey());
        assertNull(kv1.getValue());

        JsonKeyValuePair kv2 = jsonFunction.getKeyValuePair(1);
        assertNotNull(kv2);
        assertInstanceOf(AllTableColumns.class, kv2.getKey());
        assertNull(kv2.getValue());

    }

    @Test
    void testIssue1753JSonObjectAggWithColumns() throws JSQLParserException {
        String sqlStr = "SELECT JSON_OBJECTAGG( KEY q.foo VALUE q.bar) FROM dual";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr);

        sqlStr = "SELECT JSON_OBJECTAGG(foo, bar) FROM dual";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr);
    }
}
