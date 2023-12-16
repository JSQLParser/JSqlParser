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
import net.sf.jsqlparser.util.validation.feature.FeaturesAllowed;
import net.sf.jsqlparser.util.validation.feature.MySqlVersion;
import net.sf.jsqlparser.util.validation.feature.PostgresqlVersion;
import org.junit.jupiter.api.Test;

public class TableStatementValidatorTest extends ValidationTestAsserts {

    @Test
    public void testValidationSelectAllowed() throws JSQLParserException {
        String sql = "TABLE columns ORDER BY column_name LIMIT 10 OFFSET 10";
        validateNoErrors(sql, 1, MySqlVersion.V8_0);
    }

    @Test
    public void testValidationSelectNotAllowed() throws JSQLParserException {
        String sql = "TABLE columns ORDER BY column_name LIMIT 10 OFFSET 10";
        validateNotAllowed(sql, 1, 1, FeaturesAllowed.DDL, Feature.select, Feature.tableStatement);

        validateNotSupported(sql, 1, 1, Arrays.asList(
                PostgresqlVersion.V14), Feature.tableStatement);
    }

}
