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

import net.sf.jsqlparser.statement.create.table.KeyConstraint;
import net.sf.jsqlparser.statement.create.table.ConstraintKind;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.create.table.CheckConstraint;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.NamedConstraint;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class MySqlAlterConstraintTest {
    @ParameterizedTest
    @ValueSource(strings = {
            "CONSTRAINT uq UNIQUE KEY idx (id)",
            "CONSTRAINT uq UNIQUE INDEX idx USING BTREE (id DESC) COMMENT 'unique id'",
            "CONSTRAINT `unique id` UNIQUE KEY `index id` (id)",
            "CONSTRAINT UNIQUE KEY idx (id)",
            "CONSTRAINT uq UNIQUE (id)",
            "UNIQUE KEY idx (id)",
            "CONSTRAINT pk PRIMARY KEY (id)",
            "CONSTRAINT PRIMARY KEY (id)",
            "CHECK (id > 0)",
            "CONSTRAINT CHECK (id > 0) NOT ENFORCED",
            "CONSTRAINT positive CHECK (id > 0) ENFORCED",
            "CONSTRAINT fk FOREIGN KEY parent_idx (id) REFERENCES parent (id) ON DELETE CASCADE"
    })
    void createAndAlterExposeTheSameConstraint(String definition) throws JSQLParserException {
        CreateTable create = (CreateTable) CCJSqlParserUtil.parse(
                "CREATE TABLE t (id INT, " + definition + ")", p -> p.withDialect(Dialect.MYSQL));
        Alter alter = parse("ALTER TABLE t ADD " + definition + ", ADD COLUMN other INT");
        NamedConstraint actual = alter.getAlterExpressions().get(0).getConstraint();
        assertEquals(create.getTableConstraints().get(0).getClass(), actual.getClass());
        assertEquals(create.getTableConstraints().get(0).toString(), actual.toString());
        assertEquals(2, alter.getAlterExpressions().size());
        assertRoundTrip(alter);
    }

    @Test
    void constraintAndIndexNamesCanBeEditedIndependently() throws JSQLParserException {
        Alter alter = parse("ALTER TABLE t ADD CONSTRAINT uq UNIQUE KEY idx (id)");
        KeyConstraint unique = assertInstanceOf(KeyConstraint.class,
                alter.getAlterExpressions().get(0).getConstraint());
        assertEquals("uq", unique.getName());
        assertEquals("idx", unique.getIndexName());
        assertEquals(ConstraintKind.UNIQUE, unique.getKind());
        unique.setName("new_constraint");
        unique.setIndexName("new_index");
        assertEquals("ALTER TABLE t ADD CONSTRAINT new_constraint UNIQUE KEY new_index (id)",
                alter.toString());
        assertRoundTrip(alter);
    }

    @Test
    void unnamedCheckRetainsKeywordEnforcementAndExpressionVisitor() throws JSQLParserException {
        Alter alter = parse("ALTER TABLE t ADD CONSTRAINT CHECK (id > 0) NOT ENFORCED");
        CheckConstraint check = assertInstanceOf(CheckConstraint.class,
                alter.getAlterExpressions().get(0).getConstraint());
        assertTrue(check.isUseConstraintKeyword());
        assertEquals(Boolean.FALSE, check.getEnforced());
        StringBuilder buffer = new StringBuilder();
        ExpressionDeParser expressions = new ExpressionDeParser() {
            @Override
            public <S> StringBuilder visit(LongValue value, S context) {
                return getBuilder().append(10);
            }
        };
        alter.accept(new StatementDeParser(expressions, new SelectDeParser(), buffer), null);
        assertEquals("ALTER TABLE t ADD CONSTRAINT CHECK (id > 10) NOT ENFORCED",
                buffer.toString());
        assertRoundTrip(parse(buffer.toString()));
    }

    @Test
    void sharedIndexStillProvidesLegacyPrimaryKeyColumns() throws JSQLParserException {
        AlterExpression primary = parse("ALTER TABLE t ADD CONSTRAINT pk PRIMARY KEY (id)")
                .getAlterExpressions().get(0);
        assertEquals(List.of("id"), primary.getPkColumns());
    }

    @ParameterizedTest
    @ValueSource(strings = {"CHECK", "CHECK ()", "CONSTRAINT CHECK (id > 0) (id < 10)",
            "CONSTRAINT uq UNIQUE KEY idx ()"})
    void rejectsIncompleteOrRepeatedConstraintBodies(String definition) {
        assertThrows(JSQLParserException.class, () -> parse("ALTER TABLE t ADD " + definition));
    }

    private static Alter parse(String sql) throws JSQLParserException {
        return (Alter) CCJSqlParserUtil.parse(sql, p -> p.withDialect(Dialect.MYSQL));
    }

    private static void assertRoundTrip(Alter alter) throws JSQLParserException {
        StringBuilder buffer = new StringBuilder();
        alter.accept(new StatementDeParser(buffer), null);
        assertEquals(alter.toString(), buffer.toString());
        assertEquals(alter.toString(), parse(buffer.toString()).toString());
    }
}
