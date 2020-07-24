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

import java.util.Objects;

public class ValidationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(Throwable cause) {
        super(cause == null ? null : cause.getMessage(), cause);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (this == o) {
            return true;
        }

        if (o.getClass().equals(this.getClass())) {
            // exact type match!
            ValidationException ve = (ValidationException) o;
            return Objects.equals(getMessage(), ve.getMessage()) && Objects.equals(getCause(), ve.getCause());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return getMessage().hashCode() + (getCause() == null ? 0 : getCause().toString().hashCode());
    }

    @Override
    public String toString () {
        return getClass().getSimpleName() + ": " + getMessage();
    }

}
