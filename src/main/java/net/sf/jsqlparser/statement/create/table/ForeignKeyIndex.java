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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.ReferentialAction;
import net.sf.jsqlparser.statement.ReferentialAction.Action;
import net.sf.jsqlparser.statement.ReferentialAction.Type;
import net.sf.jsqlparser.statement.select.PlainSelect;

public class ForeignKeyIndex extends NamedConstraint {

    private Table table;
    private List<String> referencedColumnNames;
    private Set<ReferentialAction> referentialActions = new LinkedHashSet<>(2);

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

    /**
     * @param type
     * @param action
     */
    public void setReferentialAction(Type type, Action action) {
        setReferentialAction(type, action, true);
    }

    /**
     * @param type
     */
    public void removeReferentialAction(Type type) {
        setReferentialAction(type, null, false);
    }

    /**
     * @param type
     * @return
     */
    public ReferentialAction getReferentialAction(Type type) {
        return referentialActions.stream().filter(ra -> type.equals(ra.getType())).findFirst().orElse(null);
    }

    private void setReferentialAction(Type type, Action action, boolean set) {
        ReferentialAction found = getReferentialAction(type);
        if (set) {
            if (found == null) {
                referentialActions.add(new ReferentialAction(type, action));
            } else {
                found.setAction(action);
            }
        } else if (found != null) {
            referentialActions.remove(found);
        }
    }

    @Deprecated
    public String getOnDeleteReferenceOption() {
        ReferentialAction a = getReferentialAction(Type.DELETE);
        return a == null ? null : a.getAction().getAction();
    }

    @Deprecated
    public void setOnDeleteReferenceOption(String onDeleteReferenceOption) {
        if (onDeleteReferenceOption == null) {
            removeReferentialAction(Type.DELETE);
        } else {
            setReferentialAction(Type.DELETE, Action.byAction(onDeleteReferenceOption));
        }
    }

    @Deprecated
    public String getOnUpdateReferenceOption() {
        ReferentialAction a = getReferentialAction(Type.UPDATE);
        return a == null ? null : a.getAction().getAction();
    }

    @Deprecated
    public void setOnUpdateReferenceOption(String onUpdateReferenceOption) {
        if (onUpdateReferenceOption == null) {
            removeReferentialAction(Type.UPDATE);
        } else {
            setReferentialAction(Type.UPDATE, Action.byAction(onUpdateReferenceOption));
        }
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder(super.toString()).append(" REFERENCES ").append(table)
                .append(PlainSelect.getStringList(getReferencedColumnNames(), true, true));
        referentialActions.forEach(b::append);
        return b.toString();
    }
}
