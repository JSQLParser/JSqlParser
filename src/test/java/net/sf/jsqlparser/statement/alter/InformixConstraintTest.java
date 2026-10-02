/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.alter;

import net.sf.jsqlparser.statement.create.table.KeyConstraint;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import net.sf.jsqlparser.statement.create.table.CheckConstraint;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.util.deparser.TableElementDeParser;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.test.TestUtils;
import java.util.Set;
import java.util.List;
import java.util.stream.Stream;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.create.table.ForeignKeyConstraint;
import net.sf.jsqlparser.statement.create.table.NamedConstraint;
import net.sf.jsqlparser.statement.create.table.NamedConstraint.ConstraintNamePosition;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class InformixConstraintTest {
    static Stream<String> definitions() {
        return Stream.of("PRIMARY KEY (column)", "UNIQUE (column)",
                "FOREIGN KEY (column) REFERENCES referenced_table(referenced_column)",
                "CHECK (id > 0)", "PRIMARY KEY (id, tenant_id)",
                "FOREIGN KEY (id, tenant_id) REFERENCES parent(id, tenant_id) ON DELETE CASCADE");
    }

    @ParameterizedTest
    @MethodSource("definitions")
    void preservesNamePlacement(String definition) throws Exception {
        for (String suffix : List.of("", " CONSTRAINT constraint_name",
                " CONSTRAINT \"constraint name\"")) {
            String sql = "ALTER TABLE table_name ADD CONSTRAINT " + definition + suffix;
            Alter statement = (Alter) TestUtils.assertSqlCanBeParsedAndDeparsed(sql, true,
                    parser -> parser.withDialect(Dialect.INFORMIX));
            NamedConstraint constraint =
                    (NamedConstraint) statement.getAlterExpressions().get(0).getConstraint();
            assertEquals(ConstraintNamePosition.AFTER, constraint.getConstraintNamePosition());
            assertTrue(constraint.isUseConstraintKeyword());
            assertEquals(suffix.isEmpty() ? null : suffix.substring(" CONSTRAINT ".length()),
                    constraint.getName());
            StringBuilder deparsed = new StringBuilder();
            statement.accept(new StatementDeParser(deparsed), null);
            assertEquals(statement.toString(), deparsed.toString());
            assertEquals(statement.toString(),
                    CCJSqlParserUtil.parse(deparsed.toString(),
                            parser -> parser.withDialect(Dialect.INFORMIX)).toString());
            assertTrue(statement.toString().contains("ADD CONSTRAINT " + definition));
            if (!suffix.isEmpty()) {
                assertTrue(statement.toString().endsWith(suffix));
            }
        }
    }

    @ParameterizedTest
    @MethodSource("definitions")
    void requiresInformixDialectForTrailingNames(String definition) {
        for (String suffix : List.of(" CONSTRAINT constraint_name",
                " CONSTRAINT \"constraint name\"")) {
            String sql = "ALTER TABLE table_name ADD CONSTRAINT " + definition + suffix;
            assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql));
            for (Dialect dialect : Dialect.values()) {
                if (dialect != Dialect.INFORMIX) {
                    assertThrows(JSQLParserException.class,
                            () -> CCJSqlParserUtil.parse(sql,
                                    parser -> parser.withDialect(dialect)),
                            dialect.name());
                }
            }
        }
    }

    @Test
    void retainsDefaultInterpretationOfPrimaryAsConstraintName() throws Exception {
        String sql = "ALTER TABLE t ADD CONSTRAINT PRIMARY KEY (id)";
        Alter statement = (Alter) CCJSqlParserUtil.parse(sql);
        NamedConstraint constraint =
                (NamedConstraint) statement.getAlterExpressions().get(0).getConstraint();
        assertEquals("PRIMARY", constraint.getName());
        assertEquals("KEY", constraint.getType());
        assertEquals(ConstraintNamePosition.BEFORE, constraint.getConstraintNamePosition());
        assertEquals(sql, statement.toString());
    }

    @Test
    void exposesForeignKeyAndMutableName() throws Exception {
        Alter statement = (Alter) CCJSqlParserUtil.parse(
                "ALTER TABLE child ADD CONSTRAINT FOREIGN KEY (id) REFERENCES parent(id) CONSTRAINT fk_child",
                parser -> parser.withDialect(Dialect.INFORMIX));
        ForeignKeyConstraint key =
                (ForeignKeyConstraint) statement.getAlterExpressions().get(0).getConstraint();
        assertEquals(List.of("id"), key.getColumnsNames());
        assertEquals("parent", key.getTable().getName());
        assertEquals(List.of("id"), key.getReferencedColumnNames());
        key.setName("renamed_fk");
        assertTrue(statement.toString().endsWith("REFERENCES parent(id) CONSTRAINT renamed_fk"));
        key.setName((String) null);
        assertFalse(statement.toString().contains("fk_child"));
        assertEquals(statement.toString(), CCJSqlParserUtil.parse(statement.toString(),
                parser -> parser.withDialect(Dialect.INFORMIX)).toString());
    }

    @Test
    void keepsFollowingAlterActionsAndStatements() throws Exception {
        String sql =
                "ALTER TABLE t ADD CONSTRAINT PRIMARY KEY (id) CONSTRAINT pk_t, ADD COLUMN note INT; SELECT 1;";
        Statements statements = CCJSqlParserUtil.parseStatements(sql,
                parser -> parser.withDialect(Dialect.INFORMIX));
        assertEquals(2, statements.size());
        assertEquals(2, ((Alter) statements.get(0)).getAlterExpressions().size());
    }

    @Test
    void retainsLeadingNamesAndBuilders() throws Exception {
        NamedConstraint built = new KeyConstraint().withType("PRIMARY KEY").withName("pk_t")
                .withColumnsNames(List.of("id"));
        assertEquals(ConstraintNamePosition.BEFORE, built.getConstraintNamePosition());
        assertEquals("CONSTRAINT pk_t PRIMARY KEY (id)", built.toString());
        for (String definition : List.of("PRIMARY KEY (id)", "UNIQUE (id)",
                "FOREIGN KEY (id) REFERENCES parent(id)", "CHECK (id > 0)")) {
            String sql = "ALTER TABLE t ADD CONSTRAINT c " + definition;
            for (Alter statement : List.of((Alter) CCJSqlParserUtil.parse(sql),
                    (Alter) CCJSqlParserUtil.parse(sql,
                            parser -> parser.withDialect(Dialect.INFORMIX)))) {
                assertEquals(ConstraintNamePosition.BEFORE,
                        ((NamedConstraint) statement.getAlterExpressions().get(0).getConstraint())
                                .getConstraintNamePosition());
                assertTrue(statement.toString().contains("CONSTRAINT c " + definition));
            }
        }
    }

    @Test
    void keepsCheckExpressionVisitorAndForeignTableTraversal() throws Exception {
        Alter statement = (Alter) CCJSqlParserUtil
                .parse("ALTER TABLE child ADD CONSTRAINT CHECK (id > 0) CONSTRAINT positive_id",
                        parser -> parser.withDialect(Dialect.INFORMIX));
        CheckConstraint check =
                (CheckConstraint) statement
                        .getAlterExpressions().get(0).getConstraint();
        StringBuilder builder = new StringBuilder();
        ExpressionDeParser visitor =
                new ExpressionDeParser() {
                    @Override
                    public <S> StringBuilder visit(Column column,
                            S context) {
                        getBuilder().append("renamed_id");
                        return getBuilder();
                    }
                };
        visitor.setBuilder(builder);
        new TableElementDeParser(builder, visitor).deParse(check);
        assertEquals("CONSTRAINT CHECK (renamed_id > 0) CONSTRAINT positive_id",
                builder.toString());
        assertEquals(Set.of("child", "parent"),
                new TablesNamesFinder<>().getTables(CCJSqlParserUtil.parse(
                        "ALTER TABLE child ADD CONSTRAINT FOREIGN KEY (id) REFERENCES parent(id) CONSTRAINT fk",
                        parser -> parser.withDialect(Dialect.INFORMIX))));
    }

    @Test
    void rejectsMissingOrDuplicateNames() {
        for (String sql : List.of(
                "ALTER TABLE t ADD CONSTRAINT PRIMARY KEY (id) CONSTRAINT",
                "ALTER TABLE t ADD CONSTRAINT UNIQUE (id) CONSTRAINT a CONSTRAINT b",
                "ALTER TABLE t ADD CONSTRAINT FOREIGN KEY (id) CONSTRAINT fk",
                "ALTER TABLE t MODIFY CONSTRAINT UNIQUE (id) CONSTRAINT uk")) {
            assertThrows(JSQLParserException.class,
                    () -> CCJSqlParserUtil.parse(sql,
                            parser -> parser.withDialect(Dialect.INFORMIX)));
        }
    }
}
