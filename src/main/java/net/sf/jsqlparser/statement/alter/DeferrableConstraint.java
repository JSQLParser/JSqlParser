/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.alter;

public class DeferrableConstraint implements ConstraintState {

    private boolean not;

    public DeferrableConstraint() {
        // empty constructor
    }

    public DeferrableConstraint(boolean not) {
        this.not = not;
    }

    public boolean isNot() {
        return not;
    }

    public void setNot(boolean not) {
        this.not = not;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        if (not) {
            b.append("NOT ");
        }
        b.append("DEFERRABLE");
        return b.toString();
    }

    public DeferrableConstraint withNot(boolean not) {
        this.setNot(not);
        return this;
    }
}
