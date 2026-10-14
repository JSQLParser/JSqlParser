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

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.statement.comment.Comment;
import net.sf.jsqlparser.statement.comment.CommentTarget;
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
                Arguments.of("CONSTRAINT ck ON DOMAIN d", Feature.commentOnConstraint),
                Arguments.of("TRIGGER trg ON t", Feature.commentOnTrigger),
                Arguments.of("POLICY p ON t", Feature.commentOnPolicy),
                Arguments.of("RULE r ON v", Feature.commentOnRule),
                Arguments.of("PROCEDURE p(integer)", Feature.commentOnProcedure),
                Arguments.of("ROUTINE r", Feature.commentOnRoutine),
                Arguments.of("AGGREGATE a(*)", Feature.commentOnAggregate),
                Arguments.of("CAST (integer AS text)", Feature.commentOnCast),
                Arguments.of("ACCESS METHOD am", Feature.commentOnAccessMethod),
                Arguments.of("COLLATION c", Feature.commentOnCollation),
                Arguments.of("DATABASE d", Feature.commentOnDatabase),
                Arguments.of("EVENT TRIGGER et", Feature.commentOnEventTrigger),
                Arguments.of("EXTENSION e", Feature.commentOnExtension),
                Arguments.of("FOREIGN DATA WRAPPER w", Feature.commentOnForeignDataWrapper),
                Arguments.of("FOREIGN TABLE ft", Feature.commentOnForeignTable),
                Arguments.of("PROCEDURAL LANGUAGE l", Feature.commentOnLanguage),
                Arguments.of("PUBLICATION p", Feature.commentOnPublication),
                Arguments.of("ROLE r", Feature.commentOnRole),
                Arguments.of("SERVER s", Feature.commentOnServer),
                Arguments.of("STATISTICS st", Feature.commentOnStatistics),
                Arguments.of("SUBSCRIPTION s", Feature.commentOnSubscription),
                Arguments.of("TABLESPACE ts", Feature.commentOnTablespace),
                Arguments.of("OPERATOR - (NONE, integer)", Feature.commentOnOperator),
                Arguments.of("OPERATOR CLASS ops USING btree", Feature.commentOnOperatorClass),
                Arguments.of("OPERATOR FAMILY ops USING gist", Feature.commentOnOperatorFamily),
                Arguments.of("CONVERSION conv", Feature.commentOnConversion),
                Arguments.of("LARGE OBJECT 4242", Feature.commentOnLargeObject),
                Arguments.of("TRANSFORM FOR hstore LANGUAGE plpython3u",
                        Feature.commentOnTransform),
                Arguments.of("TEXT SEARCH CONFIGURATION cfg",
                        Feature.commentOnTextSearchConfiguration),
                Arguments.of("TEXT SEARCH DICTIONARY dict", Feature.commentOnTextSearchDictionary),
                Arguments.of("TEXT SEARCH PARSER prs", Feature.commentOnTextSearchParser),
                Arguments.of("TEXT SEARCH TEMPLATE tmpl", Feature.commentOnTextSearchTemplate));
    }

    @Test
    void coversEveryTargetKind() throws Exception {
        Set<CommentTarget.Kind> kinds = EnumSet.noneOf(CommentTarget.Kind.class);
        for (Arguments arguments : targets().collect(Collectors.toList())) {
            Comment comment = (Comment) CCJSqlParserUtil
                    .parse("COMMENT ON " + arguments.get()[0] + " IS NULL");
            kinds.add(comment.getTarget().getKind());
        }
        assertEquals(EnumSet.allOf(CommentTarget.Kind.class), kinds);
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
