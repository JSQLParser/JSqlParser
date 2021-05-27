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

import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.OracleHint;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.select.SubSelect;

public class Merge implements Statement {

    private Table table;
    private OracleHint oracleHint = null;
    private Table usingTable;
    private SubSelect usingSelect;
    private Alias usingAlias;
    private Expression onCondition;
    private MergeInsert mergeInsert;
    private MergeUpdate mergeUpdate;
    private boolean insertFirst = false;

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
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("MERGE INTO ");
        b.append(table);
        b.append(" USING ");
        if (usingTable != null) {
            b.append(usingTable.toString());
        } else if (usingSelect != null) {
            b.append("(").append(usingSelect.toString()).append(")");
        }

        if (usingAlias != null) {
            b.append(usingAlias.toString());
        }
        b.append(" ON (");
        b.append(onCondition);
        b.append(")");

        if (insertFirst && mergeInsert != null) {
            b.append(mergeInsert.toString());
        }

        if (mergeUpdate != null) {
            b.append(mergeUpdate.toString());
        }

        if (!insertFirst && mergeInsert != null) {
            b.append(mergeInsert.toString());
        }

        return b.toString();
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
