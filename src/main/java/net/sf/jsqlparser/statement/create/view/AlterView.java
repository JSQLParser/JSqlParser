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
import net.sf.jsqlparser.statement.DDLStatement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectBody;

public class AlterView extends DDLStatement {

    private Table view;
    private SelectBody selectBody;
    private boolean useReplace = false;
    private List<String> columnNames = null;

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

    public SelectBody getSelectBody() {
        return selectBody;
    }

    public void setSelectBody(SelectBody selectBody) {
        this.selectBody = selectBody;
    }

    public List<String> getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(List<String> columnNames) {
        this.columnNames = columnNames;
    }

    public boolean isUseReplace() {
        return useReplace;
    }

    public void setUseReplace(boolean useReplace) {
        this.useReplace = useReplace;
    }

    public AlterView withView(Table view) {
        this.setView(view);
        return this;
    }

    public AlterView withSelectBody(SelectBody selectBody) {
        this.setSelectBody(selectBody);
        return this;
    }

    public AlterView withUseReplace(boolean useReplace) {
        this.setUseReplace(useReplace);
        return this;
    }

    public AlterView withColumnNames(List<String> columnNames) {
        this.setColumnNames(columnNames);
        return this;
    }

    public AlterView addColumnNames(String... columnNames) {
        List<String> collection = Optional.ofNullable(getColumnNames()).orElseGet(ArrayList::new);
        Collections.addAll(collection, columnNames);
        return this.withColumnNames(collection);
    }

    public AlterView addColumnNames(Collection<String> columnNames) {
        List<String> collection = Optional.ofNullable(getColumnNames()).orElseGet(ArrayList::new);
        collection.addAll(columnNames);
        return this.withColumnNames(collection);
    }

    public <E extends SelectBody> E getSelectBody(Class<E> type) {
        return type.cast(getSelectBody());
    }

    @Override
    public StringBuilder appendTo(StringBuilder builder) {
        if (useReplace) {
            builder.append("REPLACE ");
        } else {
            builder.append("ALTER ");
        }
        builder.append("VIEW ");
        builder.append(view);
        if (columnNames != null) {
            builder.append(PlainSelect.getStringList(columnNames, true, true));
        }
        builder.append(" AS ").append(selectBody);
        return builder;
    }
}
