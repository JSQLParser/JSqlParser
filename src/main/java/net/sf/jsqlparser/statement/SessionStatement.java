/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2025 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class SessionStatement implements Statement {
    public enum Action {
        START, APPLY, DROP, SHOW, DESCRIBE;

        public static Action from(String flag) {
            return Enum.valueOf(Action.class, flag.toUpperCase());
        }
    }

    final private Action action;
    final private String id;
    final private LinkedHashMap<String, String> options = new LinkedHashMap<>();

    public SessionStatement(Action action, String id) {
        this.action = action;
        this.id = id;
    }

    public SessionStatement(String action, String id) {
        this(Action.from(action), id);
    }

    public SessionStatement(String action) {
        this(action, null);
    }


    public Action getAction() {
        return action;
    }

    public String getId() {
        return id;
    }

    public int size() {
        return options.size();
    }

    public String putOption(String key, String value) {
        return options.put(key.replaceAll("[\"']", "").toLowerCase(), value.toLowerCase());
    }

    public boolean hasOptions() {
        return !options.isEmpty();
    }

    public void clearOptions() {
        options.clear();
    }

    public boolean removeOption(String key, String value) {
        return options.remove(key, value);
    }

    public boolean containsOption(String value) {
        return options.containsValue(value);
    }

    public String removeOption(String key) {
        return options.remove(key);
    }

    public String getOption(String key) {
        return options.get(key);
    }

    public Set<String> getOptionKeySet() {
        return options.keySet();
    }

    public Set<Map.Entry<String, String>> getOptions() {
        return options.entrySet();
    }

    public boolean hasOption(String key) {
        return options.containsKey(key);
    }

    public String getOptionOrDefault(String key, String defaultValue) {
        return options.getOrDefault(key, defaultValue);
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }

    @Override
    public void accept(StatementVisitor<?> statementVisitor) {
        Statement.super.accept(statementVisitor);
    }

    @Override
    public String toString() {
        StringBuilder builder =
                new StringBuilder("SESSION " + action + " " + (id != null ? id : ""));
        if (!options.isEmpty()) {
            builder.append(" WITH ");
            int i = 0;
            for (Map.Entry<String, String> e : options.entrySet()) {
                if (i++ > 0) {
                    builder.append(", ");
                }
                builder.append(e.getKey()).append("=").append(e.getValue());
            }
        }
        builder.append(";");

        return builder.toString();
    }
}
