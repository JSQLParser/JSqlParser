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
import net.sf.jsqlparser.util.validation.ValidationTestAsserts;
import net.sf.jsqlparser.util.validation.feature.DatabaseType;

public class GroupByValidatorTest extends ValidationTestAsserts {

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
    public void testGroupingSets() throws JSQLParserException {
        for (String sql : Arrays.asList(
                "SELECT COL_1, COL_2, COL_3, COL_4, COL_5, COL_6 FROM TABLE_1 GROUP BY GROUPING SETS ((COL_1, COL_2, COL_3, COL_4), (COL_5, COL_6))",
                "SELECT COL_1 FROM TABLE_1 GROUP BY GROUPING SETS (COL_1)")) {
            validateNoErrors(sql, 1, DatabaseType.ORACLE, DatabaseType.POSTGRESQL, DatabaseType.SQLSERVER);
        }
    }

}
