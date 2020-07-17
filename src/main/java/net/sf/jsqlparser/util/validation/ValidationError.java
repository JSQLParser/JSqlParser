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
import java.util.HashSet;
import java.util.Set;

public class ValidationError {
    private final String statement;

    private Set<String> errors = new HashSet<>();
    private ValidationCapability capability;
    private Exception exception;

    public ValidationError(String statement) {
        this.statement = statement;
    }

    public ValidationError addError(String error) {
        this.errors.add(error);
        return this;
    }

    public ValidationError addErrors(Collection<String> errors) {
        this.errors.addAll(errors);
        return this;
    }

    public Set<String> getErrors() {
        return errors;
    }

    public ValidationCapability getCapability() {
        return capability;
    }

    public void setCapability(ValidationCapability databaseType) {
        this.capability = databaseType;
    }

    public ValidationError withCapability(ValidationCapability databaseType) {
        setCapability(databaseType);
        return this;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public ValidationError withException(Exception exception) {
        setException(exception);
        return this;
    }

    public String getStatement() {
        return statement;
    }

}
