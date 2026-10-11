/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2022 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.insert;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.statement.create.table.KeyElement;
import java.util.function.Consumer;

import java.io.Serializable;
import java.util.*;

/**
 * https://www.postgresql.org/docs/current/sql-insert.html
 *
 * <pre>
 * conflict_target can be one of:
 *
 *     ( { index_column_name | ( index_expression ) } [ COLLATE collation ] [ opclass ] [, ...] ) [ WHERE index_predicate ]
 *     ON CONSTRAINT constraint_name
 * </pre>
 */
public class InsertConflictTarget implements Serializable {
    private final List<KeyElement> indexElements = new ArrayList<>();
    private Expression whereExpression;
    private String constraintName;

    public InsertConflictTarget() {}

    public InsertConflictTarget(String indexColumnName, Expression indexExpression,
            Expression whereExpression, String constraintName) {
        this(indexColumnName == null ? Collections.emptyList()
                : Collections.singletonList(indexColumnName),
                indexExpression, whereExpression, constraintName);
    }

    public InsertConflictTarget(Collection<String> indexColumnNames, Expression indexExpression,
            Expression whereExpression, String constraintName) {
        if (indexColumnNames != null && !indexColumnNames.isEmpty()) {
            addAllIndexColumnNames(indexColumnNames);
        } else if (indexExpression != null) {
            setIndexExpression(indexExpression);
        }
        this.whereExpression = whereExpression;
        this.constraintName = constraintName;
    }

    /** Ordered column and expression keys, including their collation and operator class. */
    public List<KeyElement> getIndexElements() {
        return indexElements;
    }

    public void setIndexElements(List<KeyElement> elements) {
        List<KeyElement> copy = new ArrayList<>(elements);
        indexElements.clear();
        indexElements.addAll(copy);
    }

    public InsertConflictTarget withIndexElements(List<KeyElement> elements) {
        setIndexElements(elements);
        return this;
    }

    /** A mutable view of the column keys; expression keys are available via getIndexElements(). */
    public List<String> getIndexColumnNames() {
        return new AbstractList<String>() {
            private int elementIndex(int index) {
                int columnIndex = 0;
                for (int i = 0; i < indexElements.size(); i++) {
                    if (!indexElements.get(i).isExpression() && columnIndex++ == index) {
                        return i;
                    }
                }
                throw new IndexOutOfBoundsException("Column index: " + index);
            }

            @Override
            public String get(int index) {
                return indexElements.get(elementIndex(index)).getColumnName();
            }

            @Override
            public int size() {
                return (int) indexElements.stream().filter(key -> !key.isExpression()).count();
            }

            @Override
            public String set(int index, String name) {
                return indexElements.set(elementIndex(index), new KeyElement(name))
                        .getColumnName();
            }

            @Override
            public void add(int index, String name) {
                indexElements.add(index == size() ? indexElements.size() : elementIndex(index),
                        new KeyElement(name));
            }

            @Override
            public boolean addAll(Collection<? extends String> names) {
                return addAll(size(), names);
            }

            @Override
            public boolean addAll(int index, Collection<? extends String> names) {
                int insertionIndex = index == size() ? indexElements.size() : elementIndex(index);
                List<KeyElement> additions = new ArrayList<>();
                for (String name : names) {
                    additions.add(new KeyElement(name));
                }
                return indexElements.addAll(insertionIndex, additions);
            }

            @Override
            public String remove(int index) {
                return indexElements.remove(elementIndex(index)).getColumnName();
            }
        };
    }

    @Deprecated
    public String getIndexColumnName() {
        return getIndexColumnName(0);
    }

    public String getIndexColumnName(int index) {
        List<String> names = getIndexColumnNames();
        return names.size() > index ? names.get(index) : null;
    }

    public boolean addIndexColumnName(String name) {
        indexElements.removeIf(KeyElement::isExpression);
        return indexElements.add(new KeyElement(name));
    }

    public InsertConflictTarget withIndexColumnName(String name) {
        addIndexColumnName(name);
        return this;
    }

    public boolean addAllIndexColumnNames(Collection<String> names) {
        indexElements.removeIf(KeyElement::isExpression);
        return getIndexColumnNames().addAll(names);
    }

    /** Returns the first expression key, or null for a column-only target. */
    public Expression getIndexExpression() {
        return indexElements.stream().filter(KeyElement::isExpression)
                .map(KeyElement::getExpression).findFirst().orElse(null);
    }

    public void setIndexExpression(Expression expression) {
        indexElements.clear();
        if (expression != null) {
            indexElements.add(new KeyElement(expression));
        }
    }

    public InsertConflictTarget withIndexExpression(Expression expression) {
        setIndexExpression(expression);
        return this;
    }

    public Expression getWhereExpression() {
        return whereExpression;
    }

    public void setWhereExpression(Expression whereExpression) {
        this.whereExpression = whereExpression;
    }

    public InsertConflictTarget withWhereExpression(Expression whereExpression) {
        setWhereExpression(whereExpression);
        return this;
    }

    public String getConstraintName() {
        return constraintName;
    }

    public void setConstraintName(String constraintName) {
        this.constraintName = constraintName;
    }

    public InsertConflictTarget withConstraintName(String constraintName) {
        setConstraintName(constraintName);
        return this;
    }

    /** Visits expression keys and the optional index predicate. */
    public <S> void accept(ExpressionVisitor<?> visitor, S context) {
        for (KeyElement element : indexElements) {
            if (element.getExpression() != null) {
                element.getExpression().accept(visitor, context);
            }
        }
        if (whereExpression != null) {
            whereExpression.accept(visitor, context);
        }
    }

    public StringBuilder appendTo(StringBuilder builder) {
        return appendTo(builder, expression -> builder.append(expression));
    }

    public StringBuilder appendTo(StringBuilder builder, Consumer<Expression> expressionPrinter) {
        if (constraintName != null) {
            return builder.append(" ON CONSTRAINT ").append(constraintName);
        }
        builder.append(" (");
        for (int i = 0; i < indexElements.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            indexElements.get(i).appendTo(builder, expressionPrinter);
        }
        builder.append(")");
        if (whereExpression != null) {
            builder.append(" WHERE ");
            expressionPrinter.accept(whereExpression);
        }
        return builder;
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }
}
