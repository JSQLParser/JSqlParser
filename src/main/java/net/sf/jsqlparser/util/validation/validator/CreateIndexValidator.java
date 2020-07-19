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

import java.util.function.Consumer;

import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.util.validation.feature.FeatureSetValidation;
import net.sf.jsqlparser.util.validation.ValidationCapability;
import net.sf.jsqlparser.util.validation.feature.FeatureContext;

public class CreateIndexValidator extends AbstractValidator<CreateIndex> {


    @Override
    public void validate(CreateIndex createIndex) {
        for (ValidationCapability c : getCapabilities()) {
            Consumer<String> messageConsumer = getMessageConsumer(c);
            if (c instanceof FeatureSetValidation) {
                c.validate(context().put(FeatureContext.feature, Feature.createIndex), messageConsumer);
            }
        }
        //        Index index = createIndex.getIndex();
        //
        //        buffer.append("CREATE ");
        //
        //        if (index.getType() != null) {
        //            buffer.append(index.getType());
        //            buffer.append(" ");
        //        }
        //
        //        buffer.append("INDEX ");
        //        buffer.append(index.getName());
        //        buffer.append(" ON ");
        //        buffer.append(createIndex.getTable().getFullyQualifiedName());
        //
        //        String using = index.getUsing();
        //        if (using != null) {
        //            buffer.append(" USING ");
        //            buffer.append(using);
        //        }
        //
        //        if (index.getColumnsNames() != null) {
        //            buffer.append(" (");
        //            buffer.append(index.getColumnWithParams().stream()
        //                    .map(cp -> cp.columnName + (cp.getParams() != null ? " " + String.join(" ", cp.getParams()) : ""))
        //                    .collect(joining(", ")));
        //            buffer.append(")");
        //        }
        //
        //        if (createIndex.getTailParameters() != null) {
        //            for (String param : createIndex.getTailParameters()) {
        //                buffer.append(" ").append(param);
        //            }
        //        }
    }

}
