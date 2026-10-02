/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.deparser;

import net.sf.jsqlparser.statement.create.table.IndexOption;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.alter.AlterExpression;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.create.table.ColumnOption;
import net.sf.jsqlparser.statement.create.table.ConstraintAttributes;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.CheckConstraint;
import net.sf.jsqlparser.statement.create.table.Index;
import net.sf.jsqlparser.statement.create.table.KeyConstraint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TableConstraintExpressionDeParserTest {
    @ParameterizedTest
    @ValueSource(strings = {"", " NOT ENFORCED"})
    void alterCheckUsesExpressionVisitorAndPreservesAttributes(String attributes)
            throws JSQLParserException {
        assertRewrite("ALTER TABLE t ADD CONSTRAINT c CHECK (id > 0)" + attributes,
                "ALTER TABLE t ADD CONSTRAINT c CHECK (mapped_id > 100)" + attributes);
    }

    @Test
    void createAndAlterFunctionalIndexesUseExpressionVisitor() throws JSQLParserException {
        assertRewrite("CREATE TABLE t (id INT, INDEX idx ((id + 1)))",
                "CREATE TABLE t (id INT, INDEX idx ((mapped_id + 101)))");
        assertRewrite("ALTER TABLE t ADD INDEX idx ((id + 1))",
                "ALTER TABLE t ADD  INDEX idx ((mapped_id + 101))");
    }

    @ParameterizedTest
    @ValueSource(strings = {"ALTER TABLE t ADD CHECK (id > 0)",
            "ALTER TABLE t ADD INDEX ix ((id + 1))"})
    void inactiveTableDefinitionsDoNotOverrideAddedColumns(String sql) throws JSQLParserException {
        Alter alter =
                (Alter) CCJSqlParserUtil.parse(sql, parser -> parser.withDialect(Dialect.MYSQL));
        AlterExpression action = alter.getAlterExpressions().get(0);
        action.addColDataType("new_col", new ColDataType("INT"));
        action.getColDataTypeList().get(0)
                .addColumnOptions(ColumnOption.defaultValue(new LongValue(7)));
        assertEquals("ALTER TABLE t ADD new_col INT DEFAULT 7", alter.toString());
        StringBuilder buffer = new StringBuilder();
        alter.accept(new StatementDeParser(buffer), null);
        assertEquals(alter.toString(), buffer.toString());
        assertEquals(alter.toString(), CCJSqlParserUtil.parse(buffer.toString(),
                parser -> parser.withDialect(Dialect.MYSQL)).toString());
        assertEquals("ALTER TABLE t ADD new_col INT DEFAULT 107", rewrite(alter));
        action.setCommentText("'description'");
        buffer.setLength(0);
        alter.accept(new StatementDeParser(buffer), null);
        assertEquals(alter.toString(), buffer.toString());
    }

    @Test
    void alterExcludeUsesVisitorForKeysAndPredicate() throws JSQLParserException {
        CreateTable table = (CreateTable) CCJSqlParserUtil.parse(
                "CREATE TABLE t (id INT, CONSTRAINT c EXCLUDE USING gist "
                        + "((id + 1) WITH =) WHERE (id > 0) DEFERRABLE)");
        Alter alter = (Alter) CCJSqlParserUtil.parse(
                "ALTER TABLE t ADD CONSTRAINT placeholder CHECK (id > 0)");
        alter.getAlterExpressions().get(0).setConstraint(table.getTableConstraints().get(0));
        assertEquals("ALTER TABLE t ADD CONSTRAINT c EXCLUDE USING gist "
                + "((mapped_id + 101) WITH =) WHERE (mapped_id > 100) DEFERRABLE", rewrite(alter));
    }

    @Test
    void checkAttributesRemainAfterExpressionRewrite() throws JSQLParserException {
        Alter alter =
                (Alter) CCJSqlParserUtil.parse("ALTER TABLE t ADD CONSTRAINT c CHECK (id > 0)");
        ConstraintAttributes attributes = new ConstraintAttributes();
        attributes.setNotValid(true);
        alter.getAlterExpressions().get(0).getConstraint().setConstraintAttributes(attributes);
        assertEquals("ALTER TABLE t ADD CONSTRAINT c CHECK (mapped_id > 100) NOT VALID",
                rewrite(alter));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void builderDefinitionsRenderWithoutAColumnList(boolean emptyColumnList)
            throws JSQLParserException {
        CheckConstraint check = new CheckConstraint();
        check.setExpression(CCJSqlParserUtil.parseCondExpression("1 > 0"));
        CreateTable table = new CreateTable().withTable(new Table("t"))
                .withTableConstraints(List.of(check));
        if (emptyColumnList) {
            table.setColumnDefinitions(List.of());
        }
        assertEquals("CREATE TABLE t (CHECK (1 > 0))", table.toString());
        StringBuilder buffer = new StringBuilder();
        table.accept(new StatementDeParser(buffer), null);
        assertEquals(table.toString(), buffer.toString());
        assertEquals("CREATE TABLE t (CHECK (101 > 100))", rewrite(table));
        table.setTableConstraints(null);
        table.setIndexes(List.of(new Index().withType("INDEX").withName("ix")
                .withColumnsNames(List.of("id"))));
        assertEquals("CREATE TABLE t (INDEX ix (id))", table.toString());
        buffer.setLength(0);
        table.accept(new StatementDeParser(buffer), null);
        assertEquals(table.toString(), buffer.toString());
    }

    @Test
    void constraintStorageOptionsUseExpressionVisitor() throws JSQLParserException {
        CreateTable table = (CreateTable) CCJSqlParserUtil.parse(
                "CREATE TABLE t (id INT, CONSTRAINT c UNIQUE (id))");
        ((KeyConstraint) table.getTableConstraints().get(0)).setStorageParameters(
                Arrays.asList(new IndexOption("fillfactor", new LongValue(70), true)));
        assertEquals("CREATE TABLE t (id INT, CONSTRAINT c UNIQUE (id) WITH (fillfactor = 170))",
                rewrite(table));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "ALTER TABLE t ADD CONSTRAINT c FOREIGN KEY (id) REFERENCES p(id) ON DELETE CASCADE",
            "ALTER TABLE t ADD CONSTRAINT c UNIQUE (id) DEFERRABLE",
            "CREATE TABLE t (id INT, CONSTRAINT c PRIMARY KEY pk (id), FOREIGN KEY (id) REFERENCES p(id))",
            "ALTER TABLE t ADD INDEX idx (id) COMMENT 'lookup'",
            "ALTER TABLE t ADD CONSTRAINT c DEFAULT 1 FOR id",
            "ALTER TABLE t ALTER INDEX idx INVISIBLE"
    })
    void defaultDeparserPreservesCompleteConstraint(String sql) throws JSQLParserException {
        Statement statement = CCJSqlParserUtil.parse(sql);
        StringBuilder buffer = new StringBuilder();
        statement.accept(new StatementDeParser(buffer), null);
        assertEquals(statement.toString(), buffer.toString());
        assertEquals(statement.toString(), CCJSqlParserUtil.parse(buffer.toString()).toString());
    }

    private static void assertRewrite(String sql, String expected) throws JSQLParserException {
        assertEquals(expected, rewrite(CCJSqlParserUtil.parse(sql)));
    }

    private static String rewrite(Statement statement) {
        StringBuilder buffer = new StringBuilder();
        ExpressionDeParser expressions = new ExpressionDeParser() {
            @Override
            public <S> StringBuilder visit(Column column, S context) {
                return getBuilder().append("mapped_").append(column.getColumnName());
            }

            @Override
            public <S> StringBuilder visit(LongValue value, S context) {
                return getBuilder().append(value.getValue() + 100);
            }
        };
        statement.accept(new StatementDeParser(expressions, new SelectDeParser(), buffer), null);
        return buffer.toString();
    }
}
