/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.spatial;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.HexValue;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.ASTNodeAccess;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.Node;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.StmtFeature;
import net.sf.jsqlparser.statement.UnsupportedStatement;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.spatial.SpatialReferenceSystemAttribute.Kind;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.test.TestUtils;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

/** MySQL CREATE SPATIAL REFERENCE SYSTEM and DROP SPATIAL REFERENCE SYSTEM. */
class SpatialReferenceSystemTest {
    private static final Dialect[] MYSQL_OR_NONE = {Dialect.MYSQL, null};
    private static final String FULL = "CREATE SPATIAL REFERENCE SYSTEM 4120 NAME 'Greek' "
            + "ORGANIZATION 'EPSG' IDENTIFIED BY 4120 DEFINITION 'GEOGCS[\"Greek\"]' "
            + "DESCRIPTION 'd'";

    private static Statement parse(String sql, Dialect dialect) throws JSQLParserException {
        return dialect == null ? CCJSqlParserUtil.parse(sql)
                : CCJSqlParserUtil.parse(sql, p -> p.withDialect(dialect));
    }

    private static void assertSpans(String expected, String source, ASTNodeAccess access) {
        Node node = access.getASTNode();
        assertNotNull(node, expected);
        assertEquals(expected, source.substring(node.jjtGetFirstToken().absoluteBegin - 1,
                node.jjtGetLastToken().absoluteEnd - 1));
    }

    /**
     * Parses {@code sql}, which must print as written, and checks the statement span, the deparser,
     * the reparse, the feature analysis and the table finder.
     */
    private static Statement roundTrip(String sql, Dialect dialect, StmtFeature... features)
            throws Exception {
        Statement statement = parse(sql, dialect);
        assertEquals(sql, statement.toString());
        assertSpans(sql, sql, (ASTNodeAccess) statement);
        StringBuilder output = new StringBuilder();
        statement.accept(new StatementDeParser(output), null);
        assertEquals(sql, output.toString());
        Statement reparsed = parse(output.toString(), dialect);
        assertEquals(statement.getClass(), reparsed.getClass());
        assertEquals(sql, reparsed.toString());
        assertEquals(Set.of(features), statement.getFeatures().getCertain());
        assertTrue(statement.getFeatures().getUncertain().isEmpty());
        assertTrue(new TablesNamesFinder<>().getTables(statement).isEmpty());
        return statement;
    }

    private static CreateSpatialReferenceSystem create(String sql, Dialect dialect)
            throws Exception {
        return assertInstanceOf(CreateSpatialReferenceSystem.class,
                roundTrip(sql, dialect, StmtFeature.MODIFIES_SCHEMA));
    }

    private static Drop drop(String sql, Dialect dialect) throws Exception {
        return assertInstanceOf(Drop.class,
                roundTrip(sql, dialect, StmtFeature.MODIFIES_SCHEMA, StmtFeature.MODIFIES_DATA));
    }

    // ---- CREATE --------------------------------------------------------------------------------

    @Test
    void createKeepsTheSridAndEveryAttribute() throws Exception {
        for (Dialect dialect : MYSQL_OR_NONE) {
            CreateSpatialReferenceSystem statement = create(FULL, dialect);
            assertFalse(statement.isOrReplace());
            assertFalse(statement.isIfNotExists());
            assertEquals(4120L, assertInstanceOf(LongValue.class, statement.getSrid()).getValue());
            assertSpans("4120", FULL, (ASTNodeAccess) statement.getSrid());
            assertEquals("Greek", statement.getName().getValue());
            assertEquals("EPSG", statement.getOrganization().getValue());
            assertEquals(4120L, assertInstanceOf(LongValue.class, statement.getOrganizationId())
                    .getValue());
            assertEquals("GEOGCS[\"Greek\"]", statement.getDefinition().getValue());
            assertEquals("d", statement.getDescription().getValue());

            List<SpatialReferenceSystemAttribute> attributes = statement.getAttributes();
            assertEquals(Arrays.asList(Kind.NAME, Kind.ORGANIZATION, Kind.DEFINITION,
                    Kind.DESCRIPTION),
                    Arrays.asList(attributes.get(0).getKind(), attributes.get(1).getKind(),
                            attributes.get(2).getKind(), attributes.get(3).getKind()));
            assertEquals(4, attributes.size());
            assertSpans("NAME 'Greek'", FULL, attributes.get(0));
            assertSpans("'Greek'", FULL, attributes.get(0).getValue());
            assertNull(attributes.get(0).getOrganizationId());
            assertSpans("ORGANIZATION 'EPSG' IDENTIFIED BY 4120", FULL, attributes.get(1));
            assertSpans("'EPSG'", FULL, attributes.get(1).getValue());
            assertSame(statement.getOrganizationId(), attributes.get(1).getOrganizationId());
            assertSpans("DEFINITION 'GEOGCS[\"Greek\"]'", FULL, attributes.get(2));
            assertSpans("DESCRIPTION 'd'", FULL, attributes.get(3));
            assertEquals("DESCRIPTION 'd'", attributes.get(3).toString());
            assertSame(attributes.get(2), statement.getAttribute(Kind.DEFINITION));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "CREATE OR REPLACE SPATIAL REFERENCE SYSTEM 4120 NAME 'Greek' DEFINITION 'x'",
            "CREATE SPATIAL REFERENCE SYSTEM IF NOT EXISTS 4120 NAME 'Greek' DEFINITION 'x'",
            "CREATE SPATIAL REFERENCE SYSTEM 4120 DEFINITION 'x' NAME 'Greek'",
            "CREATE SPATIAL REFERENCE SYSTEM 4120 DESCRIPTION 'd' ORGANIZATION 'o' IDENTIFIED BY 1 "
                    + "DEFINITION 'x' NAME 'n'",
            "CREATE SPATIAL REFERENCE SYSTEM 4120 NAME \"n\" DEFINITION \"x\"",
            "CREATE SPATIAL REFERENCE SYSTEM 4120 NAME 'n' DEFINITION 'x' ORGANIZATION \"o\" "
                    + "IDENTIFIED BY 1",
            "CREATE SPATIAL REFERENCE SYSTEM 4120 NAME 'it''s' DEFINITION 'x'",
            "CREATE SPATIAL REFERENCE SYSTEM 4120 NAME '' DEFINITION ''",
            "CREATE SPATIAL REFERENCE SYSTEM 04120 NAME 'n' DEFINITION 'x'",
            "CREATE SPATIAL REFERENCE SYSTEM 4294967296 NAME 'n' DEFINITION 'x'",
            "CREATE SPATIAL REFERENCE SYSTEM 4120 NAME 'n' DEFINITION 'x' ORGANIZATION 'o' "
                    + "IDENTIFIED BY 4294967296",
            // the server reports a missing or repeated attribute itself, not as a syntax error
            "CREATE SPATIAL REFERENCE SYSTEM 4120",
            "CREATE OR REPLACE SPATIAL REFERENCE SYSTEM 4120",
            "CREATE SPATIAL REFERENCE SYSTEM IF NOT EXISTS 4120",
            "CREATE SPATIAL REFERENCE SYSTEM 4120 NAME 'n'",
            "CREATE SPATIAL REFERENCE SYSTEM 4120 DESCRIPTION 'd'",
            "CREATE SPATIAL REFERENCE SYSTEM 4120 ORGANIZATION 'o' IDENTIFIED BY 0",
            "CREATE SPATIAL REFERENCE SYSTEM 4120 NAME 'a' NAME 'b' DEFINITION 'x'",
            "CREATE SPATIAL REFERENCE SYSTEM 4120 NAME 'a' DEFINITION 'x' DEFINITION 'y'",
            "CREATE SPATIAL REFERENCE SYSTEM 4120 NAME 'a' DEFINITION 'x' DESCRIPTION 'd' "
                    + "DESCRIPTION 'e'",
            "CREATE SPATIAL REFERENCE SYSTEM 4120 NAME 'a' DEFINITION 'x' ORGANIZATION 'o' "
                    + "IDENTIFIED BY 1 ORGANIZATION 'p' IDENTIFIED BY 2"})
    void createTakesTheAttributesInAnyOrderAndNumber(String sql) throws Exception {
        for (Dialect dialect : MYSQL_OR_NONE) {
            CreateSpatialReferenceSystem statement = create(sql, dialect);
            assertEquals(sql.startsWith("CREATE OR REPLACE"), statement.isOrReplace());
            assertEquals(sql.contains("IF NOT EXISTS"), statement.isIfNotExists());
        }
    }

    @Test
    void repeatedAttributesStayInTheListAndTheGettersReturnTheFirst() throws Exception {
        CreateSpatialReferenceSystem statement = create(
                "CREATE SPATIAL REFERENCE SYSTEM 1 NAME 'a' ORGANIZATION 'o' IDENTIFIED BY 1 "
                        + "NAME 'b' ORGANIZATION 'p' IDENTIFIED BY 2",
                Dialect.MYSQL);
        assertEquals(4, statement.getAttributes().size());
        assertEquals("a", statement.getName().getValue());
        assertEquals("o", statement.getOrganization().getValue());
        assertEquals("1", statement.getOrganizationId().toString());
        assertNull(statement.getDefinition());
        assertNull(statement.getDescription());

        CreateSpatialReferenceSystem bare =
                create("CREATE SPATIAL REFERENCE SYSTEM 1", Dialect.MYSQL);
        assertTrue(bare.getAttributes().isEmpty());
        assertNull(bare.getName());
        assertNull(bare.getOrganization());
        assertNull(bare.getOrganizationId());
        assertNull(bare.getAttribute(Kind.NAME));
    }

    @Test
    void sridAndOrganizationIdAreUnsignedIntegersOrHexadecimalNumbers() throws Exception {
        String sql = "CREATE SPATIAL REFERENCE SYSTEM 0x3D0978 NAME 'n' DEFINITION 'x' "
                + "ORGANIZATION 'o' IDENTIFIED BY X'10'";
        for (Dialect dialect : MYSQL_OR_NONE) {
            CreateSpatialReferenceSystem statement = create(sql, dialect);
            assertEquals("0x3D0978",
                    assertInstanceOf(HexValue.class, statement.getSrid()).getValue());
            assertSpans("0x3D0978", sql, (ASTNodeAccess) statement.getSrid());
            assertEquals("X'10'",
                    assertInstanceOf(HexValue.class, statement.getOrganizationId()).getValue());
            assertSpans("X'10'", sql, (ASTNodeAccess) statement.getOrganizationId());
        }
        create("CREATE SPATIAL REFERENCE SYSTEM x'3D0978' NAME 'n'", Dialect.MYSQL);
        create("CREATE SPATIAL REFERENCE SYSTEM X'' NAME 'n'", Dialect.MYSQL);
        create("CREATE SPATIAL REFERENCE SYSTEM 0x3d0978 NAME 'n'", Dialect.MYSQL);

        // the grammar takes any 64-bit unsigned integer; the server then checks the SRID range
        String largest = "CREATE SPATIAL REFERENCE SYSTEM 18446744073709551615 NAME 'n'";
        assertEquals(new BigInteger("18446744073709551615"),
                assertInstanceOf(LongValue.class, create(largest, Dialect.MYSQL).getSrid())
                        .getBigIntegerValue());
        create("CREATE SPATIAL REFERENCE SYSTEM 000000000000000000000004000120 NAME 'n'",
                Dialect.MYSQL);
    }

    @Test
    void keywordsAreCaseInsensitiveAndDoubleQuotedStringsAreStrings() throws Exception {
        for (Dialect dialect : MYSQL_OR_NONE) {
            Statement statement = parse("create or replace spatial reference system 1 name 'n' "
                    + "definition 'x' organization 'o' identified by 2 description 'd'", dialect);
            assertEquals("CREATE OR REPLACE SPATIAL REFERENCE SYSTEM 1 NAME 'n' DEFINITION 'x' "
                    + "ORGANIZATION 'o' IDENTIFIED BY 2 DESCRIPTION 'd'", statement.toString());

            CreateSpatialReferenceSystem quoted = create(
                    "CREATE SPATIAL REFERENCE SYSTEM 1 NAME \"a b\" DEFINITION 'x'", dialect);
            assertEquals("a b", quoted.getName().getValue());
            assertEquals("\"", quoted.getName().getQuoteStr());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "CREATE OR REPLACE SPATIAL REFERENCE SYSTEM IF NOT EXISTS 4120 NAME 'n' DEFINITION 'x'",
            "CREATE SPATIAL REFERENCE SYSTEM NAME 'n' DEFINITION 'x'",
            "CREATE SPATIAL REFERENCE SYSTEM '4120' NAME 'n' DEFINITION 'x'",
            "CREATE SPATIAL REFERENCE SYSTEM `4120` NAME 'n' DEFINITION 'x'",
            "CREATE SPATIAL REFERENCE SYSTEM srs1 NAME 'n' DEFINITION 'x'",
            "CREATE SPATIAL REFERENCE SYSTEM -1 NAME 'n' DEFINITION 'x'",
            "CREATE SPATIAL REFERENCE SYSTEM +1 NAME 'n' DEFINITION 'x'",
            "CREATE SPATIAL REFERENCE SYSTEM (1) NAME 'n' DEFINITION 'x'",
            "CREATE SPATIAL REFERENCE SYSTEM 1.5 NAME 'n' DEFINITION 'x'",
            "CREATE SPATIAL REFERENCE SYSTEM 1e3 NAME 'n' DEFINITION 'x'",
            "CREATE SPATIAL REFERENCE SYSTEM 1_000 NAME 'n' DEFINITION 'x'",
            "CREATE SPATIAL REFERENCE SYSTEM 18446744073709551616 NAME 'n' DEFINITION 'x'",
            "CREATE SPATIAL REFERENCE SYSTEM 0X3D0978 NAME 'n' DEFINITION 'x'",
            "CREATE SPATIAL REFERENCE SYSTEM X'1' NAME 'n' DEFINITION 'x'",
            "CREATE SPATIAL REFERENCE SYSTEM B'1' NAME 'n' DEFINITION 'x'",
            "CREATE SPATIAL REFERENCE SYSTEM 0b101 NAME 'n' DEFINITION 'x'",
            "CREATE SPATIAL REFERENCE SYSTEM 4120, 4121 NAME 'n' DEFINITION 'x'",
            "CREATE SPATIAL REFERENCE SYSTEM IF EXISTS 4120 NAME 'n' DEFINITION 'x'",
            "CREATE SPATIAL REFERENCE SYSTEM 4120 IF NOT EXISTS NAME 'n' DEFINITION 'x'",
            "CREATE SPATIAL REFERENCE SYSTEM 4120 NAME n DEFINITION 'x'",
            "CREATE SPATIAL REFERENCE SYSTEM 4120 NAME 1 DEFINITION 'x'",
            "CREATE SPATIAL REFERENCE SYSTEM 4120 NAME N'n' DEFINITION 'x'",
            "CREATE SPATIAL REFERENCE SYSTEM 4120 NAME _utf8'n' DEFINITION 'x'",
            "CREATE SPATIAL REFERENCE SYSTEM 4120 NAME _utf8mb4'n' DEFINITION 'x'",
            "CREATE SPATIAL REFERENCE SYSTEM 4120 NAME X'41' DEFINITION 'x'",
            "CREATE SPATIAL REFERENCE SYSTEM 4120 NAME 'a' 'b' DEFINITION 'x'",
            "CREATE SPATIAL REFERENCE SYSTEM 4120 NAME ('n') DEFINITION 'x'",
            "CREATE SPATIAL REFERENCE SYSTEM 4120 NAME = 'n' DEFINITION 'x'",
            "CREATE SPATIAL REFERENCE SYSTEM 4120 NAME 'n', DEFINITION 'x'",
            "CREATE SPATIAL REFERENCE SYSTEM 4120 NAME 'n' DEFINITION",
            "CREATE SPATIAL REFERENCE SYSTEM 4120 NAME 'n' DEFINITION 'x' ORGANIZATION 'o'",
            "CREATE SPATIAL REFERENCE SYSTEM 4120 NAME 'n' ORGANIZATION 'o' IDENTIFIED BY 'x'",
            "CREATE SPATIAL REFERENCE SYSTEM 4120 NAME 'n' ORGANIZATION 'o' IDENTIFIED BY -1",
            "CREATE SPATIAL REFERENCE SYSTEM 4120 NAME 'n' ORGANIZATION 'o' IDENTIFIED BY 1.5",
            "CREATE SPATIAL REFERENCE SYSTEM 4120 NAME 'n' ORGANIZATION 'o' IDENTIFIED 1",
            "CREATE SPATIAL REFERENCE SYSTEM 4120 NAME 'n' ORGANIZATION 'o' IDENTIFIED WITH 1",
            "CREATE SPATIAL REFERENCE SYSTEM 4120 NAME 'n' ORGANIZATION IDENTIFIED BY 1",
            "CREATE SPATIAL REFERENCE SYSTEM 4120 NAME 'n' DEFINITION 'x' IDENTIFIED BY 1",
            "CREATE SPATIAL REFERENCE SYSTEM 4120 NAME 'n' DEFINITION 'x' COMMENT 'c'",
            "CREATE SPATIAL REFERENCE SYSTEM 4120 SRID 1 NAME 'n' DEFINITION 'x'",
            "CREATE SPATIAL REFERENCE SYSTEM 4120 NAME 'n' DEFINITION 'x' x"})
    void createRejectsWhatTheServerGrammarRejects(String sql) {
        for (Dialect dialect : MYSQL_OR_NONE) {
            assertThrows(JSQLParserException.class, () -> parse(sql, dialect), sql);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"CREATE SPATIAL REFERENCE 4120 NAME 'n' DEFINITION 'x'",
            "CREATE SPATIAL SYSTEM 4120 NAME 'n' DEFINITION 'x'",
            "CREATE SPATIAL REFERENCE SYSTEMS 4120 NAME 'n' DEFINITION 'x'"})
    void mySqlDialectRejectsAMisspelledObjectType(String sql) throws Exception {
        assertThrows(JSQLParserException.class, () -> parse(sql, Dialect.MYSQL), sql);
        // without a dialect only the exact words select the statement
        assertInstanceOf(UnsupportedStatement.class, parse(sql, null));
    }

    @ParameterizedTest
    @EnumSource(value = Dialect.class, names = {"MARIADB", "POSTGRESQL", "ORACLE", "SQLSERVER"})
    void otherDialectsKeepTheirReading(Dialect dialect) throws Exception {
        assertInstanceOf(UnsupportedStatement.class, parse(FULL, dialect));
        assertThrows(JSQLParserException.class,
                () -> parse("DROP SPATIAL REFERENCE SYSTEM 4120", dialect));
    }

    @Test
    void spatialIndexStatementsKeepTheirReading() throws Exception {
        for (Dialect dialect : MYSQL_OR_NONE) {
            assertInstanceOf(CreateIndex.class, parse("CREATE SPATIAL INDEX i ON t (g)", dialect));
            assertInstanceOf(Alter.class, parse("ALTER TABLE t ADD SPATIAL INDEX i (g)", dialect));
            assertEquals(Drop.ObjectType.INDEX,
                    ((Drop) parse("DROP INDEX i ON t", dialect)).getObjectType());
        }
        TestUtils.assertSqlCanBeParsedAndDeparsed("CREATE SPATIAL INDEX i ON t (g)", true);
    }

    @Test
    void createCanBeBuiltAndEdited() {
        CreateSpatialReferenceSystem statement =
                new CreateSpatialReferenceSystem(new LongValue(4120))
                        .addAttributes(
                                new SpatialReferenceSystemAttribute(Kind.NAME,
                                        new StringValue("'Greek'")),
                                new SpatialReferenceSystemAttribute(new StringValue("'EPSG'"),
                                        new LongValue(4120)))
                        .withIfNotExists(true);
        assertEquals("CREATE SPATIAL REFERENCE SYSTEM IF NOT EXISTS 4120 NAME 'Greek' "
                + "ORGANIZATION 'EPSG' IDENTIFIED BY 4120", statement.toString());
        assertThrows(IllegalArgumentException.class, () -> statement.setOrReplace(true));

        statement.setIfNotExists(false);
        statement.withOrReplace(true).withSrid(new HexValue("0x10"));
        assertThrows(IllegalArgumentException.class, () -> statement.setIfNotExists(true));
        statement.getAttributes().get(0).withValue(new StringValue("'n'"));
        statement.getAttributes().get(1).withOrganizationId(new LongValue(7));
        assertEquals("CREATE OR REPLACE SPATIAL REFERENCE SYSTEM 0x10 NAME 'n' "
                + "ORGANIZATION 'EPSG' IDENTIFIED BY 7", statement.toString());

        statement.withAttributes(Collections.singletonList(
                new SpatialReferenceSystemAttribute(Kind.DEFINITION, new StringValue("'x'"))));
        assertEquals("CREATE OR REPLACE SPATIAL REFERENCE SYSTEM 0x10 DEFINITION 'x'",
                statement.toString());
        statement.setAttributes(null);
        assertEquals("CREATE OR REPLACE SPATIAL REFERENCE SYSTEM 0x10", statement.toString());

        assertThrows(NullPointerException.class, () -> new CreateSpatialReferenceSystem(null));
        assertThrows(IllegalArgumentException.class,
                () -> new SpatialReferenceSystemAttribute(Kind.ORGANIZATION,
                        new StringValue("'o'")));
        assertThrows(IllegalArgumentException.class,
                () -> new SpatialReferenceSystemAttribute(Kind.NAME, new StringValue("'n'"))
                        .setOrganizationId(new LongValue(1)));
        assertThrows(NullPointerException.class,
                () -> new SpatialReferenceSystemAttribute(Kind.NAME, null));
        assertThrows(NullPointerException.class,
                () -> new SpatialReferenceSystemAttribute(new StringValue("'o'"), null));
    }

    // ---- DROP ----------------------------------------------------------------------------------

    @Test
    void dropNamesTheSrid() throws Exception {
        for (Dialect dialect : MYSQL_OR_NONE) {
            String sql = "DROP SPATIAL REFERENCE SYSTEM IF EXISTS 4120";
            Drop drop = drop(sql, dialect);
            assertEquals(Drop.ObjectType.SPATIAL_REFERENCE_SYSTEM, drop.getObjectType());
            assertEquals("SPATIAL REFERENCE SYSTEM", drop.getType());
            assertTrue(drop.isIfExists());
            assertEquals(1, drop.getNames().size());
            assertEquals("4120", drop.getName().getName());
            assertSpans("4120", sql, drop.getName());
            assertNull(drop.getParameters());
            assertNull(drop.getBehavior());

            assertFalse(drop("DROP SPATIAL REFERENCE SYSTEM 4120", dialect).isIfExists());
            assertEquals("0x3D0978",
                    drop("DROP SPATIAL REFERENCE SYSTEM 0x3D0978", dialect).getName().getName());
            assertEquals("X'10'",
                    drop("DROP SPATIAL REFERENCE SYSTEM X'10'", dialect).getName().getName());
            drop("DROP SPATIAL REFERENCE SYSTEM IF EXISTS 4294967296", dialect);
            assertEquals("DROP SPATIAL REFERENCE SYSTEM IF EXISTS 4120",
                    parse("drop spatial reference system if exists 4120", dialect).toString());
        }
    }

    @Test
    void dropCanBeBuilt() {
        Drop drop = new Drop().withObjectType(Drop.ObjectType.SPATIAL_REFERENCE_SYSTEM)
                .withIfExists(true).withName(new Table("4120", false));
        assertEquals("DROP SPATIAL REFERENCE SYSTEM IF EXISTS 4120", drop.toString());
        assertEquals(Drop.ObjectType.SPATIAL_REFERENCE_SYSTEM,
                new Drop().withType("spatial reference system").getObjectType());
    }

    @ParameterizedTest
    @ValueSource(strings = {"DROP SPATIAL REFERENCE SYSTEM",
            "DROP SPATIAL REFERENCE SYSTEM IF EXISTS",
            "DROP SPATIAL REFERENCE SYSTEM IF NOT EXISTS 4120",
            "DROP SPATIAL REFERENCE SYSTEM 4120, 4121",
            "DROP SPATIAL REFERENCE SYSTEM '4120'",
            "DROP SPATIAL REFERENCE SYSTEM `4120`",
            "DROP SPATIAL REFERENCE SYSTEM srs1",
            "DROP SPATIAL REFERENCE SYSTEM -1",
            "DROP SPATIAL REFERENCE SYSTEM 1.5",
            "DROP SPATIAL REFERENCE SYSTEM X'1'",
            "DROP SPATIAL REFERENCE SYSTEM 0X10",
            "DROP SPATIAL REFERENCE SYSTEM 18446744073709551616",
            "DROP SPATIAL REFERENCE SYSTEM 4120 CASCADE",
            "DROP SPATIAL REFERENCE SYSTEM 4120 RESTRICT",
            "DROP SPATIAL REFERENCE SYSTEM 4120 IF EXISTS",
            "DROP SPATIAL REFERENCE SYSTEM 4120 x",
            "DROP SPATIAL REFERENCE SYSTEM 4120 ON t",
            "DROP SPATIAL REFERENCE SYSTEM IF EXISTS 4120 NAME 'n'",
            "DROP SPATIAL REFERENCE 4120",
            "DROP SPATIAL SYSTEM 4120",
            "DROP SPATIAL 4120",
            "DROP TEMPORARY SPATIAL REFERENCE SYSTEM 4120",
            "DROP MATERIALIZED SPATIAL REFERENCE SYSTEM 4120"})
    void dropRejectsWhatTheServerGrammarRejects(String sql) {
        for (Dialect dialect : MYSQL_OR_NONE) {
            assertThrows(JSQLParserException.class, () -> parse(sql, dialect), sql);
        }
    }

    // ---- statement boundaries and visitors -----------------------------------------------------

    @Test
    void statementsEndAtTheBoundary() throws Exception {
        String script = "CREATE SPATIAL REFERENCE SYSTEM 4120 NAME 'n' DEFINITION 'x';\n"
                + "DROP SPATIAL REFERENCE SYSTEM IF EXISTS 4120;\n"
                + "CREATE OR REPLACE SPATIAL REFERENCE SYSTEM 4120;\n"
                + "SELECT 1;\n"
                + "CREATE SPATIAL INDEX i ON t (g);\n"
                + "DROP SPATIAL REFERENCE SYSTEM 4120";
        for (Dialect dialect : MYSQL_OR_NONE) {
            Statements statements = dialect == null ? CCJSqlParserUtil.parseStatements(script)
                    : CCJSqlParserUtil.parseStatements(script, p -> p.withDialect(dialect));
            assertEquals(6, statements.size());
            assertInstanceOf(CreateSpatialReferenceSystem.class, statements.get(0));
            assertEquals(Drop.ObjectType.SPATIAL_REFERENCE_SYSTEM,
                    ((Drop) statements.get(1)).getObjectType());
            assertTrue(((CreateSpatialReferenceSystem) statements.get(2)).getAttributes()
                    .isEmpty());
            assertInstanceOf(Select.class, statements.get(3));
            assertInstanceOf(CreateIndex.class, statements.get(4));
            assertInstanceOf(Drop.class, statements.get(5));
            assertSpans("CREATE SPATIAL REFERENCE SYSTEM 4120 NAME 'n' DEFINITION 'x'", script,
                    (ASTNodeAccess) statements.get(0));
            assertSpans("DROP SPATIAL REFERENCE SYSTEM IF EXISTS 4120", script,
                    (ASTNodeAccess) statements.get(1));
        }
        assertEquals("CREATE SPATIAL REFERENCE SYSTEM 1 NAME 'n'",
                parse("CREATE SPATIAL REFERENCE SYSTEM 1 NAME 'n';", Dialect.MYSQL).toString());
    }

    @Test
    void visitorsReachTheStatement() throws Exception {
        CreateSpatialReferenceSystem statement =
                (CreateSpatialReferenceSystem) parse(FULL, Dialect.MYSQL);
        assertNull(statement.accept(new StatementVisitorAdapter<String>(), null));

        StringBuilder seen = new StringBuilder();
        StatementVisitor<String> visitor = new StatementVisitorAdapter<String>() {
            @Override
            public <S> String visit(CreateSpatialReferenceSystem visited, S context) {
                seen.append(visited.getSrid()).append(context);
                return "visited";
            }
        };
        assertEquals("visited", statement.accept(visitor, "!"));
        visitor.visit(statement);
        assertEquals("4120!4120null", seen.toString());
    }
}
