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
import net.sf.jsqlparser.statement.OutputClause;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.ParenthesedSelect;
import net.sf.jsqlparser.statement.select.WithItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class Merge implements Statement {

    private List<WithItem> withItemsList;
    private Table table;
    private OracleHint oracleHint = null;
    private FromItem fromItem;
    private Expression onCondition;
    private List<MergeOperation> operations;

    private OutputClause outputClause;

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
        List<WithItem> collection =
                Optional.ofNullable(getWithItemsList()).orElseGet(ArrayList::new);
        Collections.addAll(collection, withItemsList);
        return this.withWithItemsList(collection);
    }

    public Merge addWithItemsList(Collection<? extends WithItem> withItemsList) {
        List<WithItem> collection =
                Optional.ofNullable(getWithItemsList()).orElseGet(ArrayList::new);
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

    @Deprecated
    public Table getUsingTable() {
        return fromItem instanceof Table ? (Table) fromItem : null;
    }

    @Deprecated
    public void setUsingTable(Table usingTable) {
        this.fromItem = usingTable;
    }

    @Deprecated
    public void setUsingSelect(ParenthesedSelect usingSelect) {
        this.fromItem = usingSelect;
    }

    @Deprecated
    public Alias getUsingAlias() {
        return fromItem.getAlias();
    }

    @Deprecated
    public void setUsingAlias(Alias usingAlias) {
        this.fromItem.setAlias(usingAlias);
    }

    public FromItem getFromItem() {
        return fromItem;
    }

    public void setFromItem(FromItem fromItem) {
        this.fromItem = fromItem;
    }

    public Merge withFromItem(FromItem fromItem) {
        this.setFromItem(fromItem);
        return this;
    }

    public Expression getOnCondition() {
        return onCondition;
    }

    public void setOnCondition(Expression onCondition) {
        this.onCondition = onCondition;
    }

    public List<MergeOperation> getOperations() {
        return operations;
    }

    public void setOperations(List<MergeOperation> operations) {
        this.operations = operations;
    }

    /**
     * @deprecated use {@link #getOperations()} or consider a {@link MergeOperationVisitor} instead
     */
    @Deprecated
    public MergeInsert getMergeInsert() {
        return operations.stream()
                .filter(MergeInsert.class::isInstance)
                .findFirst()
                .map(MergeInsert.class::cast)
                .orElse(null);
    }

    /**
     * @deprecated use {@link #getOperations()} or consider a {@link MergeOperationVisitor} instead
     */
    @Deprecated
    public MergeUpdate getMergeUpdate() {
        return operations.stream()
                .filter(MergeUpdate.class::isInstance)
                .findFirst()
                .map(MergeUpdate.class::cast)
                .orElse(null);
    }

    @Override
    public void accept(StatementVisitor statementVisitor) {
        statementVisitor.visit(this);
    }

    /**
     * @deprecated use {@link #getOperations()} or consider a {@link MergeOperationVisitor} instead
     */
    @Deprecated
    public boolean isInsertFirst() {
        if (operations == null || operations.isEmpty()) {
            return false;
        }
        return operations.get(0) instanceof MergeInsert;
    }

    public OutputClause getOutputClause() {
        return outputClause;
    }

    public Merge setOutputClause(OutputClause outputClause) {
        this.outputClause = outputClause;
        return this;
    }

    @Override
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPathComplexity"})
    public String toString() {
        StringBuilder b = new StringBuilder();
        if (withItemsList != null && !withItemsList.isEmpty()) {
            b.append("WITH ");
            for (Iterator<WithItem> iter = withItemsList.iterator(); iter.hasNext();) {
                WithItem withItem = iter.next();
                b.append(withItem);
                if (iter.hasNext()) {
                    b.append(",");
                }
                b.append(" ");
            }
        }
        b.append("MERGE ");
        if (oracleHint != null) {
            b.append(oracleHint).append(" ");
        }
        b.append("INTO ");
        b.append(table);
        b.append(" USING ");
        b.append(fromItem);
        b.append(" ON ");
        b.append(onCondition);

        if (operations != null && !operations.isEmpty()) {
            operations.forEach(b::append);
        }

        if (outputClause != null) {
            b.append(outputClause);
        }

        return b.toString();
    }

    @Deprecated
    public Merge withUsingTable(Table usingTable) {
        this.setUsingTable(usingTable);
        return this;
    }

    @Deprecated
    public Merge withUsingSelect(ParenthesedSelect usingSelect) {
        this.setUsingSelect(usingSelect);
        return this;
    }

    @Deprecated
    public Merge withUsingAlias(Alias usingAlias) {
        this.setUsingAlias(usingAlias);
        return this;
    }

    public Merge withOnCondition(Expression onCondition) {
        this.setOnCondition(onCondition);
        return this;
    }

    public Merge withTable(Table table) {
        this.setTable(table);
        return this;
    }

    public <E extends Expression> E getOnCondition(Class<E> type) {
        return type.cast(getOnCondition());
    }
}
