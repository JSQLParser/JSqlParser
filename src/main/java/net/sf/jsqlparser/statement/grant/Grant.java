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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

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

    public Grant addPrivileges(String... privileges) {
        List<String> collection = Optional.ofNullable(getPrivileges()).orElseGet(ArrayList::new);
        Collections.addAll(collection, privileges);
        return this.privileges(collection);
    }

    public Grant addPrivileges(Collection<String> privileges) {
        List<String> collection = Optional.ofNullable(getPrivileges()).orElseGet(ArrayList::new);
        collection.addAll(privileges);
        return this.privileges(collection);
    }

    public Grant addUsers(String... users) {
        List<String> collection = Optional.ofNullable(getUsers()).orElseGet(ArrayList::new);
        Collections.addAll(collection, users);
        return this.users(collection);
    }

    public Grant addUsers(Collection<String> users) {
        List<String> collection = Optional.ofNullable(getUsers()).orElseGet(ArrayList::new);
        collection.addAll(users);
        return this.users(collection);
    }
}
