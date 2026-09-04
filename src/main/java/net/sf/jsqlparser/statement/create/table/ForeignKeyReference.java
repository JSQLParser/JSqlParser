/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.table;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.ReferentialAction;
import net.sf.jsqlparser.statement.ReferentialAction.Action;
import net.sf.jsqlparser.statement.ReferentialAction.Type;
import net.sf.jsqlparser.statement.select.PlainSelect;

/** The target and actions of a column- or table-level foreign-key reference. */
public class ForeignKeyReference implements Serializable {

    public enum MatchType {
        FULL, PARTIAL, SIMPLE
    }

    private Table table;
    private List<String> referencedColumnNames;
    private MatchType matchType;
    private final Set<ReferentialAction> referentialActions = new LinkedHashSet<>(2);

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

    public MatchType getMatchType() {
        return matchType;
    }

    public void setMatchType(MatchType matchType) {
        this.matchType = matchType;
    }

    public Set<ReferentialAction> getReferentialActions() {
        return referentialActions;
    }

    public ReferentialAction getReferentialAction(Type type) {
        return referentialActions.stream().filter(action -> type.equals(action.getType()))
                .findFirst()
                .orElse(null);
    }

    public void setReferentialAction(Type type, Action action) {
        ReferentialAction current = getReferentialAction(type);
        if (current == null) {
            referentialActions.add(new ReferentialAction(type, action));
        } else {
            current.setAction(action);
        }
    }

    public void removeReferentialAction(Type type) {
        ReferentialAction current = getReferentialAction(type);
        if (current != null) {
            referentialActions.remove(current);
        }
    }

    public ForeignKeyReference withTable(Table table) {
        setTable(table);
        return this;
    }

    public ForeignKeyReference withReferencedColumnNames(List<String> referencedColumnNames) {
        setReferencedColumnNames(referencedColumnNames);
        return this;
    }

    public ForeignKeyReference withMatchType(MatchType matchType) {
        setMatchType(matchType);
        return this;
    }

    public ForeignKeyReference withReferentialAction(Type type, Action action) {
        setReferentialAction(type, action);
        return this;
    }

    public ForeignKeyReference addReferencedColumnNames(String... referencedColumnNames) {
        List<String> collection = Optional.ofNullable(getReferencedColumnNames())
                .orElseGet(ArrayList::new);
        Collections.addAll(collection, referencedColumnNames);
        return withReferencedColumnNames(collection);
    }

    public ForeignKeyReference addReferencedColumnNames(
            Collection<String> referencedColumnNames) {
        List<String> collection = Optional.ofNullable(getReferencedColumnNames())
                .orElseGet(ArrayList::new);
        collection.addAll(referencedColumnNames);
        return withReferencedColumnNames(collection);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("REFERENCES ").append(table);
        if (referencedColumnNames != null) {
            builder.append(PlainSelect.getStringList(referencedColumnNames, true, true));
        }
        if (matchType != null) {
            builder.append(" MATCH ").append(matchType);
        }
        referentialActions.forEach(builder::append);
        return builder.toString();
    }
}
