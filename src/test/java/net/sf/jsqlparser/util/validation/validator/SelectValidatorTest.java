package net.sf.jsqlparser.util.validation.validator;

import java.util.Arrays;
import org.junit.Test;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.util.validation.ValidationTestAsserts;
import net.sf.jsqlparser.util.validation.feature.DatabaseType;
import net.sf.jsqlparser.util.validation.feature.FeaturesAllowed;
import net.sf.jsqlparser.util.validation.feature.MariaDbVersion;

public class SelectValidatorTest extends ValidationTestAsserts {

    @Test
    public void testValidationSelectNotAllowed() throws JSQLParserException {
        String sql = "SELECT 1";
        validateNotAllowed(sql, 1, 1, FeaturesAllowed.DDL, Feature.select);
    }

    @Test
    public void testValidationSelectDistinct() throws JSQLParserException {
        String sql = "SELECT DISTINCT a, b FROM tab";
        validateNoErrors(sql, 1, DatabaseType.DATABASES);
    }

    @Test
    public void testValidationSelectUnique() throws JSQLParserException {
        String sql = "SELECT UNIQUE a, b FROM tab";
        validateNoErrors(sql, 1, DatabaseType.ORACLE, MariaDbVersion.ORACLE_MODE);
    }

    @Test
    public void testValidationSelectGroupBy() {
        String sql = "SELECT MAX(a, b, c), COUNT(*), D FROM tab1 GROUP BY D";
        validateNoErrors(sql, 1, DatabaseType.DATABASES);
    }

    @Test
    public void testValidationHaving() throws JSQLParserException {
        String sql = "SELECT MAX(tab1.b) FROM tab1 WHERE a > 34 GROUP BY tab1.b HAVING MAX(tab1.b) > 56";
        validateNoErrors(sql, 1, DatabaseType.DATABASES);
    }

    @Test
    public void testValidationLimitOffset() throws JSQLParserException {
        String sql = "SELECT * FROM mytable WHERE mytable.col = 9 LIMIT 3, ?";
        validateNoErrors(sql, 1, DatabaseType.MARIADB, DatabaseType.MYSQL);
    }

    @Test
    public void testValidationLimitAndOffset() throws JSQLParserException {
        for (String sql : Arrays.asList("SELECT * FROM mytable WHERE mytable.col = 9 LIMIT 3",
                "SELECT * FROM mytable WHERE mytable.col = 9 LIMIT ? OFFSET 3",
                "SELECT * FROM mytable WHERE mytable.col = 9 OFFSET ?")) {
            validateNoErrors(sql, 1, DatabaseType.MARIADB, DatabaseType.MYSQL, DatabaseType.POSTGRESQL);
        }
    }

    @Test
    public void testValidationFetchAndOffset() throws JSQLParserException {
        for (String sql : Arrays.asList(
                "SELECT * FROM mytable t WHERE t.col = 9 ORDER BY t.id FETCH FIRST 5 ROWS ONLY",
                "SELECT * FROM mytable t WHERE t.col = 9 ORDER BY t.id OFFSET 3 ROWS",
                "SELECT * FROM mytable t WHERE t.col = 9 ORDER BY t.id OFFSET 3 ROWS FETCH NEXT 5 ROWS ONLY")) {
            validateNoErrors(sql, 1, DatabaseType.ORACLE, DatabaseType.SQLSERVER);
        }
    }

    @Test
    public void testValidationUnion() throws JSQLParserException {
        String sql = "SELECT * FROM mytable WHERE mytable.col = 9 UNION " //
                + "SELECT * FROM mytable3 WHERE mytable3.col = ?";
        validateNoErrors(sql, 1, DatabaseType.DATABASES);
    }

    @Test
    public void testValidationSqlIntersect() throws Exception {
        String sql = "(SELECT * FROM a) INTERSECT (SELECT * FROM b)";
        validateNoErrors(sql, 1, DatabaseType.ORACLE, DatabaseType.SQLSERVER, DatabaseType.MARIADB,
                DatabaseType.POSTGRESQL, DatabaseType.H2);
    }

    @Test
    public void testValidationForUpdateWaitWithTimeout() throws JSQLParserException {
        String sql = "SELECT * FROM mytable FOR UPDATE WAIT 60";
        validateNoErrors(sql, 1, DatabaseType.ORACLE, DatabaseType.MARIADB);
    }

    @Test
    public void testValidationForUpdateNoWait() throws JSQLParserException {
        String sql = "SELECT * FROM mytable FOR UPDATE NOWAIT";
        validateNoErrors(sql, 1, DatabaseType.ORACLE, DatabaseType.MARIADB,
                DatabaseType.POSTGRESQL, DatabaseType.MYSQL);
    }

    @Test
    public void testValidationJoinOuterSimple() throws JSQLParserException {
        String sql = "SELECT * FROM foo AS f, OUTER bar AS b WHERE f.id = b.id";
        validateNotSupported(sql, 1, 1, DatabaseType.ORACLE, Feature.joinOuterSimple);
    }

    @Test
    public void testValidationJoin() throws JSQLParserException {
        for (String sql : Arrays.asList("SELECT t1.col, t2.col, t1.id FROM tab1 t1, tab2 t2 WHERE t1.id = t2.id",
                "SELECT t1.col, t2.col, t1.id FROM tab1 t1 JOIN tab2 t2 ON t1.id = t2.id",
                "SELECT t1.col, t2.col, t1.id FROM tab1 t1 INNER JOIN tab2 t2 ON t1.id = t2.id")) {
            validateNoErrors(sql, 1, DatabaseType.DATABASES);
        }
    }

    @Test
    public void testOracleHierarchicalQuery() throws JSQLParserException {
        String sql = "SELECT last_name, employee_id, manager_id, LEVEL FROM employees START WITH employee_id = 100 CONNECT BY PRIOR employee_id = manager_id ORDER SIBLINGS BY last_name";
        validateNoErrors(sql, 1, DatabaseType.ORACLE);
    }

    @Test
    public void testOracleJoin() throws JSQLParserException {
        validateNoErrors("SELECT * FROM tabelle1, tabelle2 WHERE tabelle1.a = tabelle2.b(+)", 1, 
                DatabaseType.ORACLE);
    }

    @Test
    public void testValidationLeftRightJoin() throws JSQLParserException {
        for (String sql : Arrays
                .asList("SELECT t1.col, t2.col, t1.id FROM tab1 t1 LEFT JOIN tab2 t2 ON t1.id = t2.id",
                        "SELECT t1.col, t2.col, t1.id FROM tab1 t1 LEFT OUTER JOIN tab2 t2 ON t1.id = t2.id",
                        "SELECT t1.col, t2.col, t1.id FROM tab1 t1 RIGHT JOIN tab2 t2 ON t1.id = t2.id",
                        "SELECT t1.col, t2.col, t1.id FROM tab1 t1 RIGHT OUTER JOIN tab2 t2 ON t1.id = t2.id",
                        "SELECT t1.col, t2.col, t1.id FROM tab1 t1 OUTER JOIN tab2 t2 ON t1.id = t2.id")) {
            validateNoErrors(sql, 1, DatabaseType.DATABASES);
        }
    }

    @Test
    public void testValidationWith() throws JSQLParserException {
        String statement = "WITH DINFO (DEPTNO, AVGSALARY, EMPCOUNT) AS "
                + "(SELECT OTHERS.WORKDEPT, AVG(OTHERS.SALARY), COUNT(*) FROM EMPLOYEE AS OTHERS "
                + "GROUP BY OTHERS.WORKDEPT), DINFOMAX AS (SELECT MAX(AVGSALARY) AS AVGMAX FROM DINFO) "
                + "SELECT THIS_EMP.EMPNO, THIS_EMP.SALARY, DINFO.AVGSALARY, DINFO.EMPCOUNT, DINFOMAX.AVGMAX "
                + "FROM EMPLOYEE AS THIS_EMP INNER JOIN DINFO INNER JOIN DINFOMAX "
                + "WHERE THIS_EMP.JOB = 'SALESREP' AND THIS_EMP.WORKDEPT = DINFO.DEPTNO";
        validateNoErrors(statement, 1, DatabaseType.DATABASES);
    }

    @Test
    public void testValidationWithRecursive() throws JSQLParserException {
        String statement = "WITH RECURSIVE t (n) AS ((SELECT 1) UNION ALL (SELECT n + 1 FROM t WHERE n < 100)) SELECT sum(n) FROM t";
        validateNoErrors(statement, 1, DatabaseType.H2, DatabaseType.MARIADB, DatabaseType.MYSQL,
                DatabaseType.SQLSERVER, DatabaseType.POSTGRESQL);
        validateNotSupported(statement, 1, 1, DatabaseType.ORACLE, Feature.withItemRecursive);
    }

    @Test
    public void testSelectMulipleExpressionList() {
        String sql = "SELECT * FROM mytable WHERE (SSN, SSM) IN (('11111111111111', '22222222222222'))";
        validateNoErrors(sql, 1, DatabaseType.DATABASES);
    }

}
