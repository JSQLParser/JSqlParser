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
import net.sf.jsqlparser.schema.Column;

import java.io.Serializable;
import java.util.List;

public class UnPivot implements Serializable {

    private boolean includeNulls = false;
    private boolean includeNullsSpecified = false;
    private List<Column> unpivotClause;
    private List<Column> unpivotForClause;
    private List<SelectItem> unpivotInClause;
    private Alias alias;

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

    public List<Column> getUnPivotClause() {
        return unpivotClause;
    }

    public void setUnPivotClause(List<Column> unpivotClause) {
        this.unpivotClause = unpivotClause;
    }

    public List<Column> getUnPivotForClause() {
        return unpivotForClause;
    }

    public void setUnPivotForClause(List<Column> forColumns) {
        this.unpivotForClause = forColumns;
    }

    public List<SelectItem> getUnPivotInClause() {
        return unpivotInClause;
    }

    public void setUnPivotInClause(List<SelectItem> unpivotInClause) {
        this.unpivotInClause = unpivotInClause;
    }

    @Override
    public String toString() {
        return "UNPIVOT"
                + (includeNullsSpecified && includeNulls ? " INCLUDE NULLS" : "")
                + (includeNullsSpecified && !includeNulls ? " EXCLUDE NULLS" : "")
                + " ("
                + PlainSelect.getStringList(unpivotClause, true,
                        unpivotClause != null && unpivotClause.size() > 1)
                + " FOR "
                + PlainSelect.getStringList(unpivotForClause, true,
                        unpivotForClause != null && unpivotForClause.size() > 1)
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
