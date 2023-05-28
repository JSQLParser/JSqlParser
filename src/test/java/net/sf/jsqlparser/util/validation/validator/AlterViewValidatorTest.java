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
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.util.validation.ValidationTestAsserts;
import net.sf.jsqlparser.util.validation.feature.DatabaseType;
import net.sf.jsqlparser.util.validation.feature.FeaturesAllowed;
import org.junit.jupiter.api.Test;

public class AlterViewValidatorTest extends ValidationTestAsserts {

    @Test
    public void testValidateAlterView() throws JSQLParserException {
        for (String sql : Arrays.asList("ALTER VIEW myview AS SELECT * FROM mytab")) {
            validateNoErrors(sql, 1, DatabaseType.MARIADB, DatabaseType.MYSQL, DatabaseType.SQLSERVER);
        }
    }

    @Test
    public void testValidateAlterViewNotSupported() throws JSQLParserException {
        for (String sql : Arrays.asList("REPLACE VIEW myview(a, b) AS SELECT a, b FROM mytab")) {
            for (DatabaseType type : Arrays.asList(DatabaseType.MARIADB, DatabaseType.MYSQL, DatabaseType.SQLSERVER)) {
                validateNotSupported(sql, 1, 1, type, Feature.alterViewReplace);
            }
        }
    }

    @Test
    public void testValidateAlterViewNotAllowed() throws JSQLParserException {
        validateNotAllowed("ALTER VIEW myview AS SELECT * FROM mytab", 1, 1, FeaturesAllowed.CREATE.copy().add(FeaturesAllowed.SELECT), Feature.alterView);
        validateNotAllowed("REPLACE VIEW myview(a, b) AS SELECT a, b FROM mytab", 1, 1, FeaturesAllowed.CREATE.copy().add(FeaturesAllowed.SELECT), Feature.alterView, Feature.alterViewReplace);
    }
}
