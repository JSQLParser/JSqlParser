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

import org.junit.Test;
import net.sf.jsqlparser.util.validation.ValidationTestAsserts;
import net.sf.jsqlparser.util.validation.feature.DatabaseType;

public class ValuesStatementValidatorTest extends ValidationTestAsserts {

    @Test
    public void testValidateValuesStatement() {
        validateNoErrors(
                "WITH w (col1, col2, col3) AS (VALUES ('Header1', 'Header2', 'Header3') UNION ALL SELECT a, b, c FROM tab) SELECT * FROM w",
                1, DatabaseType.SQLSERVER);
    }

}
