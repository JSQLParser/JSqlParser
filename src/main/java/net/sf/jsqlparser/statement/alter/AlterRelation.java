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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

/** PostgreSQL ALTER INDEX, ALTER VIEW properties and ALTER MATERIALIZED VIEW. */
public class AlterRelation implements Statement {
    public enum ObjectType {
        INDEX, VIEW, MATERIALIZED_VIEW
    }

    private ObjectType objectType;
    private Table relation;
    private boolean ifExists;
    private final List<RelationAlterAction> actions = new ArrayList<>();

    public ObjectType getObjectType() {
        return objectType;
    }

    public void setObjectType(ObjectType objectType) {
        this.objectType = objectType;
    }

    public Table getRelation() {
        return relation;
    }

    public void setRelation(Table relation) {
        this.relation = relation;
    }

    public boolean isIfExists() {
        return ifExists;
    }

    public void setIfExists(boolean ifExists) {
        this.ifExists = ifExists;
    }

    public List<RelationAlterAction> getActions() {
        return actions;
    }

    public StringBuilder appendTo(StringBuilder builder, Consumer<Expression> expressionPrinter) {
        builder.append("ALTER ").append(objectType.name().replace('_', ' ')).append(' ');
        if (ifExists) {
            builder.append("IF EXISTS ");
        }
        builder.append(relation).append(' ');
        for (int i = 0; i < actions.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            actions.get(i).appendTo(builder, expressionPrinter);
        }
        return builder;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        return appendTo(builder, builder::append).toString();
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> visitor, S context) {
        return visitor.visit(this, context);
    }
}
