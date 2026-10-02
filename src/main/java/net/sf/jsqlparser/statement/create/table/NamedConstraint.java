/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.table;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import net.sf.jsqlparser.expression.Expression;

/** Common syntax and attributes of a constraint; a constraint is not an index. */
public abstract class NamedConstraint implements TableElement, Serializable {
    public enum ConstraintNamePosition {
        BEFORE, AFTER
    }

    private final List<String> name = new ArrayList<>();
    private String type;
    private ConstraintKind kind = ConstraintKind.OTHER;
    private ConstraintAttributes constraintAttributes;
    private boolean useConstraintKeyword;
    private ConstraintNamePosition constraintNamePosition = ConstraintNamePosition.BEFORE;

    public String getName() {
        return name.isEmpty() ? null : String.join(".", name);
    }

    public void setName(String value) {
        name.clear();
        if (value != null) {
            name.add(value);
        }
    }

    public void setName(List<String> value) {
        name.clear();
        name.addAll(value);
    }

    public List<String> getNameParts() {
        return Collections.unmodifiableList(name);
    }

    public String getType() {
        return type;
    }

    public void setType(String value) {
        type = value;
        kind = ConstraintKind.fromType(value);
    }

    public ConstraintKind getKind() {
        return kind;
    }

    public void setKind(ConstraintKind value) {
        kind = value;
    }

    public ConstraintAttributes getConstraintAttributes() {
        return constraintAttributes;
    }

    public void setConstraintAttributes(ConstraintAttributes value) {
        constraintAttributes = value;
    }

    public boolean isUseConstraintKeyword() {
        return useConstraintKeyword;
    }

    public void setUseConstraintKeyword(boolean value) {
        useConstraintKeyword = value;
    }

    public ConstraintNamePosition getConstraintNamePosition() {
        return constraintNamePosition;
    }

    public void setConstraintNamePosition(ConstraintNamePosition value) {
        constraintNamePosition = Objects.requireNonNull(value, "constraintNamePosition");
    }

    public NamedConstraint withConstraintNamePosition(ConstraintNamePosition value) {
        setConstraintNamePosition(value);
        return this;
    }

    public NamedConstraint withUseConstraintKeyword(boolean value) {
        setUseConstraintKeyword(value);
        return this;
    }

    public NamedConstraint withKind(ConstraintKind value) {
        setKind(value);
        return this;
    }

    public NamedConstraint withConstraintAttributes(ConstraintAttributes value) {
        setConstraintAttributes(value);
        return this;
    }

    public NamedConstraint withType(String value) {
        setType(value);
        return this;
    }

    public NamedConstraint withName(String value) {
        setName(value);
        return this;
    }

    public NamedConstraint withName(List<String> value) {
        setName(value);
        return this;
    }

    public void appendConstraintPrefixTo(StringBuilder sql) {
        boolean leadingName =
                getName() != null && constraintNamePosition == ConstraintNamePosition.BEFORE;
        if (useConstraintKeyword || leadingName) {
            sql.append("CONSTRAINT");
            if (leadingName) {
                sql.append(' ').append(getName());
            }
            sql.append(' ');
        }
    }

    public void appendConstraintSuffixTo(StringBuilder sql) {
        if (constraintNamePosition == ConstraintNamePosition.AFTER && getName() != null) {
            sql.append(" CONSTRAINT ").append(getName());
        }
    }

    public void appendConstraintAttributesTo(StringBuilder sql) {
        if (constraintAttributes != null) {
            constraintAttributes.appendTo(sql);
        }
    }

    public abstract void appendTo(StringBuilder sql, Consumer<Expression> expressionPrinter);

    @Override
    public String toString() {
        StringBuilder sql = new StringBuilder();
        appendTo(sql, sql::append);
        return sql.toString();
    }
}
