/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.role;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.statement.ConfigurationParameterChange;
import net.sf.jsqlparser.statement.ConfigurationParameterChange.ValueKind;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

/**
 * PostgreSQL {@code ALTER ROLE}, {@code ALTER USER} or {@code ALTER GROUP}. The {@code SET} and
 * {@code RESET} actions use the shared {@link ConfigurationParameterChange}; the parameter
 * accessors of this class read and edit that change.
 */
public class AlterRole implements Statement {
    public enum Action {
        OPTIONS, RENAME, SET, RESET, ADD_USER, DROP_USER
    }

    private CreateRole.Command command = CreateRole.Command.ROLE;
    private String name;
    private Action action;
    private boolean useWith;
    private String newName;
    private String database;
    private ConfigurationParameterChange parameterChange;
    private List<String> users;
    private List<RoleOption> options = new ArrayList<>();

    public CreateRole.Command getCommand() {
        return command;
    }

    public void setCommand(CreateRole.Command command) {
        this.command = command;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The role action. For {@code SET} and {@code RESET}, the action of the parameter change
     * decides which of the two is returned.
     *
     * @return the action
     */
    public Action getAction() {
        if (parameterChange != null && (action == Action.SET || action == Action.RESET)) {
            return parameterChange.getAction() == ConfigurationParameterChange.Action.RESET
                    ? Action.RESET
                    : Action.SET;
        }
        return action;
    }

    /**
     * Selects the role action. {@code SET} and {@code RESET} are also applied to the parameter
     * change, which is created when missing. Other actions keep the parameter change for a later
     * switch back.
     *
     * @param action the action
     */
    public void setAction(Action action) {
        this.action = action;
        if (action == Action.SET || action == Action.RESET) {
            ensureParameterChange().setAction(action == Action.RESET
                    ? ConfigurationParameterChange.Action.RESET
                    : ConfigurationParameterChange.Action.SET);
        }
    }

    /**
     * The configuration parameter change of the {@code SET} and {@code RESET} actions.
     *
     * @return the change, or {@code null} if none was set
     */
    public ConfigurationParameterChange getParameterChange() {
        return parameterChange;
    }

    /**
     * Sets the configuration parameter change; a non-null change also selects its {@code SET} or
     * {@code RESET} action.
     *
     * @param parameterChange the change
     */
    public void setParameterChange(ConfigurationParameterChange parameterChange) {
        this.parameterChange = parameterChange;
        if (parameterChange != null) {
            action = parameterChange.getAction() == ConfigurationParameterChange.Action.RESET
                    ? Action.RESET
                    : Action.SET;
        }
    }

    private ConfigurationParameterChange ensureParameterChange() {
        if (parameterChange == null) {
            parameterChange = new ConfigurationParameterChange(action == Action.RESET
                    ? ConfigurationParameterChange.Action.RESET
                    : ConfigurationParameterChange.Action.SET, null);
        }
        return parameterChange;
    }

    public boolean isUseWith() {
        return useWith;
    }

    public void setUseWith(boolean useWith) {
        this.useWith = useWith;
    }

    public List<RoleOption> getOptions() {
        return options;
    }

    public void setOptions(List<RoleOption> options) {
        this.options = options;
    }

    public String getNewName() {
        return newName;
    }

    public void setNewName(String newName) {
        this.newName = newName;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getParameter() {
        return parameterChange == null ? null : parameterChange.getParameter();
    }

    public void setParameter(String parameter) {
        ensureParameterChange().setParameter(parameter);
    }

    public boolean isUseEquals() {
        return parameterChange != null && parameterChange.isUseEquals();
    }

    public void setUseEquals(boolean useEquals) {
        ensureParameterChange().setUseEquals(useEquals);
    }

    /**
     * Whether the parameter change assigns {@code FROM CURRENT}.
     *
     * @return {@code true} for {@link ValueKind#FROM_CURRENT}
     */
    public boolean isFromCurrent() {
        return hasValueKind(ValueKind.FROM_CURRENT);
    }

    /**
     * Selects {@link ValueKind#FROM_CURRENT}; {@code false} returns from it to the explicit values.
     *
     * @param fromCurrent whether to assign {@code FROM CURRENT}
     */
    public void setFromCurrent(boolean fromCurrent) {
        setValueKind(ValueKind.FROM_CURRENT, fromCurrent);
    }

    /**
     * Whether the parameter change assigns {@code DEFAULT}.
     *
     * @return {@code true} for {@link ValueKind#DEFAULT}
     */
    public boolean isUseDefault() {
        return hasValueKind(ValueKind.DEFAULT);
    }

    /**
     * Selects {@link ValueKind#DEFAULT}; {@code false} returns from it to the explicit values.
     *
     * @param useDefault whether to assign {@code DEFAULT}
     */
    public void setUseDefault(boolean useDefault) {
        setValueKind(ValueKind.DEFAULT, useDefault);
    }

    private boolean hasValueKind(ValueKind kind) {
        return parameterChange != null && parameterChange.getValueKind() == kind;
    }

    private void setValueKind(ValueKind kind, boolean selected) {
        if (selected) {
            ensureParameterChange().setValueKind(kind);
        } else if (hasValueKind(kind)) {
            parameterChange.setValueKind(ValueKind.VALUES);
        }
    }

    public ExpressionList<Expression> getValues() {
        return parameterChange == null ? null : parameterChange.getValues();
    }

    public void setValues(ExpressionList<Expression> values) {
        ensureParameterChange().setValues(values);
    }

    public List<String> getUsers() {
        return users;
    }

    public void setUsers(List<String> users) {
        this.users = users;
    }

    public void appendTo(StringBuilder sql, Consumer<Expression> visitor) {
        sql.append("ALTER ").append(command).append(' ').append(name);
        if (database != null) {
            sql.append(" IN DATABASE ").append(database);
        }
        switch (getAction()) {
            case OPTIONS:
                if (useWith) {
                    sql.append(" WITH");
                }
                for (RoleOption option : options) {
                    sql.append(' ');
                    option.appendTo(sql, visitor);
                }
                break;
            case RENAME:
                sql.append(" RENAME TO ").append(newName);
                break;
            case RESET:
            case SET:
                if (parameterChange == null) {
                    throw new IllegalStateException("Expected a configuration parameter change");
                }
                parameterChange.appendTo(sql.append(' '), visitor);
                break;
            case ADD_USER:
            case DROP_USER:
                sql.append(action == Action.ADD_USER ? " ADD USER " : " DROP USER ")
                        .append(String.join(", ", users));
                break;
            default:
                throw new IllegalStateException("Unknown role action: " + action);
        }
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> visitor, S context) {
        return visitor.visit(this, context);
    }

    @Override
    public String toString() {
        StringBuilder sql = new StringBuilder();
        appendTo(sql, sql::append);
        return sql.toString();
    }
}
