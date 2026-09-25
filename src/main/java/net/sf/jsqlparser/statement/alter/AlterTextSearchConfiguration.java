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

import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import java.util.ArrayList;
import java.util.List;

public class AlterTextSearchConfiguration implements Statement {
    public enum Action {
        ADD_MAPPING, ALTER_MAPPING, REPLACE_MAPPING, DROP_MAPPING, RENAME, OWNER, SET_SCHEMA
    }

    private String name;
    private Action action;
    private List<String> tokenTypes = new ArrayList<>();
    private List<String> dictionaries = new ArrayList<>();
    private String oldDictionary;
    private String newDictionary;
    private boolean ifExists;
    private String newName;
    private String owner;
    private String schemaName;

    public String getName() {
        return name;
    }

    public void setName(String value) {
        name = value;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action value) {
        action = value;
    }

    public List<String> getTokenTypes() {
        return tokenTypes;
    }

    public void setTokenTypes(List<String> value) {
        tokenTypes = value;
    }

    public List<String> getDictionaries() {
        return dictionaries;
    }

    public void setDictionaries(List<String> value) {
        dictionaries = value;
    }

    public String getOldDictionary() {
        return oldDictionary;
    }

    public void setOldDictionary(String value) {
        oldDictionary = value;
    }

    public String getNewDictionary() {
        return newDictionary;
    }

    public void setNewDictionary(String value) {
        newDictionary = value;
    }

    public boolean isIfExists() {
        return ifExists;
    }

    public void setIfExists(boolean value) {
        ifExists = value;
    }

    public String getNewName() {
        return newName;
    }

    public void setNewName(String value) {
        newName = value;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String value) {
        owner = value;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String value) {
        schemaName = value;
    }

    public StringBuilder appendTo(StringBuilder sql) {
        sql.append("ALTER TEXT SEARCH CONFIGURATION ").append(name);
        switch (action) {
            case ADD_MAPPING:
            case ALTER_MAPPING:
                sql.append(
                        action == Action.ADD_MAPPING ? " ADD MAPPING FOR " : " ALTER MAPPING FOR ")
                        .append(String.join(", ", tokenTypes)).append(" WITH ")
                        .append(String.join(", ", dictionaries));
                break;
            case REPLACE_MAPPING:
                sql.append(" ALTER MAPPING");
                if (!tokenTypes.isEmpty()) {
                    sql.append(" FOR ").append(String.join(", ", tokenTypes));
                }
                sql.append(" REPLACE ").append(oldDictionary).append(" WITH ")
                        .append(newDictionary);
                break;
            case DROP_MAPPING:
                sql.append(" DROP MAPPING");
                if (ifExists) {
                    sql.append(" IF EXISTS");
                }
                sql.append(" FOR ").append(String.join(", ", tokenTypes));
                break;
            case RENAME:
                sql.append(" RENAME TO ").append(newName);
                break;
            case OWNER:
                sql.append(" OWNER TO ").append(owner);
                break;
            case SET_SCHEMA:
                sql.append(" SET SCHEMA ").append(schemaName);
                break;
            default:
                throw new IllegalStateException("Unknown text-search action: " + action);
        }
        return sql;
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> visitor, S context) {
        return visitor.visit(this, context);
    }
}
