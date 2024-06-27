/*
 * #%L JSQLParser library %% Copyright (C) 2004 - 2020 JSQLParser %% Dual licensed under GNU LGPL
 * 2.1 or Apache License 2.0 #L%
 */
package net.sf.jsqlparser.util.validation.validator;

import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.statement.create.synonym.CreateSynonym;
import net.sf.jsqlparser.util.validation.ValidationCapability;
import net.sf.jsqlparser.util.validation.metadata.NamedObject;

/**
 * @author gitmotte
 */
public class CreateSynonymValidator extends AbstractValidator<CreateSynonym> {

    @Override
    public void validate(CreateSynonym statement) {
        for (ValidationCapability c : getCapabilities()) {
            validateFeature(Feature.createSynonym);
            validateName(c, NamedObject.synonym, statement.getSynonym().getFullyQualifiedName(),
                    false);
        }
    }
}
