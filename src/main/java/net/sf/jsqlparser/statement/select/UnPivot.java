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

import net.sf.jsqlparser.schema.Column;

import java.util.List;

public class UnPivot {

    private boolean includeNulls = false;
    private boolean includeNullsSpecified = false;
    private Column unpivotClause;
    private List<Column> unpivotForClause;
    private List<SelectExpressionItem> unpivotInClause;

    public void accept(PivotVisitor pivotVisitor) {
        pivotVisitor.visit(this);
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

    public Column getUnPivotClause() {
        return unpivotClause;
    }

    public void setUnPivotClause(Column unpivotClause) {
        this.unpivotClause = unpivotClause;
    }

    public List<Column> getUnPivotForClause() {
        return unpivotForClause;
    }

    public void setUnPivotForClause(List<Column> forColumns) {
        this.unpivotForClause = forColumns;
    }

    public List<?> getUnPivotInClause() {
        return unpivotInClause;
    }

    public void setUnPivotInClause(List<SelectExpressionItem> unpivotInClause) {
        this.unpivotInClause = unpivotInClause;
    }

    @Override
    public String toString() {
        return "UNPIVOT"
                + (includeNullsSpecified && includeNulls ? " INCLUDE NULLS" : "")
                + (includeNullsSpecified && !includeNulls ? " EXCLUDE NULLS" : "")
                + " (" + unpivotClause
                + " FOR " + PlainSelect.getStringList(unpivotForClause, true, unpivotForClause != null && unpivotForClause.size() > 1)
                + " IN " + PlainSelect.getStringList(unpivotInClause, true, true) + ")";
    }

    public UnPivot withIncludeNulls(boolean includeNulls) {
        this.setIncludeNulls(includeNulls);
        return this;
    }
}
