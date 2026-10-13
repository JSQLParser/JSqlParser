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

import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.statement.TableMaintenanceStatement;
import net.sf.jsqlparser.util.validation.ValidationCapability;
import net.sf.jsqlparser.util.validation.metadata.NamedObject;

public class TableMaintenanceValidator extends AbstractValidator<TableMaintenanceStatement> {
    @Override
    public void validate(TableMaintenanceStatement statement) {
        for (ValidationCapability c : getCapabilities()) {
            validateFeature(c, Feature.tableMaintenance);
            statement.getTables().forEach(table -> validateName(c, NamedObject.table,
                    table.getFullyQualifiedName(), true));
        }
    }
}
