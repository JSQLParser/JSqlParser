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

/**
 * can be used on unexpected errors during validation
 *
 * @author gitmotte
 */
public class UnexpectedValidationException extends ValidationException {

    private static final long serialVersionUID = 1L;

    public UnexpectedValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnexpectedValidationException(String message) {
        super(message);
    }

    public UnexpectedValidationException(Throwable cause) {
        super(cause);
    }
}
