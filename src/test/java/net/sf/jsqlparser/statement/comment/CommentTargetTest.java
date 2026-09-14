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
import java.util.ArrayList;
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
    static Stream<Arguments> names() {
        List<Arguments> cases = new ArrayList<>();
        for (CommentTarget.Kind kind : List.of(CommentTarget.Kind.INDEX, CommentTarget.Kind.SCHEMA,
                CommentTarget.Kind.SEQUENCE, CommentTarget.Kind.DOMAIN, CommentTarget.Kind.TYPE,
                CommentTarget.Kind.MATERIALIZED_VIEW)) {
            for (String name : List.of("obj", "\"a.b\"", "\"a\"\"b.c\"", "\"한.글\"", "\".\"")) {
                cases.add(Arguments.of(kind, name, null, name));
            }
            if (kind != CommentTarget.Kind.SCHEMA) {
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

    @ParameterizedTest
    @ValueSource(strings = {"f", "f()", "f(integer)", "f(IN arg integer)",
            "f(OUT result text)", "f(INOUT arg integer)", "f(VARIADIC args text[])",
            "\"s.p\".\"f.x\"(IN \"arg.x\" pg_catalog.int4[])",
            "f(timestamp with time zone, double precision)"})
    void preservesRoutineSignaturesWithoutTreatingThemAsCalls(String signature) throws Exception {
        for (Comment comment : roundTrip("COMMENT ON FUNCTION " + signature + " IS 'body'")) {
            assertEquals(CommentTarget.Kind.FUNCTION, comment.getTarget().getKind());
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

    @ParameterizedTest
    @ValueSource(strings = {"INDEX", "MATERIALIZED obj", "MATERIALIZED VIEW", "FUNCTION f(*)",
            "FUNCTION f(integer ORDER BY integer)", "FUNCTION f(IN)",
            "FUNCTION f(x integer DEFAULT 1)",
            "CONSTRAINT ck", "CONSTRAINT ck ON",
            "CONSTRAINT public.ck ON t",
            "UNKNOWN obj"})
    void rejectsIncompleteOrInvalidTargets(String target) {
        assertThrows(JSQLParserException.class, () -> parse("COMMENT ON " + target + " IS NULL"));
    }

    @Test
    void findsOnlyRelationsExplicitlyNamedByComments() throws Exception {
        for (String target : List.of("INDEX idx", "SCHEMA app", "SEQUENCE seq", "DOMAIN d",
                "TYPE ty", "FUNCTION f(int)")) {
            assertTrue(new TablesNamesFinder().getTables(parse("COMMENT ON " + target + " IS NULL"))
                    .isEmpty());
        }
        for (String target : List.of("TABLE app.t", "COLUMN app.t.c", "VIEW app.t",
                "MATERIALIZED VIEW app.t", "CONSTRAINT ck ON app.t")) {
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

    @Test
    void customDeparsersCanRewriteRelationsAndLiterals() throws Exception {
        Comment comment = parse("COMMENT ON CONSTRAINT ck ON app.t IS 'body'");
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
        assertEquals("COMMENT ON CONSTRAINT ck ON app.new_t IS 'changed'", builder.toString());
        assertEquals("COMMENT ON CONSTRAINT ck ON app.t IS 'body'", comment.toString());
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
