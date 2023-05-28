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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.util.validation.feature.DatabaseType;
import net.sf.jsqlparser.util.validation.feature.FeaturesAllowed;
import net.sf.jsqlparser.util.validation.feature.MariaDbVersion;
import net.sf.jsqlparser.util.validation.feature.MySqlVersion;
import net.sf.jsqlparser.util.validation.validator.StatementValidator;
import org.hamcrest.CoreMatchers;
import static org.hamcrest.MatcherAssert.assertThat;
import org.hamcrest.core.StringStartsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class ValidationTest extends ValidationTestAsserts {

    public static void main(String[] args) {
        System.out.println("mysql" + MySqlVersion.V8_0.getNotContained(MariaDbVersion.V10_5_4.getFeatures()));
        System.out.println("mariadb" + MariaDbVersion.V10_5_4.getNotContained(MySqlVersion.V8_0.getFeatures()));
    }

    @Test
    public void testValidationWithStatementValidator() throws JSQLParserException {
        String sql = "SELECT * FROM tab1, tab2 WHERE tab1.id (+) = tab2.ref";
        Statement stmt = CCJSqlParserUtil.parse(sql);
        StatementValidator validator = new StatementValidator();
        validator.setContext(new ValidationContext().setCapabilities(Arrays.asList(DatabaseType.SQLSERVER, DatabaseType.POSTGRESQL)));
        stmt.accept(validator);
        Map<ValidationCapability, Set<ValidationException>> unsupportedErrors = validator.getValidationErrors(DatabaseType.SQLSERVER);
        assertErrorsSize(unsupportedErrors, 1);
        assertNotSupported(unsupportedErrors.get(DatabaseType.SQLSERVER), Feature.oracleOldJoinSyntax);
        unsupportedErrors = validator.getValidationErrors(DatabaseType.POSTGRESQL);
        assertErrorsSize(unsupportedErrors, 1);
        assertNotSupported(unsupportedErrors.get(DatabaseType.POSTGRESQL), Feature.oracleOldJoinSyntax);
    }

    @Test
    public void testWithValidation() throws JSQLParserException {
        String stmt = "SELECT * FROM tab1, tab2 WHERE tab1.id (+) = tab2.ref";
        List<ValidationError> errors = Validation.validate(Collections.singletonList(DatabaseType.SQLSERVER), stmt);
        assertErrorsSize(errors, 1);
        assertEquals(stmt, errors.get(0).getStatements());
        assertEquals(DatabaseType.SQLSERVER, errors.get(0).getCapability());
        assertNotSupported(errors.get(0).getErrors(), Feature.oracleOldJoinSyntax);
    }

    @Test
    public void testWithValidationMultipleStatements() throws JSQLParserException {
        String sql = "UPDATE tab1 SET val = ? WHERE id = ?; DELETE FROM tab2 t2 WHERE t2.id = ?;";
        Validation validation = new //
        Validation(Arrays.asList(DatabaseType.SQLSERVER, DatabaseType.POSTGRESQL), sql);
        List<ValidationError> errors = validation.validate();
        assertErrorsSize(errors, 0);
        assertEquals(2, validation.getParsedStatements().getStatements().size());
    }

    @Test
    public void testWithValidationOnlyParse() throws JSQLParserException {
        String stmt = "SELECT * FROM tab1, tab2 WHERE tab1.id (+) = tab2.ref";
        List<ValidationError> errors = Validation.validate(Collections.emptyList(), stmt);
        assertErrorsSize(errors, 0);
    }

    @Test
    public void testWithValidationOnlyParse2() throws JSQLParserException {
        String sql = "SELECT * FROM tab1, tab2 WHERE value XOR other_value";
        Statement stmt = CCJSqlParserUtil.parse(sql);
        StatementValidator validator = new StatementValidator();
        validator.setContext(new ValidationContext().setCapabilities(Arrays.asList(DatabaseType.SQLSERVER, DatabaseType.MYSQL)));
        stmt.accept(validator);
        Map<ValidationCapability, Set<ValidationException>> unsupportedErrors = validator.getValidationErrors(DatabaseType.SQLSERVER);
        assertErrorsSize(unsupportedErrors, 0);
    }

    @Test
    public void testWithValidationOnlyParseInvalid() throws JSQLParserException {
        String stmt = "SELECT * FROM tab1 JOIN tab2 WHERE tab1.id (++) = tab2.ref";
        List<ValidationError> errors = Validation.validate(Collections.emptyList(), stmt);
        assertErrorsSize(errors, 1);
        ValidationException actual = errors.get(0).getErrors().stream().findFirst().get();
        assertThat(actual, CoreMatchers.instanceOf(ParseException.class));
        assertThat(actual.getMessage(), StringStartsWith.startsWith("Cannot parse statement"));
    }

    @Test
    public void testWithValidationUpdateButAcceptOnlySelects() throws JSQLParserException {
        String stmt = "UPDATE tab1 t1 SET t1.ref = ? WHERE t1.id = ?";
        List<ValidationError> errors = Validation.validate(Arrays.asList(DatabaseType.POSTGRESQL, FeaturesAllowed.SELECT.copy().add(FeaturesAllowed.JDBC)), stmt);
        assertErrorsSize(errors, 1);
        assertNotAllowed(errors.get(0).getErrors(), Feature.update);
    }

    @Test
    public void testWithValidatonAcceptOnlySelects() throws JSQLParserException {
        String stmt = "SELECT * FROM tab1 JOIN tab2 WHERE tab1.id = tab2.ref";
        List<ValidationError> errors = Validation.validate(Arrays.asList(DatabaseType.POSTGRESQL, FeaturesAllowed.SELECT), stmt);
        assertErrorsSize(errors, 0);
    }

    @Test
    public void testFeatureSetName() {
        assertEquals("SELECT + jdbc", FeaturesAllowed.SELECT.copy().add(FeaturesAllowed.JDBC).getName());
        assertEquals("UPDATE + SELECT", FeaturesAllowed.UPDATE.getName());
        assertEquals("DELETE + SELECT", FeaturesAllowed.DELETE.getName());
        assertEquals("DELETE + SELECT + UPDATE + jdbc", FeaturesAllowed.DELETE.copy().add(FeaturesAllowed.UPDATE).add(FeaturesAllowed.JDBC).getName());
        assertEquals("UPDATE + SELECT", new FeaturesAllowed().add(FeaturesAllowed.UPDATE).getName());
        assertEquals("UPDATE + SELECT + feature set", FeaturesAllowed.UPDATE.copy().add(new FeaturesAllowed(Feature.commit)).getName());
    }

    @Test
    public void testRowConstructorValidation() throws JSQLParserException {
        String stmt = "SELECT CAST(ROW(dataid, value, calcMark) AS ROW(datapointid CHAR, value CHAR, calcMark CHAR))";
        List<ValidationError> errors = Validation.validate(Arrays.asList(DatabaseType.ANSI_SQL, FeaturesAllowed.SELECT), stmt);
        assertErrorsSize(errors, 0);
    }
}
