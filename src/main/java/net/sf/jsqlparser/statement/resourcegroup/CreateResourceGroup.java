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
import java.util.Objects;
import net.sf.jsqlparser.statement.StatementVisitor;

/**
 * MySQL {@code CREATE RESOURCE GROUP group_name TYPE [=] {SYSTEM | USER} [VCPU [=] vcpu_spec [,
 * vcpu_spec] ...] [THREAD_PRIORITY [=] N] [ENABLE | DISABLE]}.
 *
 * @see <a href="https://dev.mysql.com/doc/refman/8.4/en/create-resource-group.html">MySQL CREATE
 *      RESOURCE GROUP Statement</a>
 */
public class CreateResourceGroup extends ResourceGroupDefinition {
    /** Whether the group takes system threads or user threads. */
    public enum Type {
        SYSTEM, USER
    }

    private Type type;
    private OptionAssignment typeAssignment = OptionAssignment.EQUALS;

    public CreateResourceGroup() {
        // the parser fills the statement in
    }

    public CreateResourceGroup(String name, Type type) {
        super(name);
        setType(type);
    }

    public Type getType() {
        return type;
    }

    public final void setType(Type type) {
        this.type = Objects.requireNonNull(type, "type");
    }

    public OptionAssignment getTypeAssignment() {
        return typeAssignment;
    }

    public void setTypeAssignment(OptionAssignment typeAssignment) {
        this.typeAssignment = Objects.requireNonNull(typeAssignment, "typeAssignment");
    }

    public CreateResourceGroup withTypeAssignment(OptionAssignment typeAssignment) {
        setTypeAssignment(typeAssignment);
        return this;
    }

    public CreateResourceGroup withVcpus(List<VcpuRange> vcpus) {
        setVcpus(vcpus);
        return this;
    }

    public CreateResourceGroup addVcpuRanges(Collection<? extends VcpuRange> vcpus) {
        addVcpus(vcpus);
        return this;
    }

    public CreateResourceGroup withThreadPriority(Integer threadPriority) {
        setThreadPriority(threadPriority);
        return this;
    }

    public CreateResourceGroup withState(State state) {
        setState(state);
        return this;
    }

    @Override
    public StringBuilder appendTo(StringBuilder builder) {
        builder.append("CREATE RESOURCE GROUP ").append(getName()).append(' ');
        typeAssignment.appendTo(builder, "TYPE").append(type);
        return appendOptionsTo(builder);
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }
}
