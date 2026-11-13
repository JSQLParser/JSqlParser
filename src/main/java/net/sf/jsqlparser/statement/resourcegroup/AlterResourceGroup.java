/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.resourcegroup;

import java.util.Collection;
import java.util.List;
import net.sf.jsqlparser.statement.StatementVisitor;

/**
 * MySQL {@code ALTER RESOURCE GROUP group_name [VCPU [=] vcpu_spec [, vcpu_spec] ...]
 * [THREAD_PRIORITY [=] N] [ENABLE | DISABLE] [FORCE]}. MySQL's grammar takes FORCE without DISABLE
 * too.
 *
 * @see <a href="https://dev.mysql.com/doc/refman/8.4/en/alter-resource-group.html">MySQL ALTER
 *      RESOURCE GROUP Statement</a>
 */
public class AlterResourceGroup extends ResourceGroupDefinition {
    private boolean force;

    public AlterResourceGroup() {
        // the parser fills the statement in
    }

    public AlterResourceGroup(String name) {
        super(name);
    }

    /** Whether FORCE is written: the group is changed although threads are assigned to it. */
    public boolean isForce() {
        return force;
    }

    public void setForce(boolean force) {
        this.force = force;
    }

    public AlterResourceGroup withForce(boolean force) {
        setForce(force);
        return this;
    }

    public AlterResourceGroup withVcpus(List<VcpuRange> vcpus) {
        setVcpus(vcpus);
        return this;
    }

    public AlterResourceGroup addVcpuRanges(Collection<? extends VcpuRange> vcpus) {
        addVcpus(vcpus);
        return this;
    }

    public AlterResourceGroup withThreadPriority(Integer threadPriority) {
        setThreadPriority(threadPriority);
        return this;
    }

    public AlterResourceGroup withState(State state) {
        setState(state);
        return this;
    }

    @Override
    public StringBuilder appendTo(StringBuilder builder) {
        appendOptionsTo(builder.append("ALTER RESOURCE GROUP ").append(getName()));
        return force ? builder.append(" FORCE") : builder;
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }
}
