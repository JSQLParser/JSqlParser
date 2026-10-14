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
import net.sf.jsqlparser.statement.show.ShowTableStatusStatement;
import net.sf.jsqlparser.util.validation.ValidationCapability;
import net.sf.jsqlparser.util.validation.metadata.NamedObject;

public class ShowTableStatusStatementValidator
        extends AbstractValidator<ShowTableStatusStatement> {

    @Override
    public void validate(ShowTableStatusStatement show) {
        for (ValidationCapability c : getCapabilities()) {
            validateFeature(c, Feature.showTableStatus);
            validateOptionalName(c, NamedObject.database, show.getDbName());
        }
    }
}
