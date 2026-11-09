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
import net.sf.jsqlparser.statement.maintenance.VacuumStatement;
import net.sf.jsqlparser.util.validation.ValidationCapability;
import net.sf.jsqlparser.util.validation.metadata.NamedObject;

public class VacuumValidator extends AbstractValidator<VacuumStatement> {
    @Override
    public void validate(VacuumStatement vacuum) {
        for (ValidationCapability c : getCapabilities()) {
            validateFeature(c, Feature.vacuum);
            vacuum.getTargets().forEach(target -> validateName(c, NamedObject.table,
                    target.getTable().getFullyQualifiedName(), true));
        }
    }
}
