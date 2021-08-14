/*
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2020 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.sequence;

import net.sf.jsqlparser.schema.Sequence;
import net.sf.jsqlparser.statement.DDLStatement;
import net.sf.jsqlparser.statement.StatementVisitor;

/**
 * A {@code CREATE SEQUENCE} statement
 */
public class CreateSequence extends DDLStatement {

    public Sequence sequence;

    public void setSequence(Sequence sequence) {
        this.sequence = sequence;
    }

    public Sequence getSequence() {
        return sequence;
    }

    @Override
    public void accept(StatementVisitor statementVisitor) {
        statementVisitor.visit(this);
    }

    public CreateSequence withSequence(Sequence sequence) {
        this.setSequence(sequence);
        return this;
    }

    @Override
    public StringBuilder appendTo(StringBuilder builder) {
        builder.append("CREATE SEQUENCE ").append(sequence);
        return builder;
    }
}
