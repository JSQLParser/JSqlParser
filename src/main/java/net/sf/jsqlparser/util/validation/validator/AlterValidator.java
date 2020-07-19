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

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.alter.AlterExpression;
import net.sf.jsqlparser.statement.alter.AlterExpression.ColumnDataType;
import net.sf.jsqlparser.statement.alter.AlterExpression.ColumnDropNotNull;
import net.sf.jsqlparser.util.validation.ValidationCapability;
import net.sf.jsqlparser.util.validation.metadata.NamedObject;

public class AlterValidator extends AbstractValidator<Alter> {


    @Override
    public void validate(Alter alter) {
        validateFeature(Feature.alter);

        alter.getTable().accept(getValidator(SelectValidator.class));

        alter.getAlterExpressions().forEach(e -> validate(alter, e));
    }

    public void validate(Alter alter, AlterExpression e) {
        String tableFqn = alter.getTable().getFullyQualifiedName();
        for (ValidationCapability c : getCapabilities()) {

            validateOptionalColumnName(concat(tableFqn, e.getColumnOldName()), c);
            validateOptionalColumnName(concat(tableFqn, e.getColumnName()), c);

            if (e.getColumnDropNotNullList() != null) {
                validateOptionalColumnNames(
                        concat(tableFqn, e.getColumnDropNotNullList()
                                .stream()
                                .map(ColumnDropNotNull::getColumnName)), c);
            }

            if (e.getColDataTypeList() != null) {
                validateOptionalColumnNames(concat(tableFqn, e.getColDataTypeList()
                        .stream()
                        .map(ColumnDataType::getColumnName)), c);
            }

            validateOptionalName(e.getConstraintName(), NamedObject.constraint, c);
            if (e.getPkColumns() != null) {
                validateOptionalColumnNames(concat(tableFqn, e.getPkColumns()), c);
            }

            if (e.getFkColumns() != null) {
                validateName(c, NamedObject.table, e.getFkSourceTable());
                validateOptionalColumnNames(concat(tableFqn, e.getFkColumns()), c);
                validateOptionalColumnNames(concat(tableFqn, e.getFkSourceColumns()), c);
            }

            if (e.getUk()) {
                validateName(c, NamedObject.uniqueConstraint, e.getUkName());
                validateOptionalColumnNames(concat(tableFqn, e.getUkColumns()), c);
            }

            if (e.getIndex() != null) {
                validateName(c, NamedObject.index, e.getIndex().getName());
                validateOptionalColumnNames(concat(tableFqn, e.getIndex().getColumnsNames()), c);
            }
        }
    }

    public List<String> concat(String prefix, Collection<String> values) {
        return values == null ? null : concat(prefix, values.stream());
    }

    public List<String> concat(String prefix, Stream<String> values) {
        return values.map(v -> concat(prefix, v)).collect(Collectors.toList());
    }

    public String concat(String prefix, String name) {
        return name == null ? null : new StringBuilder(prefix).append(".").append(name).toString();
    }



}
