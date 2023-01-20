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

public class ReferentialAction implements Serializable {

    private Type type;
    private Action action;

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
        return new StringBuilder(" ON ").append(getType().name()).append(" ").append(getAction().getAction())
                .toString();
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
//        if (action != other.action) {
//            return false;
//        }
//        if (type != other.type) {
//            return false;
        return action==other.action && type == other.type;
    }

    public enum Type {
        DELETE,
        UPDATE
    }

    public enum Action {
        CASCADE("CASCADE"),
        RESTRICT("RESTRICT"),
        NO_ACTION("NO ACTION"),
        SET_DEFAULT("SET DEFAULT"),
        SET_NULL("SET NULL");

        Action(String action) {
            this.action = action;
        }

        private final String action;

        /**
         * @param action
         * @return the {@link Action}, if found, otherwise <code>null</code>
         */
        public static Action byAction(String action) {
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

        @Override
        public String toString() {
            return action;
        }
    }

}
