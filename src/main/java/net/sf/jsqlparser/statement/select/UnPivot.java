/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Column;

import java.io.Serializable;
import java.util.List;

public class UnPivot implements Serializable {

    private boolean includeNulls = false;
    private boolean includeNullsSpecified = false;
    private ExpressionList<Column> unpivotClause;
    private ExpressionList<Column> unpivotForClause;
    private List<SelectItem<?>> unpivotInClause;
    private Alias alias;

    public <T, S> T accept(PivotVisitor<T> pivotVisitor, S context) {
        return pivotVisitor.visit(this, context);
    }

    public boolean getIncludeNulls() {
        return includeNulls;
    }

    public void setIncludeNulls(boolean includeNulls) {
        this.includeNullsSpecified = true;
        this.includeNulls = includeNulls;
    }

    public boolean getIncludeNullsSpecified() {
        return includeNullsSpecified;
    }

    public List<Column> getUnPivotClause() {
        return unpivotClause;
    }

    public void setUnPivotClause(ExpressionList<Column> unpivotClause) {
        this.unpivotClause = unpivotClause;
    }

    public List<Column> getUnPivotForClause() {
        return unpivotForClause;
    }

    public void setUnPivotForClause(ExpressionList<Column> forColumns) {
        this.unpivotForClause = forColumns;
    }

    public List<SelectItem<?>> getUnPivotInClause() {
        return unpivotInClause;
    }

    public void setUnPivotInClause(List<SelectItem<?>> unpivotInClause) {
        this.unpivotInClause = unpivotInClause;
    }

    @Override
    public String toString() {
        return "UNPIVOT"
                + (includeNullsSpecified && includeNulls ? " INCLUDE NULLS" : "")
                + (includeNullsSpecified && !includeNulls ? " EXCLUDE NULLS" : "")
                + " ("
                + unpivotClause.toString()
                + " FOR "
                + unpivotForClause.toString()
                + " IN " + PlainSelect.getStringList(unpivotInClause, true, true) + ")"
                + (alias != null ? alias.toString() : "");
    }

    public UnPivot withIncludeNulls(boolean includeNulls) {
        this.setIncludeNulls(includeNulls);
        return this;
    }

    public Alias getAlias() {
        return alias;
    }

    public void setAlias(Alias alias) {
        this.alias = alias;
    }
}
