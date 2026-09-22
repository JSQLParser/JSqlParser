/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.table;

import java.util.function.Consumer;
import net.sf.jsqlparser.expression.Expression;

/**
 * A PostgreSQL UNIQUE or PRIMARY KEY constraint backed by an existing index. The referenced index
 * is distinct from the constraint name and from a newly declared index.
 */
public class ConstraintUsingIndex extends NamedConstraint {
    private String existingIndexName;

    public ConstraintUsingIndex() {
        setType("UNIQUE");
    }

    /** Returns the referenced index identifier, retaining SQL quotes. */
    public String getExistingIndexName() {
        return existingIndexName;
    }

    public void setExistingIndexName(String existingIndexName) {
        this.existingIndexName = existingIndexName;
    }

    public ConstraintUsingIndex withExistingIndexName(String existingIndexName) {
        setExistingIndexName(existingIndexName);
        return this;
    }

    @Override
    public ConstraintUsingIndex withName(String name) {
        super.withName(name);
        return this;
    }

    @Override
    public ConstraintUsingIndex withType(String type) {
        super.withType(type);
        return this;
    }

    public ConstraintUsingIndex withConstraintAttributes(ConstraintAttributes attributes) {
        setConstraintAttributes(attributes);
        return this;
    }

    @Override
    public void appendTo(StringBuilder sql, Consumer<Expression> expressionPrinter) {
        appendConstraintPrefixTo(sql);
        sql.append(getType()).append(" USING INDEX ").append(existingIndexName);
        appendConstraintAttributesTo(sql);
    }
}
