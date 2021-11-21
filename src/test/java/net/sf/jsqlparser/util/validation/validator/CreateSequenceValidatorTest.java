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

public class CreateSequenceValidatorTest extends ValidationTestAsserts {

    private static final DatabaseType DATABASES_SUPPORTING_SEQUENCES[] = new DatabaseType[]{DatabaseType.ORACLE,
        DatabaseType.SQLSERVER, DatabaseType.MARIADB, DatabaseType.POSTGRESQL, DatabaseType.H2};

    @Test
    public void testValidateCreateSequence() throws JSQLParserException {
        for (String sql : Arrays.asList("CREATE SEQUENCE my_sec INCREMENT BY 2 START WITH 10",
                "CREATE SEQUENCE my_sec START WITH 2 INCREMENT BY 5 NOCACHE",
                "CREATE SEQUENCE my_sec START WITH 2 INCREMENT BY 5 CACHE 200 CYCLE")) {
            validateNoErrors(sql, 1, DATABASES_SUPPORTING_SEQUENCES);
        }
    }

    @Test
    public void testValidateCreateSequenceNotAllowed() throws JSQLParserException {
        for (String sql : Arrays.asList("CREATE SEQUENCE my_sec INCREMENT BY 2 START WITH 10",
                "CREATE SEQUENCE my_sec START WITH 2 INCREMENT BY 5 NOCACHE",
                "CREATE SEQUENCE my_sec START WITH 2 INCREMENT BY 5 CACHE 200 CYCLE")) {
            validateNotAllowed(sql, 1, 1, FeaturesAllowed.DML, Feature.createSequence);
        }
    }
}
