/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2020 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.validation.validator;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.util.validation.ValidationTestAsserts;
import net.sf.jsqlparser.util.validation.feature.DatabaseType;
import net.sf.jsqlparser.util.validation.feature.FeaturesAllowed;
import org.junit.jupiter.api.Test;

public class UpdateValidatorTest extends ValidationTestAsserts {

    @Test
    public void testValidationUpdate() throws JSQLParserException {
        String sql = "UPDATE tab1 SET ref = 5 WHERE id = 10;";
        validateNoErrors(sql, 1, DatabaseType.values());
    }

    @Test
    public void testValidationUpdateNotAllowed() throws JSQLParserException {
        String sql = "UPDATE tab1 SET ref = ? WHERE id = ?;";
        validateNotAllowed(sql, 1, 1, FeaturesAllowed.SELECT.copy().add(FeaturesAllowed.JDBC),
                Feature.update);
    }

    @Test
    public void testUpdateWithFrom() throws JSQLParserException {
        String sql = "UPDATE table1 SET columna = 5 FROM table1 LEFT JOIN table2 ON col1 = col2";
        validateNoErrors(sql, 1, DatabaseType.SQLSERVER);
    }

    @Test
    public void testUpdateMultiTable() throws JSQLParserException {
        String sql =
                "UPDATE T1, T2 SET T1.C2 = T2.C2, T2.C3 = 'UPDATED' WHERE T1.C1 = T2.C1 AND T1.C2 < 10";
        validateNoErrors(sql, 1, DatabaseType.MYSQL, DatabaseType.MARIADB);
    }

    @Test
    public void testUpdateWithSelect() throws JSQLParserException {
        String sql =
                "UPDATE mytable t1 SET (col1, col2, col3) = (SELECT a, b, c FROM mytable2 t2 WHERE t2.id = t1.id)";
        validateNoErrors(sql, 1, DatabaseType.ORACLE);
    }

    @Test
    public void testUpdateWithReturningAll() throws JSQLParserException {
        String sql = "UPDATE tablename SET col = 'thing' WHERE id = 1 RETURNING *";
        validateNoErrors(sql, 1, DatabaseType.POSTGRESQL, DatabaseType.ORACLE);
    }

    @Test
    public void testUpdateWithReturningList() throws JSQLParserException {
        String sql =
                "UPDATE tablename SET col = 'thing' WHERE id = 1 RETURNING col_1, col_2, col_3";
        validateNoErrors(sql, 1, DatabaseType.POSTGRESQL, DatabaseType.ORACLE);
    }

    @Test
    public void testUpdateWithOrderByAndLimit() throws JSQLParserException {
        String sql = "UPDATE tablename SET col = 'thing' WHERE ref > 10 ORDER BY col LIMIT 10";
        validateNoErrors(sql, 1, DatabaseType.MYSQL, DatabaseType.MARIADB);
    }

}
