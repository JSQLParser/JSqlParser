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

import java.util.stream.Stream;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.util.validation.ValidationTestAsserts;
import net.sf.jsqlparser.util.validation.feature.DatabaseType;
import net.sf.jsqlparser.util.validation.feature.FeaturesAllowed;
import net.sf.jsqlparser.util.validation.feature.MySqlVersion;
import net.sf.jsqlparser.util.validation.feature.PostgresqlVersion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class MySqlServerAdminValidatorTest extends ValidationTestAsserts {

    static Stream<Arguments> statements() {
        return Stream.of(
                Arguments.of("FLUSH TABLES t WITH READ LOCK", Feature.flush),
                Arguments.of("FLUSH LOCAL BINARY LOGS, RELAY LOGS FOR CHANNEL 'c1'",
                        Feature.flush),
                Arguments.of("KILL QUERY 5", Feature.kill),
                Arguments.of("HANDLER db1.t OPEN AS h", Feature.handler),
                Arguments.of("HANDLER h READ i1 >= (1, 2) WHERE a > 0 LIMIT 3", Feature.handler),
                Arguments.of("HANDLER h CLOSE", Feature.handler),
                Arguments.of("RESET PERSIST IF EXISTS max_connections", Feature.resetPersist));
    }

    @ParameterizedTest
    @MethodSource("statements")
    public void testAllowedForMySql(String sql, Feature feature) throws JSQLParserException {
        validateNoErrors(sql, 1, MySqlVersion.V8_0, DatabaseType.MYSQL);
    }

    @ParameterizedTest
    @MethodSource("statements")
    public void testNotAllowedForSelect(String sql, Feature feature) {
        validateNotAllowed(sql, 1, 1, FeaturesAllowed.SELECT, feature);
    }

    @ParameterizedTest
    @MethodSource("statements")
    public void testNotAllowedForPostgresql(String sql, Feature feature)
            throws JSQLParserException {
        validate(sql, 1, 1, PostgresqlVersion.V14);
    }

    @Test
    public void testPostgresqlResetStaysAllowed() throws JSQLParserException {
        validateNoErrors("RESET ALL", 1, PostgresqlVersion.V14, DatabaseType.POSTGRESQL);
        validate("RESET ALL", 1, 1, MySqlVersion.V8_0);
    }
}
