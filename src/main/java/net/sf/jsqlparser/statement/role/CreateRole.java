/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.role;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.create.user.UserAccount;

/**
 * {@code CREATE ROLE}, {@code CREATE USER} or {@code CREATE GROUP} with PostgreSQL role attributes.
 * MySQL's {@code CREATE ROLE [IF NOT EXISTS] role [, role] ...} keeps its account-style role names
 * in {@link #getRoles()} instead of {@link #getName()}.
 */
public class CreateRole implements Statement {
    public enum Command {
        ROLE, USER, GROUP
    }

    private Command command = Command.ROLE;
    private boolean ifNotExists;
    private String name;
    private final List<UserAccount> roles = new ArrayList<>();
    private boolean useWith;
    private List<RoleOption> options = new ArrayList<>();

    public Command getCommand() {
        return command;
    }

    public void setCommand(Command command) {
        this.command = command;
    }

    /** Whether MySQL's {@code IF NOT EXISTS} is present. */
    public boolean isIfNotExists() {
        return ifNotExists;
    }

    public void setIfNotExists(boolean ifNotExists) {
        this.ifNotExists = ifNotExists;
    }

    /** The PostgreSQL role name; null when MySQL role names are given in {@link #getRoles()}. */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * MySQL role names such as {@code 'reader'} or {@code 'reader'@'localhost'}, which replace
     * {@link #getName()} when present.
     */
    public List<UserAccount> getRoles() {
        return Collections.unmodifiableList(roles);
    }

    public void setRoles(Collection<? extends UserAccount> roles) {
        this.roles.clear();
        if (roles != null) {
            this.roles.addAll(roles);
        }
    }

    public boolean isUseWith() {
        return useWith;
    }

    public void setUseWith(boolean useWith) {
        this.useWith = useWith;
    }

    public List<RoleOption> getOptions() {
        return options;
    }

    public void setOptions(List<RoleOption> options) {
        this.options = options;
    }

    public void appendTo(StringBuilder sql, Consumer<Expression> visitor) {
        sql.append("CREATE ").append(command).append(' ');
        if (ifNotExists) {
            sql.append("IF NOT EXISTS ");
        }
        if (roles.isEmpty()) {
            sql.append(name);
        } else {
            sql.append(roles.stream().map(Object::toString).collect(Collectors.joining(", ")));
        }
        if (useWith) {
            sql.append(" WITH");
        }
        for (RoleOption option : options) {
            sql.append(' ');
            option.appendTo(sql, visitor);
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
}
