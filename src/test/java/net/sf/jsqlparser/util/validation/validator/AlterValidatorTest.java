/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2020 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.validation.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import net.sf.jsqlparser.util.validation.Validation;
import net.sf.jsqlparser.util.validation.ValidationError;
import net.sf.jsqlparser.util.validation.metadata.DatabaseMetaDataValidation;
import net.sf.jsqlparser.util.validation.metadata.Named;
import net.sf.jsqlparser.util.validation.metadata.NamedObject;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.feature.FeatureConfiguration;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.create.table.ForeignKeyConstraint;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.util.validation.ValidationContext;
import net.sf.jsqlparser.util.validation.ValidationTestAsserts;
import net.sf.jsqlparser.util.validation.feature.DatabaseType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class AlterValidatorTest extends ValidationTestAsserts {

    @ParameterizedTest
    @ValueSource(strings = {"ADD PRIMARY KEY (id)", "DROP FOREIGN KEY fk_name", "DROP COLUMN id"})
    void ignoresStoredDropColumnsWhenTheRenderedActionUsesAnotherTarget(String action)
            throws JSQLParserException {
        Alter alter = (Alter) CCJSqlParserUtil.parse("ALTER TABLE t " + action,
                parser -> parser.withDialect(Dialect.MYSQL));
        alter.getAlterExpressions().get(0).setDropColumns(List.of("old_id"));
        assertEquals("ALTER TABLE t " + action, alter.toString());
        List<Named> names = new ArrayList<>();
        DatabaseMetaDataValidation metadata = name -> {
            names.add(name);
            return !"old_id".equals(name.getFqn());
        };
        AlterValidator validator = new AlterValidator();
        validator.setContext(new ValidationContext().setCapabilities(List.of(metadata)));
        validator.validate(alter);
        assertTrue(validator.getValidationErrors().isEmpty(),
                validator.getValidationErrors().toString());
        assertTrue(names.stream().noneMatch(name -> "old_id".equals(name.getFqn())));
    }

    @Test
    void validatesKeyDefinitionsAndDropTargetsWithoutDuplicateKeyColumnChecks() {
        List<Named> names = new ArrayList<>();
        DatabaseMetaDataValidation metadata = name -> {
            names.add(name);
            return !"uq".equals(name.getFqn());
        };
        List<ValidationError> errors = new Validation(List.of(metadata),
                "ALTER TABLE t ADD UNIQUE KEY uq (id)").validate();
        assertTrue(errors.isEmpty(), errors.toString());
        assertEquals(1, names.stream().filter(name -> "id".equals(name.getFqn())).count());
        assertEquals(1, names.stream().filter(name -> "uq".equals(name.getFqn())).count());

        names.clear();
        errors = new Validation(List.of(metadata), "ALTER TABLE t DROP (old_a, old_b)").validate();
        assertTrue(errors.isEmpty(), errors.toString());
        assertEquals(List.of(new Named(NamedObject.table, "t"),
                new Named(NamedObject.column, "old_a"),
                new Named(NamedObject.column, "old_b")), names);
    }

    @Test
    void validatesForeignKeysFromTheirCurrentStructuredDefinition() throws JSQLParserException {
        Alter alter = (Alter) CCJSqlParserUtil.parse(
                "ALTER TABLE child ADD FOREIGN KEY (parent_id) REFERENCES old_parent (id)");
        ForeignKeyConstraint foreignKey =
                (ForeignKeyConstraint) alter.getAlterExpressions().get(0).getConstraint();
        foreignKey.setTable(new Table("app", "new_parent"));
        foreignKey.setColumnsNames(List.of("new_parent_id"));
        foreignKey.setReferencedColumnNames(List.of("new_id"));
        List<Named> names = new ArrayList<>();
        DatabaseMetaDataValidation metadata = name -> {
            names.add(name);
            return true;
        };

        assertTrue(Validation.validate(alter, Validation.createValidationContext(
                new FeatureConfiguration(), List.of(metadata))).isEmpty());
        assertEquals(List.of(new Named(NamedObject.table, "child"),
                new Named(NamedObject.table, "app.new_parent"),
                new Named(NamedObject.column, "new_id"),
                new Named(NamedObject.column, "new_parent_id")), names);
    }

    @Test
    void validatesRenamedConstraintTargetAsANewConstraint() {
        List<Named> names = new ArrayList<>();
        DatabaseMetaDataValidation metadata = name -> {
            names.add(name);
            return !"replacement".equals(name.getFqn());
        };
        List<ValidationError> errors = new Validation(List.of(metadata),
                "ALTER TABLE t RENAME CONSTRAINT original TO replacement").validate();
        assertTrue(errors.isEmpty(), errors.toString());
        assertEquals(List.of(new Named(NamedObject.table, "t"),
                new Named(NamedObject.constraint, "original"),
                new Named(NamedObject.constraint, "replacement")), names);
        DatabaseMetaDataValidation allExist = name -> true;
        errors = new Validation(List.of(allExist),
                "ALTER TABLE t RENAME CONSTRAINT original TO replacement").validate();
        assertEquals(1, errors.size());
        assertTrue(errors.toString().contains("replacement does exist"), errors.toString());
    }

    @Test
    public void testAlterTableAddColumn() throws JSQLParserException {
        String sql = "ALTER TABLE mytable ADD COLUMN mycolumn varchar (255)";
        validateNoErrors(sql, 1, DatabaseType.DATABASES);
    }

    @Test
    public void testAlterTableAddColumn_ColumnKeyWordImplicit() throws JSQLParserException {
        String sql = "ALTER TABLE mytable ADD mycolumn varchar (255)";
        validateNoErrors(sql, 1, DatabaseType.DATABASES);
    }

    @Test
    public void testAlterTableAddFunctionalIndex() throws JSQLParserException {
        String sql = "ALTER TABLE PPK_OLPN ADD INDEX fAdd ((b + c))";
        validateNoErrors(sql, 1, DatabaseType.DATABASES);
    }

    @Test
    public void testAlterTablePrimaryKey() throws JSQLParserException {
        validateNoErrors("ALTER TABLE animals ADD PRIMARY KEY (id)", 1, DatabaseType.DATABASES);
    }

    @Test
    public void testAlterTablePrimaryKeyDeferrable() throws JSQLParserException {
        validateNoErrors("ALTER TABLE animals ADD PRIMARY KEY (id) DEFERRABLE", 1,
                DatabaseType.DATABASES);
    }

    @Test
    public void testAlterTablePrimaryKeyNotDeferrable() throws JSQLParserException {
        validateNoErrors("ALTER TABLE animals ADD PRIMARY KEY (id) NOT DEFERRABLE", 1,
                DatabaseType.DATABASES);
    }

    @Test
    public void testAlterTablePrimaryKeyValidate() throws JSQLParserException {
        validateNoErrors("ALTER TABLE animals ADD PRIMARY KEY (id) VALIDATE", 1,
                DatabaseType.DATABASES);
    }

    @Test
    public void testAlterTablePrimaryKeyNoValidate() throws JSQLParserException {
        validateNoErrors("ALTER TABLE animals ADD PRIMARY KEY (id) NOVALIDATE", 1,
                DatabaseType.DATABASES);
    }

    @Test
    public void testAlterTablePrimaryKeyDeferrableValidate() throws JSQLParserException {
        validateNoErrors("ALTER TABLE animals ADD PRIMARY KEY (id) DEFERRABLE VALIDATE", 1,
                DatabaseType.DATABASES);
    }

    @Test
    public void testAlterTablePrimaryKeyDeferrableDisableNoValidate() throws JSQLParserException {
        validateNoErrors("ALTER TABLE animals ADD PRIMARY KEY (id) DEFERRABLE DISABLE NOVALIDATE",
                1,
                DatabaseType.DATABASES);
    }

    @Test
    public void testAlterTableUniqueKey() throws JSQLParserException {
        validateNoErrors(
                "ALTER TABLE `schema_migrations` ADD UNIQUE KEY `unique_schema_migrations` (`version`)",
                1,
                DatabaseType.DATABASES);
    }

    @Test
    public void testAlterTableForgeignKey() throws JSQLParserException {
        validateNoErrors(
                "ALTER TABLE test ADD FOREIGN KEY (user_id) REFERENCES ra_user (id) ON DELETE CASCADE",
                1,
                DatabaseType.DATABASES);
    }

    @Test
    public void testAlterTableAddConstraint() throws JSQLParserException {
        validateNoErrors(
                "ALTER TABLE RESOURCELINKTYPE ADD CONSTRAINT FK_RESOURCELINKTYPE_PARENTTYPE_PRIMARYKEY FOREIGN KEY (PARENTTYPE_PRIMARYKEY) REFERENCES RESOURCETYPE(PRIMARYKEY)",
                1, DatabaseType.DATABASES);
    }

    @Test
    public void testAlterTableAddConstraintWithConstraintState() throws JSQLParserException {
        validateNoErrors(
                "ALTER TABLE RESOURCELINKTYPE ADD CONSTRAINT FK_RESOURCELINKTYPE_PARENTTYPE_PRIMARYKEY FOREIGN KEY (PARENTTYPE_PRIMARYKEY) REFERENCES RESOURCETYPE(PRIMARYKEY) DEFERRABLE DISABLE NOVALIDATE",
                1, DatabaseType.DATABASES);
    }

    @Test
    public void testAlterTableAddConstraintWithConstraintState2() throws JSQLParserException {
        validateNoErrors(
                "ALTER TABLE RESOURCELINKTYPE ADD CONSTRAINT RESOURCELINKTYPE_PRIMARYKEY PRIMARY KEY (PRIMARYKEY) DEFERRABLE NOVALIDATE",
                1, DatabaseType.DATABASES);
    }

    @Test
    public void testAlterTableAddUniqueConstraint() throws JSQLParserException {
        validateNoErrors("ALTER TABLE Persons ADD UNIQUE (ID)", 1, DatabaseType.DATABASES);
    }

    @Test
    public void testAlterTableForgeignKey2() throws JSQLParserException {
        validateNoErrors("ALTER TABLE test ADD FOREIGN KEY (user_id) REFERENCES ra_user (id)", 1,
                DatabaseType.DATABASES);
    }

    @Test
    public void testAlterTableForgeignKey3() throws JSQLParserException {
        validateNoErrors(
                "ALTER TABLE test ADD FOREIGN KEY (user_id) REFERENCES ra_user (id) ON DELETE RESTRICT",
                1,
                DatabaseType.DATABASES);
    }

    @Test
    public void testAlterTableForgeignKey4() throws JSQLParserException {
        validateNoErrors(
                "ALTER TABLE test ADD FOREIGN KEY (user_id) REFERENCES ra_user (id) ON DELETE SET NULL",
                1,
                DatabaseType.DATABASES);
    }

    @Test
    public void testAlterTableDropColumn() throws JSQLParserException {
        validateNoErrors("ALTER TABLE test DROP COLUMN YYY", 1, DatabaseType.DATABASES);
    }

    @Test
    public void testAlterTableAlterColumnDropNotNullIssue918() throws JSQLParserException {
        validateNoErrors("ALTER TABLE \"user_table_t\" ALTER COLUMN name DROP NOT NULL", 1,
                DatabaseType.DATABASES);
    }

    @Test
    public void testAlterTableAlterColumnSetNotNull() throws JSQLParserException {
        validateNoErrors("ALTER TABLE user_table ALTER COLUMN name SET NOT NULL", 1,
                DatabaseType.DATABASES);
    }

}
