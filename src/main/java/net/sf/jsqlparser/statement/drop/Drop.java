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
import java.util.List;
import java.util.Optional;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.DDLStatement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.select.PlainSelect;

public class Drop extends DDLStatement {

    private String type;
    private Table name;
    private List<String> parameters;
    private boolean ifExists = false;

    @Override
    public void accept(StatementVisitor statementVisitor) {
        statementVisitor.visit(this);
    }

    public Table getName() {
        return name;
    }

    public List<String> getParameters() {
        return parameters;
    }

    public String getType() {
        return type;
    }

    public void setName(Table string) {
        name = string;
    }

    public void setParameters(List<String> list) {
        parameters = list;
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

    public Drop withIfExists(boolean ifExists) {
        this.setIfExists(ifExists);
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

    @Override
    public StringBuilder appendTo(StringBuilder builder) {
        builder.append("DROP ").append(type).append(" ").append(ifExists ? "IF EXISTS " : "").append(name.toString());

        if (parameters != null && !parameters.isEmpty()) {
            builder.append(" ").append(PlainSelect.getStringList(parameters));
        }
        return builder;
    }
}
