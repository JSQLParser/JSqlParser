/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2020 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.alter.sequence;

import net.sf.jsqlparser.schema.Sequence;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

/**
 * An {@code ALTER SEQUENCE} statement
 */
public class AlterSequence implements Statement {

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
        sql = "ALTER SEQUENCE " + sequence;
        return sql;
    }

    public AlterSequence withSequence(Sequence sequence) {
        this.setSequence(sequence);
        return this;
    }
}
