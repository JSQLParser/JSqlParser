/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2023 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.statement.select.PlainSelect;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;

class JsonExpressionTest {

    @Test
    void testIssue1792() throws JSQLParserException {
        String sqlStr =
                "SELECT ''::JSON -> 'obj'::TEXT";
        assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        sqlStr =
                "SELECT ('{\"obj\":{\"field\": \"value\"}}'::JSON -> 'obj'::TEXT ->> 'field'::TEXT)";
        assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        sqlStr =
                "SELECT\n"
                        + " CASE\n"
                        + "    WHEN true\n"
                        + "    THEN (SELECT ((('{\"obj\":{\"field\": \"value\"}}'::JSON -> 'obj'::TEXT ->> 'field'::TEXT))))\n"
                        + " END";
        assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testSnowflakeGetOperator() throws JSQLParserException {
        String sqlStr = "SELECT v:'attr[0].name' FROM vartab;";
        PlainSelect st = (PlainSelect) assertSqlCanBeParsedAndDeparsed(sqlStr, true);
        Assertions.assertInstanceOf(JsonExpression.class, st.getSelectItem(0).getExpression());
    }

    @Test
    void testDataBricksExtractPathOperator() throws JSQLParserException {
        String sqlStr = "SELECT C1:PRICE J FROM VALUES('{\"price\":5}')AS T(C1)";
        PlainSelect st = (PlainSelect) assertSqlCanBeParsedAndDeparsed(sqlStr, true);
        Assertions.assertInstanceOf(JsonExpression.class, st.getSelectItem(0).getExpression());
    }

    @Test
    void testParenthesedJsonExpressionsIssue1792() throws JSQLParserException {
        String sqlStr =
                "SELECT table_a.b_e_t,\n"
                        + "       CASE\n"
                        + "           WHEN table_a.g_o_a_c IS NULL THEN 'a'\n"
                        + "           ELSE table_a.g_o_a_c\n"
                        + "           END                                        AS e_cd,\n"
                        + "       CASE\n"
                        + "           WHEN table_a.a_f_t IS NULL THEN 'b'\n"
                        + "           ELSE table_a.a_f_t\n"
                        + "           END                                        AS a_f_t,\n"
                        + "       COUNT(1)                                       AS count,\n"
                        + "       ROUND(ABS(SUM(table_a.gb_eq))::NUMERIC, 2) AS total_x\n"
                        + "FROM (SELECT table_x.b_e_t,\n"
                        + "             table_x.b_e_a,\n"
                        + "             table_y.g_o_a_c,\n"
                        + "             table_z.a_f_t,\n"
                        + "             CASE\n"
                        + "                 WHEN table_x.b_e_a IS NOT NULL THEN table_x.b_e_a::DOUBLE PRECISION /\n"
                        + "                                                                 schema_z.g_c_r(table_x.c_c,\n"
                        + "                                                                                  'x'::CHARACTER VARYING,\n"
                        + "                                                                                  table_x.r_ts::DATE)\n"
                        + "                 ELSE\n"
                        + "                     CASE\n"
                        + "                         WHEN table_x.b_e_t::TEXT = 'p_e'::TEXT THEN (SELECT ((\n"
                        + "                                 (table_x.pld::JSON -> 'p_d'::TEXT) ->>\n"
                        + "                                 's_a'::TEXT)::DOUBLE PRECISION) / schema_z.g_c_r(fba.s_c_c,\n"
                        + "                                                                                             'x'::CHARACTER VARYING,\n"
                        + "                                                                                             table_x.r_ts::DATE)\n"
                        + "                                                                                                   FROM schema_z.f_b_a fba\n"
                        + "                                                                                                            JOIN schema_z.t_b_a_n_i table_y\n"
                        + "                                                                                                                 ON fba.b_a_i = table_y.f_b_a_id\n"
                        + "                                                                                                   WHERE table_y.t_ngn_id =\n"
                        + "                                                                                                         (((table_x.pld::JSON -> 'p_d'::TEXT) ->>\n"
                        + "                                                                                                           's_a_i'::TEXT)::BIGINT))\n"
                        + "                         WHEN table_x.b_e_t::TEXT = 'i_e'::TEXT\n"
                        + "                             THEN (SELECT (((table_x.pld::JSON -> 'i_d'::TEXT) ->> 'a'::TEXT)::DOUBLE PRECISION) /\n"
                        + "                                          schema_z.g_c_r(fba.s_c_c, 'x'::CHARACTER VARYING,\n"
                        + "                                                           table_x.r_ts::DATE)\n"
                        + "                                   FROM schema_z.f_b_a fba\n"
                        + "                                            JOIN schema_z.t_b_a_n_i table_y\n"
                        + "                                                 ON fba.b_a_i = table_y.f_b_a_id\n"
                        + "                                   WHERE table_y.t_ngn_id = (((table_x.pld::JSON -> 'i_d'::TEXT) ->>\n"
                        + "                                                             's_a_i'::TEXT)::BIGINT))\n"
                        + "                         WHEN table_x.b_e_t::TEXT = 'i_e_2'::TEXT\n"
                        + "                             THEN (SELECT (((table_x.pld::JSON -> 'i_d'::TEXT) ->> 'a'::TEXT)::DOUBLE PRECISION) /\n"
                        + "                                          schema_z.g_c_r(fba.s_c_c, 'x'::CHARACTER VARYING,\n"
                        + "                                                           table_x.r_ts::DATE)\n"
                        + "                                   FROM schema_z.f_b_a fba\n"
                        + "                                            JOIN schema_z.t_b_a_n_i table_y\n"
                        + "                                                 ON fba.b_a_i = table_y.f_b_a_id\n"
                        + "                                   WHERE table_y.t_ngn_id = (((table_x.pld::JSON -> 'id'::TEXT) ->>\n"
                        + "                                                             'd_i'::TEXT)::BIGINT))\n"
                        + "                         WHEN table_x.b_e_t::TEXT = 'm_e'::TEXT\n"
                        + "                             THEN (SELECT (((table_x.pld::JSON -> 'o'::TEXT) ->> 'eda'::TEXT)::DOUBLE PRECISION) /\n"
                        + "                                          schema_z.g_c_r(\n"
                        + "                                                  ((table_x.pld::JSON -> 'o'::TEXT) ->> 'dc'::TEXT)::CHARACTER VARYING,\n"
                        + "                                                  'x'::CHARACTER VARYING, table_x.r_ts::DATE))\n"
                        + "                         ELSE NULL::DOUBLE PRECISION\n"
                        + "                         END\n"
                        + "                 END AS gb_eq\n"
                        + "      FROM schema_z.baz\n"
                        + "               LEFT JOIN f_ctl.g_o_f_e_t_a_m table_y\n"
                        + "                         ON table_x.p_e_m LIKE table_y.f_e_m_p\n"
                        + "               LEFT JOIN f_ctl.g_o_c_a_t table_z\n"
                        + "                         ON table_z.c_a_t_c = table_y.g_o_a_c\n"
                        + "      WHERE table_x.p_st = 'E'\n"
                        + "     ) table_a\n"
                        + "GROUP BY 1, 2, 3";

        assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "SELECT ( JSONB_AGG(variables) " +
                    "   FILTER (WHERE variables IS NOT NULL) " +
                    "   OVER (PARTITION BY deviceid ORDER BY time)->>-1 )::JSONB AS variables\n" +
                    "FROM devices\n" +
                    ";",
            "SELECT ( JSONB_AGG(variables) " +
                    "       FILTER (WHERE variables IS NOT NULL) " +
                    "       OVER (PARTITION BY deviceid ORDER BY time)->>(0-1) )::JSONB AS variables\n"
                    +
                    "FROM devices\n" +
                    ";",
            "SELECT ( JSONB_AGG(variables) " +
                    "   FILTER (WHERE variables IS NOT NULL) " +
                    "   OVER (PARTITION BY deviceid ORDER BY time)->>(jsonb_array_length(JSONB_AGG(variables) FILTER (WHERE variables IS NOT NULL) OVER (PARTITION BY deviceid ORDER BY time))-1) )::JSONB AS variables\n"
                    +
                    "FROM devices\n" +
                    ";"})
    void testIssue2054(String sqlStr) throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testIssue2181() throws JSQLParserException {
        String sqlStr =
                "SELECT\n" +
                        "  1\n" +
                        "FROM\n" +
                        "  public.tbl\n" +
                        "WHERE\n" +
                        "  fieldd ->> 'att1' = 1\n" +
                        "  OR fieldd ->> 'att1' = 1\n" +
                        "  OR fieldd ->> 'att1' = 1\n" +
                        "  OR fieldd::jsonb -> 'att2' @> 1\n" +
                        "  OR fieldd::jsonb -> 'att2' @> 1\n" +
                        "  OR fieldd::jsonb -> 'att2' @> 1\n" +
                        "  OR fieldd::jsonb -> 'att2' @> 1\n" +
                        "  OR fieldd::jsonb -> 'att2' @> 1\n" +
                        "  OR fieldd::jsonb -> 'att2' @> 1\n" +
                        "  OR fieldd::jsonb -> 'att2' @> 1\n" +
                        "  OR fieldd::jsonb -> 'att2' @> 1\n" +
                        "  OR fieldd::jsonb -> 'att2' @> 1\n" +
                        "  OR fieldd::jsonb -> 'att2' @> 1\n" +
                        "  OR fieldd::jsonb -> 'att2' @> 1\n" +
                        "ORDER BY\n" +
                        "  att ASC\n" +
                        "LIMIT\n" +
                        "  1";
        assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }
}
