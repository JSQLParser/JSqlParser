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

import java.util.function.Consumer;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

public class Comment implements Statement {

    private Table table;
    private Column column;
    private Table view;
    private CommentTarget target;
    private StringValue comment;

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
        if (table != null) {
            target = null;
        }
    }

    public Column getColumn() {
        return column;
    }

    public void setColumn(Column column) {
        this.column = column;
        if (column != null) {
            target = null;
        }
    }

    public Table getView() {
        return view;
    }

    public void setView(Table view) {
        this.view = view;
        if (view != null) {
            target = null;
        }
    }

    /** Additional catalog targets; the existing table, column and view accessors remain intact. */
    public CommentTarget getTarget() {
        return target;
    }

    public void setTarget(CommentTarget target) {
        this.target = target;
        if (target != null) {
            table = null;
            column = null;
            view = null;
        }
    }

    public Comment withTarget(CommentTarget target) {
        setTarget(target);
        return this;
    }

    /** Visits the relation explicitly named by this comment, without resolving catalog objects. */
    public void visitRelations(Consumer<Table> visitor) {
        Table relation = table != null ? table
                : column != null ? column.getTable()
                        : view != null ? view
                                : target != null ? target.getReferencedRelation() : null;
        if (relation != null) {
            visitor.accept(relation);
        }
    }

    public StringValue getComment() {
        return comment;
    }

    public void setComment(StringValue comment) {
        this.comment = comment;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        return appendTo(builder, builder::append, builder::append, builder::append).toString();
    }

    public StringBuilder appendTo(StringBuilder builder, Consumer<Table> relationWriter,
            Consumer<Column> columnWriter, Consumer<StringValue> commentWriter) {
        builder.append("COMMENT ON ");
        if (table != null) {
            builder.append("TABLE ");
            relationWriter.accept(table);
            builder.append(' ');
        } else if (column != null) {
            builder.append("COLUMN ");
            columnWriter.accept(column);
            builder.append(' ');
        } else if (view != null) {
            builder.append("VIEW ");
            relationWriter.accept(view);
            builder.append(' ');
        } else if (target != null) {
            target.appendTo(builder, relationWriter);
            builder.append(' ');
        }
        // a null comment stands for PostgreSQL's COMMENT ON ... IS NULL, which removes the comment
        builder.append("IS ");
        if (comment == null) {
            builder.append("NULL");
        } else {
            commentWriter.accept(comment);
        }
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
