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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import net.sf.jsqlparser.expression.Expression;

/**
 * An exclusion constraint with key elements, supporting-index options and an optional predicate.
 */
public class ExcludeConstraint extends NamedConstraint
        implements KeyColumnSource, IndexOptionSource {
    private Expression expression;
    private List<KeyElement> columns;
    private IndexOptions indexOptions = new IndexOptions();

    public ExcludeConstraint() {
        setType("EXCLUDE");
    }

    @Override
    public ConstraintKind getKind() {
        return ConstraintKind.EXCLUDE;
    }

    public Expression getExpression() {
        return expression;
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    public <E extends Expression> E getExpression(Class<E> type) {
        return type.cast(expression);
    }

    public ExcludeConstraint withExpression(Expression expression) {
        setExpression(expression);
        return this;
    }

    @Override
    public List<KeyElement> getColumns() {
        return columns;
    }

    @Override
    public void setColumns(List<KeyElement> columns) {
        this.columns = columns;
    }

    @Override
    public IndexOptions getIndexOptions() {
        return indexOptions;
    }

    public void setIndexOptions(IndexOptions options) {
        indexOptions = Objects.requireNonNull(options, "indexOptions");
    }

    public ExcludeConstraint withColumns(List<KeyElement> columns) {
        setColumns(columns);
        return this;
    }

    public ExcludeConstraint withColumnsNames(List<String> columns) {
        setColumnsNames(columns);
        return this;
    }

    public ExcludeConstraint addColumns(KeyElement... values) {
        return addColumns(java.util.Arrays.asList(values));
    }

    public ExcludeConstraint addColumns(Collection<? extends KeyElement> values) {
        if (columns == null) {
            columns = new ArrayList<>();
        }
        columns.addAll(values);
        return this;
    }

    public ExcludeConstraint withUsing(String using) {
        setUsing(using);
        return this;
    }

    public ExcludeConstraint withIndexSpec(List<String> options) {
        setIndexSpec(options);
        return this;
    }

    public ExcludeConstraint withClustering(IndexOptions.Clustering clustering) {
        setClustering(clustering);
        return this;
    }

    @Override
    public ExcludeConstraint withType(String type) {
        setType(type);
        return this;
    }

    @Override
    public ExcludeConstraint withName(String name) {
        setName(name);
        return this;
    }

    @Override
    public ExcludeConstraint withName(List<String> name) {
        setName(name);
        return this;
    }

    @Override
    public ExcludeConstraint withUseConstraintKeyword(boolean value) {
        setUseConstraintKeyword(value);
        return this;
    }

    @Override
    public void appendTo(StringBuilder sql, Consumer<Expression> expressionPrinter) {
        appendConstraintPrefixTo(sql);
        sql.append("EXCLUDE");
        if (getUsing() != null) {
            sql.append(" USING ").append(getUsing());
        }
        if (columns != null) {
            sql.append(' ');
            appendColumnsTo(sql, expressionPrinter);
        }
        appendIndexOptionsTo(sql, expressionPrinter);
        if (expression != null) {
            sql.append(" WHERE (");
            expressionPrinter.accept(expression);
            sql.append(')');
        }
        appendConstraintSuffixTo(sql);
        appendConstraintAttributesTo(sql);
    }
}
