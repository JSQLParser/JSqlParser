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

import java.util.List;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.ReferentialAction;
import net.sf.jsqlparser.statement.select.PlainSelect;

public class ForeignKeyIndex extends NamedConstraint {

    private Table table;
    private List<String> referencedColumnNames;
    private ReferentialAction onDeleteReferentialAction;
    private ReferentialAction onUpdateReferentialAction;

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public List<String> getReferencedColumnNames() {
        return referencedColumnNames;
    }

    public void setReferencedColumnNames(List<String> referencedColumnNames) {
        this.referencedColumnNames = referencedColumnNames;
    }

    public ReferentialAction getOnDeleteReferentialAction() {
        return onDeleteReferentialAction;
    }

    public void setOnDeleteReferentialAction(ReferentialAction onDeleteReferentialAction) {
        this.onDeleteReferentialAction = onDeleteReferentialAction;
    }

    public ReferentialAction getOnUpdateReferentialAction() {
        return onUpdateReferentialAction;
    }

    public void setOnUpdateReferentialAction(ReferentialAction onUpdateReferentialAction) {
        this.onUpdateReferentialAction = onUpdateReferentialAction;
    }

    @Deprecated
    public String getOnDeleteReferenceOption() {
        return onDeleteReferentialAction == null ? null : onDeleteReferentialAction.getAction();
    }

    @Deprecated
    public void setOnDeleteReferenceOption(String onDeleteReferenceOption) {
        setOnDeleteReferentialAction(
                onDeleteReferenceOption == null ? null : ReferentialAction.byAction(onDeleteReferenceOption));
    }

    @Deprecated
    public String getOnUpdateReferenceOption() {
        return onUpdateReferentialAction == null ? null : onUpdateReferentialAction.getAction();
    }

    @Deprecated
    public void setOnUpdateReferenceOption(String onUpdateReferenceOption) {
        setOnUpdateReferentialAction(
                onUpdateReferenceOption == null ? null : ReferentialAction.byAction(onUpdateReferenceOption));
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder(super.toString());
        if (onDeleteReferentialAction != null) {
            b.append(" ON DELETE ").append(onDeleteReferentialAction.getAction());
        }
        if (onUpdateReferentialAction != null) {
            b.append(" ON UPDATE ").append(onUpdateReferentialAction.getAction());
        }
        return b.append(" REFERENCES ").append(table)
                .append(PlainSelect.getStringList(getReferencedColumnNames(), true, true)).toString();
    }
}
