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

import net.sf.jsqlparser.statement.create.table.NamedConstraint;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.create.table.CheckConstraint;
import net.sf.jsqlparser.statement.create.table.ConstraintAttributes;
import net.sf.jsqlparser.statement.create.table.ExcludeConstraint;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PostgreSqlAlterConstraintTest {
    @ParameterizedTest
    @ValueSource(strings = {
            "CONSTRAINT c CHECK (id > 0) NOT VALID",
            "CHECK (id > 0) NOT VALID",
            "CONSTRAINT c UNIQUE NULLS NOT DISTINCT (id) INCLUDE (other) WITH (fillfactor = 70) "
                    + "USING INDEX TABLESPACE fast DEFERRABLE INITIALLY DEFERRED",
            "UNIQUE (id) NOT DEFERRABLE INITIALLY IMMEDIATE",
            "CONSTRAINT c PRIMARY KEY (id) DEFERRABLE INITIALLY DEFERRED",
            "CONSTRAINT c FOREIGN KEY (id) REFERENCES parent(id) ON DELETE CASCADE "
                    + "DEFERRABLE INITIALLY DEFERRED NOT VALID",
            "FOREIGN KEY (id) REFERENCES parent(id) NOT VALID",
            "CONSTRAINT c EXCLUDE USING gist ((id + 1) WITH =) WHERE (id > 0) DEFERRABLE",
            "EXCLUDE USING gist (id WITH =)"
    })
    void sharedConstraintGrammarParsesAndRoundTrips(String definition)
            throws JSQLParserException {
        Alter alter = parse("ALTER TABLE t ADD " + definition);
        StringBuilder buffer = new StringBuilder();
        alter.accept(new StatementDeParser(buffer), null);
        assertEquals(alter.toString(), buffer.toString());
        assertEquals(alter.toString(), parse(buffer.toString()).toString());
        assertEquals(1, alter.getAlterExpressions().size());
    }

    @Test
    void constraintAttributesAndPredicatesRemainStructured() throws JSQLParserException {
        Alter alter = parse("ALTER TABLE t ADD CONSTRAINT c CHECK (id > 0) NOT VALID, "
                + "ADD CONSTRAINT u UNIQUE (id) DEFERRABLE INITIALLY DEFERRED");
        CheckConstraint check = assertInstanceOf(CheckConstraint.class,
                alter.getAlterExpressions().get(0).getConstraint());
        assertTrue(check.getConstraintAttributes().isNotValid());
        NamedConstraint unique = alter.getAlterExpressions().get(1).getConstraint();
        assertEquals(ConstraintAttributes.Initially.DEFERRED,
                unique.getConstraintAttributes().getInitially());
        assertEquals(Boolean.TRUE, unique.getConstraintAttributes().getDeferrable());
        ExcludeConstraint exclude = assertInstanceOf(ExcludeConstraint.class,
                parse("ALTER TABLE t ADD EXCLUDE USING gist ((id + 1) WITH =) WHERE (id > 0)")
                        .getAlterExpressions().get(0).getConstraint());
        assertEquals("id > 0", exclude.getExpression().toString());
    }

    @Test
    void commonProjectionPreservesLegacyKeyAccessors() throws JSQLParserException {
        AlterExpression primary = parse("ALTER TABLE t ADD PRIMARY KEY (id)")
                .getAlterExpressions().get(0);
        assertThat(primary.getPkColumns()).containsExactly("id");
        AlterExpression foreign =
                parse("ALTER TABLE t ADD FOREIGN KEY (id) REFERENCES app.parent(id)")
                        .getAlterExpressions().get(0);
        assertThat(foreign.getFkColumns()).containsExactly("id");
        assertEquals("app", foreign.getFkSourceSchema());
        assertEquals("parent", foreign.getFkSourceTable());
        assertThat(new TablesNamesFinder().getTables(
                parse("ALTER TABLE t ADD FOREIGN KEY (id) REFERENCES app.parent(id) NOT VALID")))
                .containsExactlyInAnyOrder("t", "app.parent");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "ALTER TABLE t ADD CONSTRAINT c CHECK (id > 0) NOT INVALID",
            "ALTER TABLE t ADD UNIQUE (id) INITIALLY UNKNOWN",
            "ALTER TABLE t ADD EXCLUDE USING gist (id)",
            "ALTER TABLE t ADD CONSTRAINT c CHECK (id > 0), ADD"
    })
    void invalidConstraintTailsAreRejected(String sql) {
        assertThrows(JSQLParserException.class, () -> parse(sql));
    }

    private static Alter parse(String sql) throws JSQLParserException {
        return (Alter) CCJSqlParserUtil.parse(sql,
                parser -> parser.withDialect(Dialect.POSTGRESQL));
    }
}
