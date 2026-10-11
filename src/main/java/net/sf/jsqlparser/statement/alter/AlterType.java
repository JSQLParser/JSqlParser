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
import java.util.stream.Collectors;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.statement.CascadeBehavior;
import net.sf.jsqlparser.statement.create.type.TypeAttribute;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

public class AlterType implements Statement {
    private String name;
    private Action action;
    private String newName;
    private String attributeName;
    private StringValue value;
    private StringValue newValue;
    private StringValue neighborValue;
    private Position position;
    private boolean ifNotExists;
    private CascadeBehavior behavior;
    private List<AttributeChange> attributeChanges = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public String getNewName() {
        return newName;
    }

    public void setNewName(String newName) {
        this.newName = newName;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public StringValue getValue() {
        return value;
    }

    public void setValue(StringValue value) {
        this.value = value;
    }

    public StringValue getNewValue() {
        return newValue;
    }

    public void setNewValue(StringValue newValue) {
        this.newValue = newValue;
    }

    public StringValue getNeighborValue() {
        return neighborValue;
    }

    public void setNeighborValue(StringValue neighborValue) {
        this.neighborValue = neighborValue;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public boolean isIfNotExists() {
        return ifNotExists;
    }

    public void setIfNotExists(boolean ifNotExists) {
        this.ifNotExists = ifNotExists;
    }

    public CascadeBehavior getBehavior() {
        return behavior;
    }

    public void setBehavior(CascadeBehavior behavior) {
        this.behavior = behavior;
    }

    public List<AttributeChange> getAttributeChanges() {
        return attributeChanges;
    }

    public void setAttributeChanges(List<AttributeChange> attributeChanges) {
        this.attributeChanges = attributeChanges;
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> visitor, S context) {
        return visitor.visit(this, context);
    }

    public enum Action {
        OWNER, RENAME, SET_SCHEMA, RENAME_ATTRIBUTE, ADD_VALUE, RENAME_VALUE, ATTRIBUTES
    }
    public enum Position {
        BEFORE, AFTER
    }
    public static class AttributeChange implements java.io.Serializable {
        public enum Kind {
            ADD, DROP, ALTER
        }

        private Kind kind;
        private TypeAttribute attribute;
        private boolean ifExists;
        private boolean useSetData;
        private CascadeBehavior behavior;

        public Kind getKind() {
            return kind;
        }

        public void setKind(Kind kind) {
            this.kind = kind;
        }

        public TypeAttribute getAttribute() {
            return attribute;
        }

        public void setAttribute(TypeAttribute attribute) {
            this.attribute = attribute;
        }

        public boolean isIfExists() {
            return ifExists;
        }

        public void setIfExists(boolean ifExists) {
            this.ifExists = ifExists;
        }

        public boolean isUseSetData() {
            return useSetData;
        }

        public void setUseSetData(boolean useSetData) {
            this.useSetData = useSetData;
        }

        public CascadeBehavior getBehavior() {
            return behavior;
        }

        public void setBehavior(CascadeBehavior behavior) {
            this.behavior = behavior;
        }

        @Override
        public String toString() {
            StringBuilder sql = new StringBuilder().append(kind).append(" ATTRIBUTE ");
            if (ifExists) {
                sql.append("IF EXISTS ");
            }
            sql.append(attribute.getName());
            if (kind == Kind.ALTER) {
                sql.append(useSetData ? " SET DATA TYPE" : " TYPE");
            }
            if (kind != Kind.DROP) {
                sql.append(' ').append(attribute.getDataType());
                if (attribute.getCollation() != null) {
                    sql.append(" COLLATE ").append(attribute.getCollation());
                }
            }
            if (behavior != null) {
                sql.append(' ').append(behavior);
            }
            return sql.toString();
        }
    }

    @Override
    public String toString() {
        StringBuilder sql = new StringBuilder("ALTER TYPE ").append(name).append(' ');
        switch (action) {
            case OWNER:
                sql.append("OWNER TO ").append(newName);
                break;
            case RENAME:
                sql.append("RENAME TO ").append(newName);
                break;
            case SET_SCHEMA:
                sql.append("SET SCHEMA ").append(newName);
                break;
            case RENAME_ATTRIBUTE:
                sql.append("RENAME ATTRIBUTE ").append(attributeName).append(" TO ")
                        .append(newName);
                if (behavior != null) {
                    sql.append(' ').append(behavior);
                }
                break;
            case ADD_VALUE:
                sql.append("ADD VALUE ").append(ifNotExists ? "IF NOT EXISTS " : "").append(value);
                if (position != null) {
                    sql.append(' ').append(position).append(' ').append(neighborValue);
                }
                break;
            case RENAME_VALUE:
                sql.append("RENAME VALUE ").append(value).append(" TO ").append(newValue);
                break;
            case ATTRIBUTES:
                sql.append(attributeChanges.stream().map(Object::toString)
                        .collect(Collectors.joining(", ")));
                break;
            default:
                throw new IllegalStateException("Unknown type alteration: " + action);
        }
        return sql.toString();
    }
}
