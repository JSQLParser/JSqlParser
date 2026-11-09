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

public class LockStatementValidatorTest extends ValidationTestAsserts {

    @Test
    public void testTableLocksAllowedForMySqlAndMariaDb() throws JSQLParserException {
        for (String sql : new String[] {"LOCK TABLES t READ, u AS a WRITE", "UNLOCK TABLES"}) {
            validateNoErrors(sql, 1, MySqlVersion.V8_0, MariaDbVersion.V10_5_4,
                    DatabaseType.MYSQL, DatabaseType.MARIADB);
        }
    }

    @Test
    public void testBackupLocksAllowedForMySqlOnly() throws JSQLParserException {
        for (String sql : new String[] {"LOCK INSTANCE FOR BACKUP", "UNLOCK INSTANCE"}) {
            validateNoErrors(sql, 1, MySqlVersion.V8_0, DatabaseType.MYSQL);
            validate(sql, 1, 1, MariaDbVersion.V10_5_4);
        }
    }

    @Test
    public void testPostgresqlAndOracleLocksAllowed() throws JSQLParserException {
        validateNoErrors("LOCK TABLE t IN SHARE MODE NOWAIT", 1, PostgresqlVersion.V14,
                OracleVersion.V19C, DatabaseType.POSTGRESQL, DatabaseType.ORACLE);
    }

    @Test
    public void testUnlockNotAllowedForPostgresql() throws JSQLParserException {
        validate("UNLOCK TABLES", 1, 1, PostgresqlVersion.V14);
    }

    @Test
    public void testFeatureNotAllowed() throws JSQLParserException {
        validateNotAllowed("LOCK TABLES t WRITE", 1, 1, FeaturesAllowed.SELECT,
                Feature.lock);
        validateNotAllowed("UNLOCK TABLES", 1, 1, FeaturesAllowed.SELECT, Feature.unlockTables);
        validateNotAllowed("LOCK INSTANCE FOR BACKUP", 1, 1, FeaturesAllowed.SELECT,
                Feature.backupLock);
    }
}
