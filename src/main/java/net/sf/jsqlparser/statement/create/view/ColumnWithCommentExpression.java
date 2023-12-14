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

import java.io.Serializable;

public class ColumnWithCommentExpression implements Serializable {

    private String columnName;
    private String commentText;

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        if (columnName != null) {
            b.append(columnName);
        }
        if (commentText != null) {
            b.append(" COMMENT ");
            b.append(commentText);
        }
        return b.toString();
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public ColumnWithCommentExpression withCommentText(String commentText) {
        setCommentText(commentText);
        return this;
    }

    public ColumnWithCommentExpression withColumnName(String columnName) {
        setColumnName(columnName);
        return this;
    }

}
