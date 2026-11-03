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
import net.sf.jsqlparser.util.validation.feature.DatabaseType;
import net.sf.jsqlparser.util.validation.feature.FeaturesAllowed;
import net.sf.jsqlparser.util.validation.feature.MariaDbVersion;
import net.sf.jsqlparser.util.validation.feature.MySqlVersion;
import net.sf.jsqlparser.util.validation.feature.PostgresqlVersion;
import org.junit.jupiter.api.Test;

public class MaintenanceValidatorTest extends ValidationTestAsserts {

    @Test
    public void allowedForPostgresql() {
        for (String sql : new String[] {"VACUUM (VERBOSE, ANALYZE) t", "VACUUM FULL t",
                "VACUUM", "ANALYZE VERBOSE t (a)", "ANALYZE (SKIP_LOCKED) t, s", "ANALYZE",
                "CLUSTER t USING t_pkey", "CLUSTER VERBOSE", "CHECKPOINT"}) {
            validateNoErrors(sql, 1, PostgresqlVersion.V10, PostgresqlVersion.V14,
                    DatabaseType.POSTGRESQL);
        }
    }

    @Test
    public void analyzeAllowedForMySqlAndMariaDb() {
        for (String sql : new String[] {"ANALYZE TABLE t1 UPDATE HISTOGRAM ON c1 WITH 4 BUCKETS",
                "ANALYZE NO_WRITE_TO_BINLOG TABLE t, s"}) {
            validateNoErrors(sql, 1, MySqlVersion.V8_0, MariaDbVersion.V10_5_4,
                    DatabaseType.MYSQL, DatabaseType.MARIADB);
        }
    }

    @Test
    public void notSupportedByMySql() {
        validateNotSupported("VACUUM t", 1, 1, MySqlVersion.V8_0, Feature.vacuum);
        validateNotSupported("CLUSTER t", 1, 1, MySqlVersion.V8_0, Feature.cluster);
        validateNotSupported("CHECKPOINT", 1, 1, MySqlVersion.V8_0, Feature.checkpoint);
    }

    @Test
    public void featureNotAllowed() {
        validateNotAllowed("VACUUM t", 1, 1, FeaturesAllowed.SELECT, Feature.vacuum);
        validateNotAllowed("CLUSTER", 1, 1, FeaturesAllowed.DML, Feature.cluster);
        validateNotAllowed("CHECKPOINT", 1, 1, FeaturesAllowed.DDL, Feature.checkpoint);
        validateNotAllowed("ANALYZE t", 1, 1, FeaturesAllowed.SELECT, Feature.analyze);
    }
}
