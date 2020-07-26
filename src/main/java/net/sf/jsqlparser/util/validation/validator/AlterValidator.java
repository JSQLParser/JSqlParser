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

import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.alter.AlterExpression;
import net.sf.jsqlparser.statement.alter.AlterExpression.ColumnDataType;
import net.sf.jsqlparser.statement.alter.AlterExpression.ColumnDropNotNull;
import net.sf.jsqlparser.util.validation.ValidationCapability;
import net.sf.jsqlparser.util.validation.metadata.NamedObject;

/**
 * @author gitmotte
 */
public class AlterValidator extends AbstractValidator<Alter> {

    @Override
    public void validate(Alter alter) {
        validateFeature(Feature.alter);

        validateOptionalFromItem(alter.getTable());

        alter.getAlterExpressions().forEach(e -> validate(alter, e));
    }

    public void validate(Alter alter, AlterExpression e) {
        String tableFqn = alter.getTable().getFullyQualifiedName();
        for (ValidationCapability c : getCapabilities()) {

            validateOptionalColumnName(ValidatorUtil.concat(tableFqn, e.getColumnOldName()), c);
            validateOptionalColumnName(ValidatorUtil.concat(tableFqn, e.getColumnName()), c);

            if (e.getColumnDropNotNullList() != null) {
                validateOptionalColumnNames(
                        ValidatorUtil.concat(tableFqn, e.getColumnDropNotNullList()
                                .stream()
                                .map(ColumnDropNotNull::getColumnName)), c);
            }

            if (e.getColDataTypeList() != null) {
                validateOptionalColumnNames(ValidatorUtil.concat(tableFqn, e.getColDataTypeList()
                        .stream()
                        .map(ColumnDataType::getColumnName)), c);
            }

            validateOptionalName(e.getConstraintName(), NamedObject.constraint, c);
            if (e.getPkColumns() != null) {
                validateOptionalColumnNames(ValidatorUtil.concat(tableFqn, e.getPkColumns()), c);
            }

            if (e.getFkColumns() != null) {
                validateName(c, NamedObject.table, e.getFkSourceTable());
                validateOptionalColumnNames(ValidatorUtil.concat(tableFqn, e.getFkColumns()), c);
                validateOptionalColumnNames(ValidatorUtil.concat(tableFqn, e.getFkSourceColumns()), c);
            }

            if (e.getUk()) {
                validateName(c, NamedObject.uniqueConstraint, e.getUkName());
                validateOptionalColumnNames(ValidatorUtil.concat(tableFqn, e.getUkColumns()), c);
            }

            if (e.getIndex() != null) {
                validateName(c, NamedObject.index, e.getIndex().getName());
                if (e.getIndex().getColumns() != null) {
                    validateOptionalColumnNames(ValidatorUtil.concat(tableFqn, e.getIndex().getColumnsNames()), c);
                }
            }
        }
    }



}
