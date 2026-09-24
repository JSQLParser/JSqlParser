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
import java.util.function.Consumer;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Table;

public class CheckConstraint extends NamedConstraint {

    private Table table;

    private Expression expression;

    private boolean noInherit;

    public CheckConstraint() {
        setKind(Kind.CHECK);
    }

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

    /** Whether PostgreSQL should keep this CHECK from being inherited by child tables. */
    public boolean isNoInherit() {
        return noInherit;
    }

    public void setNoInherit(boolean noInherit) {
        this.noInherit = noInherit;
    }

    public CheckConstraint withNoInherit(boolean noInherit) {
        setNoInherit(noInherit);
        return this;
    }

    public Boolean getEnforced() {
        return getConstraintAttributes() == null ? null : getConstraintAttributes().getEnforced();
    }

    public void setEnforced(Boolean enforced) {
        ConstraintAttributes attributes = getConstraintAttributes();
        if (attributes == null) {
            if (enforced == null) {
                return;
            }
            attributes = new ConstraintAttributes();
            setConstraintAttributes(attributes);
        }
        attributes.setEnforced(enforced);
    }

    @Override
    public void appendTo(StringBuilder b, Consumer<Expression> expressionPrinter) {
        appendConstraintPrefixTo(b);
        b.append("CHECK (");
        if (expression == null) {
            b.append("null");
        } else {
            expressionPrinter.accept(expression);
        }
        b.append(')');
        if (noInherit) {
            b.append(" NO INHERIT");
        }
        appendConstraintSuffixTo(b);
        appendConstraintAttributesTo(b);
    }

    public CheckConstraint withTable(Table table) {
        this.setTable(table);
        return this;
    }

    public CheckConstraint withExpression(Expression expression) {
        this.setExpression(expression);
        return this;
    }

    public CheckConstraint withEnforced(Boolean enforced) {
        setEnforced(enforced);
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
    public CheckConstraint withUseConstraintKeyword(boolean useConstraintKeyword) {
        return (CheckConstraint) super.withUseConstraintKeyword(useConstraintKeyword);
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

    @Override
    public CheckConstraint withIndexKeyword(String indexKeyword) {
        return (CheckConstraint) super.withIndexKeyword(indexKeyword);
    }
}
