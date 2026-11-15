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

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.alter.AlterExpression.ColumnDataType;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.StmtFeature;
import net.sf.jsqlparser.statement.create.table.CheckConstraint;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.create.table.ForeignKeyConstraint;
import net.sf.jsqlparser.statement.create.table.Index;
import net.sf.jsqlparser.statement.create.table.KeyConstraint;
import net.sf.jsqlparser.statement.create.table.TableElement;
import net.sf.jsqlparser.statement.select.SelectVisitorAdapter;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import net.sf.jsqlparser.util.validation.Validation;
import net.sf.jsqlparser.util.validation.feature.MySqlVersion;
import net.sf.jsqlparser.util.validation.metadata.DatabaseMetaDataValidation;
import net.sf.jsqlparser.util.validation.metadata.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * MySQL's ADD (table_element_list), see https://dev.mysql.com/doc/refman/8.4/en/alter-table.html.
 */
class MySqlAlterAddGroupAstTest {
    @ParameterizedTest
    @ValueSource(strings = {
            "a INT, CONSTRAINT ck1 CHECK (a > 0), b INT, CONSTRAINT ck2 CHECK (b > 0) NOT ENFORCED",
            "a INT, KEY ix (a), b INT DEFAULT 7, CONSTRAINT uq UNIQUE (b)",
            "CONSTRAINT ck CHECK (a > 0), a INT",
            "a INT, CONSTRAINT fk FOREIGN KEY (a) REFERENCES parent(id), b INT",
            "CONSTRAINT ck CHECK (id > 0), KEY ix (id)"})
    void retainsInterleavedDeclarationsAndFollowingActions(String elements) throws Exception {
        for (boolean mysql : new boolean[] {false, true}) {
            for (String keyword : new String[] {"ADD", "ADD COLUMN"}) {
                String sql =
                        "ALTER TABLE t " + keyword + " (" + elements + "), ADD COLUMN extra INT";
                Alter alter = (Alter) assertSqlCanBeParsedAndDeparsed(sql, true, parser -> {
                    if (mysql) {
                        parser.withDialect(Dialect.MYSQL);
                    }
                });
                assertEquals(2, alter.getAlterExpressions().size());
                assertTrue(alter.getAlterExpressions().get(0).hasAddGroupElements());
                assertFalse(alter.getAlterExpressions().get(1).hasAddGroupElements());
                assertEquals(sql, alter.toString());
                assertThat(new Validation(List.of(MySqlVersion.V8_0), sql).validate()).isEmpty();
            }
        }
    }

    @Test
    void exposesOrderedNodesAndMutableLegacyViews() throws Exception {
        Alter alter = parse("ALTER TABLE t ADD COLUMN (a INT, CONSTRAINT ck CHECK (a > 0) "
                + "NOT ENFORCED, b INT, KEY ix (a), CONSTRAINT uq UNIQUE (b))");
        AlterExpression group = alter.getAlterExpressions().get(0);
        List<TableElement> elements = group.getAddGroupElements();
        assertThat(elements).extracting(Object::getClass).containsExactly(ColumnDataType.class,
                CheckConstraint.class, ColumnDataType.class, Index.class, KeyConstraint.class);
        assertSame(elements.get(0), group.getColDataTypeList().get(0));
        assertSame(elements.get(2), group.getColDataTypeList().get(1));
        assertSame(elements.get(1), group.getConstraint());
        assertEquals(Boolean.FALSE, ((CheckConstraint) group.getConstraint()).getEnforced());
        assertSame(elements.get(3), group.getIndex());

        group.getColDataTypeList().set(1, column("c"));
        group.getColDataTypeList().add(1, column("d"));
        group.getColDataTypeList().remove(0);
        group.addColDataType(column("e"));
        group.getConstraint().setName("edited_ck");
        group.getIndex().setName("edited_ix");
        assertEquals("ALTER TABLE t ADD COLUMN (CONSTRAINT edited_ck CHECK (a > 0) NOT ENFORCED, "
                + "d INT, c INT, KEY edited_ix (a), CONSTRAINT uq UNIQUE (b), e INT)",
                alter.toString());

        CheckConstraint replacement = (CheckConstraint) parse("ALTER TABLE t ADD CHECK (id > 1)")
                .getAlterExpressions().get(0).getConstraint();
        group.setConstraint(replacement);
        group.setIndex(null);
        assertSame(replacement, elements.get(0));
        assertNull(group.getIndex());
        assertThat(elements).filteredOn(KeyConstraint.class::isInstance).hasSize(1);
    }

    @Test
    void copiesMembershipButKeepsEditableNodes() throws Exception {
        AlterExpression parsed = parse("ALTER TABLE t ADD (a INT, CHECK (a > 0))")
                .getAlterExpressions().get(0);
        List<TableElement> input = new ArrayList<>(parsed.getAddGroupElements());
        AlterExpression group = new AlterExpression();
        group.setOperation(AlterOperation.ADD);
        group.setAddGroupElements(input);
        input.clear();
        assertEquals(2, group.getAddGroupElements().size());
        assertSame(parsed.getColDataTypeList().get(0), group.getColDataTypeList().get(0));
        group.getColDataTypeList().get(0).setColumnName("edited");
        assertEquals("edited", parsed.getColDataTypeList().get(0).getColumnName());
        group.setAddGroupElements(null);
        assertFalse(group.hasAddGroupElements());
    }

    @Test
    void deparsesEveryStructuredExpressionInOrder() throws Exception {
        Alter alter = parse("ALTER TABLE t ADD (a INT DEFAULT 7, "
                + "CONSTRAINT ck CHECK (a > 0), b INT DEFAULT 9)");
        List<Long> visited = new ArrayList<>();
        ExpressionDeParser expressions = new ExpressionDeParser() {
            @Override
            public <S> StringBuilder visit(LongValue value, S context) {
                visited.add(value.getValue());
                return getBuilder().append(value.getValue() + 100);
            }
        };
        StringBuilder output = new StringBuilder();
        alter.accept(new StatementDeParser(expressions, new SelectDeParser(), output), null);
        assertEquals(List.of(7L, 0L, 9L), visited);
        String expected = "ALTER TABLE t ADD (a INT DEFAULT 107, "
                + "CONSTRAINT ck CHECK (a > 100), b INT DEFAULT 109)";
        assertEquals(expected, output.toString());
        assertEquals(expected, parse(output.toString()).toString());
    }

    @Test
    void statementVisitorSeesEveryDeclarationWithItsContext() throws Exception {
        Alter alter = parse("ALTER TABLE t ADD (a INT DEFAULT 7, "
                + "CONSTRAINT ck CHECK (a > 0), b INT DEFAULT 9)");
        List<Long> visited = new ArrayList<>();
        ExpressionVisitorAdapter<Void> expressions = new ExpressionVisitorAdapter<Void>() {
            @Override
            public <S> Void visit(LongValue value, S context) {
                assertEquals("group", context);
                visited.add(value.getValue());
                return null;
            }
        };
        alter.accept(new StatementVisitorAdapter<>(new SelectVisitorAdapter<>(expressions)),
                "group");
        assertEquals(List.of(7L, 0L, 9L), visited);
        assertThat(alter.getFeatures().getCertain()).contains(StmtFeature.MODIFIES_SCHEMA);
    }

    @Test
    void visitsForeignKeysAndAllConstraintAndIndexNames() throws Exception {
        Alter alter = parse("ALTER TABLE child ADD (parent_id INT, CONSTRAINT fk "
                + "FOREIGN KEY (parent_id) REFERENCES parent (id), other_id INT)");
        assertInstanceOf(ForeignKeyConstraint.class,
                alter.getAlterExpressions().get(0).getConstraint());
        assertThat(new TablesNamesFinder<>().getTables(alter))
                .containsExactlyInAnyOrder("child", "parent");

        List<Named> names = new ArrayList<>();
        Set<String> introduced = Set.of("ck1", "ck2", "ix1", "ix2");
        DatabaseMetaDataValidation metadata = name -> {
            names.add(name);
            return !introduced.contains(name.getFqn());
        };
        String sql = "ALTER TABLE t ADD (CONSTRAINT ck1 CHECK (id > 0), KEY ix1 (id), "
                + "CONSTRAINT ck2 CHECK (id < 10), KEY ix2 (id))";
        assertThat(new Validation(List.of(metadata), sql).validate()).isEmpty();
        assertThat(names).extracting(Named::getFqn).containsAll(introduced);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADD (a INT, b INT)", "ADD COLUMN (a INT)",
            "MODIFY (a INT, b INT)"})
    void keepsColumnOnlyAndModifyRepresentations(String action) throws Exception {
        AlterExpression expression = parse("ALTER TABLE t " + action).getAlterExpressions().get(0);
        assertNull(expression.getAddGroupElements());
        assertFalse(expression.getColDataTypeList().isEmpty());
        expression.getColDataTypeList().get(0).setColumnName("edited");
        assertTrue(expression.toString().contains("edited"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADD ()", "ADD (a INT,)", "ADD (LIKE parent)",
            "ADD (a INT, CONSTRAINT ck CHECK ())"})
    void rejectsMalformedGroups(String action) {
        for (boolean mysql : new boolean[] {false, true}) {
            assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil
                    .parse("ALTER TABLE t " + action, parser -> {
                        if (mysql) {
                            parser.withDialect(Dialect.MYSQL);
                        }
                    }));
        }
    }

    private static ColumnDataType column(String name) {
        return new ColumnDataType(name, false, new ColDataType("INT"), null);
    }

    private static Alter parse(String sql) throws JSQLParserException {
        return (Alter) CCJSqlParserUtil.parse(sql, parser -> parser.withDialect(Dialect.MYSQL));
    }
}
