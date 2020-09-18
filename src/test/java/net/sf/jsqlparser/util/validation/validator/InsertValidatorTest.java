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

import java.util.Arrays;
import org.junit.Test;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.util.validation.ValidationTestAsserts;
import net.sf.jsqlparser.util.validation.feature.DatabaseType;
import net.sf.jsqlparser.util.validation.feature.FeaturesAllowed;

public class InsertValidatorTest extends ValidationTestAsserts {

    @Test
    public void testValidationInsert() throws JSQLParserException {
        String sql = "INSERT INTO tab1 (a, b) VALUES (5, 'val')";
        validateNoErrors(sql, 1, DatabaseType.values());
    }

    @Test
    public void testValidationInsertNotAllowed() throws JSQLParserException {
        String sql = "INSERT INTO tab1 (a, b, c) VALUES (5, 'val', ?)";
        validateNotAllowed(sql, 1, 1, FeaturesAllowed.SELECT.copy().add(FeaturesAllowed.JDBC), Feature.insert,
                Feature.insertValues);
    }

    @Test
    public void testValidationInsertSelect() throws JSQLParserException {
        String sql = "INSERT INTO tab1 (a, b, c) SELECT col1, col2, ? FROM tab2 WHERE col3 = ?";
        validateNoErrors(sql, 1, DatabaseType.DATABASES);
    }

    @Test
    public void testInsertWithReturning() throws JSQLParserException {
        for (String sql : Arrays.asList("INSERT INTO mytable (mycolumn) VALUES ('1') RETURNING id",
                "INSERT INTO mytable (mycolumn) VALUES ('1') RETURNING id AS a1, id2 AS a2")) {
            validateNoErrors(sql, 1, DatabaseType.POSTGRESQL, DatabaseType.MARIADB);
        }
    }

    @Test
    public void testInsertWithReturningAll() throws JSQLParserException {
        String sql = "INSERT INTO mytable (mycolumn) VALUES ('1') RETURNING *";
        validateNoErrors(sql, 1, DatabaseType.POSTGRESQL);
    }

    @Test
    public void testDuplicateKey() throws JSQLParserException {
        String sql = "INSERT INTO Users0 (UserId, Key, Value) VALUES (51311, 'T_211', 18) ON DUPLICATE KEY UPDATE Value = 18";
        validateNoErrors(sql, 1, DatabaseType.MARIADB, DatabaseType.MYSQL);
    }

    @Test
    public void testInsertSetInDeparsing() throws JSQLParserException {
        String sql = "INSERT INTO mytable SET col1 = 12, col2 = name1 * name2;";
        validateNoErrors(sql, 1, DatabaseType.MARIADB, DatabaseType.MYSQL);
    }

    @Test
    public void testInsertMultiRowValue() throws JSQLParserException {
        String sql = "INSERT INTO mytable (col1, col2) VALUES (a, b), (d, e)";
        validateNoErrors(sql, 1, DatabaseType.SQLSERVER);
    }

}
