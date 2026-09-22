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
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.create.table.ConstraintUsingIndex;
import net.sf.jsqlparser.statement.create.table.Index;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import net.sf.jsqlparser.util.validation.ValidationContext;
import net.sf.jsqlparser.util.validation.metadata.Named;
import net.sf.jsqlparser.util.validation.metadata.NamedObject;
import net.sf.jsqlparser.util.validation.metadata.DatabaseMetaDataValidation;
import net.sf.jsqlparser.util.validation.validator.AlterValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PostgreSqlConstraintUsingIndexTest {
    @ParameterizedTest
    @ValueSource(strings = {"UNIQUE USING INDEX i", "PRIMARY KEY USING INDEX i",
            "CONSTRAINT uq UNIQUE USING INDEX i DEFERRABLE INITIALLY DEFERRED",
            "CONSTRAINT pk PRIMARY KEY USING INDEX i NOT DEFERRABLE INITIALLY IMMEDIATE",
            "CONSTRAINT \"Unique Name\" UNIQUE USING INDEX \"Index Name\""})
    void roundTripsExistingIndexConstraints(String body) throws JSQLParserException {
        String sql = "ALTER TABLE t ADD " + body;
        Alter alter = parse(sql);
        ConstraintUsingIndex constraint = assertInstanceOf(ConstraintUsingIndex.class,
                alter.getAlterExpressions().get(0).getIndex());
        assertNull(constraint.getColumns());
        assertNull(constraint.getIndexName());
        assertNull(constraint.getUsing());
        assertEquals(sql, alter.toString());
        roundTrip(alter);
    }

    @Test
    void supportsMutationAndConstruction() throws JSQLParserException {
        Alter alter = parse("ALTER TABLE t ADD CONSTRAINT uq UNIQUE USING INDEX i DEFERRABLE");
        ConstraintUsingIndex constraint =
                (ConstraintUsingIndex) alter.getAlterExpressions().get(0).getIndex();
        assertEquals(Boolean.TRUE, constraint.getConstraintAttributes().getDeferrable());
        constraint.setName("pk");
        constraint.setType("PRIMARY KEY");
        constraint.setExistingIndexName("other_index");
        assertEquals(Index.Kind.PRIMARY_KEY, constraint.getKind());
        assertEquals(
                "ALTER TABLE t ADD CONSTRAINT pk PRIMARY KEY USING INDEX other_index DEFERRABLE",
                alter.toString());
        roundTrip(alter);
        Alter created = new Alter().withTable(new Table("t"));
        created.addAlterExpressions(new AlterExpression().withOperation(AlterOperation.ADD)
                .withIndex(new ConstraintUsingIndex().withName("uq").withExistingIndexName("i")));
        assertEquals("ALTER TABLE t ADD CONSTRAINT uq UNIQUE USING INDEX i", created.toString());
        roundTrip(created);
    }

    @Test
    void distinguishesTableAndIndexAndNewConstraint() throws JSQLParserException {
        Alter alter = parse("ALTER TABLE t ADD CONSTRAINT uq UNIQUE USING INDEX i");
        assertEquals(Set.of("t"), new TablesNamesFinder().getTables(alter));
        List<Named> visited = new ArrayList<>();
        DatabaseMetaDataValidation metadata = named -> {
            visited.add(named);
            return named.getNamedObject() != NamedObject.constraint;
        };
        AlterValidator validator = new AlterValidator();
        validator.setContext(new ValidationContext().setCapabilities(List.of(metadata)));
        validator.validate(alter);
        assertTrue(validator.getValidationErrors().isEmpty());
        assertTrue(visited.stream()
                .anyMatch(n -> n.getNamedObject() == NamedObject.index && "i".equals(n.getFqn())));
        assertTrue(visited.stream().anyMatch(
                n -> n.getNamedObject() == NamedObject.constraint && "uq".equals(n.getFqn())));
    }

    @Test
    void preservesOrdinaryConstraintsAndActionBoundaries() throws JSQLParserException {
        roundTrip(parse("ALTER TABLE t ADD CONSTRAINT uq UNIQUE (id)"));
        Alter alter = parse("ALTER TABLE t ADD UNIQUE USING INDEX i, ADD COLUMN extra INT");
        assertEquals(2, alter.getAlterExpressions().size());
        roundTrip(alter);
        String oracle = "ALTER TABLE t ADD CONSTRAINT pk PRIMARY KEY (id) USING INDEX i";
        assertEquals(oracle, CCJSqlParserUtil.parse(oracle).toString());
    }

    private static Alter parse(String sql) throws JSQLParserException {
        return (Alter) CCJSqlParserUtil.parse(sql, p -> p.withDialect(Dialect.POSTGRESQL));
    }

    private static void roundTrip(Alter alter) throws JSQLParserException {
        StringBuilder sql = new StringBuilder();
        alter.accept(new StatementDeParser(sql), null);
        assertEquals(alter.toString(), sql.toString());
        assertEquals(sql.toString(), parse(sql.toString()).toString());
    }
}
