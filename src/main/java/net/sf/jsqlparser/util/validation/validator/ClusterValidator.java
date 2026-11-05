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
import net.sf.jsqlparser.statement.maintenance.ClusterStatement;
import net.sf.jsqlparser.util.validation.ValidationCapability;
import net.sf.jsqlparser.util.validation.metadata.NamedObject;

public class ClusterValidator extends AbstractValidator<ClusterStatement> {
    @Override
    public void validate(ClusterStatement cluster) {
        for (ValidationCapability c : getCapabilities()) {
            validateFeature(c, Feature.cluster);
            if (cluster.getTable() != null) {
                validateName(c, NamedObject.table, cluster.getTable().getFullyQualifiedName(),
                        true);
            }
        }
    }
}
