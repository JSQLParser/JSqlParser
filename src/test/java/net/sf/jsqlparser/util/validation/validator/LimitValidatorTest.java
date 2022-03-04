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
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.util.validation.ValidationTestAsserts;
import net.sf.jsqlparser.util.validation.feature.DatabaseType;
import org.junit.jupiter.api.Test;

public class LimitValidatorTest extends ValidationTestAsserts {

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

}
