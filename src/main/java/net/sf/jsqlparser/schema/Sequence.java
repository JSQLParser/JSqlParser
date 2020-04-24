/*
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2020 JSQLParser
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
package net.sf.jsqlparser.schema;

import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

import java.util.List;

/**
 * Represents the database type for a {@code SEQUENCE}
 */
public class Sequence extends ASTNodeAccessImpl implements MultiPartName {

    private String name;
    private String schemaName;
    private Database database;

    private List<Parameter> parameters;

    public Sequence() {
    }

    public Sequence(String name) {
        this.name = name;
    }

    public Sequence(String schemaName, String name) {
        this.schemaName = schemaName;
        this.schemaName = schemaName;
        this.name = name;
    }

    public Sequence(Database database, String schemaName, String name) {
        this.database = database;
        this.schemaName = schemaName;
        this.name = name;
    }

    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    @Override
    public String getFullyQualifiedName() {
        String fqn = "";

        if (database != null) {
            fqn += database.getFullyQualifiedName();
        }
        if (!fqn.isEmpty()) {
            fqn += ".";
        }

        if (schemaName != null) {
            fqn += schemaName;
        }
        if (!fqn.isEmpty()) {
            fqn += ".";
        }

        if (name != null) {
            fqn += name;
        }

        return fqn;
    }

    @Override
    public String toString() {
        StringBuilder sql = new StringBuilder(getFullyQualifiedName());
        if(parameters != null) {
            for (Sequence.Parameter parameter : parameters) {
                sql.append(" ").append(parameter.formatParameter());
            }
        }
        return sql.toString();
    }

    /**
     * The available parameters to a sequence
     */
    public enum ParameterType {
        INCREMENT_BY,
        START_WITH,
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
            switch (option) {
                case INCREMENT_BY:
                    return withValue("INCREMENT BY");
                case START_WITH:
                    return withValue("START WITH");
                case MAXVALUE:
                case MINVALUE:
                case CACHE:
                    return withValue(option.name());
                default:
                    // fallthrough just return option name
                    return option.name();
            }
        }

        private String withValue(String prefix) {
            return prefix + " " + value;
        }
    }
}
