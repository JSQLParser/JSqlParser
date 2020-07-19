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

import java.util.function.Consumer;

import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.statement.drop.Drop;

public class DropValidator extends AbstractValidator<Drop> {


    @Override
    public void validate(Drop drop) {
        for (ValidationCapability c : getCapabilities()) {
            Consumer<String> messageConsumer = getMessageConsumer(c);
            if (c instanceof FeatureSetValidation) {
                c.validate(context().put(FeatureSetValidation.Keys.feature, Feature.drop), messageConsumer);
            }
        }
        //        buffer.append("DROP ");
        //        buffer.append(drop.getType());
        //        if (drop.isIfExists()) {
        //            buffer.append(" IF EXISTS");
        //        }
        //
        //        buffer.append(" ").append(drop.getName());
        //
        //        if (drop.getParameters() != null && !drop.getParameters().isEmpty()) {
        //            buffer.append(" ").append(PlainSelect.getStringList(drop.getParameters()));
        //        }
    }

}
