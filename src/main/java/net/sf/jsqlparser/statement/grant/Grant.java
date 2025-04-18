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

import static java.util.stream.Collectors.joining;

import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

public class Grant implements Statement {

    private String role;
    private List<String> privileges;
    private final List<String> objectName = new ArrayList<>();
    private List<String> users;

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
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
        return objectName.isEmpty() ? null
                : objectName.stream()
                        .map(part -> part == null ? "" : part)
                        .collect(joining("."));
    }

    public void setObjectName(String objectName) {
        this.objectName.clear();
        this.objectName.add(objectName);
    }

    public void setObjectName(List<String> objectName) {
        this.objectName.clear();
        this.objectName.addAll(objectName);
    }

    public List<String> getObjectNameParts() {
        return objectName;
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

    public Grant withRole(String role) {
        this.setRole(role);
        return this;
    }

    public Grant withPrivileges(List<String> privileges) {
        this.setPrivileges(privileges);
        return this;
    }

    public Grant withObjectName(String objectName) {
        this.setObjectName(objectName);
        return this;
    }

    public Grant withObjectName(List<String> objectName) {
        this.setObjectName(objectName);
        return this;
    }

    public Grant withUsers(List<String> users) {
        this.setUsers(users);
        return this;
    }

    public Grant addPrivileges(String... privileges) {
        List<String> collection = Optional.ofNullable(getPrivileges()).orElseGet(ArrayList::new);
        Collections.addAll(collection, privileges);
        return this.withPrivileges(collection);
    }

    public Grant addPrivileges(Collection<String> privileges) {
        List<String> collection = Optional.ofNullable(getPrivileges()).orElseGet(ArrayList::new);
        collection.addAll(privileges);
        return this.withPrivileges(collection);
    }

    public Grant addUsers(String... users) {
        List<String> collection = Optional.ofNullable(getUsers()).orElseGet(ArrayList::new);
        Collections.addAll(collection, users);
        return this.withUsers(collection);
    }

    public Grant addUsers(Collection<String> users) {
        List<String> collection = Optional.ofNullable(getUsers()).orElseGet(ArrayList::new);
        collection.addAll(users);
        return this.withUsers(collection);
    }
}
