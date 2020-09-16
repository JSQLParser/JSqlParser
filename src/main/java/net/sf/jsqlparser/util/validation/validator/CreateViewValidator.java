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
import net.sf.jsqlparser.statement.create.view.CreateView;
import net.sf.jsqlparser.statement.create.view.ForceOption;
import net.sf.jsqlparser.statement.create.view.TemporaryOption;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.util.validation.ValidationCapability;

/**
 * @author gitmotte
 */
public class CreateViewValidator extends AbstractValidator<CreateView> {

    @Override
    public void validate(CreateView createView) {
        for (ValidationCapability c : getCapabilities()) {
            validateFeature(c, Feature.createView);
            validateFeature(c, createView.isOrReplace(), Feature.createOrReplaceView);
            validateFeature(c, !ForceOption.NONE.equals(createView.getForce()), Feature.createViewForce);
            validateFeature(c, !TemporaryOption.NONE.equals(createView.getTemporary()), Feature.createViewTemporary);
            validateFeature(c, createView.isMaterialized(), Feature.createViewMaterialized);

            // TODO validate for not existing ?? this may be a little bit more complex
            // because database-names share one space in most databases
            // validateNameNotExists(c, NamedObject.view,
            // createView.getView().getFullyQualifiedName());
            // validateOptionalColumnNames(createView.getColumnNames(), c);
        }
        SelectValidator v = getValidator(SelectValidator.class);
        Select select = createView.getSelect();
        if (isNotEmpty(select.getWithItemsList())) {
            select.getWithItemsList().forEach(wi -> wi.accept(v));
        }
        select.getSelectBody().accept(v);

    }

}
