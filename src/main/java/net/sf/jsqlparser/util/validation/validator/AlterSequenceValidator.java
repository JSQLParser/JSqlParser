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
import net.sf.jsqlparser.statement.alter.sequence.AlterSequence;
import net.sf.jsqlparser.util.validation.metadata.NamedObject;

/**
 * @author gitmotte
 */
public class AlterSequenceValidator extends AbstractValidator<AlterSequence> {

    @Override
    public void validate(AlterSequence statement) {
        validateFeatureAndName(Feature.alterSequence, NamedObject.sequence, statement.getSequence().getFullyQualifiedName());
    }
}
