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
import java.util.Optional;
import java.util.stream.Collectors;

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

    public List<String> getParameters() {
        return parameters;
    }

    public void setParameters(List<String> list) {
        parameters = list;
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
        String sql = "DROP "
                + (isUsingTemporary ? "TEMPORARY " : "")
                + (materialized ? "MATERIALIZED " : "")
                + type + " "
                + (ifExists ? "IF EXISTS " : "") + names.stream().map(Table::toString)
                        .collect(Collectors.joining(", "));

        if (type.equals("FUNCTION")) {
            sql += formatFuncParams(getParamsByType("FUNCTION"));
        }

        if (parameters != null && !parameters.isEmpty()) {
            sql += " " + PlainSelect.getStringList(parameters, false, false);
        }

        return sql;
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
        List<String> collection = Optional.ofNullable(getParameters()).orElseGet(ArrayList::new);
        Collections.addAll(collection, parameters);
        return this.withParameters(collection);
    }

    public Drop addParameters(Collection<String> parameters) {
        List<String> collection = Optional.ofNullable(getParameters()).orElseGet(ArrayList::new);
        collection.addAll(parameters);
        return this.withParameters(collection);
    }
}
