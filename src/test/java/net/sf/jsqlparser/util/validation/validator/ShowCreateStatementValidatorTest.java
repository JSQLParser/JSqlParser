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

public class ShowCreateStatementValidatorTest extends ValidationTestAsserts {

    @Test
    public void testAllowedForMySqlAndMariaDb() throws JSQLParserException {
        for (String sql : new String[] {"SHOW CREATE TABLE t", "SHOW CREATE VIEW db.v",
                "SHOW CREATE PROCEDURE p", "SHOW CREATE FUNCTION f", "SHOW CREATE TRIGGER tr",
                "SHOW CREATE EVENT e", "SHOW CREATE DATABASE IF NOT EXISTS db",
                "SHOW CREATE USER 'u'@'h'"}) {
            validateNoErrors(sql, 1, MySqlVersion.V8_0, MariaDbVersion.V10_5_4,
                    DatabaseType.MYSQL, DatabaseType.MARIADB);
        }
    }

    @Test
    public void testNotAllowedForPostgresql() throws JSQLParserException {
        validate("SHOW CREATE TABLE t", 1, 1, PostgresqlVersion.V14);
    }

    @Test
    public void testFeatureNotAllowed() throws JSQLParserException {
        validateNotAllowed("SHOW CREATE TABLE t", 1, 1, FeaturesAllowed.DML, Feature.showCreate);
    }

    @Test
    public void testMetadataLooksUpTablesAndViews() throws JSQLParserException {
        List<String> names = new ArrayList<>();
        AbstractDatabaseMetaDataCapability metadata =
                new AbstractDatabaseMetaDataCapability(null, NamesLookup.NO_TRANSFORMATION) {
                    @Override
                    protected boolean tableExists(Map<Named, Boolean> results, Named named) {
                        names.add("table " + named.getFqn());
                        return true;
                    }

                    @Override
                    protected boolean viewExists(Map<Named, Boolean> results, Named named) {
                        names.add("view " + named.getFqn());
                        return true;
                    }
                };

        validateNoErrors("SHOW CREATE TABLE s.t", 1, metadata);
        validateNoErrors("SHOW CREATE VIEW v", 1, metadata);
        validateNoErrors("SHOW CREATE TRIGGER tr", 1, metadata);
        assertEquals(Arrays.asList("table s.t", "view v"), names);
    }
}
