package net.sf.jsqlparser.util.validation;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.util.validation.feature.DatabaseType;

public class ValidationTestAsserts {

    public static void assertNotSupported(Collection<ValidationException> errors, Feature... feature) {
        assertEquals(toSet(f -> f + " not supported.", feature), toErrorsSet(errors));
    }

    public static void assertNotAllowed(Collection<ValidationException> errors, Feature... feature) {
        assertEquals(toSet(f -> f + " not allowed.", feature), toErrorsSet(errors));
    }

    public static void assertErrorsSize(Collection<?> errors, int size) {
        assertNotNull(errors);
        assertEquals(String.format("Expected %d errors, but got: %s", size, errors.toString()), size, errors.size());
    }

    public static void assertErrorsSize(Map<?, ?> errors, int size) {
        assertNotNull(errors);
        assertEquals(size, errors.size());
    }

    protected void validateNoErrors(String sql, int statementCount, DatabaseType... databaseTypes) {
        ValidationUtil validation = new ValidationUtil( //
                Arrays.asList(databaseTypes), sql);
        List<ValidationError> errors = validation.validate();

        assertErrorsSize(errors, 0);
        assertEquals(statementCount, validation.getParsedStatements().getStatements().size());
    }

    // PRIVATES //

    private static Set<String> toErrorsSet(Collection<ValidationException> errors) {
        return errors.stream().map(Exception::getMessage).collect(Collectors.toSet());
    }

    private static Set<String> toSet(Function<Feature, String> message, Feature... feature) {
        return Arrays.stream(feature).map(message).collect(Collectors.toSet());
    }

}
