/*
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2015 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.merge;

import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.select.SubSelect;

/**
 * Merge - statement
 *
 * @author tw
 */
public class Merge implements Statement {

    private Table table;
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

        if (insertFirst) {
            if (mergeInsert != null) {
                b.append(mergeInsert.toString());
            }
        }

        if (mergeUpdate != null) {
            b.append(mergeUpdate.toString());
        }

        if (!insertFirst) {
            if (mergeInsert != null) {
                b.append(mergeInsert.toString());
            }
        }

        return b.toString();
    }
}
