/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2024 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.sequence;

import net.sf.jsqlparser.schema.Sequence;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

/**
 * A {@code CREATE SEQUENCE} statement
 */
public class CreateSequence implements Statement {

    public enum Persistence {
        TEMP, TEMPORARY, UNLOGGED, LOCAL_TEMP, LOCAL_TEMPORARY, GLOBAL_TEMP, GLOBAL_TEMPORARY
    }

    public Sequence sequence;
    private Persistence persistence;
    private boolean ifNotExists;

    public Persistence getPersistence() {
        return persistence;
    }

    public void setPersistence(Persistence persistence) {
        this.persistence = persistence;
    }

    public boolean isIfNotExists() {
        return ifNotExists;
    }

    public void setIfNotExists(boolean ifNotExists) {
        this.ifNotExists = ifNotExists;
    }

    public StringBuilder appendTo(StringBuilder builder) {
        builder.append("CREATE ");
        if (persistence != null) {
            builder.append(persistence.name().replace('_', ' ')).append(' ');
        }
        builder.append("SEQUENCE ");
        if (ifNotExists) {
            builder.append("IF NOT EXISTS ");
        }
        return builder.append(sequence);
    }


    public Sequence getSequence() {
        return sequence;
    }

    public void setSequence(Sequence sequence) {
        this.sequence = sequence;
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }

    public CreateSequence withSequence(Sequence sequence) {
        this.setSequence(sequence);
        return this;
    }
}
