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

public class ShowTablesStatementValidatorTest extends ValidationTestAsserts {

    @Test
    public void testValidationShowTables() throws Exception {
        for (String sql : Arrays.asList("SHOW TABLES", "SHOW EXTENDED FULL TABLES", "SHOW EXTENDED TABLES FROM db_name",
                "SHOW FULL TABLES IN db_name", "SHOW TABLES LIKE '%FOO%'", "SHOW TABLES WHERE table_name = 'FOO'")) {
            validateNoErrors(sql, 1, DatabaseType.MARIADB, DatabaseType.MYSQL);
        }
    }

    @Test
    public void testValidationShowTablesNotAllowed() throws Exception {
        for (String sql : Arrays.asList("SHOW TABLES", "SHOW EXTENDED FULL TABLES", "SHOW EXTENDED TABLES FROM db_name",
                "SHOW FULL TABLES IN db_name", "SHOW TABLES LIKE '%FOO%'", "SHOW TABLES WHERE table_name = 'FOO'")) {
            validateNotAllowed(sql, 1, 1, FeaturesAllowed.DML, Feature.showTables);
        }
    }

}
