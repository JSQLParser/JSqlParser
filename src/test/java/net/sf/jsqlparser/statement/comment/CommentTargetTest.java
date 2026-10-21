/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.comment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.RoutineReference;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.StmtFeature;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.select.SelectVisitorAdapter;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class CommentTargetTest {
    // PostgreSQL accepts schema-qualified names only for these kinds
    private static final Set<CommentTarget.Kind> SCHEMA_QUALIFIED = EnumSet.of(
            CommentTarget.Kind.INDEX, CommentTarget.Kind.SEQUENCE, CommentTarget.Kind.DOMAIN,
            CommentTarget.Kind.TYPE, CommentTarget.Kind.MATERIALIZED_VIEW,
            CommentTarget.Kind.FOREIGN_TABLE, CommentTarget.Kind.COLLATION,
            CommentTarget.Kind.STATISTICS);

    private static final Set<CommentTarget.Kind> UNQUALIFIED = EnumSet.of(
            CommentTarget.Kind.SCHEMA, CommentTarget.Kind.ACCESS_METHOD,
            CommentTarget.Kind.DATABASE, CommentTarget.Kind.EVENT_TRIGGER,
            CommentTarget.Kind.EXTENSION, CommentTarget.Kind.FOREIGN_DATA_WRAPPER,
            CommentTarget.Kind.LANGUAGE, CommentTarget.Kind.PUBLICATION, CommentTarget.Kind.ROLE,
            CommentTarget.Kind.SERVER, CommentTarget.Kind.SUBSCRIPTION,
            CommentTarget.Kind.TABLESPACE);

    static Stream<Arguments> names() {
        List<Arguments> cases = new ArrayList<>();
        Set<CommentTarget.Kind> kinds = EnumSet.copyOf(SCHEMA_QUALIFIED);
        kinds.addAll(UNQUALIFIED);
        for (CommentTarget.Kind kind : kinds) {
            for (String name : List.of("obj", "\"a.b\"", "\"a\"\"b.c\"", "\"한.글\"", "\".\"")) {
                cases.add(Arguments.of(kind, name, null, name));
            }
            if (SCHEMA_QUALIFIED.contains(kind)) {
                cases.add(Arguments.of(kind, "public.\"a.b\"", "public", "\"a.b\""));
                cases.add(Arguments.of(kind, "\"s.p\".\"a.b\"", "\"s.p\"", "\"a.b\""));
            }
        }
        return cases.stream();
    }

    @ParameterizedTest
    @MethodSource("names")
    void preservesNamedTargetsAndTheirComponents(CommentTarget.Kind kind, String name,
            String schema, String leaf) throws Exception {
        for (String literal : List.of("'body'", "$tag$body;\nquote's \\path$tag$", "NULL", "''")) {
            String sql =
                    "COMMENT ON " + kind.name().replace('_', ' ') + " " + name + " IS " + literal;
            for (Comment comment : roundTrip(sql)) {
                assertNull(comment.getTable());
                assertNull(comment.getColumn());
                assertNull(comment.getView());
                CommentTarget target = comment.getTarget();
                assertEquals(kind, target.getKind());
                assertEquals(leaf, target.getName().getName());
                assertEquals(schema, target.getName().getSchemaName());
                assertEquals(name, target.getName().getFullyQualifiedName());
                if (literal.equals("NULL")) {
                    assertNull(comment.getComment());
                } else {
                    assertEquals(literal, comment.getComment().toString());
                }
            }
        }
    }

    static Stream<Arguments> signatures() {
        List<Arguments> cases = new ArrayList<>();
        for (CommentTarget.Kind kind : List.of(CommentTarget.Kind.FUNCTION,
                CommentTarget.Kind.PROCEDURE, CommentTarget.Kind.ROUTINE)) {
            for (String signature : List.of("f", "f()", "f(integer)", "f(IN arg integer)",
                    "f(OUT result text)", "f(INOUT arg integer)", "f(VARIADIC args text[])",
                    "\"s.p\".\"f.x\"(IN \"arg.x\" pg_catalog.int4[])",
                    "f(timestamp with time zone, double precision)")) {
                cases.add(Arguments.of(kind, signature));
            }
        }
        return cases.stream();
    }

    @ParameterizedTest
    @MethodSource("signatures")
    void preservesRoutineSignaturesWithoutTreatingThemAsCalls(CommentTarget.Kind kind,
            String signature) throws Exception {
        for (Comment comment : roundTrip("COMMENT ON " + kind + " " + signature + " IS 'body'")) {
            assertEquals(kind, comment.getTarget().getKind());
            assertNull(comment.getTarget().getName());
            RoutineReference routine = comment.getTarget().getRoutine();
            assertEquals(
                    signature.contains("(") ? signature.substring(0, signature.indexOf('('))
                            : signature,
                    routine.getName());
            assertEquals(!signature.contains("("), routine.getArguments() == null);
            assertTrue(new TablesNamesFinder().getTables(comment).isEmpty());
            assertTrue(comment.getFeatures().modifiesSchema());
            assertFalse(comment.getFeatures().may(StmtFeature.READS_DATA));
            assertFalse(comment.getFeatures().isOpaque());
            assertTrue(comment.getFeatures().getUnresolvedReferences().isEmpty());
        }
    }

    @Test
    void exposesArgumentModeNameAndDataType() throws Exception {
        Comment comment = parse(
                "COMMENT ON FUNCTION f(INOUT \"arg.x\" app.custom_type, VARIADIC rest text[]) IS NULL");
        List<RoutineReference.Argument> arguments = comment.getTarget().getRoutine().getArguments();
        assertEquals(RoutineReference.Argument.Mode.INOUT, arguments.get(0).getMode());
        assertEquals("\"arg.x\"", arguments.get(0).getName());
        assertEquals("app.custom_type", arguments.get(0).getDataType().toString());
        assertEquals(RoutineReference.Argument.Mode.VARIADIC, arguments.get(1).getMode());
        assertEquals("rest", arguments.get(1).getName());
        assertEquals("text[]", arguments.get(1).getDataType().toString());
    }

    @Test
    void preservesAggregateSignatures() throws Exception {
        RoutineReference star = aggregate("app.agg(*)");
        assertEquals("app.agg", star.getName());
        assertTrue(star.isAllArguments());
        assertNull(star.getOrderByArguments());

        RoutineReference direct = aggregate("agg(integer, text)");
        assertFalse(direct.isAllArguments());
        assertEquals(2, direct.getArguments().size());
        assertNull(direct.getOrderByArguments());

        RoutineReference ordered = aggregate("rank(VARIADIC \"any\" ORDER BY VARIADIC \"any\")");
        assertEquals(RoutineReference.Argument.Mode.VARIADIC,
                ordered.getArguments().get(0).getMode());
        assertEquals(RoutineReference.Argument.Mode.VARIADIC,
                ordered.getOrderByArguments().get(0).getMode());

        // ORDER is not reserved, so it must not be taken for the name of a direct argument
        RoutineReference orderedOnly = aggregate("mode(ORDER BY anyelement)");
        assertTrue(orderedOnly.getArguments().isEmpty());
        assertEquals("anyelement",
                orderedOnly.getOrderByArguments().get(0).getDataType().toString());
    }

    private static RoutineReference aggregate(String signature) throws JSQLParserException {
        List<Comment> comments = roundTrip("COMMENT ON AGGREGATE " + signature + " IS NULL");
        for (Comment comment : comments) {
            assertEquals(CommentTarget.Kind.AGGREGATE, comment.getTarget().getKind());
            assertEquals(signature, comment.getTarget().getRoutine().toString());
            assertTrue(new TablesNamesFinder().getTables(comment).isEmpty());
        }
        return comments.get(0).getTarget().getRoutine();
    }

    @ParameterizedTest
    @ValueSource(strings = {"t", "\"t.x\"", "public.\"t.x\"", "\"s.p\".\"t.x\""})
    void distinguishesConstraintTablesFromDomains(String owner) throws Exception {
        for (boolean domain : List.of(false, true)) {
            String sql = "COMMENT ON CONSTRAINT \"c.k\" ON " + (domain ? "DOMAIN " : "") + owner
                    + " IS $$body$$";
            for (Comment comment : roundTrip(sql)) {
                CommentTarget target = comment.getTarget();
                assertEquals(CommentTarget.Kind.CONSTRAINT, target.getKind());
                assertEquals("\"c.k\"", target.getName().getName());
                assertNull(target.getName().getSchemaName());
                assertEquals(domain, target.isOnDomain());
                assertEquals(owner, target.getRelation().getFullyQualifiedName());
                assertEquals(domain ? Set.of() : Set.of(owner),
                        new TablesNamesFinder().getTables(comment));
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"domain", "domain.t", "\"DOMAIN\""})
    void domainKeywordCanNameTheConstraintTable(String name) throws Exception {
        for (Comment comment : roundTrip("COMMENT ON CONSTRAINT ck ON " + name + " IS NULL")) {
            assertFalse(comment.getTarget().isOnDomain());
            assertEquals(name, comment.getTarget().getRelation().getFullyQualifiedName());
            assertEquals(Set.of(name), new TablesNamesFinder().getTables(comment));
        }
    }

    static Stream<Arguments> relationOwnedTargets() {
        List<Arguments> cases = new ArrayList<>();
        for (CommentTarget.Kind kind : List.of(CommentTarget.Kind.TRIGGER,
                CommentTarget.Kind.POLICY, CommentTarget.Kind.RULE)) {
            for (String owner : List.of("t", "\"t.x\"", "public.\"t.x\"", "\"s.p\".\"t.x\"",
                    "domain")) {
                cases.add(Arguments.of(kind, owner));
            }
        }
        return cases.stream();
    }

    @ParameterizedTest
    @MethodSource("relationOwnedTargets")
    void namesTriggersPoliciesAndRulesWithinTheirTable(CommentTarget.Kind kind, String owner)
            throws Exception {
        for (Comment comment : roundTrip(
                "COMMENT ON " + kind + " \"o.n\" ON " + owner + " IS 'body'")) {
            CommentTarget target = comment.getTarget();
            assertEquals(kind, target.getKind());
            assertEquals("\"o.n\"", target.getName().getName());
            assertNull(target.getName().getSchemaName());
            assertFalse(target.isOnDomain());
            assertEquals(owner, target.getRelation().getFullyQualifiedName());
            assertEquals(Set.of(owner), new TablesNamesFinder().getTables(comment));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"bigint AS int4", "character varying AS text",
            "text[] AS app.custom_type", "timestamp with time zone AS date",
            "double precision AS time without time zone"})
    void preservesCastSourceAndTargetTypes(String types) throws Exception {
        for (Comment comment : roundTrip("COMMENT ON CAST (" + types + ") IS 'body'")) {
            CommentTarget target = comment.getTarget();
            assertEquals(CommentTarget.Kind.CAST, target.getKind());
            assertNull(target.getName());
            assertNull(target.getRoutine());
            assertEquals(types, target.getSourceType() + " AS " + target.getTargetType());
            assertTrue(new TablesNamesFinder().getTables(comment).isEmpty());
        }
    }

    @Test
    void recordsProceduralLanguageSpelling() throws Exception {
        for (boolean procedural : List.of(false, true)) {
            String sql = "COMMENT ON " + (procedural ? "PROCEDURAL " : "")
                    + "LANGUAGE plpgsql IS 'body'";
            for (Comment comment : roundTrip(sql)) {
                assertEquals(CommentTarget.Kind.LANGUAGE, comment.getTarget().getKind());
                assertEquals(procedural, comment.getTarget().isProcedural());
                assertEquals("plpgsql", comment.getTarget().getName().getName());
                assertEquals(sql, comment.toString());
            }
        }
    }

    @Test
    void kindPredicatesDescribeTheReferenceShape() {
        Set<CommentTarget.Kind> routines = EnumSet.noneOf(CommentTarget.Kind.class);
        Set<CommentTarget.Kind> owned = EnumSet.noneOf(CommentTarget.Kind.class);
        for (CommentTarget.Kind kind : CommentTarget.Kind.values()) {
            if (kind.isRoutine()) {
                routines.add(kind);
            }
            if (kind.isOwnedByRelation()) {
                owned.add(kind);
            }
        }
        assertEquals(EnumSet.of(CommentTarget.Kind.FUNCTION, CommentTarget.Kind.PROCEDURE,
                CommentTarget.Kind.ROUTINE, CommentTarget.Kind.AGGREGATE), routines);
        assertEquals(EnumSet.of(CommentTarget.Kind.CONSTRAINT, CommentTarget.Kind.TRIGGER,
                CommentTarget.Kind.POLICY, CommentTarget.Kind.RULE), owned);
    }

    @ParameterizedTest
    @ValueSource(strings = {"COMMENT ON TRIGGER trg ON users IS 'x'",
            "COMMENT ON POLICY p1 ON users IS 'x'", "COMMENT ON RULE r1 ON app.v IS NULL",
            "COMMENT ON EXTENSION postgis IS 'x'", "COMMENT ON DATABASE mydb IS 'x'",
            "COMMENT ON ROLE app_read IS 'x'", "COMMENT ON TABLESPACE pg_default IS ''",
            "COMMENT ON EVENT TRIGGER et IS 'x'", "COMMENT ON PUBLICATION pub IS 'x'",
            "COMMENT ON SUBSCRIPTION sub IS 'x'", "COMMENT ON SERVER srv IS 'x'",
            "COMMENT ON FOREIGN DATA WRAPPER postgres_fdw IS 'x'",
            "COMMENT ON FOREIGN TABLE app.ft IS 'x'",
            "COMMENT ON PROCEDURAL LANGUAGE plpgsql IS 'x'",
            "COMMENT ON COLLATION app.\"de-DE\" IS 'x'", "COMMENT ON ACCESS METHOD heap2 IS 'x'",
            "COMMENT ON STATISTICS app.st IS 'x'",
            "COMMENT ON PROCEDURE app.p(IN a integer, INOUT b text) IS 'x'",
            "COMMENT ON ROUTINE r() IS 'x'",
            "COMMENT ON AGGREGATE percentile_disc(float8 ORDER BY anyelement) IS 'x'",
            "COMMENT ON CAST (bigint AS int4) IS 'x'"})
    void parsesAndDeparsesWithAndWithoutPostgreSqlDialect(String sql) throws Exception {
        assertSqlCanBeParsedAndDeparsed(sql, false,
                parser -> parser.withDialect(Dialect.POSTGRESQL));
        assertSqlCanBeParsedAndDeparsed(sql, false, null);
    }

    @Test
    void kindWordsRemainIdentifiers() throws Exception {
        assertSqlCanBeParsedAndDeparsed("SELECT role, rule, server, extension, routine, "
                + "statistics, collation, publication, subscription, language, procedural, "
                + "access, method, wrapper FROM extension");
        Comment rule = roundTrip("COMMENT ON RULE rule ON rule IS NULL").get(0);
        assertEquals("rule", rule.getTarget().getName().getName());
        assertEquals("rule", rule.getTarget().getRelation().getName());
        assertEquals("server", roundTrip("COMMENT ON SERVER server IS NULL").get(0).getTarget()
                .getName().getName());
        assertEquals("role", roundTrip("COMMENT ON TABLE role IS NULL").get(0).getTable()
                .getName());
        assertEquals("server.language", roundTrip("COMMENT ON COLUMN server.language IS NULL")
                .get(0).getColumn().getFullyQualifiedName());
    }

    @Test
    void rendersProgrammaticTargets() {
        CommentTarget trigger = new CommentTarget();
        trigger.setKind(CommentTarget.Kind.TRIGGER);
        trigger.setName(new Table("trg"));
        trigger.setRelation(new Table("app", "t"));
        assertEquals("COMMENT ON TRIGGER trg ON app.t IS 'body'",
                new Comment().withTarget(trigger).withComment(new StringValue("body")).toString());

        CommentTarget cast = new CommentTarget();
        cast.setKind(CommentTarget.Kind.CAST);
        cast.setSourceType(new ColDataType("integer"));
        cast.setTargetType(new ColDataType("text"));
        assertEquals("COMMENT ON CAST (integer AS text) IS NULL",
                new Comment().withTarget(cast).toString());

        CommentTarget named = new CommentTarget();
        named.setKind(CommentTarget.Kind.ROLE);
        named.setName(new Table("r"));
        named.setProcedural(true);
        assertEquals("ROLE r", named.toString());
        named.setKind(CommentTarget.Kind.LANGUAGE);
        assertEquals("PROCEDURAL LANGUAGE r", named.toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"INDEX", "MATERIALIZED obj", "MATERIALIZED VIEW", "FUNCTION f(*)",
            "FUNCTION f(integer ORDER BY integer)", "FUNCTION f(IN)",
            "FUNCTION f(x integer DEFAULT 1)",
            "CONSTRAINT ck", "CONSTRAINT ck ON",
            "CONSTRAINT public.ck ON t",
            "TRIGGER trg", "TRIGGER trg ON", "TRIGGER app.trg ON t", "TRIGGER trg ON DOMAIN d",
            "POLICY p", "RULE r ON DOMAIN d", "PROCEDURE p(*)",
            "ROUTINE r(integer ORDER BY integer)", "FUNCTION f(ORDER BY integer)",
            "AGGREGATE agg", "CAST (integer)", "CAST integer AS text", "CAST (integer AS text",
            "FOREIGN ft", "FOREIGN DATA fdw", "EVENT et", "ACCESS heap", "ACCESS METHOD",
            "PROCEDURAL plpgsql", "LANGUAGE", "ROLE",
            "UNKNOWN obj"})
    void rejectsIncompleteOrInvalidTargets(String target) {
        assertThrows(JSQLParserException.class, () -> parse("COMMENT ON " + target + " IS NULL"));
    }

    @Test
    void findsOnlyRelationsExplicitlyNamedByComments() throws Exception {
        for (String target : List.of("INDEX idx", "SCHEMA app", "SEQUENCE seq", "DOMAIN d",
                "TYPE ty", "FUNCTION f(int)", "PROCEDURE app.p(int)", "ROUTINE r",
                "AGGREGATE a(*)", "CAST (integer AS text)", "COLLATION app.c",
                "STATISTICS app.st", "ACCESS METHOD am", "DATABASE d", "EVENT TRIGGER et",
                "EXTENSION e", "FOREIGN DATA WRAPPER w", "LANGUAGE l", "PUBLICATION p", "ROLE r",
                "SERVER s", "SUBSCRIPTION s", "TABLESPACE ts")) {
            assertTrue(new TablesNamesFinder().getTables(parse("COMMENT ON " + target + " IS NULL"))
                    .isEmpty());
        }
        for (String target : List.of("TABLE app.t", "COLUMN app.t.c", "VIEW app.t",
                "MATERIALIZED VIEW app.t", "CONSTRAINT ck ON app.t", "FOREIGN TABLE app.t",
                "TRIGGER trg ON app.t", "POLICY p ON app.t", "RULE r ON app.t")) {
            assertEquals(Set.of("app.t"),
                    new TablesNamesFinder().getTables(parse("COMMENT ON " + target + " IS NULL")));
        }
    }

    @Test
    void defaultConfigurationAcceptsUnambiguousTargetsButTagsRemainOptIn() throws Exception {
        Comment plain = (Comment) CCJSqlParserUtil.parse("COMMENT ON INDEX idx IS 'body'");
        assertEquals(CommentTarget.Kind.INDEX, plain.getTarget().getKind());
        assertThrows(JSQLParserException.class,
                () -> CCJSqlParserUtil.parse("COMMENT ON INDEX idx IS $tag$body$tag$"));
        Comment tagged = (Comment) CCJSqlParserUtil.parse("COMMENT ON INDEX idx IS $tag$body$tag$",
                p -> p.withDollarQuotedStringTags(true));
        assertEquals("body", tagged.getComment().getValue());
    }

    @Test
    void keepsFollowingStatementsOutsideCommentBodies() throws Exception {
        Statements statements = CCJSqlParserUtil.parseStatements(
                "COMMENT ON FUNCTION f() IS $tag$body; SELECT 0$tag$; COMMENT ON INDEX idx IS NULL; SELECT 42",
                p -> p.withDialect(Dialect.POSTGRESQL));
        assertEquals(3, statements.size());
        assertEquals("body; SELECT 0", ((Comment) statements.get(0)).getComment().getValue());
        assertEquals("SELECT 42", statements.get(2).toString());
    }

    @Test
    void retainsLegacyAccessorsAndAllowsSwitchingTargets() throws Exception {
        Comment comment = parse("COMMENT ON INDEX idx IS 'body'");
        CommentTarget target = comment.getTarget();
        comment.setTable(new Table("t"));
        assertNull(comment.getTarget());
        assertEquals("COMMENT ON TABLE t IS 'body'", comment.toString());
        comment.withTarget(target);
        assertNull(comment.getTable());
        assertEquals("COMMENT ON INDEX idx IS 'body'", comment.toString());
        comment.setColumn(new Column(new Table("t"), "c"));
        assertNull(comment.getTarget());
        assertEquals("COMMENT ON COLUMN t.c IS 'body'", comment.toString());
    }

    @Test
    void expressionVisitorsReceiveCommentLiteralAndContext() throws Exception {
        List<String> values = new ArrayList<>();
        ExpressionVisitorAdapter<Void> expressions = new ExpressionVisitorAdapter<Void>() {
            @Override
            public <S> Void visit(StringValue value, S context) {
                assertEquals("context", context);
                values.add(value.getValue());
                return null;
            }
        };
        parse("COMMENT ON INDEX idx IS 'body'").accept(
                new StatementVisitorAdapter<>(new SelectVisitorAdapter<>(expressions)), "context");
        assertEquals(List.of("body"), values);
    }

    @ParameterizedTest
    @ValueSource(strings = {"CONSTRAINT ck ON", "TRIGGER trg ON", "POLICY p ON", "RULE r ON",
            "FOREIGN TABLE", "MATERIALIZED VIEW"})
    void customDeparsersCanRewriteRelationsAndLiterals(String target) throws Exception {
        Comment comment = parse("COMMENT ON " + target + " app.t IS 'body'");
        StringBuilder builder = new StringBuilder();
        ExpressionDeParser expressions = new ExpressionDeParser() {
            @Override
            public <S> StringBuilder visit(StringValue value, S context) {
                assertEquals("context", context);
                return getBuilder().append("'changed'");
            }
        };
        SelectDeParser selects = new SelectDeParser() {
            @Override
            public <S> StringBuilder visit(Table table, S context) {
                assertEquals("context", context);
                return getBuilder().append("app.new_t");
            }
        };
        comment.accept(new StatementDeParser(expressions, selects, builder), "context");
        assertEquals("COMMENT ON " + target + " app.new_t IS 'changed'", builder.toString());
        assertEquals("COMMENT ON " + target + " app.t IS 'body'", comment.toString());
    }

    private static Comment parse(String sql) throws JSQLParserException {
        return (Comment) CCJSqlParserUtil.parse(sql, p -> p.withDialect(Dialect.POSTGRESQL));
    }

    private static List<Comment> roundTrip(String sql) throws JSQLParserException {
        Comment comment = parse(sql);
        StringBuilder builder = new StringBuilder();
        comment.accept(new StatementDeParser(builder), null);
        assertEquals(comment.toString(), builder.toString());
        return List.of(comment, parse(comment.toString()), parse(builder.toString()));
    }
}
