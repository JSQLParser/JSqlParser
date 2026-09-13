/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.validation.validator;

import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.util.validation.ValidationTestAsserts;
import net.sf.jsqlparser.util.validation.feature.FeaturesAllowed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class UpdateSetValidationTest extends ValidationTestAsserts {
    @ParameterizedTest
    @ValueSource(strings = {"UPDATE t SET a = ?, b = 1", "UPDATE t SET a = 1, b = ?",
            "UPDATE t SET a = 1, b = ?, c = 3", "UPDATE t SET a = 1, b = 2, c = ?",
            "UPDATE t SET (b, c) = (2, ?), a = 1",
            "UPDATE t SET a = 1, b = (SELECT x FROM s WHERE x = ?)",
            "MERGE INTO t USING s ON t.id = s.id WHEN MATCHED THEN UPDATE SET a = 1, b = ?"})
    void validatesValuesInEveryAssignment(String sql) {
        validateNotAllowed(sql, 1, 1, FeaturesAllowed.DML.copy().remove(Feature.jdbcParameter),
                Feature.jdbcParameter);
        validateNoErrors(sql, 1, FeaturesAllowed.DML.copy().add(FeaturesAllowed.JDBC));
    }

    @Test
    void reportsIndependentFeaturesAcrossDifferentAssignments() {
        validateNotAllowed("UPDATE t SET a = 1, b = COALESCE(c, 0), d = ?", 1, 1,
                FeaturesAllowed.UPDATE.copy().remove(Feature.function, Feature.jdbcParameter),
                Feature.function, Feature.jdbcParameter);
    }

    @Test
    void keepsTupleAndSubqueryAssignmentsValid() {
        validateNoErrors("UPDATE t SET (a, b) = (SELECT c, d FROM s), e = 1", 1,
                FeaturesAllowed.UPDATE);
    }
}
