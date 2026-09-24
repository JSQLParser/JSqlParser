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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.table.ConstraintAttributes;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.Index;
import net.sf.jsqlparser.statement.create.table.NotNullConstraint;
import net.sf.jsqlparser.util.TableDefinitionTraversal;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.AlterDeParser;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import net.sf.jsqlparser.util.validation.ValidationContext;
import net.sf.jsqlparser.util.validation.metadata.DatabaseMetaDataValidation;
import net.sf.jsqlparser.util.validation.metadata.Named;
import net.sf.jsqlparser.util.validation.metadata.NamedObject;
import net.sf.jsqlparser.util.validation.validator.AlterValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PostgreSqlNotNullConstraintTest {
    @ParameterizedTest
    @ValueSource(strings = {"NOT NULL id", "CONSTRAINT nn NOT NULL id",
            "CONSTRAINT nn NOT NULL id NO INHERIT", "CONSTRAINT \"NN Name\" NOT NULL \"ID\""})
    void roundTripsCreateAndAlter(String body) throws JSQLParserException {
        for (String sql : new String[] {"CREATE TABLE t (id INT, " + body + ")",
                "ALTER TABLE t ADD " + body + " NOT VALID"}) {
            Statement statement = parse(sql);
            assertEquals(sql, statement.toString());
            NotNullConstraint constraint = constraint(statement);
            assertEquals(Index.Kind.NOT_NULL, constraint.getKind());
            assertNotNull(constraint.getColumn());
            roundTrip(statement);
        }
    }

    @Test
    void supportsEditingAndConstruction() throws JSQLParserException {
        Alter alter =
                (Alter) parse("ALTER TABLE t ADD CONSTRAINT nn NOT NULL id NO INHERIT NOT VALID");
        NotNullConstraint constraint = constraint(alter);
        assertTrue(constraint.isNoInherit());
        assertTrue(constraint.getConstraintAttributes().isNotValid());
        constraint.setName("new_nn");
        constraint.getColumn().setColumnName("other_id");
        constraint.setNoInherit(false);
        assertEquals("ALTER TABLE t ADD CONSTRAINT new_nn NOT NULL other_id NOT VALID",
                alter.toString());
        roundTrip(alter);
        ConstraintAttributes attributes = new ConstraintAttributes();
        attributes.setNotValid(true);
        alter.getAlterExpressions().get(0).setIndex(new NotNullConstraint().withName("nn")
                .withColumn(new Column("id")).withNoInherit(true)
                .withConstraintAttributes(attributes));
        assertEquals("ALTER TABLE t ADD CONSTRAINT nn NOT NULL id NO INHERIT NOT VALID",
                alter.toString());
        roundTrip(alter);
        assertEquals(Index.Kind.NOT_NULL, new Index().withType("NOT NULL").getKind());
    }

    @Test
    void visitsTargetColumnAndUsesCustomExpressionDeparser() throws JSQLParserException {
        Alter alter = (Alter) parse("ALTER TABLE t ADD CONSTRAINT nn NOT NULL id");
        List<Expression> visited = new ArrayList<>();
        TableDefinitionTraversal.visit(alter.getAlterExpressions().get(0), visited::add, table -> {
        });
        assertEquals(List.of(constraint(alter).getColumn()), visited);
        CreateTable table = (CreateTable) parse("CREATE TABLE t (id INT, NOT NULL id)");
        visited.clear();
        TableDefinitionTraversal.visit(table, visited::add, source -> {
        });
        assertEquals(List.of(constraint(table).getColumn()), visited);
        assertEquals(Set.of("t"), new TablesNamesFinder().getTables(alter));
        StringBuilder sql = new StringBuilder();
        ExpressionDeParser expressions = new ExpressionDeParser() {
            @Override
            public <S> StringBuilder visit(Column column, S context) {
                return getBuilder().append("replacement");
            }
        };
        expressions.setBuilder(sql);
        new AlterDeParser(sql, expressions).deParse(alter);
        assertEquals("ALTER TABLE t ADD CONSTRAINT nn NOT NULL replacement", sql.toString());
    }

    @Test
    void validatesNewConstraintAndExistingColumn() throws JSQLParserException {
        List<Named> visited = new ArrayList<>();
        DatabaseMetaDataValidation metadata = named -> {
            visited.add(named);
            return named.getNamedObject() != NamedObject.constraint;
        };
        AlterValidator validator = new AlterValidator();
        validator.setContext(new ValidationContext().setCapabilities(List.of(metadata)));
        validator.validate((Alter) parse("ALTER TABLE t ADD CONSTRAINT nn NOT NULL id NOT VALID"));
        assertTrue(validator.getValidationErrors().isEmpty());
        assertTrue(visited.stream().anyMatch(
                n -> n.getNamedObject() == NamedObject.column && "id".equals(n.getFqn())));
        assertTrue(visited.stream().anyMatch(
                n -> n.getNamedObject() == NamedObject.constraint && "nn".equals(n.getFqn())));
        assertTrue(visited.stream().noneMatch(n -> n.getNamedObject() == NamedObject.index));
    }

    @Test
    void preservesColumnNullabilityAndActionBoundaries() throws JSQLParserException {
        roundTrip(parse("ALTER TABLE t ADD NOT NULL id NOT VALID, ADD COLUMN extra INT"));
        for (String sql : new String[] {"CREATE TABLE t (id INT NOT NULL)",
                "ALTER TABLE t ALTER COLUMN id SET NOT NULL",
                "ALTER TABLE t ALTER COLUMN id DROP NOT NULL"}) {
            assertEquals(sql, CCJSqlParserUtil.parse(sql).toString());
            roundTrip(parse(sql));
        }
    }

    private static NotNullConstraint constraint(Statement statement) {
        return (NotNullConstraint) (statement instanceof Alter
                ? ((Alter) statement).getAlterExpressions().get(0).getIndex()
                : ((CreateTable) statement).getIndexes().get(0));
    }

    private static Statement parse(String sql) throws JSQLParserException {
        return CCJSqlParserUtil.parse(sql, p -> p.withDialect(Dialect.POSTGRESQL));
    }

    private static void roundTrip(Statement statement) throws JSQLParserException {
        StringBuilder sql = new StringBuilder();
        statement.accept(new StatementDeParser(sql), null);
        assertEquals(statement.toString(), sql.toString());
        assertEquals(sql.toString(), parse(sql.toString()).toString());
    }
}
