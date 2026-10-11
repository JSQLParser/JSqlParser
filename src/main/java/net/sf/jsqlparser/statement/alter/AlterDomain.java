/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.alter;

import java.util.function.Consumer;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.CascadeBehavior;
import net.sf.jsqlparser.statement.create.domain.DomainConstraint;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

public class AlterDomain implements Statement {
    private String name;
    private Action action;
    private Expression defaultExpression;
    private DomainConstraint constraint;
    private String constraintName;
    private String newName;
    private boolean notValid;
    private boolean ifExists;
    private CascadeBehavior behavior;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public Expression getDefaultExpression() {
        return defaultExpression;
    }

    public void setDefaultExpression(Expression defaultExpression) {
        this.defaultExpression = defaultExpression;
    }

    public DomainConstraint getConstraint() {
        return constraint;
    }

    public void setConstraint(DomainConstraint constraint) {
        this.constraint = constraint;
    }

    public String getConstraintName() {
        return constraintName;
    }

    public void setConstraintName(String constraintName) {
        this.constraintName = constraintName;
    }

    public String getNewName() {
        return newName;
    }

    public void setNewName(String newName) {
        this.newName = newName;
    }

    public boolean isNotValid() {
        return notValid;
    }

    public void setNotValid(boolean notValid) {
        this.notValid = notValid;
    }

    public boolean isIfExists() {
        return ifExists;
    }

    public void setIfExists(boolean ifExists) {
        this.ifExists = ifExists;
    }

    public CascadeBehavior getBehavior() {
        return behavior;
    }

    public void setBehavior(CascadeBehavior behavior) {
        this.behavior = behavior;
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> visitor, S context) {
        return visitor.visit(this, context);
    }

    public enum Action {
        SET_DEFAULT, DROP_DEFAULT, SET_NOT_NULL, DROP_NOT_NULL, ADD_CONSTRAINT, DROP_CONSTRAINT, RENAME_CONSTRAINT, VALIDATE_CONSTRAINT, OWNER, RENAME, SET_SCHEMA
    }

    public void appendTo(StringBuilder sql, Consumer<Expression> expressions) {
        sql.append("ALTER DOMAIN ").append(name).append(' ');
        switch (action) {
            case SET_DEFAULT:
                sql.append("SET DEFAULT ");
                expressions.accept(defaultExpression);
                break;
            case DROP_DEFAULT:
                sql.append("DROP DEFAULT");
                break;
            case SET_NOT_NULL:
                sql.append("SET NOT NULL");
                break;
            case DROP_NOT_NULL:
                sql.append("DROP NOT NULL");
                break;
            case ADD_CONSTRAINT:
                sql.append("ADD ");
                constraint.appendTo(sql, expressions);
                if (notValid) {
                    sql.append(" NOT VALID");
                }
                break;
            case DROP_CONSTRAINT:
                sql.append("DROP CONSTRAINT ").append(ifExists ? "IF EXISTS " : "")
                        .append(constraintName);
                if (behavior != null) {
                    sql.append(' ').append(behavior);
                }
                break;
            case RENAME_CONSTRAINT:
                sql.append("RENAME CONSTRAINT ").append(constraintName).append(" TO ")
                        .append(newName);
                break;
            case VALIDATE_CONSTRAINT:
                sql.append("VALIDATE CONSTRAINT ").append(constraintName);
                break;
            case OWNER:
                sql.append("OWNER TO ").append(newName);
                break;
            case RENAME:
                sql.append("RENAME TO ").append(newName);
                break;
            case SET_SCHEMA:
                sql.append("SET SCHEMA ").append(newName);
                break;
            default:
                throw new IllegalStateException("Unknown domain alteration: " + action);
        }
    }

    @Override
    public String toString() {
        StringBuilder sql = new StringBuilder();
        appendTo(sql, sql::append);
        return sql.toString();
    }
}
