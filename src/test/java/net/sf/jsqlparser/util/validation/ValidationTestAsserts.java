package net.sf.jsqlparser.util.validation;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.util.validation.feature.FeaturesAllowed;
import net.sf.jsqlparser.util.validation.feature.Version;

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
     * @param size
     */
    public static void assertErrorsSize(Collection<?> errors, int size) {
        assertNotNull(errors);
        assertEquals(String.format("Expected %d errors, but got: %s", size, errors.toString()), size, errors.size());
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
    public static void validateNoErrors(String sql, int statementCount, Version... versions) {
        ValidationUtil validation = new ValidationUtil( //
                Arrays.asList(versions), sql);
        List<ValidationError> errors = validation.validate();

        assertErrorsSize(errors, 0);
        assertEquals(statementCount, validation.getParsedStatements().getStatements().size());
    }

    /**
     * @param sql
     * @param statementCount
     * @param errorCount
     * @param allowed        - the allowed feature
     * @param features       - the features not allowed, assert errormessages
     *                       against
     *                       {@link #assertNotAllowed(Collection, Feature...)}
     */
    public static void validateNotAllowed(String sql, int statementCount, int errorCount, FeaturesAllowed allowed,
            Feature... features) {
        validateNotAllowed(sql, statementCount, errorCount, Collections.singleton(allowed), features);
    }

    /**
     * @param sql
     * @param statementCount
     * @param errorCount
     * @param allowed        - the allowed features
     * @param features       - the features not allowed, assert errormessages
     *                       against
     *                       {@link #assertNotAllowed(Collection, Feature...)}
     */
    public static void validateNotAllowed(String sql, int statementCount, int errorCount,
            Collection<FeaturesAllowed> allowed,
            Feature... features) {
        ValidationUtil validation = new ValidationUtil(allowed, sql);
        List<ValidationError> errors = validation.validate();

        assertErrorsSize(errors, errorCount);
        assertEquals(statementCount, validation.getParsedStatements().getStatements().size());
        assertNotAllowed(errors.get(0).getErrors(), features);
    }

    /**
     * @param sql
     * @param statementCount
     * @param errorCount
     * @param supported - the supported features
     * @param features - the features not supported, assert errormessages
     * against
     * {@link #assertNotSupported(Collection, Feature...)
     */
    public static void validateNotSupported(String sql, int statementCount, int errorCount, Version supported,
            Feature... features) {
        validateNotSupported(sql, statementCount, errorCount, Collections.singleton(supported), features);
    }

    /**
     * @param sql
     * @param statementCount
     * @param errorCount
     * @param supported - the supported features
     * @param features - the features not supported, assert errormessages
     * against
     * {@link #assertNotSupported(Collection, Feature...)
     */
    public static void validateNotSupported(String sql, int statementCount, int errorCount,
            Collection<Version> supported, Feature... features) {
        ValidationUtil validation = new ValidationUtil(supported //
                , sql);
        List<ValidationError> errors = validation.validate();

        assertErrorsSize(errors, errorCount);
        assertEquals(statementCount, validation.getParsedStatements().getStatements().size());
        assertErrorsSize(errors.get(0).getErrors(), features.length);
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
