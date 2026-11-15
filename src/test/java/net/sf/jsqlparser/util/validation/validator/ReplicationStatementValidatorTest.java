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

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.util.validation.ValidationTestAsserts;
import net.sf.jsqlparser.util.validation.feature.DatabaseType;
import net.sf.jsqlparser.util.validation.feature.FeaturesAllowed;
import net.sf.jsqlparser.util.validation.feature.MariaDbVersion;
import net.sf.jsqlparser.util.validation.feature.MySqlVersion;
import net.sf.jsqlparser.util.validation.feature.OracleVersion;
import net.sf.jsqlparser.util.validation.feature.PostgresqlVersion;
import org.junit.jupiter.api.Test;

public class ReplicationStatementValidatorTest extends ValidationTestAsserts {
    private static final String[] REPLICATION = {
            "CHANGE REPLICATION SOURCE TO SOURCE_HOST = 'h', SOURCE_PORT = 3306",
            "CHANGE REPLICATION FILTER REPLICATE_DO_DB = (d1)",
            "START REPLICA IO_THREAD",
            "STOP REPLICA FOR CHANNEL 'c1'",
            "RESET BINARY LOGS AND GTIDS",
            "RESET REPLICA ALL"};

    @Test
    public void testReplicationStatementsAllowedForMySql() throws JSQLParserException {
        for (String sql : REPLICATION) {
            validateNoErrors(sql, 1, MySqlVersion.V8_0, DatabaseType.MYSQL);
            validate(sql, 1, 1, MariaDbVersion.V10_5_4);
            validate(sql, 1, 1, PostgresqlVersion.V14);
        }
    }

    @Test
    public void testPurgeBinaryLogsAllowedForMySqlAndMariaDb() throws JSQLParserException {
        validateNoErrors("PURGE BINARY LOGS TO 'b.000001'", 1, MySqlVersion.V8_0,
                MariaDbVersion.V10_5_4, DatabaseType.MYSQL, DatabaseType.MARIADB);
        validate("PURGE BINARY LOGS TO 'b.000001'", 1, 1, OracleVersion.V19C);
        // Oracle's PURGE keeps its own feature
        validateNoErrors("PURGE TABLE t", 1, OracleVersion.V19C);
        validate("PURGE TABLE t", 1, 1, MySqlVersion.V8_0);
    }

    @Test
    public void testFeatureNotAllowed() throws JSQLParserException {
        validateNotAllowed("CHANGE REPLICATION SOURCE TO SOURCE_HOST = 'h'", 1, 1,
                FeaturesAllowed.SELECT, Feature.changeReplicationSource);
        validateNotAllowed("CHANGE REPLICATION FILTER REPLICATE_DO_DB = ()", 1, 1,
                FeaturesAllowed.SELECT, Feature.changeReplicationFilter);
        validateNotAllowed("START REPLICA", 1, 1, FeaturesAllowed.SELECT, Feature.startReplica);
        validateNotAllowed("STOP REPLICA", 1, 1, FeaturesAllowed.SELECT, Feature.stopReplica);
        validateNotAllowed("RESET REPLICA ALL", 1, 1, FeaturesAllowed.SELECT,
                Feature.resetReplication);
        validateNotAllowed("PURGE BINARY LOGS BEFORE NOW()", 1, 1, FeaturesAllowed.SELECT,
                Feature.purgeBinaryLogs);
    }
}
