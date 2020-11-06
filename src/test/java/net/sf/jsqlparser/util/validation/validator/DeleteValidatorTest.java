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

public class DeleteValidatorTest extends ValidationTestAsserts {

    @Test
    public void testValidationDelete() throws JSQLParserException {
        String sql = "DELETE FROM tab1 WHERE ref IN (SELECT id FROM tab2 WHERE criteria = ?); "
                + "DELETE FROM tab2 t2 WHERE t2.criteria = ?;";
        validateNoErrors(sql, 2, DatabaseType.DATABASES);
    }

    @Test
    public void testValidationDeleteNotAllowed() throws JSQLParserException {
        String sql = "DELETE FROM tab2 t2 WHERE t2.criteria = ?;";
        validateNotAllowed(sql, 1, 1, FeaturesAllowed.SELECT.copy().add(FeaturesAllowed.JDBC), Feature.delete);
    }

    @Test
    public void testValidationDeleteSupportedAndNotSupported() throws JSQLParserException {
        String sql = "DELETE a1, a2 FROM t1 AS a1 INNER JOIN t2 AS a2 WHERE a1.id = a2.id;";
        validateNotSupported(sql, 1, 1, Arrays.asList(DatabaseType.H2), Feature.deleteTables, Feature.deleteJoin);
        validateNoErrors(sql, 1, DatabaseType.MARIADB, DatabaseType.MYSQL);
    }


    @Test
    public void testValidationDeleteLimitOrderBy() throws JSQLParserException {
        String sql = "DELETE FROM table t WHERE t.criteria > 5 ORDER BY t.criteria LIMIT 1;";
        validateNoErrors(sql, 1, DatabaseType.MARIADB, DatabaseType.MYSQL);
    }

}
