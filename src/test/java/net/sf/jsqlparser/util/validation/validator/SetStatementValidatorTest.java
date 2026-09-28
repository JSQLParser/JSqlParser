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
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.util.validation.ValidationTestAsserts;
import net.sf.jsqlparser.util.validation.feature.DatabaseType;
import net.sf.jsqlparser.util.validation.feature.FeaturesAllowed;
import org.junit.jupiter.api.Test;

public class SetStatementValidatorTest extends ValidationTestAsserts {

    @Test
    public void validatesToSeparatelyFromTheGeneralSetFeature() {
        String sql = "SET LOCAL search_path TO my_schema, public";
        validateNoErrors(sql, 1, DatabaseType.POSTGRESQL);
        validateNotAllowed(sql, 1, 1, new FeaturesAllowed("set-only", Feature.set),
                Feature.setAssignmentTo);
    }

    @Test
    public void testValidateSet() throws JSQLParserException {
        for (String sql : Arrays.asList(
                "SET statement_timeout = 0; SET deferred_name_resolution true;",
                "SET tester 5; SET v = 1, c = 3;",
                "SET standard_conforming_strings = on;SET statement_timeout = 0")) {
            validateNoErrors(sql, 2, DatabaseType.POSTGRESQL);
        }
    }

}
