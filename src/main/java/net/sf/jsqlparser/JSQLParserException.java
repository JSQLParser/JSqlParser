/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser;

public class JSQLParserException extends Exception {

    private static final long serialVersionUID = -4200894355696788796L;

    public JSQLParserException() {
        super();
    }

    public JSQLParserException(String message, Throwable cause) {
        super(message, cause);
    }

    public JSQLParserException(String message) {
        super(message);
    }

    public JSQLParserException(Throwable cause) {
        super(cause == null ? null : cause.getMessage(), cause);
    }

}
