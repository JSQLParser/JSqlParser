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
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.Index;
import net.sf.jsqlparser.util.validation.ValidationCapability;
import net.sf.jsqlparser.util.validation.metadata.NamedObject;

/**
 * @author gitmotte
 */
public class CreateTableValidator extends AbstractValidator<CreateTable> {


    @Override
    public void validate(CreateTable createTable) {
        for (ValidationCapability c : getCapabilities()) {
            validateFeature(c, Feature.createTable);
            validateFeature(c, createTable.isUnlogged(), Feature.createTableUnlogged);
            validateOptionalFeature(c, createTable.getCreateOptionsStrings(),
                    Feature.createTableCreateOptionStrings);
            validateOptionalFeature(c, createTable.getTableOptionsStrings(),
                    Feature.createTableTableOptionStrings);
            validateFeature(c, createTable.isIfNotExists(), Feature.createTableIfNotExists);
            validateOptionalFeature(c, createTable.getRowMovement(),
                    Feature.createTableRowMovement);
            validateOptionalFeature(c, createTable.getSelect(), Feature.createTableFromSelect);
            if (isNotEmpty(createTable.getIndexes())) {
                for (Index i : createTable.getIndexes()) {
                    validateName(c, NamedObject.index, i.getName());
                }
            }
            validateName(c, NamedObject.table, createTable.getTable().getFullyQualifiedName(),
                    false);
        }

        if (createTable.getSelect() != null) {
            getValidator(StatementValidator.class).validate(createTable.getSelect());
        }
    }

}
