/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.user;

import java.io.Serializable;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.HexValue;
import net.sf.jsqlparser.expression.StringValue;

/**
 * One multi-factor authentication change of a MySQL {@code ALTER USER} account:
 * {@code ADD factor IDENTIFIED ...}, {@code MODIFY factor IDENTIFIED ...}, {@code DROP factor}, or
 * a registration step of a passwordless device, {@code factor INITIATE REGISTRATION},
 * {@code factor FINISH REGISTRATION SET CHALLENGE_RESPONSE AS '...'} and {@code factor UNREGISTER}.
 * The factor is written {@code 2 FACTOR} or {@code 3 FACTOR}; the server checks the number when it
 * executes the statement.
 */
public class UserFactorOperation implements Serializable {

    public enum Operation {
        ADD, MODIFY, DROP, INITIATE_REGISTRATION, FINISH_REGISTRATION, UNREGISTER;

        /** Whether an {@code IDENTIFIED} clause follows the factor, as for ADD and MODIFY. */
        public boolean hasAuthentication() {
            return this == ADD || this == MODIFY;
        }

        /** Whether this is a registration step, written after the factor. */
        public boolean isRegistration() {
            return this == INITIATE_REGISTRATION || this == FINISH_REGISTRATION
                    || this == UNREGISTER;
        }
    }

    private Operation operation;
    private int factor;
    private UserAuthentication authentication;
    private Expression challengeResponse;

    public UserFactorOperation() {
        // for the fluent setters
    }

    public UserFactorOperation(Operation operation, int factor) {
        this.operation = operation;
        this.factor = factor;
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    /** The factor number of {@code n FACTOR}: 2 or 3 in a statement the server executes. */
    public int getFactor() {
        return factor;
    }

    public void setFactor(int factor) {
        this.factor = factor;
    }

    /** The authentication of the added or modified factor, or null for the other operations. */
    public UserAuthentication getAuthentication() {
        return authentication;
    }

    public void setAuthentication(UserAuthentication authentication) {
        this.authentication = authentication;
    }

    /**
     * The value after {@code SET CHALLENGE_RESPONSE AS} of FINISH REGISTRATION: a
     * {@link StringValue} or a {@link HexValue}, or null for the other operations.
     */
    public Expression getChallengeResponse() {
        return challengeResponse;
    }

    public void setChallengeResponse(Expression challengeResponse) {
        if (challengeResponse != null && !(challengeResponse instanceof StringValue)
                && !(challengeResponse instanceof HexValue)) {
            throw new IllegalArgumentException(
                    "A challenge response is a string or hexadecimal literal: "
                            + challengeResponse);
        }
        this.challengeResponse = challengeResponse;
    }

    @Override
    public String toString() {
        String factorClause = factor + " FACTOR";
        switch (operation) {
            case ADD:
            case MODIFY:
                return operation + " " + factorClause + " " + authentication;
            case DROP:
                return "DROP " + factorClause;
            case INITIATE_REGISTRATION:
                return factorClause + " INITIATE REGISTRATION";
            case FINISH_REGISTRATION:
                return factorClause + " FINISH REGISTRATION SET CHALLENGE_RESPONSE AS "
                        + challengeResponse;
            case UNREGISTER:
                return factorClause + " UNREGISTER";
            default:
                throw new IllegalStateException("Unsupported factor operation: " + operation);
        }
    }

    public UserFactorOperation withOperation(Operation operation) {
        setOperation(operation);
        return this;
    }

    public UserFactorOperation withFactor(int factor) {
        setFactor(factor);
        return this;
    }

    public UserFactorOperation withAuthentication(UserAuthentication authentication) {
        setAuthentication(authentication);
        return this;
    }

    public UserFactorOperation withChallengeResponse(Expression challengeResponse) {
        setChallengeResponse(challengeResponse);
        return this;
    }
}
