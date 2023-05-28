/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2020 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.validation.validator;

import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.statement.grant.Grant;
import net.sf.jsqlparser.util.validation.ValidationCapability;
import net.sf.jsqlparser.util.validation.metadata.NamedObject;

/**
 * @author gitmotte
 */
public class GrantValidator extends AbstractValidator<Grant> {

    @Override
    public void validate(Grant grant) {
        for (ValidationCapability c : getCapabilities()) {
            validateFeature(c, Feature.grant);
            if (isNotEmpty(grant.getUsers())) {
                grant.getUsers().forEach(u -> validateName(NamedObject.user, u));
            }
            if (grant.getRole() != null) {
                validateName(NamedObject.role, grant.getRole());
            }
            // can't validate grant.getObjectName() - don't know the kind of this object.
        }
    }
}
