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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.util.validation.feature.FeaturesAllowed;
import net.sf.jsqlparser.util.validation.feature.Version;
import net.sf.jsqlparser.util.validation.metadata.DatabaseMetaDataValidation;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ValidationTestAsserts {

    /**
     * @param errors
     * @param feature
     */
    public static void assertNotSupported(Collection<ValidationException> errors, Feature... feature) {
        assertEquals(toSet(f -> f + " not supported.", feature), toErrorsSet(errors));
    }

    /**
     * @param errors
     * @param feature
     */
    public static void assertNotAllowed(Collection<ValidationException> errors, Feature... feature) {
        assertEquals(toSet(f -> f + " not allowed.", feature), toErrorsSet(errors));
    }

    /**
     * @param errors
     * @param checkForExists
     * @param names
     */
    public static void assertMetadata(Collection<ValidationException> errors, boolean checkForExists, String... names) {
        assertEquals(Stream.of(names).map(f -> String.format("%s does %sexist.", f, checkForExists ? "not " : "")).collect(Collectors.toSet()), toErrorsSet(errors));
    }

    /**
     * @param errors
     * @param size
     */
    public static void assertErrorsSize(Collection<?> errors, int size) {
        assertNotNull(errors);
        assertEquals(size, errors.size(), String.format("Expected %d errors, but got: %s", size, errors.toString()));
    }

    /**
     * @param errors
     * @param size
     */
    public static void assertErrorsSize(Map<?, ?> errors, int size) {
        assertNotNull(errors);
        assertEquals(size, errors.size());
    }

    /**
     * validates and asserts that no errors occured
     *
     * @param sql
     * @param statementCount
     * @param versions
     */
    public static void validateNoErrors(String sql, int statementCount, ValidationCapability... versions) {
        Validation validation = new //
        Validation(Arrays.asList(versions), sql);
        List<ValidationError> errors = validation.validate();
        assertErrorsSize(errors, 0);
        assertEquals(statementCount, validation.getParsedStatements().getStatements().size());
    }

    public static List<ValidationError> validate(String sql, int statementCount, int errorCount, Collection<? extends ValidationCapability> validationCapabilities) {
        Validation validation = new Validation(validationCapabilities, sql);
        List<ValidationError> errors = validation.validate();
        assertErrorsSize(errors, errorCount);
        assertEquals(statementCount, validation.getParsedStatements().getStatements().size());
        return errors;
    }

    public static List<ValidationError> validate(String sql, int statementCount, int errorCount, ValidationCapability... validationCapabilities) {
        return validate(sql, statementCount, errorCount, Arrays.asList(validationCapabilities));
    }

    /**
     * @param sql
     * @param statementCount
     * @param errorCount
     * @param allowed
     * @param exists
     * @param names
     */
    public static void validateMetadata(String sql, int statementCount, int errorCount, DatabaseMetaDataValidation allowed, boolean exists, String... names) {
        validateMetadata(sql, statementCount, errorCount, Collections.singleton(allowed), exists, names);
    }

    /**
     * @param sql
     * @param statementCount
     * @param errorCount
     * @param allowed
     * @param exists
     * @param names
     */
    public static void validateMetadata(String sql, int statementCount, int errorCount, Collection<DatabaseMetaDataValidation> allowed, boolean exists, String... names) {
        List<ValidationError> errors = validate(sql, statementCount, errorCount, allowed);
        assertMetadata(errors.get(0).getErrors(), exists, names);
    }

    /**
     * @param sql
     * @param statementCount
     * @param errorCount
     * @param allowed - the allowed feature
     * @param features - the features not allowed, assert errormessages against
     * {@link #assertNotAllowed(Collection, Feature...)}
     */
    public static void validateNotAllowed(String sql, int statementCount, int errorCount, FeaturesAllowed allowed, Feature... features) {
        validateNotAllowed(sql, statementCount, errorCount, Collections.singleton(allowed), features);
    }

    /**
     * @param sql
     * @param statementCount
     * @param errorCount
     * @param allowed - the allowed features
     * @param features - the features not allowed, assert errormessages against
     * {@link #assertNotAllowed(Collection, Feature...)}
     */
    public static void validateNotAllowed(String sql, int statementCount, int errorCount, Collection<FeaturesAllowed> allowed, Feature... features) {
        List<ValidationError> errors = validate(sql, statementCount, errorCount, allowed);
        assertNotAllowed(errors.get(0).getErrors(), features);
    }

    /**
     * @param sql
     * @param statementCount
     * @param errorCount
     * @param supported - the supported features
     * @param features - the features not supported, assert errormessages against null null     {@link #assertNotSupported(Collection, Feature...)
     */
    public static void validateNotSupported(String sql, int statementCount, int errorCount, Version supported, Feature... features) {
        validateNotSupported(sql, statementCount, errorCount, Collections.singleton(supported), features);
    }

    /**
     * @param sql
     * @param statementCount
     * @param errorCount
     * @param supported - the supported features
     * @param features - the features not supported, assert errormessages against null null     {@link #assertNotSupported(Collection, Feature...)
     */
    public static void validateNotSupported(String sql, int statementCount, int errorCount, Collection<Version> supported, Feature... features) {
        List<ValidationError> errors = validate(sql, statementCount, errorCount, supported);
        assertNotSupported(errors.get(0).getErrors(), features);
    }

    // PRIVATES //
    private static Set<String> toErrorsSet(Collection<ValidationException> errors) {
        return errors.stream().map(Exception::getMessage).collect(Collectors.toSet());
    }

    private static Set<String> toSet(Function<Feature, String> message, Feature... feature) {
        return Arrays.stream(feature).map(message).collect(Collectors.toSet());
    }
}
