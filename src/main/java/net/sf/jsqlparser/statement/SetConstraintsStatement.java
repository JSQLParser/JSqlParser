/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.statement.create.table.ConstraintAttributes;

/**
 * {@code SET CONSTRAINTS {ALL | name [, ...]} {DEFERRED | IMMEDIATE}}, which changes when the
 * deferrable constraints of the current transaction are checked.
 */
public class SetConstraintsStatement extends ASTNodeAccessImpl implements Statement {
    private final List<String> constraintNames = new ArrayList<>();
    private ConstraintAttributes.Initially timing;

    public SetConstraintsStatement() {
        this(ConstraintAttributes.Initially.IMMEDIATE);
    }

    public SetConstraintsStatement(ConstraintAttributes.Initially timing) {
        setTiming(timing);
    }

    /**
     * Mutable list of the named constraints, each possibly schema-qualified and kept as written. An
     * empty list means ALL.
     */
    public List<String> getConstraintNames() {
        return constraintNames;
    }

    public SetConstraintsStatement addConstraintName(String name) {
        constraintNames.add(Objects.requireNonNull(name, "name"));
        return this;
    }

    /** Whether the statement applies to all deferrable constraints. */
    public boolean isAll() {
        return constraintNames.isEmpty();
    }

    /** The check timing, shared with the INITIALLY clause of constraint definitions. */
    public ConstraintAttributes.Initially getTiming() {
        return timing;
    }

    public void setTiming(ConstraintAttributes.Initially timing) {
        this.timing = Objects.requireNonNull(timing, "timing");
    }

    public SetConstraintsStatement withTiming(ConstraintAttributes.Initially timing) {
        setTiming(timing);
        return this;
    }

    public StringBuilder appendTo(StringBuilder builder) {
        builder.append("SET CONSTRAINTS ");
        if (isAll()) {
            builder.append("ALL");
        } else {
            builder.append(String.join(", ", constraintNames));
        }
        return builder.append(' ').append(timing);
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> visitor, S context) {
        return visitor.visit(this, context);
    }
}
