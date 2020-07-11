/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2020 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.show;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

import java.util.EnumSet;

/**
 * A {@code SHOW TABLES} statement
 * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/show-tables.html">MySQL show tables</a>
 */
public class ShowTablesStatement implements Statement {

    private EnumSet<Modifiers> modifiers;
    private SelectionMode selectionMode;
    private String dbName;
    private Expression likeExpression;
    private Expression whereCondition;

    public EnumSet<Modifiers> getModifiers() {
        return modifiers;
    }

    public void setModifiers(EnumSet<Modifiers> modifiers) {
        this.modifiers = modifiers;
    }

    public SelectionMode getSelectionMode() {
        return selectionMode;
    }

    public void setSelectionMode(SelectionMode selectionMode) {
        this.selectionMode = selectionMode;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public Expression getLikeExpression() {
        return likeExpression;
    }

    public void setLikeExpression(Expression likeExpression) {
        this.likeExpression = likeExpression;
    }

    public Expression getWhereCondition() {
        return whereCondition;
    }

    public void setWhereCondition(Expression whereCondition) {
        this.whereCondition = whereCondition;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SHOW");

        if (modifiers.contains(Modifiers.EXTENDED)) {
            builder.append(" EXTENDED");
        }
        if (modifiers.contains(Modifiers.FULL)) {
            builder.append(" FULL");
        }

        builder.append(" TABLES");

        if (dbName != null) {
            builder.append(" ").append(selectionMode.name()).append(" ").append(dbName);
        }

        if (likeExpression != null) {
            builder.append(" ").append("LIKE").append(" ").append(likeExpression);
        }

        if (whereCondition != null) {
            builder.append(" ").append("WHERE").append(" ").append(whereCondition);
        }

        return builder.toString();
    }

    @Override
    public void accept(StatementVisitor statementVisitor) {
        statementVisitor.visit(this);
    }

    public enum SelectionMode {
        FROM, IN
    }

    public enum Modifiers {
        EXTENDED, FULL
    }
}
