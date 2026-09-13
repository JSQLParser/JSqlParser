/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.validation.validator;

import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.List;
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.parser.feature.FeatureConfiguration;
import net.sf.jsqlparser.util.validation.Validation;
import net.sf.jsqlparser.util.validation.ValidationError;
import net.sf.jsqlparser.util.validation.feature.FeaturesAllowed;
import net.sf.jsqlparser.util.validation.metadata.DatabaseMetaDataValidation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class MatchRecognizeValidatorTest {
    private static final String SQL = "SELECT * FROM events MATCH_RECOGNIZE (ORDER BY seq "
            + "MEASURES SUM(A.price) AS total PATTERN (A+) DEFINE A AS price > 0)";

    private static FeatureConfiguration configuration() {
        FeatureConfiguration configuration = new FeatureConfiguration();
        configuration.setValue(Feature.dialect, "BIGQUERY");
        return configuration;
    }

    @Test
    void permitsSupportedFeaturesAndDoesNotResolveSymbolsAsTables() {
        List<String> names = new ArrayList<>();
        DatabaseMetaDataValidation metadata = name -> {
            names.add(name.getFqn());
            return true;
        };
        List<ValidationError> errors =
                new Validation(configuration(), List.of(FeaturesAllowed.SELECT, metadata), SQL)
                        .validate();
        assertTrue(errors.isEmpty(), errors.toString());
        assertTrue(names.contains("events"));
        assertTrue(names.contains("price"));
        assertFalse(names.contains("A.price"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"A{10001}", "A{4,2}", "A B", "B", "A{@@lo}"})
    void invalidBoundsAndSymbols(String pattern) {
        List<ValidationError> errors = new Validation(configuration(),
                List.of(FeaturesAllowed.SELECT), SQL.replace("A+", pattern)).validate();
        assertFalse(errors.isEmpty());
    }

    @Test
    void duplicateSymbolsAreCaseInsensitive() {
        String sql =
                SQL.replace("DEFINE A AS price > 0", "DEFINE A AS price > 0, `a` AS price < 0");
        List<ValidationError> errors =
                new Validation(configuration(), List.of(FeaturesAllowed.SELECT), sql).validate();
        assertTrue(errors.toString().contains("Duplicate row pattern variable"));
    }

    @Test
    void featureCanBeDisabledIndependently() {
        List<ValidationError> errors = new Validation(configuration(),
                List.of(new FeaturesAllowed("basic", Feature.select, Feature.function,
                        Feature.orderBy)),
                SQL).validate();
        assertTrue(errors.toString().contains("matchRecognize"));
    }

    private static List<ValidationError> validate(String dialect, String sql) {
        FeatureConfiguration configuration = new FeatureConfiguration();
        configuration.setValue(Feature.dialect, dialect);
        return new Validation(configuration, List.of(FeaturesAllowed.SELECT), sql).validate();
    }

    @ParameterizedTest
    @ValueSource(
            strings = {"A{0}", "A{0,0}", "A{3,1}", "A{@lo}", "| A", "A |"})
    void oracleRejectsInvalidBoundsAndEmptyAlternatives(String pattern) {
        List<ValidationError> errors = validate("ORACLE", SQL.replace("A+", pattern));
        assertFalse(errors.isEmpty(), pattern);
    }

    @Test
    void oracleAllowsImplicitVariablesAndPreservesQuotedCase() {
        assertTrue(validate("ORACLE", SQL.replace("A+", "A B+")).isEmpty());
        String caseSensitive = SQL.replace("A+", "A \"a\"").replace("DEFINE A AS price > 0",
                "DEFINE A AS price > 0, \"a\" AS price < 0");
        assertTrue(validate("ORACLE", caseSensitive).isEmpty());
        assertFalse(validate("ORACLE", caseSensitive.replace("\"a\" AS", "\"A\" AS")).isEmpty());
    }

    @Test
    void validatesSubsetAndSkipTargets() {
        String base = SQL.replace("A+", "A B+").replace("DEFINE", "SUBSET U = (A, B) DEFINE");
        assertTrue(validate("ORACLE", base).isEmpty());
        assertFalse(validate("ORACLE", base.replace("U = (A, B)", "U = (A, C)")).isEmpty());
        assertFalse(validate("ORACLE", base.replace("U = (A, B)", "A = (A, B)")).isEmpty());
        assertTrue(validate("ORACLE", base.replace("PATTERN", "AFTER MATCH SKIP TO LAST B PATTERN"))
                .isEmpty());
        assertFalse(validate("ORACLE", base.replace("PATTERN", "AFTER MATCH SKIP TO C PATTERN"))
                .isEmpty());
        assertFalse(validate("SNOWFLAKE", base).isEmpty());
    }

    @Test
    void exclusionCannotReturnUnmatchedRows() {
        String query = SQL.replace("PATTERN (A+)",
                "ALL ROWS PER MATCH WITH UNMATCHED ROWS PATTERN ({- A+ -})");
        for (String dialect : List.of("ORACLE", "SNOWFLAKE")) {
            List<ValidationError> errors = validate(dialect, query);
            assertTrue(errors.toString().contains("exclusion cannot be combined"),
                    errors.toString());
        }
    }

    @Test
    void validatesDialectSpecificOptionsAndFunctionModes() {
        String options =
                SQL.substring(0, SQL.length() - 1) + " OPTIONS (use_longest_match = TRUE))";
        assertFalse(validate("ORACLE", options).isEmpty());
        assertFalse(validate("SNOWFLAKE", options).isEmpty());
        assertFalse(validate("BIGQUERY", SQL.replace("SUM(A.price)", "RUNNING SUM(A.price)"))
                .isEmpty());
        assertFalse(validate("ORACLE",
                SQL.replace("DEFINE A AS price > 0", "DEFINE A AS FINAL SUM(price) > 0"))
                .isEmpty());
        assertTrue(validate("ORACLE",
                SQL.replace("DEFINE A AS price > 0", "DEFINE A AS RUNNING SUM(price) > 0"))
                .isEmpty());
    }

    @Test
    void inputAliasScopeAndNestedQueriesStillReachMetadataValidation() {
        List<String> names = new ArrayList<>();
        DatabaseMetaDataValidation metadata = name -> {
            names.add(name.getFqn());
            return true;
        };
        String query = SQL.replace("FROM events", "FROM events A")
                .replace("ORDER BY seq", "ORDER BY A.seq")
                .replace("SUM(A.price)",
                        "SUM(A.price) + (SELECT 1 FROM lookup A WHERE A.nested_value > 0)");
        List<ValidationError> errors =
                new Validation(configuration(), List.of(FeaturesAllowed.SELECT, metadata), query)
                        .validate();
        assertTrue(errors.isEmpty(), errors.toString());
        assertTrue(names.contains("A.seq"), names.toString());
        assertTrue(names.contains("lookup"), names.toString());
        assertTrue(names.contains("A.nested_value"), names.toString());
    }

    @Test
    void oracleFeatureSetRecognizesTheNewClause() {
        FeatureConfiguration configuration = new FeatureConfiguration();
        configuration.setValue(Feature.dialect, "ORACLE");
        List<ValidationError> errors = new Validation(configuration,
                List.of(net.sf.jsqlparser.util.validation.feature.OracleVersion.V19C), SQL)
                .validate();
        assertTrue(errors.isEmpty(), errors.toString());
    }
}
