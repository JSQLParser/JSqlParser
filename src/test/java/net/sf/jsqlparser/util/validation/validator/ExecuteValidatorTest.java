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
import org.junit.jupiter.api.Test;
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.util.validation.ValidationTestAsserts;
import net.sf.jsqlparser.util.validation.feature.DatabaseType;
import net.sf.jsqlparser.util.validation.feature.FeaturesAllowed;

public class ExecuteValidatorTest extends ValidationTestAsserts {

    @Test
    public void testValidationExecute() throws Exception {
        for (String sql : Arrays.asList("EXECUTE myproc 'a', 2, 'b'")) {
            validateNoErrors(sql, 1, DatabaseType.SQLSERVER);
        }
    }

    @Test
    public void testValidationExec() throws Exception {
        for (String sql : Arrays.asList("EXEC myproc 'a', 2, 'b'", "EXEC procedure @param = 1",
                "EXEC procedure @param = 'foo'", "EXEC procedure @param = 'foo', @param2 = 'foo2'")) {
            validateNoErrors(sql, 1, DatabaseType.SQLSERVER);
        }
    }

    @Test
    public void testValidationCall() throws Exception {
        for (String sql : Arrays.asList("CALL myproc 'a', 2, 'b'", "CALL BAR.FOO", "CALL myproc ('a', 2, 'b')")) {
            validateNoErrors(sql, 1, DatabaseType.MARIADB, DatabaseType.POSTGRESQL, DatabaseType.MYSQL);
        }
    }

    @Test
    public void testValidationCallNotSupported() throws Exception {
        for (String sql : Arrays.asList("CALL myproc 'a', 2, 'b'", "CALL BAR.FOO", "CALL myproc ('a', 2, 'b')")) {
            validateNotSupported(sql, 1, 1, DatabaseType.SQLSERVER, Feature.executeCall);
        }
    }

    @Test
    public void testValidationExecuteNotAllowed() throws Exception {
        for (String sql : Arrays.asList("EXECUTE myproc 'a', 2, 'b'")) {
            validateNotAllowed(sql, 1, 1, FeaturesAllowed.DML, Feature.execute, Feature.executeExecute);
        }
    }

    @Test
    public void testValidationExecNotAllowed() throws Exception {
        for (String sql : Arrays.asList("EXEC myproc 'a', 2, 'b'", "EXEC procedure @param = 1",
                "EXEC procedure @param = 'foo'", "EXEC procedure @param = 'foo', @param2 = 'foo2'")) {
            validateNotAllowed(sql, 1, 1, FeaturesAllowed.DML, Feature.execute, Feature.executeExec);
        }
    }

    @Test
    public void testValidationCallNotAllowed() throws Exception {
        for (String sql : Arrays.asList("CALL myproc 'a', 2, 'b'", "CALL BAR.FOO", "CALL myproc ('a', 2, 'b')")) {
            validateNotAllowed(sql, 1, 1, FeaturesAllowed.DML, Feature.execute, Feature.executeCall);
        }
    }

}
