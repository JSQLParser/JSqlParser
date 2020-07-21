/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2020 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.util.validation.feature.DatabaseType;
import net.sf.jsqlparser.util.validation.feature.FeaturesAllowed;
import net.sf.jsqlparser.util.validation.validator.StatementValidator;
import org.hamcrest.core.StringStartsWith;
import org.junit.Test;

public class ValidationTest {

    @Test
    public void testValidaton() throws JSQLParserException {
        String sql = "SELECT * FROM tab1, tab2 WHERE tab1.id (+) = tab2.ref";
        Statement stmt = CCJSqlParserUtil.parse(sql);

        StatementValidator validator = new StatementValidator();
        validator.setCapabilities(Arrays.asList(DatabaseType.SQLSERVER, DatabaseType.POSTGRESQL));
        stmt.accept(validator);

        Map<ValidationCapability, Set<String>> unsupportedErrors = validator.getValidationErrors(DatabaseType.SQLSERVER);
        assertNotNull(unsupportedErrors);
        assertEquals(1, unsupportedErrors.size());
        assertEquals(new HashSet<>(Arrays.asList(Feature.oracleOldJoinSyntax + " not supported.")),
                unsupportedErrors.get(DatabaseType.SQLSERVER));

        unsupportedErrors = validator.getValidationErrors(DatabaseType.POSTGRESQL);
        assertNotNull(unsupportedErrors);
        assertEquals(1, unsupportedErrors.size());
        assertEquals(new HashSet<>(Arrays.asList(Feature.oracleOldJoinSyntax + " not supported.")),
                unsupportedErrors.get(DatabaseType.POSTGRESQL));
    }

    @Test
    public void testWithValidatonUtil() throws JSQLParserException {

        String stmt = "SELECT * FROM tab1, tab2 WHERE tab1.id (+) = tab2.ref";
        List<ValidationError> errors = ValidationUtil.validate(
                Collections.singletonList(DatabaseType.SQLSERVER), stmt);

        assertNotNull(errors);
        assertEquals(1, errors.size());
        assertEquals(stmt, errors.get(0).getStatement());
        assertEquals(DatabaseType.SQLSERVER, errors.get(0).getCapability());
        assertEquals(new HashSet<>(Arrays.asList(Feature.oracleOldJoinSyntax + " not supported.")),
                errors.get(0).getErrors());
        assertNull(errors.get(0).getException());

    }

    @Test
    public void testWithValidatonUtilOnlyParse() throws JSQLParserException {

        String stmt = "SELECT * FROM tab1, tab2 WHERE tab1.id (+) = tab2.ref";
        List<ValidationError> errors = ValidationUtil.validate(Collections.emptyList(), stmt);

        assertNotNull(errors);
        assertEquals(0, errors.size());

    }

    @Test
    public void testWithValidatonUtilOnlyParseInvalid() throws JSQLParserException {

        String stmt = "SELECT * FROM tab1 JOIN tab2 WHERE tab1.id (++) = tab2.ref";
        List<ValidationError> errors = ValidationUtil.validate(Collections.emptyList(), stmt);

        assertNotNull(errors);
        assertEquals(1, errors.size());
        assertNotNull(errors.get(0).getException());
        assertThat(errors.get(0).getErrors().stream().findFirst().get(),
                StringStartsWith.startsWith("Cannot parse statement"));

    }

    @Test
    public void testWithValidatonUtilAcceptOnlyUpdates() throws JSQLParserException {

        String stmt = "SELECT * FROM tab1 JOIN tab2 WHERE tab1.id = tab2.ref";
        List<ValidationError> errors = ValidationUtil.validate(
                Arrays.asList(DatabaseType.POSTGRESQL, FeaturesAllowed.UPDATE), stmt);

        assertNotNull(errors);
        assertEquals(1, errors.size());
        assertNull(errors.get(0).getException());
        assertEquals(new HashSet<>(Arrays.asList("select not allowed.")), errors.get(0).getErrors());

    }

    @Test
    public void testWithValidatonUtilAcceptOnlySelects() throws JSQLParserException {

        String stmt = "SELECT * FROM tab1 JOIN tab2 WHERE tab1.id = tab2.ref";
        List<ValidationError> errors = ValidationUtil.validate(
                Arrays.asList(DatabaseType.POSTGRESQL, FeaturesAllowed.SELECT), stmt);

        assertNotNull(errors);
        assertEquals(0, errors.size());

    }

}