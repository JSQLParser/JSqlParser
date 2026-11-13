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

import java.io.Serializable;
import java.util.Objects;

/**
 * One element of a MySQL resource group's VCPU list: a single CPU number such as {@code 5}, or an
 * inclusive range such as {@code 0-3}. MySQL checks the numbers against the machine, and that a
 * range does not run backwards, when it executes the statement.
 */
public class VcpuRange implements Serializable {
    private int start;
    private Integer end;

    /** A single CPU. */
    public VcpuRange(int start) {
        this(start, null);
    }

    /** The CPUs from {@code start} to {@code end}; a null {@code end} names the single CPU. */
    public VcpuRange(int start, Integer end) {
        setStart(start);
        setEnd(end);
    }

    private static int requireCpuNumber(int number) {
        if (number < 0) {
            throw new IllegalArgumentException("A CPU number is not negative: " + number);
        }
        return number;
    }

    public int getStart() {
        return start;
    }

    public final void setStart(int start) {
        this.start = requireCpuNumber(start);
    }

    /** The last CPU of a range written as {@code start-end}, or null for a single CPU. */
    public Integer getEnd() {
        return end;
    }

    public final void setEnd(Integer end) {
        if (end != null) {
            requireCpuNumber(end);
        }
        this.end = end;
    }

    /** Whether the element is written as {@code start-end}. */
    public boolean isRange() {
        return end != null;
    }

    /** The last CPU the element covers: the end of a range, or the single CPU. */
    public int getLast() {
        return end != null ? end : start;
    }

    public StringBuilder appendTo(StringBuilder builder) {
        builder.append(start);
        return end != null ? builder.append('-').append(end) : builder;
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof VcpuRange)) {
            return false;
        }
        VcpuRange range = (VcpuRange) other;
        return start == range.start && Objects.equals(end, range.end);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end);
    }
}
