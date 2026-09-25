/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.alter;

import java.util.Objects;
import net.sf.jsqlparser.statement.create.table.ConstraintAttributes;

/** Changes an existing constraint using the attributes shared with CREATE and ADD. */
public class AlterConstraintAttributes extends AlterExpression {
    private ConstraintAttributes attributes = new ConstraintAttributes();

    public AlterConstraintAttributes() {
        setOperation(AlterOperation.ALTER);
        setConstraintType("CONSTRAINT");
    }

    public ConstraintAttributes getAttributes() {
        return attributes;
    }

    public void setAttributes(ConstraintAttributes attributes) {
        this.attributes = Objects.requireNonNull(attributes, "attributes");
    }

    @Override
    public String getConstraintName() {
        return getConstraintSymbol();
    }

    @Override
    public void setConstraintName(String name) {
        setConstraintSymbol(name);
    }

    @Override
    public boolean isEnforced() {
        return Boolean.TRUE.equals(attributes.getEnforced());
    }

    @Override
    public void setEnforced(boolean enforced) {
        attributes.setEnforced(enforced);
    }

    @Override
    protected void appendBody(StringBuilder builder) {
        builder.append("ALTER CONSTRAINT ").append(getConstraintSymbol());
        attributes.appendTo(builder);
    }
}
