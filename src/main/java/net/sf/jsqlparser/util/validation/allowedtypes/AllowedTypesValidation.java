/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2020 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.validation.allowedtypes;

import java.util.Collection;
import java.util.function.Consumer;

import net.sf.jsqlparser.util.validation.ValidationCapability;
import net.sf.jsqlparser.util.validation.ValidationContext;

public class AllowedTypesValidation implements ValidationCapability {

    public static final String NAME = "allowed types";

    @Override
    public void validate(ValidationContext context, Consumer<String> errorMessageConsumer) {
        Object arg = context.getOptional(AllowedTypesContext.argument, Object.class);
        Boolean allowNull = context.getOptional(AllowedTypesContext.allow_null, Boolean.class);
        @SuppressWarnings("unchecked")
        Collection<Class<?>> allowedTypes = context.get(AllowedTypesContext.allowed_types, Collection.class);
        if (arg != null) {
            boolean error = true;
            for (Class<?> cls : allowedTypes) {
                if (cls.isAssignableFrom(arg.getClass())) {
                    error = false;
                    break;
                }
            }
            if (error) {
                errorMessageConsumer.accept(
                        arg.getClass() + " is not a valid argument - expected one of " + allowedTypes);
            }
        } else if (Boolean.FALSE.equals(allowNull)) {
            errorMessageConsumer.accept("argument is missing one of " + allowedTypes);
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

}
