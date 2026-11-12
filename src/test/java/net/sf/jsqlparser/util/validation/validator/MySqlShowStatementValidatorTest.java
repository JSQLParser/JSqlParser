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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.util.validation.ValidationTestAsserts;
import net.sf.jsqlparser.util.validation.feature.DatabaseType;
import net.sf.jsqlparser.util.validation.feature.FeaturesAllowed;
import net.sf.jsqlparser.util.validation.feature.MariaDbVersion;
import net.sf.jsqlparser.util.validation.feature.MySqlVersion;
import net.sf.jsqlparser.util.validation.feature.PostgresqlVersion;
import net.sf.jsqlparser.util.validation.metadata.AbstractDatabaseMetaDataCapability;
import net.sf.jsqlparser.util.validation.metadata.Named;
import net.sf.jsqlparser.util.validation.metadata.NamesLookup;
import org.junit.jupiter.api.Test;

public class MySqlShowStatementValidatorTest extends ValidationTestAsserts {

    @Test
    public void testAllowedForMySqlAndMariaDb() throws JSQLParserException {
        for (String sql : new String[] {"SHOW GLOBAL VARIABLES LIKE 'a%'", "SHOW FULL PROCESSLIST",
                "SHOW TRIGGERS FROM db", "SHOW GRANTS FOR 'u'@'%'", "SHOW WARNINGS LIMIT 5",
                "SHOW BINLOG EVENTS IN 'b.000001'", "SHOW ENGINE INNODB STATUS",
                "SHOW PROCEDURE CODE p", "SHOW BINARY LOGS"}) {
            validateNoErrors(sql, 1, MySqlVersion.V8_0, MariaDbVersion.V10_5_4,
                    DatabaseType.MYSQL, DatabaseType.MARIADB);
        }
    }

    @Test
    public void testNotAllowedForPostgresql() throws JSQLParserException {
        validate("SHOW GLOBAL STATUS", 1, 1, PostgresqlVersion.V14);
    }

    @Test
    public void testFeatureNotAllowed() throws JSQLParserException {
        validateNotAllowed("SHOW DATABASES LIKE 'a%'", 1, 1, FeaturesAllowed.DML,
                Feature.mySqlShow);
    }

    @Test
    public void testMetadataLooksUpDatabasesAndProcedures() throws JSQLParserException {
        List<String> names = new ArrayList<>();
        AbstractDatabaseMetaDataCapability metadata =
                new AbstractDatabaseMetaDataCapability(null, NamesLookup.NO_TRANSFORMATION) {
                    @Override
                    protected boolean databaseExists(Map<Named, Boolean> results, Named named) {
                        names.add("database " + named.getFqn());
                        return true;
                    }

                    @Override
                    protected boolean procedureExists(Map<Named, Boolean> results, Named named) {
                        names.add("procedure " + named.getFqn());
                        return true;
                    }
                };

        validateNoErrors("SHOW EVENTS IN db", 1, metadata);
        validateNoErrors("SHOW PROCEDURE CODE s.p", 1, metadata);
        validateNoErrors("SHOW FUNCTION CODE f", 1, metadata);
        validateNoErrors("SHOW GLOBAL STATUS", 1, metadata);
        assertEquals(Arrays.asList("database db", "procedure s.p"), names);
    }
}
