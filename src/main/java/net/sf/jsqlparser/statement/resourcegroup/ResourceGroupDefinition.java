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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.statement.Statement;

/**
 * The name and the options that MySQL's CREATE RESOURCE GROUP and ALTER RESOURCE GROUP share:
 * {@code [VCPU [=] vcpu_spec [, vcpu_spec] ...] [THREAD_PRIORITY [=] N] [ENABLE | DISABLE]}, in
 * that order. The commas of the VCPU list are optional in MySQL and are always written here.
 */
public abstract class ResourceGroupDefinition extends ASTNodeAccessImpl implements Statement {
    /** Whether threads may be assigned to the group. */
    public enum State {
        ENABLE, DISABLE
    }

    private String name;
    private List<VcpuRange> vcpus = new ArrayList<>();
    private OptionAssignment vcpuAssignment = OptionAssignment.EQUALS;
    private Integer threadPriority;
    private OptionAssignment threadPriorityAssignment = OptionAssignment.EQUALS;
    private State state;

    protected ResourceGroupDefinition() {
        // the parser fills the statement in
    }

    protected ResourceGroupDefinition(String name) {
        setName(name);
    }

    public String getName() {
        return name;
    }

    public final void setName(String name) {
        this.name = Objects.requireNonNull(name, "name");
    }

    /** The VCPU list; empty when the statement has no VCPU clause. */
    public List<VcpuRange> getVcpus() {
        return vcpus;
    }

    public void setVcpus(List<VcpuRange> vcpus) {
        this.vcpus = vcpus == null ? new ArrayList<>() : vcpus;
    }

    public void addVcpus(VcpuRange... vcpus) {
        addVcpus(Arrays.asList(vcpus));
    }

    public void addVcpus(Collection<? extends VcpuRange> vcpus) {
        this.vcpus.addAll(vcpus);
    }

    public OptionAssignment getVcpuAssignment() {
        return vcpuAssignment;
    }

    public void setVcpuAssignment(OptionAssignment vcpuAssignment) {
        this.vcpuAssignment = Objects.requireNonNull(vcpuAssignment, "vcpuAssignment");
    }

    /** The THREAD_PRIORITY value, or null when the statement has no such clause. */
    public Integer getThreadPriority() {
        return threadPriority;
    }

    public void setThreadPriority(Integer threadPriority) {
        this.threadPriority = threadPriority;
    }

    public OptionAssignment getThreadPriorityAssignment() {
        return threadPriorityAssignment;
    }

    public void setThreadPriorityAssignment(OptionAssignment threadPriorityAssignment) {
        this.threadPriorityAssignment =
                Objects.requireNonNull(threadPriorityAssignment, "threadPriorityAssignment");
    }

    /** ENABLE or DISABLE, or null when the statement has neither. */
    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    /** Appends the shared options, each with a leading space. */
    protected StringBuilder appendOptionsTo(StringBuilder builder) {
        if (!vcpus.isEmpty()) {
            vcpuAssignment.appendTo(builder.append(' '), "VCPU");
            for (int i = 0; i < vcpus.size(); i++) {
                vcpus.get(i).appendTo(i > 0 ? builder.append(", ") : builder);
            }
        }
        if (threadPriority != null) {
            threadPriorityAssignment.appendTo(builder.append(' '), "THREAD_PRIORITY")
                    .append(threadPriority);
        }
        if (state != null) {
            builder.append(' ').append(state);
        }
        return builder;
    }

    public abstract StringBuilder appendTo(StringBuilder builder);

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }
}
