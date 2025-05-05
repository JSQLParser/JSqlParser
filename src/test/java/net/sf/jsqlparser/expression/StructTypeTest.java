/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2024 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;

class StructTypeTest {
    @Test
    void testStructTypeBigQuery() throws JSQLParserException {
        String sqlStr = "SELECT t, len, FORMAT('%T', LPAD(t, len)) AS LPAD FROM UNNEST([\n" +
                "  STRUCT('abc' AS t, 5 AS len),\n" +
                "  ('abc', 2),\n" +
                "  ('例子', 4)\n" +
                "])";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        sqlStr = "SELECT STRUCT(1, t.str_col)";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        sqlStr = "SELECT STRUCT(1 AS a, 'abc' AS b)";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        sqlStr = "SELECT STRUCT<x int64, y string>(1, t.str_col)";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testStructTypeDuckDB() throws JSQLParserException {
        // @todo: check why the white-space after the "{" is needed?!
        String sqlStr = "SELECT { t:'abc',len:5}";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        sqlStr = "SELECT UNNEST({ t:'abc', len:5 })";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        sqlStr = "SELECT * from (SELECT UNNEST([{ t:'abc', len:5 }]))";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        sqlStr = "SELECT * from (SELECT UNNEST([{ t:'abc', len:5 }, ('abc', 6) ], recursive => true))";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testStructTypeConstructorDuckDB() throws JSQLParserException {
        // @todo: check why the white-space after the "{" is needed?!
        String sqlStr = "SELECT { t:'abc',len:5}";
        List<SelectItem<?>> selectItems = List.of(
                new SelectItem<>("abc", "t"), new SelectItem<>(5, "len"));
        StructType struct = new StructType(StructType.Dialect.DUCKDB, selectItems);
        PlainSelect select = new PlainSelect().withSelectItems(new SelectItem<>(struct));
        TestUtils.assertStatementCanBeDeparsedAs(select, sqlStr, true);
    }

    @Test
    @Disabled
    void testStructTypeWithArgumentsDuckDB() throws JSQLParserException {
        // @todo: check why the white-space after the "{" is needed?!
        String sqlStr = "SELECT { t:'abc',len:5}::STRUCT( t VARCHAR, len INTEGER)";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        sqlStr = "SELECT t, len, LPAD(t, len, ' ') as padded from (\n" +
                "select Unnest([\n" +
                "  { t:'abc', len: 5}::STRUCT(t VARCHAR, len INTEGER),\n" +
                "  { t:'abc', len: 5},\n" +
                "  ('abc', 2),\n" +
                "  ('例子', 4)\n" +
                "], \"recursive\" => true))";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }
}
