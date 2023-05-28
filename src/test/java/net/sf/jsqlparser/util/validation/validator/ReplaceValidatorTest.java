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

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.util.validation.ValidationTestAsserts;
import net.sf.jsqlparser.util.validation.feature.DatabaseType;
import net.sf.jsqlparser.util.validation.feature.FeaturesAllowed;
import org.junit.jupiter.api.Test;
import java.util.Arrays;

public class ReplaceValidatorTest extends ValidationTestAsserts {

    @Test
    public void testValidateReplace() throws JSQLParserException {
        for (String sql : Arrays.asList("REPLACE mytable SET col1='as', col2=?, col3=565", "REPLACE mytable (col1, col2, col3) VALUES ('as', ?, 565)", "REPLACE mytable (col1, col2, col3) SELECT * FROM mytable3")) {
            validateNoErrors(sql, 1, DatabaseType.MARIADB, DatabaseType.MYSQL);
        }
    }

    @Test
    public void testValidateReplaceNotAllowed() throws JSQLParserException {
        for (String sql : Arrays.asList("REPLACE mytable SET col1='as', col2=?, col3=565", "REPLACE mytable (col1, col2, col3) VALUES ('as', ?, 565)", "REPLACE mytable (col1, col2, col3) SELECT * FROM mytable3")) {
            validateNotAllowed(sql, 1, 1, FeaturesAllowed.SELECT.copy().add(FeaturesAllowed.JDBC), Feature.upsert);
        }
    }
}
