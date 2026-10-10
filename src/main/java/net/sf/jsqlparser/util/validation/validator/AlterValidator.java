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
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.alter.AlterExpression;
import net.sf.jsqlparser.statement.alter.AlterExpression.ColumnDataType;
import net.sf.jsqlparser.statement.alter.AlterExpression.ColumnDropNotNull;
import net.sf.jsqlparser.statement.alter.AlterExpression.ColumnSetNotNull;
import net.sf.jsqlparser.statement.alter.AlterOperation;
import net.sf.jsqlparser.statement.create.table.NamedConstraint;
import net.sf.jsqlparser.statement.create.table.KeyColumnSource;
import net.sf.jsqlparser.statement.create.table.KeyElement;
import net.sf.jsqlparser.statement.create.table.ConstraintUsingIndex;
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

            if (e.getColDataTypeList() != null) {
                boolean validateForExist =
                        !EnumSet.of(AlterOperation.ADD).contains(e.getOperation());
                validateOptionalColumnNames(c,
                        ValidationUtil.map(e.getColDataTypeList(), ColumnDataType::getColumnName),
                        validateForExist,
                        NamedObject.table);
            }

            validateOptionalName(c, NamedObject.constraint, e.getConstraintName());
            validateOptionalName(c, NamedObject.constraint, e.getNewConstraintName(), null, false,
                    NamedObject.table);
            if (e.getPkColumns() != null) {
                validateOptionalColumnNames(c, e.getPkColumns());
            }

            if (e.getFkColumns() != null) {
                validateName(c, NamedObject.table, e.getFkSourceTable());
                validateOptionalColumnNames(c, e.getFkColumns());
                validateOptionalColumnNames(c, e.getFkSourceColumns());
            }

            if (e.getUk()) {
                validateName(c, NamedObject.uniqueConstraint, e.getUkName());
                validateOptionalColumnNames(c, e.getUkColumns(), NamedObject.uniqueConstraint);
            }

            NamedConstraint constraint = e.getConstraint();
            if (constraint != null) {
                validateOptionalName(c, NamedObject.constraint, constraint.getName(), null, false,
                        NamedObject.table);
                if (constraint instanceof ConstraintUsingIndex) {
                    validateName(c, NamedObject.index,
                            ((ConstraintUsingIndex) constraint).getExistingIndexName());
                }
                if (constraint instanceof KeyColumnSource) {
                    KeyColumnSource keys = (KeyColumnSource) constraint;
                    if (keys.getColumns() != null) {
                        validateOptionalColumnNames(c, keys.getColumns().stream()
                                .filter(key -> !key.isExpression()).map(KeyElement::getColumnName)
                                .collect(toList()), NamedObject.constraint);
                    }
                }
            }
            if (e.getIndex() != null) {
                validateName(c, NamedObject.index, e.getIndex().getName());
                if (e.getIndex().getColumns() != null) {
                    validateOptionalColumnNames(c, e.getIndex().getColumns().stream()
                            .filter(key -> !key.isExpression()).map(KeyElement::getColumnName)
                            .collect(toList()), NamedObject.index);
                }
            }
        }
    }


}
