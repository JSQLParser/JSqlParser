/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import java.util.Objects;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

/**
 * PostgreSQL {@code DISCARD {ALL | PLANS | SEQUENCES | TEMP | TEMPORARY}}, which releases the
 * session state that the target names.
 *
 * @see <a href="https://www.postgresql.org/docs/18/sql-discard.html">DISCARD</a>
 */
public class DiscardStatement extends ASTNodeAccessImpl implements Statement {
    private Target target = Target.ALL;

    /**
     * What DISCARD releases. {@code TEMP} and {@code TEMPORARY} are one target, both kept so that
     * the statement prints as written.
     */
    public enum Target {
        /** Every kind of session state, including the other targets. */
        ALL,
        /** Cached query plans. */
        PLANS,
        /** Cached sequence state. */
        SEQUENCES,
        /** Temporary tables, spelled TEMP. */
        TEMP,
        /** Temporary tables, spelled TEMPORARY. */
        TEMPORARY
    }

    public DiscardStatement() {}

    public DiscardStatement(Target target) {
        setTarget(target);
    }

    public Target getTarget() {
        return target;
    }

    public void setTarget(Target target) {
        this.target = Objects.requireNonNull(target, "target");
    }

    public DiscardStatement withTarget(Target target) {
        setTarget(target);
        return this;
    }

    /** Whether the statement drops the temporary tables of the session: ALL, TEMP or TEMPORARY. */
    public boolean isDroppingTemporaryTables() {
        return target == Target.ALL || target == Target.TEMP || target == Target.TEMPORARY;
    }

    @Override
    public StringBuilder appendTo(StringBuilder builder) {
        return builder.append("DISCARD ").append(target.name());
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }
}
