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
import net.sf.jsqlparser.util.validation.feature.PostgresqlVersion;
import org.junit.jupiter.api.Test;

/** MySQL CREATE and DROP SPATIAL REFERENCE SYSTEM and ALTER INSTANCE. */
public class MySqlInstanceStatementsValidatorTest extends ValidationTestAsserts {
    private static final String CREATE =
            "CREATE SPATIAL REFERENCE SYSTEM 4120 NAME 'Greek' DEFINITION 'x'";
    private static final String ALTER = "ALTER INSTANCE RELOAD TLS FOR CHANNEL mysql_main";

    @Test
    public void testAllowedForMySql() throws JSQLParserException {
        for (String sql : new String[] {CREATE, ALTER,
                "DROP SPATIAL REFERENCE SYSTEM IF EXISTS 4120"}) {
            validateNoErrors(sql, 1, MySqlVersion.V8_0, DatabaseType.MYSQL);
        }
    }

    @Test
    public void testNotAllowedForMariaDbAndPostgresql() throws JSQLParserException {
        for (String sql : new String[] {CREATE, ALTER}) {
            validate(sql, 1, 1, MariaDbVersion.V10_5_4);
            validate(sql, 1, 1, PostgresqlVersion.V14);
        }
    }

    @Test
    public void testFeatureNotAllowed() throws JSQLParserException {
        validateNotAllowed(CREATE, 1, 1, FeaturesAllowed.SELECT,
                Feature.createSpatialReferenceSystem);
        validateNotAllowed(ALTER, 1, 1, FeaturesAllowed.SELECT, Feature.alterInstance);
        validateNotAllowed("DROP SPATIAL REFERENCE SYSTEM 4120", 1, 1, FeaturesAllowed.SELECT,
                Feature.drop);
    }
}
