/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2020 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import java.io.Serializable;
import java.util.Locale;
import java.util.List;
import java.util.Objects;
import net.sf.jsqlparser.statement.select.PlainSelect;

public class ReferentialAction implements Serializable {

    private Type type;
    private Action action;
    private List<String> columnNames;

    public ReferentialAction() {
        // default constructor
    }

    public ReferentialAction(Type type, Action action) {
        this.type = type;
        this.action = action;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public ReferentialAction withType(Type type) {
        setType(type);
        return this;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public ReferentialAction withAction(Action action) {
        setAction(action);
        return this;
    }

    /** Columns affected by PostgreSQL ON DELETE SET NULL or SET DEFAULT; null means all columns. */
    public List<String> getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(List<String> columnNames) {
        this.columnNames = columnNames;
    }

    public ReferentialAction withColumnNames(List<String> columnNames) {
        setColumnNames(columnNames);
        return this;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((action == null) ? 0 : action.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return " ON " + getType().name() + " " +
                getAction().getAction()
                + (columnNames == null ? ""
                        : " " + PlainSelect.getStringList(columnNames, true, true));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ReferentialAction other = (ReferentialAction) obj;
        return action == other.action && type == other.type
                && Objects.equals(columnNames, other.columnNames);
    }

    public enum Type {
        DELETE, UPDATE;

        public static Type from(String name) {
            return Enum.valueOf(Type.class, name.toUpperCase(Locale.ROOT));
        }
    }

    public enum Action {
        CASCADE("CASCADE"), RESTRICT("RESTRICT"), NO_ACTION("NO ACTION"), SET_DEFAULT(
                "SET DEFAULT"), SET_NULL("SET NULL");

        private final String action;

        Action(String action) {
            this.action = action;
        }

        /**
         * @param action
         * @return the {@link Action}, if found, otherwise <code>null</code>
         */
        public static Action from(String action) {
            // We can't use Enum.valueOf() since there White Space involved
            for (Action a : values()) {
                if (a.getAction().equals(action)) {
                    return a;
                }
            }
            return null;
        }

        public String getAction() {
            return action;
        }
    }

}
