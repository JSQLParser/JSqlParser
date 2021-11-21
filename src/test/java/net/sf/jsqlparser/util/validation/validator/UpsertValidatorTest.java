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

public class UpsertValidatorTest extends ValidationTestAsserts {

    @Test
    public void testValidationExecuteNotSupported() throws Exception {
        for (String sql : Arrays.asList("UPSERT INTO TEST (NAME, ID) VALUES ('foo', 123)",
                "UPSERT INTO TEST (ID, COUNTER) VALUES (123, 0) ON DUPLICATE KEY UPDATE COUNTER = COUNTER + 1",
                "UPSERT INTO test.targetTable (col1, col2) SELECT * FROM test.sourceTable",
                "UPSERT INTO mytable (mycolumn) WITH a AS (SELECT mycolumn FROM mytable) SELECT mycolumn FROM a")) {
            for (DatabaseType type : DatabaseType.DATABASES) {
                validateNotSupported(sql, 1, 1, type, Feature.upsert);
            }
        }
    }

    @Test
    public void testValidationExecuteNotAllowed() throws Exception {
        for (String sql : Arrays.asList("UPSERT INTO TEST (NAME, ID) VALUES ('foo', 123)",
                "UPSERT INTO TEST (ID, COUNTER) VALUES (123, 0) ON DUPLICATE KEY UPDATE COUNTER = COUNTER + 1")) {
            validateNotAllowed(sql, 1, 1, FeaturesAllowed.DDL, Feature.upsert);
        }
    }

}
