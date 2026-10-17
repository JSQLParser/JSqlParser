/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.validation.validator;

import static java.util.stream.Collectors.toList;

import java.util.EnumSet;
import java.util.List;
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.alter.AlterExpression;
import net.sf.jsqlparser.statement.alter.AlterExpression.ColumnDataType;
import net.sf.jsqlparser.statement.alter.AlterExpression.ColumnDropNotNull;
import net.sf.jsqlparser.statement.alter.AlterExpression.ColumnSetNotNull;
import net.sf.jsqlparser.statement.alter.AlterOperation;
import net.sf.jsqlparser.statement.alter.ColumnPosition;
import net.sf.jsqlparser.statement.create.table.NamedConstraint;
import net.sf.jsqlparser.statement.create.table.KeyColumnSource;
import net.sf.jsqlparser.statement.create.table.KeyElement;
import net.sf.jsqlparser.statement.create.table.ConstraintUsingIndex;
import net.sf.jsqlparser.statement.create.table.ForeignKeyConstraint;
import net.sf.jsqlparser.statement.create.table.ConstraintKind;
import net.sf.jsqlparser.statement.create.table.KeyConstraint;
import net.sf.jsqlparser.statement.create.table.Index;
import net.sf.jsqlparser.statement.create.table.TableElement;
import net.sf.jsqlparser.util.TableDefinitionTraversal;
import net.sf.jsqlparser.util.validation.ValidationCapability;
import net.sf.jsqlparser.util.validation.ValidationUtil;
import net.sf.jsqlparser.util.validation.metadata.NamedObject;

/**
 * @author gitmotte
 */
public class AlterValidator extends AbstractValidator<Alter> {

    @Override
    public void validate(Alter alter) {
        validateFeature(Feature.alterTable);

        validateOptionalFromItem(alter.getTable());

        alter.getAlterExpressions().forEach(e -> validate(alter, e));
    }

    public void validate(Alter alter, AlterExpression e) {
        TableDefinitionTraversal.visit(e, this::validateOptionalExpression,
                this::validateOptionalFromItem);
        TableElement definition = e.getActiveTableDefinition();
        for (ValidationCapability c : getCapabilities()) {

            validateOptionalColumnName(c, e.getColumnOldName());
            validateOptionalColumnName(c, e.getColumnName());

            if (e.getColumnDropNotNullList() != null) {
                validateOptionalColumnNames(c, ValidationUtil.map(e.getColumnDropNotNullList(),
                        ColumnDropNotNull::getColumnName));
            }

            if (e.getColumnSetNotNullList() != null) {
                validateOptionalColumnNames(c, ValidationUtil.map(e.getColumnSetNotNullList(),
                        ColumnSetNotNull::getColumnName));
            }

            if (e.hasActiveColumnDefinitions()) {
                boolean validateForExist =
                        !EnumSet.of(AlterOperation.ADD).contains(e.getOperation());
                validateOptionalColumnNames(c,
                        ValidationUtil.map(e.getColDataTypeList(), ColumnDataType::getColumnName),
                        validateForExist,
                        NamedObject.table);
                validateOptionalColumnNames(c, precedingColumnNames(e), NamedObject.table);
            }

            validateOptionalName(c, NamedObject.constraint, e.getConstraintName());
            validateOptionalName(c, NamedObject.constraint, e.getNewConstraintName(), null, false,
                    NamedObject.table);
            if (hasActiveDropColumns(e)) {
                validateOptionalColumnNames(c, e.getDropColumns());
            }

            validateConstraint(c, definition);
            validateIndex(c, e, definition);
        }
    }

    private void validateConstraint(ValidationCapability capability, TableElement definition) {
        if (!(definition instanceof NamedConstraint)) {
            return;
        }
        NamedConstraint constraint = (NamedConstraint) definition;
        validateOptionalName(capability, NamedObject.constraint, constraint.getName(), null, false,
                NamedObject.table);
        if (constraint instanceof ForeignKeyConstraint) {
            validateOptionalColumnNames(capability,
                    ((ForeignKeyConstraint) constraint).getReferencedColumnNames());
        }
        if (constraint instanceof KeyConstraint && constraint.getKind() == ConstraintKind.UNIQUE) {
            validateOptionalName(capability, NamedObject.uniqueConstraint,
                    ((KeyConstraint) constraint).getIndexName(), null, false, NamedObject.table);
        }
        if (constraint instanceof ConstraintUsingIndex) {
            validateName(capability, NamedObject.index,
                    ((ConstraintUsingIndex) constraint).getExistingIndexName());
        }
        if (constraint instanceof KeyColumnSource) {
            KeyColumnSource keys = (KeyColumnSource) constraint;
            if (keys.getColumns() != null) {
                validateOptionalColumnNames(capability, keys.getColumns().stream()
                        .filter(key -> !key.isExpression()).map(KeyElement::getColumnName)
                        .collect(toList()), NamedObject.constraint);
            }
        }
    }

    private boolean hasActiveDropColumns(AlterExpression expression) {
        if (expression.getOperation() == null || expression.getDropColumns() == null) {
            return false;
        }
        switch (expression.getOperation()) {
            case DROP:
                return expression.getColumnName() == null && !expression.getDropColumns().isEmpty();
            case DROP_UNIQUE:
                return true;
            case DROP_FOREIGN_KEY:
                return expression.getConstraintName() == null;
            default:
                return false;
        }
    }

    private void validateIndex(ValidationCapability capability, AlterExpression expression,
            TableElement definition) {
        Index index = expression.getIndex();
        boolean rename = expression.getOperation() == AlterOperation.RENAME_INDEX
                || expression.getOperation() == AlterOperation.RENAME_KEY;
        if (index == null || definition != index && !rename) {
            return;
        }
        if (rename && expression.getOldIndex() != null) {
            validateName(capability, NamedObject.index, expression.getOldIndex().getName());
        }
        boolean introducedName = expression.getOperation() == AlterOperation.ADD || rename;
        validateOptionalName(capability, NamedObject.index, index.getName(), null, !introducedName);
        if (definition == index && index.getColumns() != null) {
            validateOptionalColumnNames(capability, index.getColumns().stream()
                    .filter(key -> !key.isExpression()).map(KeyElement::getColumnName)
                    .collect(toList()), NamedObject.index);
        }
    }

    /** Returns the existing columns named by AFTER positions. */
    private static List<String> precedingColumnNames(AlterExpression expression) {
        return expression.getColDataTypeList().stream().map(ColumnDataType::getPosition)
                .filter(position -> position != null
                        && position.getKind() == ColumnPosition.Kind.AFTER)
                .map(ColumnPosition::getColumnName).collect(toList());
    }
}
