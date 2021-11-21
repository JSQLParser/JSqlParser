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
import org.junit.jupiter.api.Test;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.util.validation.ValidationTestAsserts;
import net.sf.jsqlparser.util.validation.feature.DatabaseType;

public class SetStatementValidatorTest extends ValidationTestAsserts {

    @Test
    public void testValidateSet() throws JSQLParserException {
        for (String sql : Arrays.asList("SET statement_timeout = 0; SET deferred_name_resolution true;",
                "SET tester 5; SET v = 1, c = 3;", "SET standard_conforming_strings = on;SET statement_timeout = 0")) {
            validateNoErrors(sql, 2, DatabaseType.POSTGRESQL);
        }
    }

}
