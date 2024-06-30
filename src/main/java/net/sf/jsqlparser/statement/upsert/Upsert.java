/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.upsert;

import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.select.Values;
import net.sf.jsqlparser.statement.update.UpdateSet;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class Upsert implements Statement {

    private Table table;
    private ExpressionList<Column> columns;
    private ExpressionList<?> expressions;
    private Select select;
    private List<UpdateSet> updateSets;
    private List<UpdateSet> duplicateUpdateSets;
    private UpsertType upsertType = UpsertType.UPSERT;
    private boolean isUsingInto;

    public List<UpdateSet> getUpdateSets() {
        return updateSets;
    }

    public Upsert setUpdateSets(List<UpdateSet> updateSets) {
        this.updateSets = updateSets;
        return this;
    }

    public List<UpdateSet> getDuplicateUpdateSets() {
        return duplicateUpdateSets;
    }

    public Upsert setDuplicateUpdateSets(List<UpdateSet> duplicateUpdateSets) {
        this.duplicateUpdateSets = duplicateUpdateSets;
        return this;
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }

    public UpsertType getUpsertType() {
        return upsertType;
    }

    public void setUpsertType(UpsertType upsertType) {
        this.upsertType = upsertType;
    }

    public Upsert withUpsertType(UpsertType upsertType) {
        setUpsertType(upsertType);
        return this;
    }

    public boolean isUsingInto() {
        return isUsingInto;
    }

    public void setUsingInto(boolean useInto) {
        this.isUsingInto = useInto;
    }

    public Upsert withUsingInto(boolean useInto) {
        setUsingInto(useInto);
        return this;
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table name) {
        table = name;
    }

    public ExpressionList<Column> getColumns() {
        return columns;
    }

    public void setColumns(ExpressionList<Column> list) {
        columns = list;
    }

    public ExpressionList getExpressions() {
        return expressions;
    }

    public void setExpressions(ExpressionList list) {
        expressions = list;
    }

    @Deprecated
    public ExpressionList<?> getSetExpressions() {
        return expressions;
    }

    public Select getSelect() {
        return select;
    }

    public void setSelect(Select select) {
        this.select = select;
    }

    public Values getValues() {
        return select.getValues();
    }

    public PlainSelect getPlainSelect() {
        return select.getPlainSelect();
    }

    public SetOperationList getSetOperationList() {
        return select.getSetOperationList();
    }

    @Override
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPathComplexity"})
    public String toString() {
        StringBuilder sb = new StringBuilder();

        switch (upsertType) {
            case REPLACE:
            case REPLACE_SET:
                sb.append("REPLACE ");
                break;
            case INSERT_OR_ABORT:
                sb.append("INSERT OR ABORT ");
                break;
            case INSERT_OR_FAIL:
                sb.append("INSERT OR FAIL ");
                break;
            case INSERT_OR_IGNORE:
                sb.append("INSERT OR IGNORE ");
                break;
            case INSERT_OR_REPLACE:
                sb.append("INSERT OR REPLACE ");
                break;
            case INSERT_OR_ROLLBACK:
                sb.append("INSERT OR ROLLBACK ");
                break;
            case UPSERT:
            default:
                sb.append("UPSERT ");
        }

        if (isUsingInto) {
            sb.append("INTO ");
        }
        sb.append(table).append(" ");

        if (updateSets != null) {
            sb.append("SET ");
            UpdateSet.appendUpdateSetsTo(sb, updateSets);
        } else {
            if (columns != null) {
                sb.append(columns).append(" ");
            }
            if (select != null) {
                sb.append(select);
            }
        }

        if (duplicateUpdateSets != null) {
            sb.append(" ON DUPLICATE KEY UPDATE ");
            UpdateSet.appendUpdateSetsTo(sb, duplicateUpdateSets);
        }

        return sb.toString();
    }

    public Upsert withSelect(Select select) {
        this.setSelect(select);
        return this;
    }

    public Upsert withTable(Table table) {
        this.setTable(table);
        return this;
    }

    public Upsert withColumns(ExpressionList<Column> columns) {
        this.setColumns(columns);
        return this;
    }

    public Upsert withExpressions(ExpressionList expressions) {
        this.setExpressions(expressions);
        return this;
    }

    public Upsert addColumns(Column... columns) {
        return this.addColumns(Arrays.asList(columns));
    }

    public Upsert addColumns(Collection<? extends Column> columns) {
        ExpressionList<Column> collection =
                Optional.ofNullable(getColumns()).orElseGet(ExpressionList::new);
        collection.addAll(columns);
        return this.withColumns(collection);
    }
}
