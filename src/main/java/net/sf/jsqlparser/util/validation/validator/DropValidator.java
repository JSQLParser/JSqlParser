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
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.util.validation.ValidationCapability;
import net.sf.jsqlparser.util.validation.metadata.NamedObject;

/**
 * @author gitmotte
 */
public class DropValidator extends AbstractValidator<Drop> {


    @Override
    public void validate(Drop drop) {
        for (ValidationCapability c : getCapabilities()) {
            validateFeature(c, Feature.drop);
        }
        String type = drop.getType();
        if (NamedObject.table.name().equalsIgnoreCase(type)) {
            drop.getName().accept(getValidator(SelectValidator.class));
        }
    }

}
