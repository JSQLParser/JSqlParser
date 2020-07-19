/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.validation;

import java.util.stream.Collectors;

import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.alter.AlterExpression;
import net.sf.jsqlparser.statement.alter.AlterExpression.ColumnDataType;
import net.sf.jsqlparser.statement.alter.AlterExpression.ColumnDropNotNull;
import net.sf.jsqlparser.util.validation.DatabaseMetaDataValidation.NamedObject;

public class AlterValidator extends AbstractValidator<Alter> {


    @Override
    public void validate(Alter alter) {
        validateFeature(Feature.alter);

        alter.getTable().accept(getValidator(SelectValidator.class));

        alter.getAlterExpressions().forEach(this::validate);
    }

    public void validate(AlterExpression e) {
        for (ValidationCapability c : getCapabilities()) {

            validateOptionalColumnName(e.getColumnOldName(), c);
            validateOptionalColumnName(e.getColumnName(), c);

            if (e.getColumnDropNotNullList() != null) {
                validateOptionalColumnNames(e.getColumnDropNotNullList()
                        .stream().map(ColumnDropNotNull::getColumnName)
                        .collect(Collectors.toList()), c);
            }

            if (e.getColDataTypeList() != null) {
                validateOptionalColumnNames(e.getColDataTypeList()
                        .stream().map(ColumnDataType::getColumnName)
                        .collect(Collectors.toList()), c);
            }

            validateOptionalName(e.getConstraintName(), NamedObject.constraint, c);
            validateOptionalColumnNames(e.getPkColumns(), c);

            if (e.getFkColumns() != null) {
                validateName(c, NamedObject.table, e.getFkSourceTable());
                validateOptionalColumnNames(e.getFkColumns(), c);
                validateOptionalColumnNames(e.getFkSourceColumns(), c);
            }

            if (e.getUk()) {
                validateName(c, NamedObject.uniqueConstraint, e.getUkName());
                validateOptionalColumnNames(e.getUkColumns(), c);
            }

            if (e.getIndex() != null) {
                validateName(c, NamedObject.index, e.getIndex().getName());
                validateOptionalColumnNames(e.getIndex().getColumnsNames(), c);
            }
        }
    }



}
