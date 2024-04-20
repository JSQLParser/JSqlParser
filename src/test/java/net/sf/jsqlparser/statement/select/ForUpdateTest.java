package net.sf.jsqlparser.statement.select;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Test;

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
}
