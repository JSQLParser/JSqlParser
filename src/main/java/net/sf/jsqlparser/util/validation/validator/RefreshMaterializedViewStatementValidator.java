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

import net.sf.jsqlparser.statement.refreshView.RefreshType;
import net.sf.jsqlparser.util.validation.ValidationCapability;
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.statement.refreshView.RefreshMaterializedViewStatement;
import net.sf.jsqlparser.util.validation.metadata.NamedObject;

/**
 * @author jxnu-liguobin
 */

public class RefreshMaterializedViewStatementValidator extends AbstractValidator<RefreshMaterializedViewStatement> {

    @Override
    public void validate(RefreshMaterializedViewStatement viewStatement) {
        validateFeatureAndName(Feature.refreshMaterializedView, NamedObject.table, viewStatement.getTableName());
        for (ValidationCapability c : getCapabilities()) {
            // default
            validateFeature(c, viewStatement.getRefreshType().compareTo(RefreshType.DEFAULT) == 0, Feature.refreshMaterializedView);
            // specify WITH DATA
            validateFeature(c, viewStatement.getRefreshType().compareTo(RefreshType.WITH_DATA) == 0, Feature.refreshMaterializedWithDataView);
            // specify WITH DATA and CONCURRENTLY
            validateOptionalFeature(c, viewStatement.getRefreshType().compareTo(RefreshType.WITH_DATA) == 0 && viewStatement.isConcurrently(), Feature.refreshMaterializedWithDataView);
            validateOptionalFeature(c, viewStatement.getRefreshType().compareTo(RefreshType.WITH_DATA) == 0 && viewStatement.isConcurrently(), Feature.refreshMaterializedConcurrentlyView);
            // specify WITH NO DATA
            validateOptionalFeature(c, viewStatement.getRefreshType().compareTo(RefreshType.WITH_NO_DATA) == 0 && !viewStatement.isConcurrently(), Feature.refreshMaterializedWithNoDataView);
        }
    }
}