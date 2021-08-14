/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.comment;

import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.DDLStatement;
import net.sf.jsqlparser.statement.StatementVisitor;

public class Comment extends DDLStatement {

    private Table table;
    private Column column;
    private Table view;
    private StringValue comment;

    @Override
    public void accept(StatementVisitor statementVisitor) {
        statementVisitor.visit(this);
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public Column getColumn() {
        return column;
    }

    public void setColumn(Column column) {
        this.column = column;
    }

    public Table getView() {
        return view;
    }

    public void setView(Table view) {
        this.view = view;
    }

    public StringValue getComment() {
        return comment;
    }

    public void setComment(StringValue comment) {
        this.comment = comment;
    }

    @Override
    public StringBuilder appendTo(StringBuilder builder) {
        builder.append("COMMENT ON ");
        if (table != null) {
            builder.append("TABLE ").append(table).append(" ");
        } else if (column != null) {
            builder.append("COLUMN ").append(column).append(" ");
        } else if (view != null) {
            builder.append("VIEW ").append(view).append(" ");
        }
        builder.append("IS ").append(comment);
        return builder;
    }

    public Comment withTable(Table table) {
        this.setTable(table);
        return this;
    }

    public Comment withColumn(Column column) {
        this.setColumn(column);
        return this;
    }

    public Comment withComment(StringValue comment) {
        this.setComment(comment);
        return this;
    }
}
