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
import net.sf.jsqlparser.util.validation.feature.MariaDbVersion;
import net.sf.jsqlparser.util.validation.feature.MySqlVersion;
import net.sf.jsqlparser.util.validation.feature.PostgresqlVersion;
import org.junit.jupiter.api.Test;

public class TablespaceValidatorTest extends ValidationTestAsserts {

    @Test
    public void testMySqlTablespaces() {
        for (String sql : new String[] {
                "CREATE TABLESPACE ts1 ADD DATAFILE 'ts1.ibd' ENGINE = InnoDB",
                "CREATE UNDO TABLESPACE u1 ADD DATAFILE 'u1.ibu'",
                "ALTER TABLESPACE ts1 RENAME TO ts2", "ALTER UNDO TABLESPACE u1 SET INACTIVE"}) {
            validateNoErrors(sql, 1, MySqlVersion.V8_0, FeaturesAllowed.DDL);
        }
    }

    @Test
    public void testPostgreSqlTablespaces() {
        for (String sql : new String[] {
                "CREATE TABLESPACE fast OWNER u1 LOCATION '/ssd1/pg' WITH (random_page_cost = 1.1)",
                "ALTER TABLESPACE fast OWNER TO bob",
                "ALTER TABLESPACE fast SET (seq_page_cost = 2)",
                "ALTER TABLESPACE fast RESET (seq_page_cost)"}) {
            validateNoErrors(sql, 1, PostgresqlVersion.V10, PostgresqlVersion.V14,
                    FeaturesAllowed.DDL);
        }
    }

    @Test
    public void testNotSupportedByMariaDb() {
        validateNotSupported("CREATE TABLESPACE ts1 ADD DATAFILE 'ts1.ibd'", 1, 1,
                MariaDbVersion.V10_5_4, Feature.createTablespace);
        validateNotSupported("ALTER TABLESPACE ts1 RENAME TO ts2", 1, 1,
                MariaDbVersion.V10_5_4, Feature.alterTablespace);
    }

    @Test
    public void testFeatureNotAllowed() {
        validateNotAllowed("CREATE TABLESPACE fast LOCATION '/ssd1/pg'", 1, 1,
                FeaturesAllowed.ALTER, Feature.createTablespace);
        validateNotAllowed("ALTER TABLESPACE fast RENAME TO faster", 1, 1,
                FeaturesAllowed.CREATE, Feature.alterTablespace);
    }
}
