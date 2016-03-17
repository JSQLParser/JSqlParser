/*
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2015 JSQLParser
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
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

        if (mergeUpdate != null) {
            b.append(mergeUpdate.toString());
        }

        if (mergeInsert != null) {
            b.append(mergeInsert.toString());
        }

        return b.toString();
    }
}
