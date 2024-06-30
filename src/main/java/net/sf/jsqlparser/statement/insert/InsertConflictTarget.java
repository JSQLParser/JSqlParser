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

import java.io.Serializable;
import java.util.*;

/**
 * https://www.postgresql.org/docs/current/sql-insert.html
 *
 * <pre>
 * conflict_target can be one of:
 *
 *     ( { index_column_name | ( index_expression ) } [ COLLATE collation ] [ opclass ] [, ...] ) [ WHERE index_predicate ]
 *     ON CONSTRAINT constraint_name
 * </pre>
 * <p>
 * Currently, COLLATE is not supported yet.
 */
public class InsertConflictTarget implements Serializable {

    ArrayList<String> indexColumnNames = new ArrayList<>();
    Expression indexExpression;
    Expression whereExpression;
    String constraintName;

    public InsertConflictTarget(String indexColumnName, Expression indexExpression,
            Expression whereExpression, String constraintName) {
        this.indexColumnNames.add(indexColumnName);
        this.indexExpression = indexExpression;

        this.whereExpression = whereExpression;
        this.constraintName = constraintName;
    }

    public InsertConflictTarget(Collection<String> indexColumnName, Expression indexExpression,
            Expression whereExpression, String constraintName) {
        this.indexColumnNames.addAll(indexColumnName);
        this.indexExpression = indexExpression;

        this.whereExpression = whereExpression;
        this.constraintName = constraintName;
    }

    public List<String> getIndexColumnNames() {
        return indexColumnNames;
    }

    @Deprecated
    public String getIndexColumnName() {
        return indexColumnNames.isEmpty() ? null : indexColumnNames.get(0);
    }

    public String getIndexColumnName(int index) {
        return indexColumnNames.size() > index ? indexColumnNames.get(index) : null;
    }

    public boolean addIndexColumnName(String indexColumnName) {
        this.indexExpression = null;
        return this.indexColumnNames.add(indexColumnName);
    }

    public InsertConflictTarget withIndexColumnName(String indexColumnName) {
        this.indexExpression = null;
        this.indexColumnNames.add(indexColumnName);
        return this;
    }

    public boolean addAllIndexColumnNames(Collection<String> indexColumnName) {
        this.indexExpression = null;
        return this.indexColumnNames.addAll(indexColumnName);
    }


    public Expression getIndexExpression() {
        return indexExpression;
    }

    public void setIndexExpression(Expression indexExpression) {
        this.indexExpression = indexExpression;
        this.indexColumnNames.clear();
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
        if (constraintName == null) {
            builder.append(" ( ");

            // @todo: Index Expression is not supported yet
            if (!indexColumnNames.isEmpty()) {
                boolean insertComma = false;
                for (String s : indexColumnNames) {
                    builder.append(insertComma ? ", " : " ").append(s);
                    insertComma |= true;
                }
            } else {
                builder.append(" ( ").append(indexExpression).append(" )");
            }
            builder.append(" ");

            // @todo: Collate is not supported yet

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
