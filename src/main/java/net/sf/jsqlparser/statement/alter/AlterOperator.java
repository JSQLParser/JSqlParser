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
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.statement.DefinitionElement;
import net.sf.jsqlparser.statement.OperatorSignature;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

/**
 * PostgreSQL {@code ALTER OPERATOR [schema.]op ({left_type | NONE}, right_type)} with one of
 * {@code OWNER TO role}, {@code SET SCHEMA schema} and {@code SET (property [= value], ...)}.
 * PostgreSQL checks the property names when it runs the statement, so they are not restricted.
 */
public class AlterOperator extends ASTNodeAccessImpl implements Statement {
    public enum Action {
        OWNER, SET_SCHEMA, SET_PROPERTIES
    }

    private OperatorSignature operator;
    private Action action;
    private String newName;
    private List<DefinitionElement> properties = new ArrayList<>();

    /**
     * The operator and its operand types, read as in DROP OPERATOR.
     *
     * @return the operator signature
     */
    public OperatorSignature getOperator() {
        return operator;
    }

    public void setOperator(OperatorSignature operator) {
        this.operator = operator;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    /**
     * The properties of {@code SET (property [= value], ...)}, such as {@code RESTRICT = NONE}.
     *
     * @return the properties in source order, empty for another action
     */
    public List<DefinitionElement> getProperties() {
        return properties;
    }

    public void setProperties(List<DefinitionElement> properties) {
        this.properties = properties;
    }

    /**
     * The new name of {@code RENAME TO}, the role of {@code OWNER TO} or the schema of
     * {@code SET SCHEMA}.
     *
     * @return the name, or {@code null} for another action
     */
    public String getNewName() {
        return newName;
    }

    public void setNewName(String newName) {
        this.newName = newName;
    }

    @Override
    public StringBuilder appendTo(StringBuilder builder) {
        builder.append("ALTER OPERATOR ").append(operator);
        switch (action) {
            case OWNER:
                return builder.append(" OWNER TO ").append(newName);
            case SET_SCHEMA:
                return builder.append(" SET SCHEMA ").append(newName);
            case SET_PROPERTIES:
                return builder.append(" SET ").append(DefinitionElement.toString(properties));
            default:
                throw new IllegalStateException("Unknown operator alteration: " + action);
        }
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }
}
