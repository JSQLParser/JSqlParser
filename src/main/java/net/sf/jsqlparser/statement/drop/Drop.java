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
import java.util.Map;
import java.util.Optional;

import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.select.PlainSelect;

public class Drop implements Statement {

    private String type;
    private Table name;
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
        return name;
    }

    public void setName(Table string) {
        name = string;
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
                + (ifExists ? "IF EXISTS " : "") + name.toString();

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
