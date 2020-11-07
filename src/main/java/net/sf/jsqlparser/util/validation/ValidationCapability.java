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

import java.util.function.Consumer;

public interface ValidationCapability {

    /**
     * @return a name of this {@link ValidationCapability}
     */
    default String getName() {
        return getClass().getSimpleName();
    }

    /**
     * Validate and add {@link ValidationException}'s to given consumer.
     *
     * @param context
     * @param errorConsumer
     */
    void validate(ValidationContext context, Consumer<ValidationException> errorConsumer);

    default ValidationException toError(String message) {
        return new ValidationException(message);
    }

    default ValidationException toError(String message, Throwable th) {
        return new ValidationException(message, th);
    }

}
