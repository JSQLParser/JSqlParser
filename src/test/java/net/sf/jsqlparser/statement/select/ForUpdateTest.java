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
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ForUpdateTest {

    @Test
    void testOracleForUpdate() throws JSQLParserException {
        String sqlStr = "SELECT e.employee_id, e.salary, e.commission_pct\n"
                + "   FROM employees e, departments d\n"
                + "   WHERE job_id = 'SA_REP'\n"
                + "   AND e.department_id = d.department_id\n"
                + "   AND location_id = 2500\n"
                + "   ORDER BY e.employee_id\n"
                + "   FOR UPDATE;\n";

        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        sqlStr = "SELECT e.employee_id, e.salary, e.commission_pct\n"
                + "   FROM employees e JOIN departments d\n"
                + "   USING (department_id)\n"
                + "   WHERE job_id = 'SA_REP'\n"
                + "   AND location_id = 2500\n"
                + "   ORDER BY e.employee_id\n"
                + "   FOR UPDATE OF e.salary;";

        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testMySqlIssue1995() throws JSQLParserException {
        String sqlStr = "select * from t_demo where a = 1 order by b asc limit 1 for update";

        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testForUpdateMultipleTables() throws JSQLParserException {
        String sqlStr =
                "select employee_id from (select employee_id+1 as employee_id from employees)"
                        + " for update of a, b.c, d skip locked";

        Statement stmt = TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
        PlainSelect plainSelect = (PlainSelect) stmt;

        assertThat(plainSelect.getForMode()).isEqualTo(ForMode.UPDATE);
        assertThat(plainSelect.getForUpdateTables()).hasSize(3);
        assertThat(plainSelect.isSkipLocked()).isTrue();

        ForUpdateClause forUpdate = plainSelect.getForUpdate();
        assertThat(forUpdate).isNotNull();
        assertThat(forUpdate.isForUpdate()).isTrue();
        assertThat(forUpdate.getTables()).hasSize(3);
        assertThat(forUpdate.isSkipLocked()).isTrue();
    }

    @Test
    void testForUpdateOrderByAfter() throws JSQLParserException {
        String sqlStr =
                "select su.ttype, su.cid, su.s_id, sessiontimezone from sku su"
                        + " where (nvl(su.up, 'n') = 'n' and su.ttype = :b0)"
                        + " for update of su.up order by su.d";

        Statement stmt = TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
        PlainSelect plainSelect = (PlainSelect) stmt;

        assertThat(plainSelect.getForMode()).isEqualTo(ForMode.UPDATE);
        assertThat(plainSelect.getForUpdateTables()).hasSize(1);
        assertThat(plainSelect.getOrderByElements()).hasSize(1);
        assertThat(plainSelect.isForUpdateBeforeOrderBy()).isTrue();
    }

    @Test
    void testForUpdateDetection() throws JSQLParserException {
        Statement stmt = CCJSqlParserUtil.parse("SELECT * FROM users FOR UPDATE");
        PlainSelect plainSelect = (PlainSelect) stmt;

        // ForMode is set for FOR UPDATE
        assertThat(plainSelect.getForMode()).isEqualTo(ForMode.UPDATE);

        // getForUpdate() returns a ForUpdateClause
        ForUpdateClause forUpdate = plainSelect.getForUpdate();
        assertThat(forUpdate).isNotNull();
        assertThat(forUpdate.isForUpdate()).isTrue();
        assertThat(forUpdate.isForShare()).isFalse();
        assertThat(forUpdate.getTables()).isNull();
    }

    @Test
    void testForShare() throws JSQLParserException {
        Statement stmt = CCJSqlParserUtil.parse("SELECT * FROM users FOR SHARE");
        PlainSelect plainSelect = (PlainSelect) stmt;

        assertThat(plainSelect.getForMode()).isEqualTo(ForMode.SHARE);

        ForUpdateClause forUpdate = plainSelect.getForUpdate();
        assertThat(forUpdate).isNotNull();
        assertThat(forUpdate.isForShare()).isTrue();
        assertThat(forUpdate.isForUpdate()).isFalse();
    }

    @Test
    void testForUpdateNowait() throws JSQLParserException {
        String sqlStr =
                "select employee_id from (select employee_id+1 as employee_id from employees)"
                        + " for update of employee_id nowait";
        Statement stmt = TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
        PlainSelect plainSelect = (PlainSelect) stmt;

        assertThat(plainSelect.getForMode()).isEqualTo(ForMode.UPDATE);
        assertThat(plainSelect.isNoWait()).isTrue();

        ForUpdateClause forUpdate = plainSelect.getForUpdate();
        assertThat(forUpdate.isNoWait()).isTrue();
        assertThat(forUpdate.isSkipLocked()).isFalse();
    }

    @Test
    void testForUpdateWait() throws JSQLParserException {
        String sqlStr =
                "select employee_id from (select employee_id+1 as employee_id from employees)"
                        + " for update of employee_id wait 10";
        Statement stmt = TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
        PlainSelect plainSelect = (PlainSelect) stmt;

        assertThat(plainSelect.getWait()).isNotNull();
        assertThat(plainSelect.getWait().getTimeout()).isEqualTo(10L);

        ForUpdateClause forUpdate = plainSelect.getForUpdate();
        assertThat(forUpdate.getWait()).isNotNull();
        assertThat(forUpdate.getWait().getTimeout()).isEqualTo(10L);
    }
}
