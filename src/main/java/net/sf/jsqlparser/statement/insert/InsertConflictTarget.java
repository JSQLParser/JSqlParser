/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2022 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.insert;

import net.sf.jsqlparser.expression.Expression;

/**
 * https://www.postgresql.org/docs/current/sql-insert.html
 * <pre>
 * conflict_target can be one of:
 *
 *     ( { index_column_name | ( index_expression ) } [ COLLATE collation ] [ opclass ] [, ...] ) [ WHERE index_predicate ]
 *     ON CONSTRAINT constraint_name
 * </pre>
 * Currently, COLLATE is not supported yet.
 */
public class InsertConflictTarget {

    String indexColumnName;
    Expression indexExpression;
    Expression whereExpression;
    String constraintName;

    public InsertConflictTarget(String indexColumnName, Expression indexExpression, Expression whereExpression, String constraintName) {
        this.indexColumnName = indexColumnName;
        this.indexExpression = indexExpression;

        this.whereExpression = whereExpression;
        this.constraintName = constraintName;
    }

    public String getIndexColumnName() {
        return indexColumnName;
    }

    public void setIndexColumnName(String indexColumnName) {
        this.indexColumnName = indexColumnName;
        this.indexExpression = null;
    }

    public InsertConflictTarget withIndexColumnName(String indexColumnName) {
        setIndexColumnName(indexColumnName);
        return this;
    }

    public Expression getIndexExpression() {
        return indexExpression;
    }

    public void setIndexExpression(Expression indexExpression) {
        this.indexExpression = indexExpression;
        this.indexColumnName = null;
    }

    public InsertConflictTarget withIndexExpression(Expression indexExpression) {
        setIndexExpression(indexExpression);
        return this;
    }

    public Expression getWhereExpression() {
        return whereExpression;
    }

    public void setWhereExpression(Expression whereExpression) {
        this.whereExpression = whereExpression;
    }

    public InsertConflictTarget withWhereExpression(Expression whereExpression) {
        setWhereExpression(whereExpression);
        return this;
    }

    public String getConstraintName() {
        return constraintName;
    }

    public void setConstraintName(String constraintName) {
        this.constraintName = constraintName;
    }

    public InsertConflictTarget withConstraintName(String constraintName) {
        setConstraintName(constraintName);
        return this;
    }

    public StringBuilder appendTo(StringBuilder builder) {
        if (constraintName==null) {
            builder.append(" ( ");

            //@todo: Index Expression is not supported yet
            //if (indexColumnName != null) {
                builder.append(indexColumnName);
            //} else {
            //    builder.append(" ( ").append(indexExpression).append(" )");
            //}
            builder.append(" ");

            //@todo: Collate is not supported yet

            builder.append(") ");

            if (whereExpression != null) {
                builder.append(" WHERE ").append(whereExpression);
            }
        } else {
            builder.append(" ON CONSTRAINT ").append(constraintName);
        }
        return builder;
    }

    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }
}
