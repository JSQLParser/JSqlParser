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

import java.util.List;
import net.sf.jsqlparser.schema.Table;

import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.select.PlainSelect;

public class Drop implements Statement {

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

    @Override
    public String toString() {
        String sql = "DROP " + type + " " + (ifExists ? "IF EXISTS " : "") + name.toString();

        if (parameters != null && !parameters.isEmpty()) {
            sql += " " + PlainSelect.getStringList(parameters);
        }

        return sql;
    }
}
