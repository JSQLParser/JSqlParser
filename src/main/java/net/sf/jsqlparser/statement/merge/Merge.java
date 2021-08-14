/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.merge;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.OracleHint;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.DMLStatement;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.select.WithItem;

public class Merge extends DMLStatement {

    private List<WithItem> withItemsList;
    private Table table;
    private OracleHint oracleHint = null;
    private Table usingTable;
    private SubSelect usingSelect;
    private Alias usingAlias;
    private Expression onCondition;
    private MergeInsert mergeInsert;
    private MergeUpdate mergeUpdate;
    private boolean insertFirst = false;

    public List<WithItem> getWithItemsList() {
        return withItemsList;
    }

    public void setWithItemsList(List<WithItem> withItemsList) {
        this.withItemsList = withItemsList;
    }

    public Merge withWithItemsList(List<WithItem> withItemsList) {
        this.setWithItemsList(withItemsList);
        return this;
    }

    public Merge addWithItemsList(WithItem... withItemsList) {
        List<WithItem> collection = Optional.ofNullable(getWithItemsList()).orElseGet(ArrayList::new);
        Collections.addAll(collection, withItemsList);
        return this.withWithItemsList(collection);
    }

    public Merge addWithItemsList(Collection<? extends WithItem> withItemsList) {
        List<WithItem> collection = Optional.ofNullable(getWithItemsList()).orElseGet(ArrayList::new);
        collection.addAll(withItemsList);
        return this.withWithItemsList(collection);
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table name) {
        table = name;
    }

    public OracleHint getOracleHint() {
        return oracleHint;
    }

    public void setOracleHint(OracleHint oracleHint) {
        this.oracleHint = oracleHint;
    }

    public Table getUsingTable() {
        return usingTable;
    }

    public void setUsingTable(Table usingTable) {
        this.usingTable = usingTable;
    }

    public SubSelect getUsingSelect() {
        return usingSelect;
    }

    public void setUsingSelect(SubSelect usingSelect) {
        this.usingSelect = usingSelect;
        if (this.usingSelect != null) {
            this.usingSelect.setUseBrackets(false);
        }
    }

    public Alias getUsingAlias() {
        return usingAlias;
    }

    public void setUsingAlias(Alias usingAlias) {
        this.usingAlias = usingAlias;
    }

    public Expression getOnCondition() {
        return onCondition;
    }

    public void setOnCondition(Expression onCondition) {
        this.onCondition = onCondition;
    }

    public MergeInsert getMergeInsert() {
        return mergeInsert;
    }

    public void setMergeInsert(MergeInsert insert) {
        this.mergeInsert = insert;
    }

    public MergeUpdate getMergeUpdate() {
        return mergeUpdate;
    }

    public void setMergeUpdate(MergeUpdate mergeUpdate) {
        this.mergeUpdate = mergeUpdate;
    }

    @Override
    public void accept(StatementVisitor statementVisitor) {
        statementVisitor.visit(this);
    }

    public boolean isInsertFirst() {
        return insertFirst;
    }

    public void setInsertFirst(boolean insertFirst) {
        this.insertFirst = insertFirst;
    }

    @Override
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPathComplexity"})
    public StringBuilder appendTo(StringBuilder builder) {
        if (withItemsList != null && !withItemsList.isEmpty()) {
            builder.append("WITH ");
            for (Iterator<WithItem> iter = withItemsList.iterator(); iter.hasNext();) {
                WithItem withItem = iter.next();
                builder.append(withItem);
                if (iter.hasNext()) {
                    builder.append(",");
                }
                builder.append(" ");
            }
        }
        builder.append("MERGE INTO ");
        builder.append(table);
        builder.append(" USING ");
        if (usingTable != null) {
            builder.append(usingTable);
        } else if (usingSelect != null) {
            builder.append("(").append(usingSelect).append(")");
        }

        if (usingAlias != null) {
            builder.append(usingAlias);
        }
        builder.append(" ON (");
        builder.append(onCondition);
        builder.append(")");

        if (insertFirst && mergeInsert != null) {
            builder.append(mergeInsert);
        }

        if (mergeUpdate != null) {
            builder.append(mergeUpdate);
        }

        if (!insertFirst && mergeInsert != null) {
            builder.append(mergeInsert);
        }
        return builder;
    }

    public Merge withUsingTable(Table usingTable) {
        this.setUsingTable(usingTable);
        return this;
    }

    public Merge withUsingSelect(SubSelect usingSelect) {
        this.setUsingSelect(usingSelect);
        return this;
    }

    public Merge withUsingAlias(Alias usingAlias) {
        this.setUsingAlias(usingAlias);
        return this;
    }

    public Merge withOnCondition(Expression onCondition) {
        this.setOnCondition(onCondition);
        return this;
    }

    public Merge withMergeUpdate(MergeUpdate mergeUpdate) {
        this.setMergeUpdate(mergeUpdate);
        return this;
    }

    public Merge withInsertFirst(boolean insertFirst) {
        this.setInsertFirst(insertFirst);
        return this;
    }

    public Merge withTable(Table table) {
        this.setTable(table);
        return this;
    }

    public Merge withMergeInsert(MergeInsert mergeInsert) {
        this.setMergeInsert(mergeInsert);
        return this;
    }

    public <E extends Expression> E getOnCondition(Class<E> type) {
        return type.cast(getOnCondition());
    }
}
