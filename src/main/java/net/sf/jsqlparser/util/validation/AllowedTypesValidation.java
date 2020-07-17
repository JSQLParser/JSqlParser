/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2020 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.validation;

import java.util.Collection;
import java.util.function.Consumer;

public class AllowedTypesValidation implements ValidationCapability {

    public enum Keys implements ContextKey {
        /**
         * a collection of allowed {@link Class}es
         */
        allowed_types,
        /**
         * the object given (may be null)
         */
        argument,
        /**
         * a boolean, default = true
         */
        allow_null
    }

    @Override
    public void validate(ValidationContext context, Consumer<String> errorMessageConsumer) {
        Object arg = context.get(Keys.argument, Object.class);
        Boolean allowNull = context.get(Keys.allow_null, Boolean.class);
        @SuppressWarnings("unchecked")
        Collection<Class<?>> allowedTypes = context.get(Keys.allowed_types, Collection.class);
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


}
