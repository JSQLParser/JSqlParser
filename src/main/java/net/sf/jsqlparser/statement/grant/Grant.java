/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.grant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.create.user.RoleSelection;

public class Grant implements Statement {
    private PrivilegeClause clause = new PrivilegeClause();
    private final List<GrantOption> options = new ArrayList<>();
    private Grantee asUser;
    private RoleSelection withRole;

    public PrivilegeClause getClause() {
        return clause;
    }

    public void setClause(PrivilegeClause clause) {
        this.clause = clause;
    }

    /**
     * The {@code WITH} options as a mutable list: {@code GRANT OPTION}, {@code ADMIN OPTION} or
     * PostgreSQL membership options such as {@code INHERIT TRUE, SET FALSE}.
     */
    public List<GrantOption> getOptions() {
        return options;
    }

    public void setOptions(Collection<? extends GrantOption> options) {
        List<GrantOption> copy = options == null ? new ArrayList<>() : new ArrayList<>(options);
        this.options.clear();
        this.options.addAll(copy);
    }

    /** The first {@code WITH} option, or null; see {@link #getOptions()}. */
    public GrantOption getOption() {
        return options.isEmpty() ? null : options.get(0);
    }

    /** Replaces all {@code WITH} options with the given option, or removes them for null. */
    public void setOption(GrantOption option) {
        setOptions(option == null ? null : Collections.singletonList(option));
    }

    /** The MySQL {@code AS user} account, or null when omitted. */
    public Grantee getAsUser() {
        return asUser;
    }

    public void setAsUser(Grantee asUser) {
        this.asUser = asUser;
    }

    /** The MySQL {@code WITH ROLE} selection of the {@code AS user} clause, or null. */
    public RoleSelection getWithRole() {
        return withRole;
    }

    public void setWithRole(RoleSelection withRole) {
        this.withRole = withRole;
    }

    public List<Privilege> getPrivilegeItems() {
        return clause.getPrivileges();
    }

    public void setPrivilegeItems(List<Privilege> privileges) {
        clause.setPrivileges(privileges);
    }

    /** The target, or null for a role membership. */
    public PrivilegeTarget getTarget() {
        return clause.getTarget();
    }

    public void setTarget(PrivilegeTarget target) {
        clause.setTarget(target);
    }

    public List<String> getRoles() {
        return clause.getRoles();
    }

    public void setRoles(List<String> roles) {
        clause.setRoles(roles);
    }

    public String getRole() {
        return clause.getRoleItems().isEmpty() ? null : clause.getRoles().get(0);
    }

    /** Makes this a membership of the given role, which has neither privileges nor a target. */
    public void setRole(String role) {
        clause.setRoles(new ArrayList<>());
        if (role != null) {
            clause.getRoles().add(role);
            clause.setPrivileges(null);
            clause.setTarget(null);
        }
    }

    /** Mutable legacy string view over the typed privileges. */
    public List<String> getPrivileges() {
        if (clause.getPrivileges() == null) {
            return null;
        }
        return new StringListView<>(() -> clause.getPrivileges(), Privilege::new);
    }

    public void setPrivileges(List<String> privileges) {
        List<Privilege> items = null;
        if (privileges != null) {
            items = new ArrayList<>();
            for (String privilege : privileges) {
                items.add(new Privilege(privilege));
            }
        }
        clause.setPrivileges(items);
    }

    /**
     * The first named target as dotted text. MySQL wildcard levels keep their wildcard, for example
     * {@code db.*} or {@code *.*}.
     */
    public String getObjectName() {
        PrivilegeTarget target = clause.getTarget();
        if (target == null) {
            return null;
        }
        if (target.getWildcard() != null) {
            return target.getTargetText();
        }
        if (target.getNames().isEmpty() || target.getNames().get(0).isEmpty()) {
            return null;
        }
        List<String> parts = target.getNames().get(0);
        return parts.stream().map(part -> part == null ? "" : part)
                .collect(Collectors.joining("."));
    }

    public void setObjectName(String objectName) {
        setObjectName(Collections.singletonList(objectName));
    }

    /** Names the first target, replacing a MySQL wildcard level. */
    public void setObjectName(List<String> objectName) {
        List<String> parts = new ArrayList<>(objectName);
        getObjectNameParts().clear();
        getObjectNameParts().addAll(parts);
        clause.getTarget().setWildcard(null);
    }

    /** Mutable parts of the first named target, retained for compatibility. */
    public List<String> getObjectNameParts() {
        if (clause.getTarget() == null) {
            clause.setTarget(new PrivilegeTarget());
        }
        if (clause.getTarget().getNames().isEmpty()) {
            clause.getTarget().getNames().add(new ArrayList<>());
        }
        return clause.getTarget().getNames().get(0);
    }

    public List<String> getUsers() {
        return clause.getGrantees();
    }

    public void setUsers(List<String> users) {
        clause.setGrantees(users);
    }

    public void appendTo(StringBuilder sql, Consumer<Expression> visitor) {
        sql.append("GRANT ");
        clause.appendTo(sql, false, visitor);
        if (!options.isEmpty()) {
            sql.append(" WITH ").append(
                    options.stream().map(Object::toString).collect(Collectors.joining(", ")));
        }
        if (asUser != null) {
            sql.append(" AS ").append(asUser);
            if (withRole != null) {
                sql.append(" WITH ROLE ").append(withRole);
            }
        }
        if (clause.getGrantor() != null) {
            sql.append(" GRANTED BY ").append(clause.getGrantor());
        }
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> visitor, S context) {
        return visitor.visit(this, context);
    }

    @Override
    public String toString() {
        StringBuilder sql = new StringBuilder();
        appendTo(sql, sql::append);
        return sql.toString();
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
        return addPrivileges(Arrays.asList(privileges));
    }

    public Grant addPrivileges(Collection<String> privileges) {
        List<String> additions = new ArrayList<>(privileges);
        if (clause.getPrivileges() == null) {
            clause.setPrivileges(new ArrayList<>());
        }
        for (String privilege : additions) {
            clause.getPrivileges().add(new Privilege(privilege));
        }
        return this;
    }

    public Grant addUsers(String... users) {
        return addUsers(Arrays.asList(users));
    }

    public Grant addUsers(Collection<String> users) {
        List<String> additions = new ArrayList<>(users);
        if (clause.getGranteeItems() == null) {
            clause.setGranteeItems(new ArrayList<>());
        }
        for (String user : additions) {
            clause.getGranteeItems().add(Grantee.valueOf(user));
        }
        return this;
    }
}
