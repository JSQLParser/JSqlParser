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

import java.util.Arrays;

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
        String type = drop.getType();
        for (ValidationCapability c : getCapabilities()) {
            validateFeature(c, Feature.drop);
            validateFeature(c, NamedObject.table.name().equalsIgnoreCase(type), Feature.dropTable);
            validateFeature(c, NamedObject.index.equalsIgnoreCase(type), Feature.dropIndex);
            validateFeature(c, NamedObject.view.equalsIgnoreCase(type), Feature.dropView);
            validateFeature(c, NamedObject.schema.equalsIgnoreCase(type), Feature.dropSchema);
            validateFeature(c, NamedObject.sequence.equalsIgnoreCase(type), Feature.dropSequence);

            validateFeature(c, drop.isIfExists() && NamedObject.table.name().equalsIgnoreCase(type),
                    Feature.dropTableIfExists);
            validateFeature(c, drop.isIfExists() && NamedObject.index.equalsIgnoreCase(type),
                    Feature.dropIndexIfExists);
            validateFeature(c, drop.isIfExists() && NamedObject.view.equalsIgnoreCase(type),
                    Feature.dropViewIfExists);
            validateFeature(c, drop.isIfExists() && NamedObject.schema.equalsIgnoreCase(type),
                    Feature.dropSchemaIfExists);
            validateFeature(c, drop.isIfExists() && NamedObject.sequence.equalsIgnoreCase(type),
                    Feature.dropSequenceIfExists);
        }

        NamedObject named = NamedObject.forName(type);
        if (Arrays.asList(NamedObject.table, NamedObject.view).contains(named)) {
            validateName(named, drop.getName().getFullyQualifiedName());
        }
    }

}
