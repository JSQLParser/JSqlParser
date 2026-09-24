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
import net.sf.jsqlparser.schema.Column;

/** PostgreSQL 18 table-level NOT NULL constraint, optionally named and non-inheritable. */
public class NotNullConstraint extends NamedConstraint {
    private Column column;
    private boolean noInherit;

    public NotNullConstraint() {
        setType("NOT NULL");
    }

    public Column getColumn() {
        return column;
    }

    public void setColumn(Column column) {
        this.column = column;
    }

    public boolean isNoInherit() {
        return noInherit;
    }

    public void setNoInherit(boolean noInherit) {
        this.noInherit = noInherit;
    }

    public NotNullConstraint withColumn(Column column) {
        setColumn(column);
        return this;
    }

    public NotNullConstraint withNoInherit(boolean noInherit) {
        setNoInherit(noInherit);
        return this;
    }

    @Override
    public NotNullConstraint withName(String name) {
        setName(name);
        return this;
    }

    public NotNullConstraint withConstraintAttributes(ConstraintAttributes attributes) {
        setConstraintAttributes(attributes);
        return this;
    }

    @Override
    public void appendTo(StringBuilder sql, Consumer<Expression> expressionPrinter) {
        if (column == null) {
            throw new IllegalStateException("NOT NULL requires a target column");
        }
        appendConstraintPrefixTo(sql);
        sql.append("NOT NULL ");
        expressionPrinter.accept(column);
        if (noInherit) {
            sql.append(" NO INHERIT");
        }
        appendConstraintAttributesTo(sql);
    }
}
