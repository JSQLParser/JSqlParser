/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.drop;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.select.PlainSelect;

public class Drop implements Statement {

    public enum ObjectType {
        DATABASE, EVENT, FUNCTION, INDEX, PROCEDURE, SCHEMA, SEQUENCE, SERVER, TABLE, TABLESPACE, TRIGGER, VIEW, OTHER
    }

    private String type;
    private ObjectType objectType = ObjectType.OTHER;
    private final List<Table> names = new ArrayList<>();
    private List<String> parameters;
    private Table table;
    private int tablePosition;
    private Map<String, List<String>> typeToParameters = new HashMap<>();
    private boolean ifExists = false;
    private boolean materialized = false;

    private boolean isUsingTemporary;

    public static String formatFuncParams(List<String> params) {
        if (params == null) {
            return "";
        }
        return params.isEmpty() ? "()" : PlainSelect.getStringList(params, true, true);
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }

    public Table getName() {
        return names.isEmpty() ? null : names.get(0);
    }

    public void setName(Table name) {
        names.clear();
        if (name != null) {
            names.add(name);
        }
    }

    public List<Table> getNames() {
        return Collections.unmodifiableList(names);
    }

    public void setNames(Collection<? extends Table> names) {
        this.names.clear();
        if (names != null) {
            this.names.addAll(names);
        }
    }

    /** Returns legacy tokens, including ON and its owner. Structured owners produce a snapshot. */
    public List<String> getParameters() {
        if (table == null) {
            return parameters;
        }
        List<String> tokens = parameters == null ? new ArrayList<>() : new ArrayList<>(parameters);
        tokens.add(tablePosition, "ON");
        tokens.add(tablePosition + 1, table.toString());
        return tokens;
    }

    /** The table explicitly named by DROP INDEX ... ON, or null when SQL does not name one. */
    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        setTable(table, this.table == null ? 0 : tablePosition);
    }

    /** Places ON among the remaining parameter tokens, preserving their source order. */
    public void setTable(Table table, int parameterPosition) {
        int size = parameters == null ? 0 : parameters.size();
        if (parameterPosition < 0 || parameterPosition > size) {
            throw new IllegalArgumentException("Invalid ON table position: " + parameterPosition);
        }
        this.table = table;
        tablePosition = table == null ? 0 : parameterPosition;
    }

    public Drop withTable(Table table) {
        setTable(table);
        return this;
    }

    /** Visits table/view targets and explicit index owners without resolving catalog objects. */
    public void visitTables(Consumer<Table> visitor) {
        if (objectType == ObjectType.TABLE || objectType == ObjectType.VIEW) {
            names.forEach(visitor);
        } else if (objectType == ObjectType.INDEX && table != null) {
            visitor.accept(table);
        }
    }

    /** Replaces all legacy parameters and clears the structured ON table. */
    public void setParameters(List<String> list) {
        parameters = list;
        table = null;
        tablePosition = 0;
    }

    public String getType() {
        return type;
    }

    public void setType(String string) {
        type = string;
        try {
            objectType = ObjectType.valueOf(string.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException | NullPointerException ignored) {
            objectType = ObjectType.OTHER;
        }
    }

    public ObjectType getObjectType() {
        return objectType;
    }

    public void setObjectType(ObjectType objectType) {
        this.objectType = objectType == null ? ObjectType.OTHER : objectType;
        if (this.objectType != ObjectType.OTHER) {
            this.type = this.objectType.name();
        }
    }

    public boolean isIfExists() {
        return ifExists;
    }

    public void setIfExists(boolean ifExists) {
        this.ifExists = ifExists;
    }

    public boolean isUsingTemporary() {
        return isUsingTemporary;
    }

    public void setUsingTemporary(boolean useTemporary) {
        this.isUsingTemporary = useTemporary;
    }

    public Drop withUsingTemporary(boolean useTemporary) {
        setUsingTemporary(useTemporary);
        return this;
    }

    public boolean isMaterialized() {
        return materialized;
    }

    public void setMaterialized(boolean materialized) {
        this.materialized = materialized;
    }

    public Map<String, List<String>> getTypeToParameters() {
        return typeToParameters;
    }

    public void setTypeToParameters(Map<String, List<String>> typeToParameters) {
        this.typeToParameters = typeToParameters;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        return appendTo(builder, builder::append).toString();
    }

    public StringBuilder appendTo(StringBuilder builder, Consumer<Table> tablePrinter) {
        builder.append("DROP ");
        if (isUsingTemporary) {
            builder.append("TEMPORARY ");
        }
        if (materialized) {
            builder.append("MATERIALIZED ");
        }
        builder.append(type).append(ifExists ? " IF EXISTS " : " ");
        for (int i = 0; i < names.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            if (objectType == ObjectType.TABLE || objectType == ObjectType.VIEW) {
                tablePrinter.accept(names.get(i));
            } else {
                builder.append(names.get(i));
            }
        }
        if ("FUNCTION".equals(type)) {
            builder.append(formatFuncParams(getParamsByType("FUNCTION")));
        }
        int size = parameters == null ? 0 : parameters.size();
        for (int i = 0; i <= size; i++) {
            if (table != null && i == tablePosition) {
                builder.append(" ON ");
                tablePrinter.accept(table);
            }
            if (i < size) {
                builder.append(' ').append(parameters.get(i));
            }
        }
        return builder;
    }

    public List<String> getParamsByType(String type) {
        return typeToParameters.get(type);
    }

    public Drop withIfExists(boolean ifExists) {
        this.setIfExists(ifExists);
        return this;
    }

    public Drop withMaterialized(boolean materialized) {
        this.setMaterialized(materialized);
        return this;
    }

    public Drop withType(String type) {
        this.setType(type);
        return this;
    }

    public Drop withName(Table name) {
        this.setName(name);
        return this;
    }

    public Drop withNames(Collection<? extends Table> names) {
        setNames(names);
        return this;
    }

    public Drop addNames(Table... names) {
        Collections.addAll(this.names, names);
        return this;
    }

    public Drop withObjectType(ObjectType objectType) {
        setObjectType(objectType);
        return this;
    }

    public Drop withParameters(List<String> parameters) {
        this.setParameters(parameters);
        return this;
    }

    public Drop addParameters(String... parameters) {
        return addParameters(java.util.Arrays.asList(parameters));
    }

    /** Appends trailing tokens without discarding a structured ON table. */
    public Drop addParameters(Collection<String> parameters) {
        if (this.parameters == null) {
            this.parameters = new ArrayList<>();
        }
        this.parameters.addAll(parameters);
        return this;
    }
}
