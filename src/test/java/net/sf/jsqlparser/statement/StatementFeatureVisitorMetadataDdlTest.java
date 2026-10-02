/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import static org.junit.jupiter.api.Assertions.*;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.alter.AlterCollation;
import net.sf.jsqlparser.statement.alter.AlterStatistics;
import net.sf.jsqlparser.statement.alter.AlterTablespaceMove;
import net.sf.jsqlparser.statement.alter.AlterTextSearchConfiguration;
import net.sf.jsqlparser.statement.alter.database.AlterDatabase;
import net.sf.jsqlparser.statement.alter.schema.AlterSchema;
import net.sf.jsqlparser.statement.create.textsearch.CreateTextSearchConfiguration;
import net.sf.jsqlparser.statement.create.user.CreateUser;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class StatementFeatureVisitorMetadataDdlTest {
    static Stream<Arguments> definitions() {
        return Stream.of(
                Arguments.of("ALTER COLLATION c REFRESH VERSION", AlterCollation.class),
                Arguments.of("ALTER COLLATION c RENAME TO c2", AlterCollation.class),
                Arguments.of("ALTER DATABASE d READ ONLY = 0", AlterDatabase.class),
                Arguments.of("ALTER DATABASE d CHARACTER SET utf8mb4 COLLATE utf8mb4_bin",
                        AlterDatabase.class),
                Arguments.of("ALTER SCHEMA s RENAME TO s2", AlterSchema.class),
                Arguments.of("ALTER SCHEMA s OWNER TO CURRENT_USER", AlterSchema.class),
                Arguments.of("ALTER STATISTICS s RENAME TO s2", AlterStatistics.class),
                Arguments.of("ALTER STATISTICS s SET STATISTICS 500", AlterStatistics.class),
                Arguments.of("ALTER TABLE ALL IN TABLESPACE old_space SET TABLESPACE new_space",
                        AlterTablespaceMove.class),
                Arguments.of(
                        "ALTER INDEX ALL IN TABLESPACE old_space SET TABLESPACE new_space NOWAIT",
                        AlterTablespaceMove.class),
                Arguments.of("CREATE TEXT SEARCH CONFIGURATION c (COPY = pg_catalog.simple)",
                        CreateTextSearchConfiguration.class),
                Arguments.of("CREATE TEXT SEARCH CONFIGURATION c (PARSER = p)",
                        CreateTextSearchConfiguration.class),
                Arguments.of("ALTER TEXT SEARCH CONFIGURATION c ADD MAPPING FOR word WITH simple",
                        AlterTextSearchConfiguration.class),
                Arguments.of("ALTER TEXT SEARCH CONFIGURATION c DROP MAPPING FOR word",
                        AlterTextSearchConfiguration.class),
                Arguments.of("CREATE USER u", CreateUser.class),
                Arguments.of("CREATE USER u IDENTIFIED BY 'password'", CreateUser.class));
    }

    @ParameterizedTest
    @MethodSource("definitions")
    void metadataDefinitionsHaveOnlySchemaEffectsAcrossEntryPoints(String sql, Class<?> type)
            throws Exception {
        Statement statement = TestUtils.assertSqlCanBeParsedAndDeparsed(sql);
        assertInstanceOf(type, statement);
        assertSchemaOnly(statement.getFeatures());
        assertSchemaOnly(StatementFeatureVisitor.analyse(statement));
        assertSchemaOnly(StatementFeatureVisitor.analyse(sql));
        assertSchemaOnly(StatementFeatureVisitor.analyse(statement, name -> true));
        Statements script = CCJSqlParserUtil.parseStatements(sql);
        assertSchemaOnly(StatementFeatureVisitor.analyse(script));
        List<StatementFeatures> each = StatementFeatureVisitor.analyseEach(script);
        assertEquals(1, each.size());
        assertSchemaOnly(each.get(0));
    }

    @ParameterizedTest
    @MethodSource("definitions")
    void classificationDoesNotSuppressFollowingExecutedSql(String sql, Class<?> type)
            throws Exception {
        Statements script = CCJSqlParserUtil.parseStatements(sql + "; SELECT f(id) FROM t");
        assertInstanceOf(type, script.getStatements().get(0));
        List<StatementFeatures> each = StatementFeatureVisitor.analyseEach(script);
        assertSchemaOnly(each.get(0));
        assertEquals(Set.of("f"), each.get(1).getUnresolvedReferences());
        assertEquals(EnumSet.of(StmtFeature.READS_DATA, StmtFeature.RETURNS_RESULT_SET),
                each.get(1).getCertain());
        StatementFeatures union = StatementFeatureVisitor.analyse(script);
        assertEquals(EnumSet.of(StmtFeature.MODIFIES_SCHEMA, StmtFeature.READS_DATA,
                StmtFeature.RETURNS_RESULT_SET), union.getCertain());
        assertEquals(Set.of("f"), union.getUnresolvedReferences());
        assertTrue(union.mayModifyData());
    }

    @ParameterizedTest
    @ValueSource(
            strings = {"CREATE ROLE r", "CREATE TRIGGER tr AFTER INSERT ON t EXECUTE FUNCTION f()",
                    "CREATE POLICY p ON t USING (f(id) > 0)"})
    void existingStoredDefinitionsRemainSchemaOnly(String sql) throws Exception {
        assertSchemaOnly(TestUtils.assertSqlCanBeParsedAndDeparsed(sql).getFeatures());
    }

    @Test
    void unknownAndUnsupportedStatementsRemainConservative() throws Exception {
        Statement unknown = new Statement() {
            @Override
            public <T, S> T accept(StatementVisitor<T> visitor, S context) {
                return null;
            }
        };
        assertOpaque(unknown.getFeatures());
        assertOpaque(StatementFeatureVisitor.analyse(new Statements()));
        StatementFeatures unsupported = CCJSqlParserUtil
                .parse("garbage", p -> p.withUnsupportedStatements(true)).getFeatures();
        assertOpaque(unsupported);
        assertEquals(Set.of("unsupported"), unsupported.getUnresolvedReferences());
    }

    private static void assertSchemaOnly(StatementFeatures features) {
        assertEquals(EnumSet.of(StmtFeature.MODIFIES_SCHEMA), features.getCertain());
        assertTrue(features.getUncertain().isEmpty());
        assertTrue(features.getUnresolvedReferences().isEmpty());
    }

    private static void assertOpaque(StatementFeatures features) {
        assertEquals(EnumSet.of(StmtFeature.OPAQUE), features.getCertain());
        assertEquals(
                EnumSet.of(StmtFeature.READS_DATA, StmtFeature.RETURNS_RESULT_SET,
                        StmtFeature.MODIFIES_DATA, StmtFeature.MODIFIES_SCHEMA),
                features.getUncertain());
    }
}
