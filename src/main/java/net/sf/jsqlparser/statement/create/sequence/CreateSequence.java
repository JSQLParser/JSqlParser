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

    public Sequence sequence;

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
        String sql;
        sql = "CREATE SEQUENCE " + sequence;
        return sql;
    }

    public CreateSequence withSequence(Sequence sequence) {
        this.setSequence(sequence);
        return this;
    }
}
