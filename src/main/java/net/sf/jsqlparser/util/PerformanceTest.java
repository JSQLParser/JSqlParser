/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2023 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util;

import net.sf.jsqlparser.parser.CCJSqlParser;

public class PerformanceTest {
    @SuppressWarnings("PMD.ExcessiveMethodLength")
    public static void main(String[] args) throws Exception {
        String sqlStr = "SELECT  e.id\n" +
                "        , e.code\n" +
                "        , e.review_type\n" +
                "        , e.review_object\n" +
                "        , e.review_first_datetime AS reviewfirsttime\n" +
                "        , e.review_latest_datetime AS reviewnewtime\n" +
                "        , e.risk_event\n" +
                "        , e.risk_detail\n" +
                "        , e.risk_grade\n" +
                "        , e.risk_status\n" +
                "        , If( e.deal_type IS NULL\n" +
                "            OR e.deal_type = '', '--', e.deal_type ) AS dealtype\n" +
                "        , e.deal_result\n" +
                "        , If( e.deal_remark IS NULL\n" +
                "            OR e.deal_remark = '', '--', e.deal_remark ) AS dealremark\n" +
                "        , e.is_deleted\n" +
                "        , e.review_object_id\n" +
                "        , e.archive_id\n" +
                "        , e.feature AS featurename\n" +
                "        , Ifnull( ( SELECT real_name\n" +
                "                    FROM bladex.blade_user\n" +
                "                    WHERE id = e.review_first_user ), ( SELECT DISTINCT\n" +
                "                                                            real_name\n" +
                "                                                        FROM app_sys.asys_uniapp_rn_auth\n"
                +
                "                                                        WHERE uniapp_user_id = e.review_first_user\n"
                +
                "                                                            AND is_disable = 0 ) ) AS reviewfirstuser\n"
                +
                "        , Ifnull( ( SELECT real_name\n" +
                "                    FROM bladex.blade_user\n" +
                "                    WHERE id = e.review_latest_user ), (    SELECT DISTINCT\n" +
                "                                                                real_name\n" +
                "                                                            FROM app_sys.asys_uniapp_rn_auth\n"
                +
                "                                                            WHERE uniapp_user_id = e.review_latest_user\n"
                +
                "                                                                AND is_disable = 0 ) ) AS reviewnewuser\n"
                +
                "        , If( ( SELECT real_name\n" +
                "                FROM bladex.blade_user\n" +
                "                WHERE id = e.deal_user ) IS NOT NULL\n" +
                "            AND e.deal_user != - 9999, (    SELECT real_name\n" +
                "                                            FROM bladex.blade_user\n" +
                "                                            WHERE id = e.deal_user ), '--' ) AS dealuser\n"
                +
                "        , CASE\n" +
                "                WHEN 'COMPANY'\n" +
                "                    THEN Concat( (  SELECT ar.customer_name\n" +
                "                                    FROM mtp_cs.mtp_rsk_cust_archive ar\n" +
                "                                    WHERE ar.is_deleted = 0\n" +
                "                                        AND ar.id = e.archive_id ), If( (   SELECT alias\n"
                +
                "                                                                            FROM web_crm.wcrm_customer\n"
                +
                "                                                                            WHERE id = e.customer_id ) = ''\n"
                +
                "                OR (    SELECT alias\n" +
                "                        FROM web_crm.wcrm_customer\n" +
                "                        WHERE id = e.customer_id ) IS NULL, ' ', Concat( '（', ( SELECT alias\n"
                +
                "                                                                                FROM web_crm.wcrm_customer\n"
                +
                "                                                                                WHERE id = e.customer_id ), '）' ) ) )\n"
                +
                "                WHEN 'EMPLOYEE'\n" +
                "                    THEN (  SELECT Concat( auth.real_name, ' ', auth.phone )\n" +
                "                            FROM app_sys.asys_uniapp_rn_auth auth\n" +
                "                            WHERE auth.is_disable = 0\n" +
                "                                AND auth.uniapp_user_id = e.uniapp_user_id )\n" +
                "                WHEN 'DEAL'\n" +
                "                    THEN (  SELECT DISTINCT\n" +
                "                                Concat( batch.code, '-', detail.line_seq\n" +
                "                                        , ' ', Ifnull( (    SELECT DISTINCT\n" +
                "                                                                auth.real_name\n" +
                "                                                            FROM app_sys.asys_uniapp_rn_auth auth\n"
                +
                "                                                            WHERE auth.uniapp_user_id = e.uniapp_user_id\n"
                +
                "                                                                AND auth.is_disable = 0 ), ' ' ) )\n"
                +
                "                            FROM web_pym.wpym_payment_batch_detail detail\n" +
                "                                LEFT JOIN web_pym.wpym_payment_batch batch\n" +
                "                                    ON detail.payment_batch_id = batch.id\n" +
                "                            WHERE detail.id = e.review_object_id )\n" +
                "                WHEN 'TASK'\n" +
                "                    THEN (  SELECT code\n" +
                "                            FROM web_tm.wtm_task task\n" +
                "                            WHERE e.review_object_id = task.id )\n" +
                "                ELSE NULL\n" +
                "            END AS reviewobjectname\n" +
                "        , CASE\n" +
                "                WHEN 4\n" +
                "                    THEN 'HIGH_LEVEL'\n" +
                "                WHEN 3\n" +
                "                    THEN 'MEDIUM_LEVEL'\n" +
                "                WHEN 2\n" +
                "                    THEN 'LOW_LEVEL'\n" +
                "                ELSE 'HEALTHY'\n" +
                "            END AS risklevel\n" +
                "FROM mtp_cs.mtp_rsk_event e\n" +
                "WHERE e.is_deleted = 0\n" +
                "ORDER BY e.review_latest_datetime DESC\n" +
                "LIMIT 30\n" +
                ";";

        long startMillis = System.currentTimeMillis();
        for (int i = 1; i < 1000; i++) {
            final CCJSqlParser parser = new CCJSqlParser(sqlStr)
                    .withSquareBracketQuotation(false)
                    .withAllowComplexParsing(true)
                    .withBackslashEscapeCharacter(false);
            parser.Statements();
            long endMillis = System.currentTimeMillis();
            System.out.println("Duration [ms]: " + (endMillis - startMillis) / i);
        }
    }
}
