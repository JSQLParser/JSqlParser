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

import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

/**
 * PostgreSQL {@code ALTER RULE name ON table RENAME TO new_name}. Names retain SQL identifier
 * quoting.
 */
public class AlterRule extends ASTNodeAccessImpl implements Statement {
    private String name;
    private Table table;
    private String newName;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The table or view the rule applies to.
     *
     * @return the table
     */
    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public String getNewName() {
        return newName;
    }

    public void setNewName(String newName) {
        this.newName = newName;
    }

    public AlterRule withName(String name) {
        setName(name);
        return this;
    }

    public AlterRule withTable(Table table) {
        setTable(table);
        return this;
    }

    public AlterRule withNewName(String newName) {
        setNewName(newName);
        return this;
    }

    public StringBuilder appendTo(StringBuilder builder) {
        return builder.append("ALTER RULE ").append(name).append(" ON ").append(table)
                .append(" RENAME TO ").append(newName);
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
