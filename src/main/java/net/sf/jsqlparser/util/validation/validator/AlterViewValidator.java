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
import net.sf.jsqlparser.statement.create.view.AlterView;
import net.sf.jsqlparser.util.validation.ValidationCapability;
import net.sf.jsqlparser.util.validation.metadata.NamedObject;

/**
 * @author gitmotte
 */
public class AlterViewValidator extends AbstractValidator<AlterView> {

    @Override
    public void validate(AlterView alterView) {
        for (ValidationCapability c : getCapabilities()) {
            validateFeature(Feature.alterView);
            validateFeature(c, alterView.isUseReplace(), Feature.alterViewReplace);
            validateName(c, NamedObject.view, alterView.getView().getFullyQualifiedName());
            validateOptionalColumnNames(alterView.getColumnNames(), c);
        }
        alterView.getSelectBody().accept(getValidator(SelectValidator.class));
    }

}
