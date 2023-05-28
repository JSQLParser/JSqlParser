/*
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2020 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.schema;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

/**
 * Represents the database type for a {@code SEQUENCE}
 */
public class Sequence extends ASTNodeAccessImpl implements MultiPartName {

    private static final int NAME_IDX = 0;

    private static final int SCHEMA_IDX = 1;

    private static final int DATABASE_IDX = 2;

    private static final int SERVER_IDX = 3;

    private List<String> partItems = new ArrayList<>();

    private List<Parameter> parameters;

    public Sequence() {
    }

    public Sequence(List<String> partItems) {
        this.partItems = new ArrayList<>(partItems);
        Collections.reverse(this.partItems);
    }

    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public Database getDatabase() {
        return new Database(getIndex(DATABASE_IDX));
    }

    public void setDatabase(Database database) {
        setIndex(DATABASE_IDX, database.getDatabaseName());
        if (database.getServer() != null) {
            setIndex(SERVER_IDX, database.getServer().getFullyQualifiedName());
        }
    }

    public Sequence withDatabase(Database database) {
        setDatabase(database);
        return this;
    }

    public String getSchemaName() {
        return getIndex(SCHEMA_IDX);
    }

    public void setSchemaName(String string) {
        setIndex(SCHEMA_IDX, string);
    }

    public Sequence withSchemaName(String string) {
        setSchemaName(string);
        return this;
    }

    public String getName() {
        return getIndex(NAME_IDX);
    }

    public void setName(String string) {
        setIndex(NAME_IDX, string);
    }

    public Sequence withName(String string) {
        setName(string);
        return this;
    }

    private void setIndex(int idx, String value) {
        int size = partItems.size();
        for (int i = 0; i < idx - size + 1; i++) {
            partItems.add(null);
        }
        partItems.set(idx, value);
    }

    private String getIndex(int idx) {
        if (idx < partItems.size()) {
            return partItems.get(idx);
        } else {
            return null;
        }
    }

    @Override
    public String getFullyQualifiedName() {
        StringBuilder fqn = new StringBuilder();
        for (int i = partItems.size() - 1; i >= 0; i--) {
            String part = partItems.get(i);
            if (part == null) {
                part = "";
            }
            fqn.append(part);
            if (i != 0) {
                fqn.append(".");
            }
        }
        return fqn.toString();
    }

    @Override
    public String toString() {
        StringBuilder sql = new StringBuilder(getFullyQualifiedName());
        if (parameters != null) {
            for (Sequence.Parameter parameter : parameters) {
                sql.append(" ").append(parameter.formatParameter());
            }
        }
        return sql.toString();
    }

    public Sequence withParameters(List<Parameter> parameters) {
        this.setParameters(parameters);
        return this;
    }

    public Sequence addParameters(Parameter... parameters) {
        List<Parameter> collection = Optional.ofNullable(getParameters()).orElseGet(ArrayList::new);
        Collections.addAll(collection, parameters);
        return this.withParameters(collection);
    }

    public Sequence addParameters(Collection<? extends Parameter> parameters) {
        List<Parameter> collection = Optional.ofNullable(getParameters()).orElseGet(ArrayList::new);
        collection.addAll(parameters);
        return this.withParameters(collection);
    }

    /**
     * The available parameters to a sequence
     */
    public enum ParameterType {

        INCREMENT_BY,
        START_WITH,
        RESTART_WITH,
        MAXVALUE,
        NOMAXVALUE,
        MINVALUE,
        NOMINVALUE,
        CYCLE,
        NOCYCLE,
        CACHE,
        NOCACHE,
        ORDER,
        NOORDER,
        KEEP,
        NOKEEP,
        SESSION,
        GLOBAL
    }

    /**
     * Represents a parameter when declaring a sequence
     */
    public static class Parameter {

        private final ParameterType option;

        private Long value;

        public Parameter(ParameterType option) {
            this.option = option;
        }

        public Long getValue() {
            return value;
        }

        public void setValue(Long value) {
            this.value = value;
        }

        public String formatParameter() {
            switch(option) {
                case INCREMENT_BY:
                    return prefix("INCREMENT BY");
                case START_WITH:
                    return prefix("START WITH");
                case RESTART_WITH:
                    if (value != null) {
                        return prefix("RESTART WITH");
                    } else {
                        return "RESTART";
                    }
                case MAXVALUE:
                case MINVALUE:
                case CACHE:
                    return prefix(option.name());
                default:
                    // fallthrough just return option name
                    return option.name();
            }
        }

        private String prefix(String prefix) {
            return prefix + " " + value;
        }

        public Parameter withValue(Long value) {
            this.setValue(value);
            return this;
        }
    }
}
