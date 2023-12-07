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
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.util.validation.ValidationTestAsserts;
import net.sf.jsqlparser.util.validation.feature.DatabaseType;
import net.sf.jsqlparser.util.validation.feature.FeaturesAllowed;
import org.junit.jupiter.api.Test;

/**
 *
 * @author jxnu-liguobin
 */

public class RefreshMaterializedViewStatementValidatorTest extends ValidationTestAsserts {

    @Test
    public void testValidationRefresh() throws Exception {
        for (String sql : Arrays.asList("REFRESH MATERIALIZED VIEW my_view")) {
            validateNoErrors(sql, 1, DatabaseType.POSTGRESQL);
        }
    }

    @Test
    public void testValidationRefreshWithData() throws Exception {
        for (String sql : Arrays
                .asList("REFRESH MATERIALIZED VIEW CONCURRENTLY my_view WITH DATA")) {
            validateNoErrors(sql, 1, DatabaseType.POSTGRESQL);
        }

        for (String sql : Arrays.asList("REFRESH MATERIALIZED VIEW my_view WITH DATA")) {
            validateNoErrors(sql, 1, DatabaseType.POSTGRESQL);
        }
    }

    @Test
    public void testValidationRefreshWithConcurrently() throws Exception {
        for (String sql : Arrays.asList("REFRESH MATERIALIZED VIEW CONCURRENTLY my_view")) {
            validateNoErrors(sql, 1, DatabaseType.POSTGRESQL);
        }
    }


    @Test
    public void testValidationRefreshNotAllowed() throws Exception {
        for (String sql : Arrays.asList("REFRESH MATERIALIZED VIEW my_view")) {
            validateNotAllowed(sql, 1, 1, FeaturesAllowed.DML,
                    Feature.refreshMaterializedView,
                    Feature.refreshMaterializedConcurrentlyView,
                    Feature.refreshMaterializedWithNoDataView,
                    Feature.refreshMaterializedWithDataView);
        }
    }
}
