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
import org.junit.Test;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.util.validation.ValidationTestAsserts;
import net.sf.jsqlparser.util.validation.feature.DatabaseType;

public class AlterSequenceValidatorTest extends ValidationTestAsserts {

    private static final DatabaseType DATABASES_SUPPORTING_SEQUENCES[] = new DatabaseType[] { DatabaseType.ORACLE,
            DatabaseType.SQLSERVER, DatabaseType.MARIADB, DatabaseType.POSTGRESQL, DatabaseType.H2 };

    @Test
    public void testValidatorAlterSequence() throws JSQLParserException {
        for (String sql : Arrays.asList("ALTER SEQUENCE my_seq","ALTER SEQUENCE my_seq INCREMENT BY 1","ALTER SEQUENCE my_seq START WITH 10","ALTER SEQUENCE my_seq MAXVALUE 5",
                "ALTER SEQUENCE my_seq NOMAXVALUE","ALTER SEQUENCE my_seq MINVALUE 5","ALTER SEQUENCE my_seq NOMINVALUE","ALTER SEQUENCE my_seq CYCLE",
                "ALTER SEQUENCE my_sec INCREMENT BY 2 START WITH 10","ALTER SEQUENCE my_sec START WITH 2 INCREMENT BY 5 NOCACHE","ALTER SEQUENCE my_sec START WITH 2 INCREMENT BY 5 CACHE 200 CYCLE")) { 
            validateNoErrors(sql, 1, DATABASES_SUPPORTING_SEQUENCES);
        }
    }
}