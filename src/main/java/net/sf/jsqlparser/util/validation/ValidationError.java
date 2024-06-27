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

import net.sf.jsqlparser.statement.Statement;

public class ValidationError {

    private final String statements;
    private Statement parsedStatement;

    private Set<ValidationException> errors = new HashSet<>();
    private ValidationCapability capability;

    public ValidationError(String statements) {
        this.statements = statements;
    }

    public ValidationError addError(ValidationException error) {
        this.errors.add(error);
        return this;
    }

    public ValidationError addErrors(Collection<ValidationException> errors) {
        this.errors.addAll(errors);
        return this;
    }

    /**
     * @return the set of {@link ValidationException}'s (no duplicates)
     */
    public Set<ValidationException> getErrors() {
        return errors;
    }

    /**
     * @return the {@link ValidationCapability} which produced this error
     */
    public ValidationCapability getCapability() {
        return capability;
    }

    public void setCapability(ValidationCapability databaseType) {
        this.capability = databaseType;
    }

    /**
     * @return the parsed {@link Statement}, if parsing was possible
     */
    public Statement getParsedStatement() {
        return parsedStatement;
    }

    public void setParsedStatement(Statement parsedStatement) {
        this.parsedStatement = parsedStatement;
    }

    /**
     * @return the statements (may be more than one) given for validation
     */
    public String getStatements() {
        return statements;
    }

    public ValidationError withCapability(ValidationCapability databaseType) {
        setCapability(databaseType);
        return this;
    }

    public ValidationError withParsedStatement(Statement parsedStatement) {
        setParsedStatement(parsedStatement);
        return this;
    }

    @Override
    public String toString() {
        return "ValidationError [\nstatement=" + statements + "\ncapability="
                + (capability != null ? capability.getName() : "<null>") + "\nerrors=" + errors
                + "\n]";
    }


}
