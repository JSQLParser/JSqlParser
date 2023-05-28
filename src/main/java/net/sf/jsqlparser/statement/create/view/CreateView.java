/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;

public class CreateView implements Statement {

    private Table view;

    private Select select;

    private boolean orReplace = false;

    private List<String> columnNames = null;

    private boolean materialized = false;

    private ForceOption force = ForceOption.NONE;

    private TemporaryOption temp = TemporaryOption.NONE;

    private AutoRefreshOption autoRefresh = AutoRefreshOption.NONE;

    private boolean withReadOnly = false;

    private boolean ifNotExists = false;

    @Override
    public void accept(StatementVisitor statementVisitor) {
        statementVisitor.visit(this);
    }

    public Table getView() {
        return view;
    }

    public void setView(Table view) {
        this.view = view;
    }

    public boolean isOrReplace() {
        return orReplace;
    }

    /**
     * @param orReplace was "OR REPLACE" specified?
     */
    public void setOrReplace(boolean orReplace) {
        this.orReplace = orReplace;
    }

    public Select getSelect() {
        return select;
    }

    public void setSelect(Select select) {
        this.select = select;
    }

    public List<String> getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(List<String> columnNames) {
        this.columnNames = columnNames;
    }

    public boolean isMaterialized() {
        return materialized;
    }

    public void setMaterialized(boolean materialized) {
        this.materialized = materialized;
    }

    public ForceOption getForce() {
        return force;
    }

    public void setForce(ForceOption force) {
        this.force = force;
    }

    public TemporaryOption getTemporary() {
        return temp;
    }

    public void setTemporary(TemporaryOption temp) {
        this.temp = temp;
    }

    public AutoRefreshOption getAutoRefresh() {
        return autoRefresh;
    }

    public void setAutoRefresh(AutoRefreshOption autoRefresh) {
        this.autoRefresh = autoRefresh;
    }

    public boolean isWithReadOnly() {
        return withReadOnly;
    }

    public void setWithReadOnly(boolean withReadOnly) {
        this.withReadOnly = withReadOnly;
    }

    public boolean isIfNotExists() {
        return ifNotExists;
    }

    public void setIfNotExists(boolean ifNotExists) {
        this.ifNotExists = ifNotExists;
    }

    @Override
    public String toString() {
        StringBuilder sql = new StringBuilder("CREATE ");
        if (isOrReplace()) {
            sql.append("OR REPLACE ");
        }
        appendForceOptionIfApplicable(sql);
        if (temp != TemporaryOption.NONE) {
            sql.append(temp.name()).append(" ");
        }
        if (isMaterialized()) {
            sql.append("MATERIALIZED ");
        }
        sql.append("VIEW ");
        sql.append(view);
        if (ifNotExists) {
            sql.append(" IF NOT EXISTS");
        }
        if (autoRefresh != AutoRefreshOption.NONE) {
            sql.append(" AUTO REFRESH ").append(autoRefresh.name());
        }
        if (columnNames != null) {
            sql.append(PlainSelect.getStringList(columnNames, true, true));
        }
        sql.append(" AS ").append(select);
        if (isWithReadOnly()) {
            sql.append(" WITH READ ONLY");
        }
        return sql.toString();
    }

    private void appendForceOptionIfApplicable(StringBuilder sql) {
        switch(force) {
            case FORCE:
                sql.append("FORCE ");
                break;
            case NO_FORCE:
                sql.append("NO FORCE ");
                break;
            default:
        }
    }

    public CreateView withView(Table view) {
        this.setView(view);
        return this;
    }

    public CreateView withSelect(Select select) {
        this.setSelect(select);
        return this;
    }

    public CreateView withOrReplace(boolean orReplace) {
        this.setOrReplace(orReplace);
        return this;
    }

    public CreateView withColumnNames(List<String> columnNames) {
        this.setColumnNames(columnNames);
        return this;
    }

    public CreateView withMaterialized(boolean materialized) {
        this.setMaterialized(materialized);
        return this;
    }

    public CreateView withForce(ForceOption force) {
        this.setForce(force);
        return this;
    }

    public CreateView withWithReadOnly(boolean withReadOnly) {
        this.setWithReadOnly(withReadOnly);
        return this;
    }

    public CreateView addColumnNames(String... columnNames) {
        List<String> collection = Optional.ofNullable(getColumnNames()).orElseGet(ArrayList::new);
        Collections.addAll(collection, columnNames);
        return this.withColumnNames(collection);
    }

    public CreateView addColumnNames(Collection<String> columnNames) {
        List<String> collection = Optional.ofNullable(getColumnNames()).orElseGet(ArrayList::new);
        collection.addAll(columnNames);
        return this.withColumnNames(collection);
    }
}
