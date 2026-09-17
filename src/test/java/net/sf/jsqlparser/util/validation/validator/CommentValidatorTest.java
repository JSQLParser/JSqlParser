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
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.util.validation.ValidationTestAsserts;
import net.sf.jsqlparser.util.validation.feature.DatabaseType;
import net.sf.jsqlparser.util.validation.feature.FeaturesAllowed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class CommentValidatorTest extends ValidationTestAsserts {
    static Stream<Arguments> targets() {
        return Stream.of(Arguments.of("INDEX idx", Feature.commentOnIndex),
                Arguments.of("SCHEMA app", Feature.commentOnSchema),
                Arguments.of("SEQUENCE seq", Feature.commentOnSequence),
                Arguments.of("DOMAIN d", Feature.commentOnDomain),
                Arguments.of("TYPE ty", Feature.commentOnType),
                Arguments.of("MATERIALIZED VIEW mv", Feature.commentOnMaterializedView),
                Arguments.of("FUNCTION f(IN x integer)", Feature.commentOnFunction),
                Arguments.of("CONSTRAINT ck ON t", Feature.commentOnConstraint),
                Arguments.of("CONSTRAINT ck ON DOMAIN d", Feature.commentOnConstraint));
    }

    @ParameterizedTest
    @MethodSource("targets")
    void validatesEachTargetCapability(String target, Feature feature) {
        String sql = "COMMENT ON " + target + " IS 'body'";
        validateNoErrors(sql, 1, DatabaseType.POSTGRESQL);
        validateNotAllowed(sql, 1, 1, new FeaturesAllowed("comment only", Feature.comment),
                feature);
        validateNotAllowed(sql, 1, 1, FeaturesAllowed.DML, Feature.comment, feature);
    }

    @Test
    void retainsLegacyCommentCapabilities() {
        validateNoErrors(
                "COMMENT ON TABLE t IS 'body'; COMMENT ON COLUMN t.c IS NULL; COMMENT ON VIEW v IS ''",
                3, DatabaseType.POSTGRESQL, DatabaseType.ORACLE, DatabaseType.H2);
        validateNotAllowed("COMMENT ON COLUMN t.c IS NULL", 1, 1,
                new FeaturesAllowed("table comments", Feature.comment, Feature.commentOnTable),
                Feature.commentOnColumn);
    }
}
