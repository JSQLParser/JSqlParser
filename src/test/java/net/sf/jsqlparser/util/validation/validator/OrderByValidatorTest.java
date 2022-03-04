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

import net.sf.jsqlparser.util.validation.ValidationTestAsserts;
import net.sf.jsqlparser.util.validation.feature.DatabaseType;
import org.junit.jupiter.api.Test;

public class OrderByValidatorTest extends ValidationTestAsserts {

    @Test
    public void testOrderBy() {
        String sql = "SELECT * FROM tab ORDER BY a ASC, b DESC, c";
        validateNoErrors(sql, 1, DatabaseType.DATABASES);
    }

    @Test
    public void testOrderByNullOrdering() {
        String sql = "SELECT * FROM tab ORDER BY a ASC NULLS FIRST, b DESC NULLS LAST";
        validateNoErrors(sql, 1, DatabaseType.ORACLE, DatabaseType.POSTGRESQL,
                DatabaseType.H2);
    }

}
