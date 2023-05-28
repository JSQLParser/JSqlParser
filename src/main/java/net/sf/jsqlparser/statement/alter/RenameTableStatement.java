/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2021 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.alter;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

/**
 * @author are
 * @see  <a href="https://docs.oracle.com/cd/B19306_01/server.102/b14200/statements_9019.htm">Rename</a>
 */
public class RenameTableStatement implements Statement {

    private final LinkedHashMap<Table, Table> tableNames = new LinkedHashMap<>();

    private boolean usingTableKeyword = false;

    private boolean usingIfExistsKeyword = false;

    private String waitDirective = "";

    public RenameTableStatement(Table oldName, Table newName) {
        tableNames.put(Objects.requireNonNull(oldName, "The OLD NAME of the Rename Statement must not be null."), Objects.requireNonNull(newName, "The NEW NAME of the Rename Statement must not be null."));
    }

    public RenameTableStatement(Table oldName, Table newName, boolean usingTableKeyword, boolean usingIfExistsKeyword, String waitDirective) {
        tableNames.put(Objects.requireNonNull(oldName, "The OLD NAME of the Rename Statement must not be null."), Objects.requireNonNull(newName, "The NEW NAME of the Rename Statement must not be null."));
        this.usingTableKeyword = usingTableKeyword;
        this.usingIfExistsKeyword = usingIfExistsKeyword;
        this.waitDirective = waitDirective;
    }

    public void addTableNames(Table oldName, Table newName) {
        tableNames.put(Objects.requireNonNull(oldName, "The OLD NAME of the Rename Statement must not be null."), Objects.requireNonNull(newName, "The NEW NAME of the Rename Statement must not be null."));
    }

    public boolean isUsingTableKeyword() {
        return usingTableKeyword;
    }

    public void setUsingTableKeyword(boolean usingTableKeyword) {
        this.usingTableKeyword = usingTableKeyword;
    }

    public RenameTableStatement withUsingTableKeyword(boolean usingTableKeyword) {
        this.usingTableKeyword = usingTableKeyword;
        return this;
    }

    public boolean isUsingIfExistsKeyword() {
        return usingIfExistsKeyword;
    }

    public void setUsingIfExistsKeyword(boolean usingIfExistsKeyword) {
        this.usingIfExistsKeyword = usingIfExistsKeyword;
    }

    public RenameTableStatement withUsingIfExistsKeyword(boolean usingIfExistsKeyword) {
        this.usingIfExistsKeyword = usingIfExistsKeyword;
        return this;
    }

    public String getWaitDirective() {
        return waitDirective;
    }

    public void setWaitDirective(String waitDirective) {
        this.waitDirective = waitDirective;
    }

    public RenameTableStatement withWaitDirective(String waitDirective) {
        this.waitDirective = waitDirective;
        return this;
    }

    public int getTableNamesSize() {
        return tableNames.size();
    }

    public boolean isTableNamesEmpty() {
        return tableNames.isEmpty();
    }

    public Set<Map.Entry<Table, Table>> getTableNames() {
        return tableNames.entrySet();
    }

    @Override
    public void accept(StatementVisitor statementVisitor) {
        statementVisitor.visit(this);
    }

    public StringBuilder appendTo(StringBuilder builder) {
        int i = 0;
        for (Entry<Table, Table> e : tableNames.entrySet()) {
            if (i == 0) {
                builder.append("RENAME").append(usingTableKeyword ? " TABLE " : " ").append(usingIfExistsKeyword ? " IF EXISTS " : " ").append(e.getKey()).append(waitDirective != null && waitDirective.length() > 0 ? " " + waitDirective : "").append(" TO ").append(e.getValue());
            } else {
                builder.append(", ").append(e.getKey()).append(" TO ").append(e.getValue());
            }
            i++;
        }
        return builder;
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }
}
