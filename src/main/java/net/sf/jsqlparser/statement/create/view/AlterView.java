/*
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2013 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.view;

import java.util.List;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectBody;

/**
 * A "CREATE VIEW" statement
 */
public class AlterView implements Statement {

    private Table view;
    private SelectBody selectBody;
    private boolean useReplace = false;
    private List<String> columnNames = null;

    @Override
    public void accept(StatementVisitor statementVisitor) {
        statementVisitor.visit(this);
    }

    /**
     * In the syntax tree, a view looks and acts just like a Table.
     *
     * @return The name of the view to be created.
     */
    public Table getView() {
        return view;
    }

    public void setView(Table view) {
        this.view = view;
    }

    /**
     * @return the SelectBody
     */
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


    @Override
    public String toString() {
        StringBuilder sql;
        if(useReplace){
            sql = new StringBuilder("REPLACE ");
        }else{
            sql = new StringBuilder("ALTER ");
        }
        sql.append("VIEW ");
        sql.append(view);
        if (columnNames != null) {
            sql.append(PlainSelect.getStringList(columnNames, true, true));
        }
        sql.append(" AS ").append(selectBody);
        return sql.toString();
    }
}
