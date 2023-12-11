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
import net.sf.jsqlparser.statement.refresh.RefreshMaterializedViewStatement;
import net.sf.jsqlparser.util.validation.ValidationCapability;
import net.sf.jsqlparser.util.validation.metadata.NamedObject;

/**
 * @author jxnu-liguobin
 */

public class RefreshMaterializedViewStatementValidator
        extends AbstractValidator<RefreshMaterializedViewStatement> {

    @Override
    public void validate(RefreshMaterializedViewStatement viewStatement) {
        validateFeatureAndName(Feature.refreshMaterializedView, NamedObject.table,
                viewStatement.getView().getName());
        for (ValidationCapability c : getCapabilities()) {
            // default
            validateFeature(c, viewStatement.getRefreshMode() == null,
                    Feature.refreshMaterializedView);
            // specify WITH DATA
            validateOptionalFeature(c, viewStatement.getRefreshMode(),
                    Feature.refreshMaterializedWithDataView);
            validateOptionalFeature(c, viewStatement.getRefreshMode(),
                    Feature.refreshMaterializedWithNoDataView);
        }
    }
}
