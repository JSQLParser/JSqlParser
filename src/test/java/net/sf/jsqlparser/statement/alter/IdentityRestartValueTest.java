/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.alter;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class IdentityRestartValueTest {
    @ParameterizedTest
    @ValueSource(strings = {"20", "WITH 20", "+20", "-20", "WITH -20", "9223372036854775807"})
    void representsOptionalWithUsingTheSameValue(String value) throws JSQLParserException {
        for (Dialect dialect : new Dialect[] {null, Dialect.POSTGRESQL}) {
            Alter alter = parse("ALTER TABLE t ALTER COLUMN id RESTART " + value, dialect);
            IdentityAlteration restart = identity(alter).get(0);
            assertEquals(IdentityAlteration.Kind.RESTART, restart.getKind());
            assertEquals(Long.valueOf(value.replace("WITH ", "")), restart.getRestartWith());
            roundTrip(alter, dialect);
        }
    }

    @Test
    void editsAndConstructsRestartAndPreservesBareRestart() throws JSQLParserException {
        Alter alter = parse("ALTER TABLE t ALTER COLUMN id RESTART", Dialect.POSTGRESQL);
        assertNull(identity(alter).get(0).getRestartWith());
        identity(alter).get(0).setRestartWith(20L);
        assertEquals("ALTER TABLE t ALTER COLUMN id RESTART WITH 20", alter.toString());
        roundTrip(alter, Dialect.POSTGRESQL);
        alter.getAlterExpressions().get(0).getColDataTypeList().get(0).setIdentityAlterations(
                List.of(new IdentityAlteration(IdentityAlteration.Kind.RESTART)
                        .withRestartWith(40L)));
        assertEquals("ALTER TABLE t ALTER COLUMN id RESTART WITH 40", alter.toString());
        roundTrip(alter, Dialect.POSTGRESQL);
        identity(alter).get(0).setRestartWith(null);
        assertEquals("ALTER TABLE t ALTER COLUMN id RESTART", alter.toString());
    }

    @Test
    void respectsIdentityAndAlterActionBoundaries() throws JSQLParserException {
        Alter alter = parse(
                "ALTER TABLE t ALTER COLUMN id SET CACHE 10 RESTART 20 SET NO CYCLE, ADD COLUMN extra INT",
                Dialect.POSTGRESQL);
        assertEquals(3, identity(alter).size());
        assertEquals(2, alter.getAlterExpressions().size());
        roundTrip(alter, Dialect.POSTGRESQL);
        roundTrip(parse("ALTER TABLE t ALTER COLUMN id RESTART SET CACHE 10", Dialect.POSTGRESQL),
                Dialect.POSTGRESQL);
        assertThrows(JSQLParserException.class,
                () -> parse("ALTER TABLE t ALTER COLUMN id RESTART WITH", Dialect.POSTGRESQL));
    }

    private static List<IdentityAlteration> identity(Alter alter) {
        return alter.getAlterExpressions().get(0).getColDataTypeList().get(0)
                .getIdentityAlterations();
    }

    private static Alter parse(String sql, Dialect dialect) throws JSQLParserException {
        return (Alter) CCJSqlParserUtil.parse(sql, p -> {
            if (dialect != null) {
                p.withDialect(dialect);
            }
        });
    }

    private static void roundTrip(Alter alter, Dialect dialect) throws JSQLParserException {
        StringBuilder sql = new StringBuilder();
        alter.accept(new StatementDeParser(sql), null);
        assertEquals(alter.toString(), sql.toString());
        assertEquals(sql.toString(), parse(sql.toString(), dialect).toString());
    }
}
