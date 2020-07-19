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

import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.statement.create.view.AlterView;
import net.sf.jsqlparser.util.validation.DatabaseMetaDataValidation.NamedObject;

public class AlterViewValidator extends AbstractValidator<AlterView> {

    @Override
    public void validate(AlterView alterView) {
        for (ValidationCapability c : getCapabilities()) {
            validateFeature(Feature.alterView);
            validateName(c, NamedObject.view, alterView.getView().getFullyQualifiedName());
            validateOptionalColumnNames(alterView.getColumnNames(), c);
        }
        alterView.getSelectBody().accept(getValidator(SelectValidator.class));
    }

}
