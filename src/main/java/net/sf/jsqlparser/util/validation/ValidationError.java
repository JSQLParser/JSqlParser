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

    private Set<ValidationException> errors = new HashSet<>();
    private ValidationCapability capability;

    public ValidationError(String statement) {
        this.statement = statement;
    }

    public ValidationError addError(ValidationException error) {
        this.errors.add(error);
        return this;
    }

    public ValidationError addErrors(Collection<ValidationException> errors) {
        this.errors.addAll(errors);
        return this;
    }

    public Set<ValidationException> getErrors() {
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

    public String getStatement() {
        return statement;
    }

    @Override
    public String toString() {
        return "ValidationError [statement=" + statement
                + ", capability=" + (capability != null ? capability.getName() : "<null>")
                + ", errors=" + errors + "]";
    }

}
