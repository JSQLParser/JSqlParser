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

import org.junit.jupiter.api.Test;

import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.util.validation.ValidationTestAsserts;
import net.sf.jsqlparser.util.validation.feature.DatabaseType;
import net.sf.jsqlparser.util.validation.feature.FeaturesAllowed;
import net.sf.jsqlparser.util.validation.metadata.AbstractDatabaseMetaDataCapability;
import net.sf.jsqlparser.util.validation.metadata.Named;
import net.sf.jsqlparser.util.validation.metadata.NamesLookup;

/**
 *
 * @author Jayant Kumar Yadav
 */

public class ShowIndexStatementValidatorTest extends ValidationTestAsserts {

    @Test
    public void testValidationShowIndex() throws Exception {
        for (String sql : Arrays.asList("SHOW INDEX FROM mydatabase",
                "SHOW EXTENDED KEYS IN t FROM db WHERE Non_unique = 0")) {
            validateNoErrors(sql, 1, DatabaseType.MARIADB, DatabaseType.MYSQL);
        }
    }


    @Test
    public void testValidationShowIndexNotAllowed() throws Exception {
        for (String sql : Arrays.asList("SHOW INDEX FROM mydatabase")) {
            validateNotAllowed(sql, 1, 1, FeaturesAllowed.DML, Feature.showIndex);
        }
    }

    @Test
    public void testMetadataLooksUpTheTableInTheTrailingDatabase() throws Exception {
        List<String> names = new ArrayList<>();
        AbstractDatabaseMetaDataCapability metadata =
                new AbstractDatabaseMetaDataCapability(null, NamesLookup.NO_TRANSFORMATION) {
                    @Override
                    protected boolean tableExists(Map<Named, Boolean> results, Named named) {
                        names.add(named.getFqn());
                        return true;
                    }
                };

        validateNoErrors("SHOW INDEX FROM t FROM db", 1, metadata);
        validateNoErrors("SHOW INDEXES IN s.t2", 1, metadata);
        assertEquals(Arrays.asList("db.t", "s.t2"), names);
    }
}
