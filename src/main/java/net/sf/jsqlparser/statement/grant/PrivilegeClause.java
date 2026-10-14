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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.create.user.UserAccount;

/**
 * Shared, editable GRANT/REVOKE payload. Null privileges denote role membership; role memberships,
 * and MySQL {@code REVOKE ALL PRIVILEGES, GRANT OPTION}, have no target.
 *
 * <p>
 * Roles, grantees and the grantor are typed. The string accessors {@link #getRoles()},
 * {@link #getGrantees()} and {@link #getGrantedBy()} are derived from them and convert assigned
 * text with {@link Grantee#valueOf(String)}.
 */
public class PrivilegeClause implements Serializable {
    private List<Privilege> privileges;
    private List<Grantee> granteeItems;
    private Grantee grantor;
    private List<UserAccount> roleItems = new ArrayList<>();
    private PrivilegeTarget target = new PrivilegeTarget();

    public List<Privilege> getPrivileges() {
        return privileges;
    }

    public void setPrivileges(List<Privilege> privileges) {
        this.privileges = privileges;
    }

    /** The granted or revoked roles of a role membership, as a mutable list. */
    public List<UserAccount> getRoleItems() {
        return roleItems;
    }

    public void setRoleItems(Collection<? extends UserAccount> roleItems) {
        this.roleItems = roleItems == null ? new ArrayList<>() : new ArrayList<>(roleItems);
    }

    /** Mutable legacy string view of {@link #getRoleItems()}. */
    public List<String> getRoles() {
        return new StringListView<>(() -> roleItems, Grantee::accountOf);
    }

    public void setRoles(List<String> roles) {
        roleItems = convert(roles, Grantee::accountOf);
        if (roleItems == null) {
            roleItems = new ArrayList<>();
        }
    }

    public PrivilegeTarget getTarget() {
        return target;
    }

    public void setTarget(PrivilegeTarget target) {
        this.target = target;
    }

    /** The grantees or revokees as a mutable list, or null when unset. */
    public List<Grantee> getGranteeItems() {
        return granteeItems;
    }

    public void setGranteeItems(Collection<? extends Grantee> granteeItems) {
        this.granteeItems = granteeItems == null ? null : new ArrayList<>(granteeItems);
    }

    /** Mutable legacy string view of {@link #getGranteeItems()}, or null when unset. */
    public List<String> getGrantees() {
        return granteeItems == null ? null
                : new StringListView<>(() -> granteeItems, Grantee::valueOf);
    }

    public void setGrantees(List<String> grantees) {
        granteeItems = convert(grantees, Grantee::valueOf);
    }

    /** The PostgreSQL {@code GRANTED BY} role, or null when omitted. */
    public Grantee getGrantor() {
        return grantor;
    }

    public void setGrantor(Grantee grantor) {
        this.grantor = grantor;
    }

    public String getGrantedBy() {
        return grantor == null ? null : grantor.toString();
    }

    public void setGrantedBy(String grantedBy) {
        grantor = grantedBy == null ? null : Grantee.valueOf(grantedBy);
    }

    private static <T> List<T> convert(List<String> values, Function<String, T> parse) {
        if (values == null) {
            return null;
        }
        List<T> items = new ArrayList<>();
        for (String value : new ArrayList<>(values)) {
            items.add(parse.apply(value));
        }
        return items;
    }

    public void appendTo(StringBuilder sql, boolean revoke, Consumer<Expression> visitor) {
        if (privileges == null) {
            sql.append(join(roleItems));
        } else {
            for (int i = 0; i < privileges.size(); i++) {
                if (i > 0) {
                    sql.append(", ");
                }
                privileges.get(i).appendTo(sql, visitor);
            }
            if (target != null) {
                sql.append(" ON ").append(target);
            }
        }
        sql.append(revoke ? " FROM " : " TO ").append(join(granteeItems));
    }

    private static String join(List<?> items) {
        return items.stream().map(Object::toString).collect(Collectors.joining(", "));
    }

    public void visit(Consumer<Table> tables, Consumer<Expression> expressions) {
        if (privileges != null) {
            if (target != null) {
                target.visitTables(tables);
            }
            for (Privilege privilege : privileges) {
                if (privilege.getColumns() != null) {
                    privilege.getColumns().forEach(expressions);
                }
            }
        }
    }
}
