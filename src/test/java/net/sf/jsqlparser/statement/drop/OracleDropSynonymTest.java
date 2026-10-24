/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.drop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class OracleDropSynonymTest {
    @ParameterizedTest
    @ValueSource(strings = {"sample_syn", "\"Sample.Synonym\""})
    void preservesSynonymModifiers(String name) throws JSQLParserException {
        for (String scope : new String[] {"", "PUBLIC "}) {
            for (String existence : new String[] {"", "IF EXISTS "}) {
                for (String force : new String[] {"", " FORCE"}) {
                    String sql = "DROP " + scope + "SYNONYM " + existence + name + force;
                    for (Dialect dialect : new Dialect[] {null, Dialect.ORACLE}) {
                        Drop drop = parse(sql, dialect);
                        assertEquals(Drop.ObjectType.SYNONYM, drop.getObjectType());
                        assertEquals("SYNONYM", drop.getType());
                        assertEquals(!scope.isEmpty(), drop.isPublicSynonym());
                        assertEquals(!existence.isEmpty(), drop.isIfExists());
                        assertEquals(!force.isEmpty(), drop.isForce());
                        assertEquals(1, drop.getNames().size());
                        assertEquals(name, drop.getName().getName());
                        assertEquals(sql, drop.toString());
                        roundTrip(drop, dialect);
                        assertEquals(2,
                                CCJSqlParserUtil.parseStatements(sql + "; SELECT 1").size());
                    }
                }
            }
        }
    }

    @Test
    void preservesQualifiedNamesAndSupportsModelChanges() throws JSQLParserException {
        Drop drop = parse("DROP SYNONYM IF EXISTS \"hr.schema\".\"sample.syn\" FORCE",
                Dialect.ORACLE);
        assertEquals("\"hr.schema\"", drop.getName().getSchemaName());
        assertEquals("\"sample.syn\"", drop.getName().getName());
        drop.getName().setName("replacement");
        assertEquals("DROP SYNONYM IF EXISTS \"hr.schema\".replacement FORCE", drop.toString());
        roundTrip(drop, Dialect.ORACLE);

        Drop created = new Drop().withObjectType(Drop.ObjectType.SYNONYM)
                .withPublicSynonym(true).withIfExists(true).withName(new Table("s"))
                .withForce(true);
        assertEquals("DROP PUBLIC SYNONYM IF EXISTS s FORCE", created.toString());
        roundTrip(created, Dialect.ORACLE);
        created.setPublicSynonym(false);
        created.setForce(false);
        assertEquals("DROP SYNONYM IF EXISTS s", created.toString());
        roundTrip(created, null);
    }

    @Test
    void keepsPostgreSqlForceSyntax() throws JSQLParserException {
        Drop database = parse("DROP DATABASE d WITH (FORCE)", Dialect.POSTGRESQL);
        assertTrue(database.isForce());
        assertEquals("DROP DATABASE d WITH (FORCE)", database.toString());
        roundTrip(database, Dialect.POSTGRESQL);
    }

    @ParameterizedTest
    @ValueSource(strings = {"DROP PUBLIC TABLE t", "DROP SYNONYM",
            "DROP PUBLIC SYNONYM IF EXISTS",
            "DROP SYNONYM s FORCE FORCE", "DROP SYNONYM s WITH (FORCE)",
            "DROP TABLE t FORCE"})
    void rejectsMalformedSynonymClauses(String sql) {
        assertThrows(JSQLParserException.class, () -> parse(sql, Dialect.ORACLE));
    }

    private static Drop parse(String sql, Dialect dialect) throws JSQLParserException {
        return (Drop) CCJSqlParserUtil.parse(sql, parser -> {
            if (dialect != null) {
                parser.withDialect(dialect);
            }
        });
    }

    private static void roundTrip(Drop drop, Dialect dialect) throws JSQLParserException {
        StringBuilder output = new StringBuilder();
        drop.accept(new StatementDeParser(output), null);
        assertEquals(drop.toString(), output.toString());
        Drop reparsed = parse(output.toString(), dialect);
        assertEquals(drop.getObjectType(), reparsed.getObjectType());
        assertEquals(drop.isPublicSynonym(), reparsed.isPublicSynonym());
        assertEquals(drop.isIfExists(), reparsed.isIfExists());
        assertEquals(drop.isForce(), reparsed.isForce());
        assertEquals(output.toString(), reparsed.toString());
    }
}
