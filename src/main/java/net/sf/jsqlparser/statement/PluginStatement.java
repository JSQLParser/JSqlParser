/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

/**
 * The MySQL statements that load and unload server plugins and components:
 * {@code INSTALL PLUGIN name SONAME 'library'}, {@code UNINSTALL PLUGIN name},
 * {@code INSTALL COMPONENT 'urn'[, ...] [SET [GLOBAL | PERSIST] variable = value[, ...]]} and
 * {@code UNINSTALL COMPONENT 'urn'[, ...]}. DuckDB's {@code INSTALL extension} is an
 * {@link ExtensionStatement}.
 *
 * @see <a href="https://dev.mysql.com/doc/refman/8.4/en/component-statements.html">MySQL Plugin and
 *      Component Statements</a>
 */
public class PluginStatement extends ASTNodeAccessImpl implements Statement {
    /** The statement's first word. */
    public enum Action {
        INSTALL, UNINSTALL
    }

    /** What the statement loads or unloads. */
    public enum ObjectType {
        PLUGIN, COMPONENT
    }

    private Action action;
    private ObjectType objectType;
    private String pluginName;
    private StringValue library;
    private List<StringValue> components = new ArrayList<>();
    private List<ComponentVariable> variables = new ArrayList<>();

    public PluginStatement(Action action, ObjectType objectType) {
        this.action = Objects.requireNonNull(action, "action");
        this.objectType = Objects.requireNonNull(objectType, "objectType");
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = Objects.requireNonNull(action, "action");
    }

    public ObjectType getObjectType() {
        return objectType;
    }

    public void setObjectType(ObjectType objectType) {
        this.objectType = Objects.requireNonNull(objectType, "objectType");
    }

    /** The plugin name as written, or null for a component statement. */
    public String getPluginName() {
        return pluginName;
    }

    public void setPluginName(String pluginName) {
        this.pluginName = pluginName;
    }

    public PluginStatement withPluginName(String pluginName) {
        setPluginName(pluginName);
        return this;
    }

    /** The plugin library file after SONAME; only INSTALL PLUGIN names one. */
    public StringValue getLibrary() {
        return library;
    }

    public void setLibrary(StringValue library) {
        this.library = library;
    }

    public PluginStatement withLibrary(StringValue library) {
        setLibrary(library);
        return this;
    }

    /** The component URNs, in the order written; empty for a plugin statement. */
    public List<StringValue> getComponents() {
        return components;
    }

    public void setComponents(List<StringValue> components) {
        this.components = Objects.requireNonNull(components, "components");
    }

    public PluginStatement addComponents(StringValue... components) {
        for (StringValue component : components) {
            this.components.add(component);
        }
        return this;
    }

    /** The assignments after SET; only INSTALL COMPONENT has them. */
    public List<ComponentVariable> getVariables() {
        return variables;
    }

    public void setVariables(List<ComponentVariable> variables) {
        this.variables = Objects.requireNonNull(variables, "variables");
    }

    public PluginStatement addVariables(ComponentVariable... variables) {
        for (ComponentVariable variable : variables) {
            this.variables.add(variable);
        }
        return this;
    }

    /** Passes the literals and the assigned values to {@code visitor}. */
    public void visitExpressions(Consumer<Expression> visitor) {
        if (objectType == ObjectType.PLUGIN) {
            if (library != null) {
                visitor.accept(library);
            }
            return;
        }
        components.forEach(visitor);
        for (ComponentVariable variable : variables) {
            visitor.accept(variable.getValue());
        }
    }

    public StringBuilder appendTo(StringBuilder builder, Consumer<Expression> expressionPrinter) {
        builder.append(action).append(' ').append(objectType);
        if (objectType == ObjectType.PLUGIN) {
            builder.append(' ').append(pluginName);
            if (library != null) {
                builder.append(" SONAME ");
                expressionPrinter.accept(library);
            }
            return builder;
        }
        for (int i = 0; i < components.size(); i++) {
            builder.append(i == 0 ? " " : ", ");
            expressionPrinter.accept(components.get(i));
        }
        for (int i = 0; i < variables.size(); i++) {
            builder.append(i == 0 ? " SET " : ", ");
            variables.get(i).appendTo(builder, expressionPrinter);
        }
        return builder;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        return appendTo(builder, builder::append).toString();
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }
}
