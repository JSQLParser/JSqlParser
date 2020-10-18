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

import java.util.Collection;
import java.util.List;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Table;

public class CheckConstraint extends NamedConstraint {

    private Table table;

    private Expression expression;

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public Expression getExpression() {
        return expression;
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    @Override
    public String toString() {
        return "CONSTRAINT " + getName() + " CHECK (" + expression + ")";
    }

    public CheckConstraint withTable(Table table) {
        this.setTable(table);
        return this;
    }

    public CheckConstraint withExpression(Expression expression) {
        this.setExpression(expression);
        return this;
    }

    public <E extends Expression> E getExpression(Class<E> type) {
        return type.cast(getExpression());
    }

    @Override
    public CheckConstraint withType(String type) {
        return (CheckConstraint) super.withType(type);
    }

    @Override
    public CheckConstraint withUsing(String using) {
        return (CheckConstraint) super.withUsing(using);
    }

    @Override
    public CheckConstraint withName(List<String> name) {
        return (CheckConstraint) super.withName(name);
    }

    @Override
    public CheckConstraint withName(String name) {
        return (CheckConstraint) super.withName(name);
    }

    @Override
    public CheckConstraint withColumnsNames(List<String> list) {
        return (CheckConstraint) super.withColumnsNames(list);
    }

    @Override
    public CheckConstraint withColumns(List<ColumnParams> columns) {
        return (CheckConstraint) super.withColumns(columns);
    }

    @Override
    public CheckConstraint addColumns(ColumnParams... functionDeclarationParts) {
        return (CheckConstraint) super.addColumns(functionDeclarationParts);
    }

    @Override
    public CheckConstraint addColumns(Collection<? extends ColumnParams> functionDeclarationParts) {
        return (CheckConstraint) super.addColumns(functionDeclarationParts);
    }

    @Override
    public CheckConstraint withIndexSpec(List<String> idxSpec) {
        return (CheckConstraint) super.withIndexSpec(idxSpec);
    }

}
