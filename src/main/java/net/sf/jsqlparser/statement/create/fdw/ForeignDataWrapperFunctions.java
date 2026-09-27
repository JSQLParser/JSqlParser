/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.fdw;

import java.io.Serializable;

/** Distinguishes omitted handler/validator clauses from explicit NO HANDLER/VALIDATOR. */
public class ForeignDataWrapperFunctions implements Serializable {
    private boolean handlerSpecified;
    private String handler;
    private boolean validatorSpecified;
    private String validator;

    public boolean isHandlerSpecified() {
        return handlerSpecified;
    }

    public String getHandler() {
        return handler;
    }

    public void setHandler(String handler) {
        this.handler = handler;
        handlerSpecified = true;
    }

    public void clearHandler() {
        handler = null;
        handlerSpecified = false;
    }

    public boolean isValidatorSpecified() {
        return validatorSpecified;
    }

    public String getValidator() {
        return validator;
    }

    public void setValidator(String validator) {
        this.validator = validator;
        validatorSpecified = true;
    }

    public void clearValidator() {
        validator = null;
        validatorSpecified = false;
    }

    public void appendTo(StringBuilder sql) {
        if (handlerSpecified) {
            sql.append(handler == null ? " NO HANDLER" : " HANDLER " + handler);
        }
        if (validatorSpecified) {
            sql.append(validator == null ? " NO VALIDATOR" : " VALIDATOR " + validator);
        }
    }
}
