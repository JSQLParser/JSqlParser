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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.sf.jsqlparser.statement.DefinitionElement;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

/**
 * PostgreSQL ALTER TEXT SEARCH DICTIONARY. Options can be set or removed; ownership, rename and
 * schema changes are supported.
 */
public class AlterTextSearchDictionary extends ASTNodeAccessImpl implements Statement {
    public enum Action {
        OPTIONS, RENAME, OWNER, SET_SCHEMA
    }

    private String name;
    private Action action;
    private String newName;
    private String schemaName;
    private String owner;
    private List<DefinitionElement> options = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AlterTextSearchDictionary withName(String name) {
        setName(name);
        return this;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public AlterTextSearchDictionary withAction(Action action) {
        setAction(action);
        return this;
    }

    public String getNewName() {
        return newName;
    }

    public void setNewName(String newName) {
        this.newName = newName;
    }

    public AlterTextSearchDictionary withNewName(String newName) {
        setNewName(newName);
        return this;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public AlterTextSearchDictionary withSchemaName(String schemaName) {
        setSchemaName(schemaName);
        return this;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public AlterTextSearchDictionary withOwner(String owner) {
        setOwner(owner);
        return this;
    }

    public List<DefinitionElement> getOptions() {
        return options;
    }

    public void setOptions(List<DefinitionElement> options) {
        this.options = options;
    }

    public AlterTextSearchDictionary withOptions(List<DefinitionElement> options) {
        setOptions(options);
        return this;
    }

    /** Returns the last matching option; a bare option has a null argument and removes it. */
    public DefinitionElement getOption(String name) {
        return find(options, name);
    }

    private static DefinitionElement find(List<DefinitionElement> elements, String name) {
        String requested = name.startsWith("\"")
                ? name.substring(1, name.length() - 1).replace("\"\"", "\"")
                : name.toLowerCase(Locale.ROOT);
        for (int i = elements.size() - 1; i >= 0; i--) {
            DefinitionElement element = elements.get(i);
            String key = element.getName();
            if (key.startsWith("\"")) {
                key = key.substring(1, key.length() - 1).replace("\"\"", "\"");
            } else {
                key = key.toLowerCase(Locale.ROOT);
            }
            if (requested.equals(key)) {
                return element;
            }
        }
        return null;
    }

    public StringBuilder appendTo(StringBuilder sql) {
        sql.append("ALTER TEXT SEARCH DICTIONARY ").append(name);
        switch (action) {
            case OPTIONS:
                return sql.append(' ').append(DefinitionElement.toString(options));
            case OWNER:
                return sql.append(" OWNER TO ").append(owner);
            case RENAME:
                return sql.append(" RENAME TO ").append(newName);
            case SET_SCHEMA:
                return sql.append(" SET SCHEMA ").append(schemaName);
            default:
                throw new IllegalStateException("Unknown text-search action: " + action);
        }
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
