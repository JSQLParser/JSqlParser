/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2020 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.validation;

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
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.util.validation.ValidationCapability;
import net.sf.jsqlparser.util.validation.DatabaseType;
import net.sf.jsqlparser.util.validation.StatementValidator;
import net.sf.jsqlparser.util.validation.ValidationError;
import net.sf.jsqlparser.util.validation.ValidationUtil;
import org.hamcrest.core.StringStartsWith;
import org.junit.Test;

public class ValidationTest {

    @Test
    public void testValidaton() throws JSQLParserException {
        String sql = "SELECT * FROM tab1, tab2 WHERE tab1.id (+) = tab2.ref";
        Statement stmt = CCJSqlParserUtil.parse(sql);

        StatementValidator validator = new StatementValidator();
        validator.setCapabilities(Arrays.asList(DatabaseType.sqlserver, DatabaseType.postgresql));
        stmt.accept(validator);

        Map<ValidationCapability, Set<String>> unsupportedErrors = validator.getValidationErrors(DatabaseType.sqlserver);
        assertNotNull(unsupportedErrors);
        assertEquals(1, unsupportedErrors.size());
        assertEquals(new HashSet<>(Arrays.asList("joinOldOracleSyntax not supported.")),
                unsupportedErrors.get(DatabaseType.sqlserver));

        unsupportedErrors = validator.getValidationErrors(DatabaseType.postgresql);
        assertNotNull(unsupportedErrors);
        assertEquals(1, unsupportedErrors.size());
        assertEquals(new HashSet<>(Arrays.asList("joinOldOracleSyntax not supported.")),
                unsupportedErrors.get(DatabaseType.postgresql));
    }

    @Test
    public void testWithValidatonUtil() throws JSQLParserException {

        String stmt = "SELECT * FROM tab1, tab2 WHERE tab1.id (+) = tab2.ref";
        List<ValidationError> errors = ValidationUtil.validate(
                Collections.singletonList(DatabaseType.sqlserver), stmt);

        assertNotNull(errors);
        assertEquals(1, errors.size());
        assertEquals(stmt, errors.get(0).getStatement());
        assertEquals(DatabaseType.sqlserver, errors.get(0).getCapability());
        assertEquals(new HashSet<>(Arrays.asList("joinOldOracleSyntax not supported.")), errors.get(0).getErrors());
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

}