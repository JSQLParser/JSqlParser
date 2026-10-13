/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.validation.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.feature.FeatureConfiguration;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.alter.AlterExpression;
import net.sf.jsqlparser.statement.alter.AlterOperation;
import net.sf.jsqlparser.statement.create.table.Index;
import net.sf.jsqlparser.util.validation.Validation;
import net.sf.jsqlparser.util.validation.ValidationCapability;
import net.sf.jsqlparser.util.validation.ValidationException;
import net.sf.jsqlparser.util.validation.metadata.DatabaseMetaDataValidation;
import net.sf.jsqlparser.util.validation.metadata.Named;
import net.sf.jsqlparser.util.validation.metadata.NamedObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class AlterIndexValidationTest {
    private static final Named CHILD = new Named(NamedObject.table, "child");
    private static final Named ID = new Named(NamedObject.column, "id");
    private static final Named INDEX = new Named(NamedObject.index, "ix");

    @ParameterizedTest
    @ValueSource(strings = {"INDEX", "KEY", "FULLTEXT INDEX", "SPATIAL INDEX"})
    void addIndexRequiresANewNameAndExistingColumns(String type) throws JSQLParserException {
        Alter statement = parse("ALTER TABLE child ADD " + type + " ix (id)", Dialect.MYSQL);
        List<Named> visited = new ArrayList<>();
        Map<ValidationCapability, Set<ValidationException>> errors =
                validate(statement, Set.of(CHILD, ID), visited);
        assertTrue(errors.isEmpty(), errors.toString());
        assertEquals(List.of(CHILD, INDEX, ID), visited);

        errors = validate(statement, Set.of(CHILD, ID, INDEX), new ArrayList<>());
        assertEquals(1, errors.size());
        assertTrue(errors.toString().contains("ix does exist"), errors.toString());

        errors = validate(statement, Set.of(CHILD), new ArrayList<>());
        assertEquals(1, errors.size());
        assertTrue(errors.toString().contains("id does not exist"), errors.toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"INDEX (id)", "KEY (id)", "INDEX ((id + 1))"})
    void unnamedIndexesSkipNameLookupButKeepColumnValidation(String definition)
            throws JSQLParserException {
        Alter statement = parse("ALTER TABLE child ADD " + definition, Dialect.MYSQL);
        assertNull(statement.getAlterExpressions().get(0).getIndex().getName());
        List<Named> visited = new ArrayList<>();
        Map<ValidationCapability, Set<ValidationException>> errors =
                validate(statement, Set.of(CHILD, ID), visited);
        assertTrue(errors.isEmpty(), errors.toString());
        assertEquals(List.of(CHILD, ID), visited);
        errors = validate(statement, Set.of(CHILD), new ArrayList<>());
        assertTrue(errors.toString().contains("id does not exist"), errors.toString());
    }

    @Test
    void namedExpressionIndexesValidateTheirExpressionColumnsOnce() throws JSQLParserException {
        Alter statement = parse("ALTER TABLE child ADD INDEX ix ((id + 1))", Dialect.MYSQL);
        List<Named> visited = new ArrayList<>();
        Map<ValidationCapability, Set<ValidationException>> errors =
                validate(statement, Set.of(CHILD, ID), visited);
        assertTrue(errors.isEmpty(), errors.toString());
        assertEquals(List.of(CHILD, ID, INDEX), visited);
        errors = validate(statement, Set.of(CHILD), new ArrayList<>());
        assertTrue(errors.toString().contains("id does not exist"), errors.toString());
    }

    @Test
    void quotedNamesReachTheMetadataContractUnchanged() throws JSQLParserException {
        Alter statement = parse("ALTER TABLE `child table` ADD INDEX `new index` (`id.column`)",
                Dialect.MYSQL);
        Named table = new Named(NamedObject.table, "`child table`");
        Named column = new Named(NamedObject.column, "`id.column`");
        Named index = new Named(NamedObject.index, "`new index`");
        List<Named> visited = new ArrayList<>();
        Map<ValidationCapability, Set<ValidationException>> errors =
                validate(statement, Set.of(table, column), visited);
        assertTrue(errors.isEmpty(), errors.toString());
        assertEquals(List.of(table, index, column), visited);
        errors = validate(statement, Set.of(table, column, index), new ArrayList<>());
        assertTrue(errors.toString().contains("`new index` does exist"), errors.toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"INDEX", "KEY"})
    void dropIndexStillRequiresAnExistingName(String keyword) throws JSQLParserException {
        Alter statement = parse("ALTER TABLE child DROP " + keyword + " ix", Dialect.MYSQL);
        List<Named> visited = new ArrayList<>();
        Map<ValidationCapability, Set<ValidationException>> errors =
                validate(statement, Set.of(CHILD, INDEX), visited);
        assertTrue(errors.isEmpty(), errors.toString());
        assertEquals(List.of(CHILD, INDEX), visited);
        errors = validate(statement, Set.of(CHILD), new ArrayList<>());
        assertTrue(errors.toString().contains("ix does not exist"), errors.toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"INDEX", "KEY"})
    void renameRequiresTheOldNameAndIntroducesTheNewName(String keyword)
            throws JSQLParserException {
        Alter statement = parse("ALTER TABLE child RENAME " + keyword
                + " `old index` TO `new index`", Dialect.MYSQL);
        Named oldIndex = new Named(NamedObject.index, "`old index`");
        Named newIndex = new Named(NamedObject.index, "`new index`");
        List<Named> visited = new ArrayList<>();
        Map<ValidationCapability, Set<ValidationException>> errors =
                validate(statement, Set.of(CHILD, oldIndex), visited);
        assertTrue(errors.isEmpty(), errors.toString());
        assertEquals(List.of(CHILD, oldIndex, newIndex), visited);

        errors = validate(statement, Set.of(CHILD), new ArrayList<>());
        assertTrue(errors.toString().contains("`old index` does not exist"), errors.toString());
        errors = validate(statement, Set.of(CHILD, oldIndex, newIndex), new ArrayList<>());
        assertTrue(errors.toString().contains("`new index` does exist"), errors.toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"PRIMARY KEY", "UNIQUE"})
    void constraintUsingIndexStillReferencesAnExistingIndex(String type)
            throws JSQLParserException {
        Alter statement = parse("ALTER TABLE child ADD CONSTRAINT c " + type + " USING INDEX ix",
                Dialect.POSTGRESQL);
        assertNull(statement.getAlterExpressions().get(0).getIndex());
        List<Named> visited = new ArrayList<>();
        Map<ValidationCapability, Set<ValidationException>> errors =
                validate(statement, Set.of(CHILD, INDEX), visited);
        assertTrue(errors.isEmpty(), errors.toString());
        assertEquals(List.of(CHILD, new Named(NamedObject.constraint, "c"), INDEX), visited);
        errors = validate(statement, Set.of(CHILD), new ArrayList<>());
        assertTrue(errors.toString().contains("ix does not exist"), errors.toString());
    }

    @Test
    void buildersUseTheSameDefinitionRole() {
        Alter statement = new Alter();
        statement.setTable(new Table("child"));
        statement.addAlterExpression(new AlterExpression().withOperation(AlterOperation.ADD)
                .withIndex(new Index().withType("INDEX").withName("ix")
                        .withColumnsNames(List.of("id"))));
        List<Named> visited = new ArrayList<>();
        Map<ValidationCapability, Set<ValidationException>> errors =
                validate(statement, Set.of(CHILD, ID), visited);
        assertTrue(errors.isEmpty(), errors.toString());
        assertEquals(List.of(CHILD, INDEX, ID), visited);
    }

    private static Alter parse(String sql, Dialect dialect) throws JSQLParserException {
        return (Alter) CCJSqlParserUtil.parse(sql, parser -> parser.withDialect(dialect));
    }

    private static Map<ValidationCapability, Set<ValidationException>> validate(Alter statement,
            Set<Named> existing, List<Named> visited) {
        DatabaseMetaDataValidation metadata = name -> {
            visited.add(name);
            return existing.contains(name);
        };
        return Validation.validate(statement, Validation.createValidationContext(
                new FeatureConfiguration(), List.of(metadata)));
    }
}
