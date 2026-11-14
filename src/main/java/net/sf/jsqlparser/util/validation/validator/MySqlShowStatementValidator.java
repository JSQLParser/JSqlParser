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
import net.sf.jsqlparser.statement.show.MySqlShowStatement;
import net.sf.jsqlparser.util.validation.ValidationCapability;
import net.sf.jsqlparser.util.validation.metadata.NamedObject;

public class MySqlShowStatementValidator extends AbstractValidator<MySqlShowStatement> {

    @Override
    public void validate(MySqlShowStatement show) {
        for (ValidationCapability c : getCapabilities()) {
            validateFeature(c, Feature.mySqlShow);
            validateOptionalName(c, NamedObject.database, show.getDbName());
            if (show.getKind() == MySqlShowStatement.Kind.PROCEDURE_CODE
                    && show.getRoutineName() != null) {
                validateName(c, NamedObject.procedure,
                        show.getRoutineName().getFullyQualifiedName());
            }
        }
    }
}
