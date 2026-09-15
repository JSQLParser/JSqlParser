/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2024 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DuckDBTest {

    @Test
    void testFileTable() throws JSQLParserException {
        String sqlStr = "SELECT * FROM '/tmp/test.parquet'";
        PlainSelect select = (PlainSelect) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
        Table table = (Table) select.getFromItem();

        Assertions.assertEquals("'/tmp/test.parquet'", table.getName());
    }

    @Test
    void testCreateWithStruct() throws JSQLParserException {
        String sqlStr =
                "CREATE TABLE starbake.array_test (\n" +
                        "  keys VARCHAR[] NOT NULL,\n" +
                        "  values1 struct( field1 varchar(255), field2 double) NOT NULL,\n" +
                        "  values2 struct( field1 varchar(255), field2 double) NOT NULL\n" +
                        ");";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testGlobOperator() throws JSQLParserException {
        String sqlStr = "SELECT * FROM t WHERE b GLOB 'y*'";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testNotGlobOperator() throws JSQLParserException {
        String sqlStr = "SELECT * FROM t WHERE b NOT GLOB 'y*'";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testGlobRemainsUsableAsIdentifier() throws JSQLParserException {
        String sqlStr = "SELECT glob FROM t";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testSemiJoinWithoutLeft() throws JSQLParserException {
        String sqlStr = "SELECT * FROM t SEMI JOIN u USING (id)";
        PlainSelect select = (PlainSelect) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
        Join join = select.getJoins().get(0);

        Assertions.assertTrue(join.isSemi());
        Assertions.assertFalse(join.isLeft());
        Assertions.assertNull(((Table) select.getFromItem()).getAlias());
    }

    @Test
    void testAntiJoinWithoutLeft() throws JSQLParserException {
        String sqlStr = "SELECT * FROM t ANTI JOIN u ON t.id = u.id";
        PlainSelect select = (PlainSelect) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
        Join join = select.getJoins().get(0);

        Assertions.assertTrue(join.isAnti());
        Assertions.assertNull(((Table) select.getFromItem()).getAlias());
    }

    @Test
    void testLeftAntiJoin() throws JSQLParserException {
        String sqlStr = "SELECT * FROM t LEFT ANTI JOIN u ON t.id = u.id";
        PlainSelect select = (PlainSelect) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
        Join join = select.getJoins().get(0);

        Assertions.assertTrue(join.isLeft());
        Assertions.assertTrue(join.isAnti());
    }

    @Test
    void testAntiRemainsUsableAsIdentifier() throws JSQLParserException {
        String sqlStr = "SELECT anti FROM t anti";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }
}
