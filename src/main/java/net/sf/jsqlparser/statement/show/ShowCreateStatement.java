/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.show;

import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.create.user.UserAccount;

/**
 * A MySQL {@code SHOW CREATE} statement, which returns the definition of one object.
 *
 * <ul>
 * <li>{@code TABLE}, {@code VIEW}, {@code PROCEDURE}, {@code FUNCTION}, {@code TRIGGER} and
 * {@code EVENT} name the object with {@link #getName()}.</li>
 * <li>{@code DATABASE} (also spelled {@code SCHEMA}) uses {@link #getDatabaseName()} and may carry
 * {@code IF NOT EXISTS}.</li>
 * <li>{@code USER} uses {@link #getAccount()}, which is {@code CURRENT_USER} for the session
 * account.</li>
 * </ul>
 *
 * @see <a href="https://dev.mysql.com/doc/refman/8.4/en/show-create-table.html">MySQL SHOW CREATE
 *      TABLE</a>
 */
public class ShowCreateStatement extends ASTNodeAccessImpl implements Statement {

    public enum ObjectType {
        TABLE, VIEW, PROCEDURE, FUNCTION, TRIGGER, EVENT, DATABASE, USER;

        /** Whether objects of this type are named by {@link ShowCreateStatement#getName()}. */
        public boolean isNamedByTable() {
            return this != DATABASE && this != USER;
        }
    }

    private ObjectType objectType;
    private boolean schemaKeyword;
    private boolean ifNotExists;
    private Table name;
    private String databaseName;
    private UserAccount account;
    private boolean currentUserParentheses;

    public ShowCreateStatement() {
        // empty constructor
    }

    public ShowCreateStatement(ObjectType objectType, Table name) {
        this.objectType = objectType;
        this.name = name;
    }

    public ObjectType getObjectType() {
        return objectType;
    }

    public void setObjectType(ObjectType objectType) {
        this.objectType = objectType;
    }

    /** Whether {@code DATABASE} was spelled {@code SCHEMA}. */
    public boolean isSchemaKeyword() {
        return schemaKeyword;
    }

    public void setSchemaKeyword(boolean schemaKeyword) {
        this.schemaKeyword = schemaKeyword;
    }

    /** Whether {@code SHOW CREATE DATABASE IF NOT EXISTS} was written. */
    public boolean isIfNotExists() {
        return ifNotExists;
    }

    public void setIfNotExists(boolean ifNotExists) {
        this.ifNotExists = ifNotExists;
    }

    /** The table, view, routine, trigger or event name; null for DATABASE and USER. */
    public Table getName() {
        return name;
    }

    public void setName(Table name) {
        this.name = name;
    }

    /** The database name of {@code SHOW CREATE DATABASE}; null otherwise. */
    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    /** The account of {@code SHOW CREATE USER}; null otherwise. */
    public UserAccount getAccount() {
        return account;
    }

    public void setAccount(UserAccount account) {
        this.account = account;
    }

    /** Whether the account was written as {@code CURRENT_USER()}. */
    public boolean isCurrentUserParentheses() {
        return currentUserParentheses;
    }

    public void setCurrentUserParentheses(boolean currentUserParentheses) {
        this.currentUserParentheses = currentUserParentheses;
    }

    /** Whether the account is {@code CURRENT_USER} or {@code CURRENT_USER()}. */
    public boolean isCurrentUser() {
        if (account == null || account.getHost() != null || account.getUser() == null) {
            return false;
        }
        StringValue user = account.getUser();
        return "".equals(user.getQuoteStr()) && "CURRENT_USER".equalsIgnoreCase(user.getValue());
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("SHOW CREATE ");
        if (objectType == ObjectType.DATABASE) {
            builder.append(schemaKeyword ? "SCHEMA" : "DATABASE");
            if (ifNotExists) {
                builder.append(" IF NOT EXISTS");
            }
            builder.append(' ').append(databaseName);
        } else if (objectType == ObjectType.USER) {
            builder.append("USER ").append(account);
            if (currentUserParentheses) {
                builder.append("()");
            }
        } else {
            builder.append(objectType).append(' ').append(name);
        }
        return builder.toString();
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }

    public ShowCreateStatement withObjectType(ObjectType objectType) {
        this.setObjectType(objectType);
        return this;
    }

    public ShowCreateStatement withSchemaKeyword(boolean schemaKeyword) {
        this.setSchemaKeyword(schemaKeyword);
        return this;
    }

    public ShowCreateStatement withIfNotExists(boolean ifNotExists) {
        this.setIfNotExists(ifNotExists);
        return this;
    }

    public ShowCreateStatement withName(Table name) {
        this.setName(name);
        return this;
    }

    public ShowCreateStatement withDatabaseName(String databaseName) {
        this.setDatabaseName(databaseName);
        return this;
    }

    public ShowCreateStatement withAccount(UserAccount account) {
        this.setAccount(account);
        return this;
    }

    public ShowCreateStatement withCurrentUserParentheses(boolean currentUserParentheses) {
        this.setCurrentUserParentheses(currentUserParentheses);
        return this;
    }
}
