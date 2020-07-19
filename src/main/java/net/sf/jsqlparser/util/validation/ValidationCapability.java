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
     * @param context
     * @param errorMessageConsumer
     */
    void validate(ValidationContext context, Consumer<String> errorMessageConsumer) throws ValidationException;

    /**
     * @return a name of this {@link ValidationCapability}, forwards by default to
     *         {@link #toString()}
     */
    default String getName() {
        return getClass().getSimpleName();
    }

}
