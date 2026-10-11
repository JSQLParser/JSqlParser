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

import net.sf.jsqlparser.statement.create.table.ConstraintKind;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.create.table.DefaultConstraint;
import net.sf.jsqlparser.test.TestUtils;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.AlterDeParser;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.validation.validator.AlterValidator;
import net.sf.jsqlparser.util.validation.ValidationContext;
import net.sf.jsqlparser.util.validation.metadata.DatabaseMetaDataValidation;
import net.sf.jsqlparser.util.validation.metadata.Named;
import net.sf.jsqlparser.util.validation.metadata.NamedObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SqlServerDefaultConstraintTest {
    @ParameterizedTest
    @ValueSource(strings = {
            "ALTER TABLE dbo.virtual_production_record ADD CONSTRAINT DF_virtual_production_record__d DEFAULT ((0)) FOR _d",
            "ALTER TABLE [dbo].[t] ADD CONSTRAINT [DF_value] DEFAULT ((0)) FOR [value] WITH VALUES",
            "ALTER TABLE t ADD DEFAULT 0 FOR c",
            "ALTER TABLE t ADD DEFAULT NULL FOR c",
            "ALTER TABLE t ADD DEFAULT -1 FOR c WITH VALUES",
            "ALTER TABLE t ADD DEFAULT N'it''s fine' FOR c",
            "ALTER TABLE t ADD CONSTRAINT df DEFAULT (GETDATE()) FOR created_at",
            "ALTER TABLE t ADD DEFAULT (NEXT VALUE FOR dbo.seq) FOR id",
            "ALTER TABLE t ADD DEFAULT (1 + 2) FOR c",
            "ALTER TABLE t ADD CONSTRAINT df DEFAULT CAST(0 AS INT) FOR c",
            "ALTER TABLE t ADD DEFAULT 0 FOR a, ADD DEFAULT 1 FOR b",
            "ALTER TABLE t ADD DEFAULT 0 FOR c, ADD CONSTRAINT ck CHECK (c >= 0)"
    })
    void roundTrip(String sql) throws JSQLParserException {
        Alter alter = (Alter) TestUtils.assertSqlCanBeParsedAndDeparsed(sql, true,
                parser -> parser.withSquareBracketQuotation(true));
        assertEquals(alter.toString(), parse(sql).toString());
        assertInstanceOf(DefaultConstraint.class,
                alter.getAlterExpressions().get(0).getConstraint());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "ALTER TABLE t ADD DEFAULT FOR c",
            "ALTER TABLE t ADD DEFAULT 0",
            "ALTER TABLE t ADD CONSTRAINT df DEFAULT 0 FOR",
            "ALTER TABLE t ADD CONSTRAINT DEFAULT 0 FOR c",
            "ALTER TABLE t ADD DEFAULT 0 FOR c WITH",
            "ALTER TABLE t ADD DEFAULT 0 FOR c WITH VALUES WITH VALUES",
            "ALTER TABLE t ALTER CONSTRAINT df DEFAULT 0 FOR c",
            "ALTER TABLE t MODIFY DEFAULT 0 FOR c"
    })
    void rejectMalformedSyntax(String sql) {
        assertThrows(JSQLParserException.class, () -> parse(sql));
    }

    @Test
    void inspectAndModifyStructuredConstraint() throws JSQLParserException {
        Alter alter = parse("ALTER TABLE t ADD CONSTRAINT df DEFAULT 0 FOR c WITH VALUES");
        AlterExpression action = alter.getAlterExpressions().get(0);
        DefaultConstraint constraint = (DefaultConstraint) action.getConstraint();
        assertEquals(AlterOperation.ADD, action.getOperation());
        assertEquals(ConstraintKind.DEFAULT, constraint.getKind());
        assertEquals("DEFAULT", constraint.getType());
        assertEquals("df", constraint.getName());
        assertEquals("c", constraint.getColumn().getColumnName());
        assertEquals(0, ((LongValue) constraint.getExpression()).getValue());
        assertTrue(constraint.isWithValues());
        constraint.withName((String) null).withExpression(new LongValue(2))
                .withColumn(new Column("other_column")).withWithValues(false);
        assertEquals("ALTER TABLE t ADD DEFAULT 2 FOR other_column", alter.toString());
        TestUtils.assertSqlCanBeParsedAndDeparsed(alter.toString());
    }

    @Test
    void deparserVisitsDefaultAndTargetColumn() throws JSQLParserException {
        Alter alter = parse("ALTER TABLE t ADD CONSTRAINT df DEFAULT ((0)) FOR c WITH VALUES");
        StringBuilder sql = new StringBuilder();
        ExpressionDeParser expressions = new ExpressionDeParser() {
            @Override
            public <S> StringBuilder visit(LongValue value, S context) {
                return getBuilder().append('1');
            }

            @Override
            public <S> StringBuilder visit(Column column, S context) {
                return getBuilder().append("new_").append(column.getColumnName());
            }
        };
        expressions.setBuilder(sql);
        new AlterDeParser(sql, expressions).deParse(alter);
        assertEquals("ALTER TABLE t ADD CONSTRAINT df DEFAULT ((1)) FOR new_c WITH VALUES",
                sql.toString());
    }

    @Test
    void validationTraversesStructuredValueAndColumn() throws JSQLParserException {
        Alter alter = parse("ALTER TABLE dbo.t ADD DEFAULT (GETDATE()) FOR created_at");
        List<String> visited = new ArrayList<>();
        AlterValidator validator = new AlterValidator() {
            @Override
            public void validateOptionalExpression(Expression expression) {
                visited.add(expression.toString());
            }
        };
        validator.setContext(new ValidationContext().setCapabilities(List.of()));
        validator.validate(alter);
        assertEquals(List.of("(GETDATE())", "created_at"), visited);
        assertEquals(Set.of("dbo.t"), new TablesNamesFinder().getTables(alter));
    }

    @Test
    void existingColumnDefaultsAndCreateTableRemainSupported() throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(
                "CREATE TABLE t (c INT DEFAULT ((0)), CONSTRAINT pk PRIMARY KEY CLUSTERED (c))");
        TestUtils.assertSqlCanBeParsedAndDeparsed("ALTER TABLE t ALTER COLUMN c SET DEFAULT 0");
        TestUtils.assertSqlCanBeParsedAndDeparsed("ALTER TABLE t ALTER COLUMN c DROP DEFAULT");
        assertEquals(ConstraintKind.DEFAULT, ConstraintKind.fromType("DEFAULT"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "CONSTRAINT df "})
    void validateOptionalConstraintNameAsNewConstraint(String prefix) throws JSQLParserException {
        List<Named> visited = new ArrayList<>();
        DatabaseMetaDataValidation metadata = named -> {
            visited.add(named);
            return named.getNamedObject() != NamedObject.constraint;
        };
        AlterValidator validator = new AlterValidator();
        validator.setContext(new ValidationContext().setCapabilities(List.of(metadata)));
        validator.validate(parse("ALTER TABLE t ADD " + prefix + "DEFAULT 0 FOR c"));
        assertTrue(validator.getValidationErrors().isEmpty());
        assertTrue(visited.stream().anyMatch(n -> n.getNamedObject() == NamedObject.column
                && n.getFqn().equals("c")));
        assertEquals(prefix.isEmpty() ? 0 : 1,
                visited.stream().filter(n -> n.getNamedObject() == NamedObject.constraint).count());
        assertTrue(visited.stream().noneMatch(n -> n.getNamedObject() == NamedObject.index));
    }

    private Alter parse(String sql) throws JSQLParserException {
        return (Alter) CCJSqlParserUtil.parse(sql,
                parser -> parser.withDialect(Dialect.SQLSERVER).withSquareBracketQuotation(true)
                        .withUnsupportedStatements(false));
    }
}
