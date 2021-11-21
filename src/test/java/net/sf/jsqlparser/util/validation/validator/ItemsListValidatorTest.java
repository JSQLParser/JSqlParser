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
import net.sf.jsqlparser.util.validation.ValidationTestAsserts;
import net.sf.jsqlparser.util.validation.feature.DatabaseType;
import org.junit.jupiter.api.Test;

public class ItemsListValidatorTest extends ValidationTestAsserts {

    @Test
    public void testValidationExpressionList() {
        validateNoErrors("select coalesce(a, b, c) from tab", 1, DatabaseType.DATABASES);
    }

    @Test
    public void testInsertMultiRowValue() throws JSQLParserException {
        String sql = "INSERT INTO mytable (col1, col2) VALUES (a, b), (d, e)";
        validateNoErrors(sql, 1, DatabaseType.SQLSERVER);
    }

}
