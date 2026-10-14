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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.util.validation.ValidationTestAsserts;
import net.sf.jsqlparser.util.validation.feature.DatabaseType;
import net.sf.jsqlparser.util.validation.feature.FeaturesAllowed;
import net.sf.jsqlparser.util.validation.metadata.AbstractDatabaseMetaDataCapability;
import net.sf.jsqlparser.util.validation.metadata.Named;
import net.sf.jsqlparser.util.validation.metadata.NamesLookup;
import org.junit.jupiter.api.Test;

public class ShowColumnsStatementValidatorTest extends ValidationTestAsserts {

    @Test
    public void testValidationShowTables() throws Exception {
        for (String sql : Arrays.asList("SHOW COLUMNS FROM mytable",
                "SHOW FULL COLUMNS FROM mytable FROM db LIKE 'a%'",
                "SHOW EXTENDED FIELDS IN db.mytable WHERE `Key` = 'PRI'")) {
            validateNoErrors(sql, 1, DatabaseType.MARIADB, DatabaseType.MYSQL);
        }
    }

    @Test
    public void testValidationShowTablesNotAllowed() throws Exception {
        for (String sql : Arrays.asList("SHOW COLUMNS FROM mytable",
                "SHOW FULL FIELDS FROM mytable")) {
            validateNotAllowed(sql, 1, 1, FeaturesAllowed.DML, Feature.showColumns);
        }
    }

    @Test
    public void testMetadataUsesTheDatabaseClause() throws Exception {
        List<String> tables = new ArrayList<>();
        AbstractDatabaseMetaDataCapability metadata =
                new AbstractDatabaseMetaDataCapability(null, NamesLookup.NO_TRANSFORMATION) {
                    @Override
                    protected boolean tableExists(Map<Named, Boolean> results, Named named) {
                        tables.add(named.getFqn());
                        return true;
                    }
                };

        validateNoErrors("SHOW COLUMNS FROM t FROM db", 1, metadata);
        validateNoErrors("SHOW FULL COLUMNS FROM s.t2", 1, metadata);
        assertEquals(Arrays.asList("db.t", "s.t2"), tables);
    }

}
