/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2020 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.grant;

import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

import java.util.List;

public class Grant implements Statement {

    private String role;
    private List<String> privileges;
    private String objectName;
    private List<String> users;

    @Override
    public void accept(StatementVisitor statementVisitor) {
        statementVisitor.visit(this);
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public List<String> getPrivileges() {
        return privileges;
    }

    public void setPrivileges(List<String> privileges) {
        this.privileges = privileges;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public List<String> getUsers() {
        return users;
    }

    public void setUsers(List<String> users) {
        this.users = users;
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();

        buffer.append("GRANT ");
        if (role != null) {
            buffer.append(role);
        } else {
            for (int i = 0; i < getPrivileges().size(); i++) {
                if (i != 0) {
                    buffer.append(", ");
                }
                buffer.append(privileges.get(i));
            }
            buffer.append(" ON ");
            buffer.append(getObjectName());
        }
        buffer.append(" TO ");
        for (int i = 0; i < getUsers().size(); i++) {
            if (i != 0) {
                buffer.append(", ");
            }
            buffer.append(users.get(i));
        }
        return buffer.toString();
    }

    public Grant role(String role) {
        this.setRole(role);
        return this;
    }

    public Grant privileges(List<String> privileges) {
        this.setPrivileges(privileges);
        return this;
    }

    public Grant objectName(String objectName) {
        this.setObjectName(objectName);
        return this;
    }

    public Grant users(List<String> users) {
        this.setUsers(users);
        return this;
    }
}
