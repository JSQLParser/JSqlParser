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
 * The MySQL and MariaDB XA statements, which control a branch of a distributed transaction:
 * {@code XA {START | BEGIN} xid [JOIN | RESUME]}, {@code XA END xid [SUSPEND [FOR MIGRATE]]},
 * {@code XA PREPARE xid}, {@code XA COMMIT xid [ONE PHASE]}, {@code XA ROLLBACK xid} and
 * {@code XA RECOVER [CONVERT XID]}, which names no xid.
 *
 * @see <a href="https://dev.mysql.com/doc/refman/8.4/en/xa-statements.html">MySQL XA Transaction
 *      SQL Statements</a>
 */
public class XaStatement extends ASTNodeAccessImpl implements Statement {
    /** The word after XA; START and BEGIN are synonyms. */
    public enum Action {
        START, BEGIN, END, PREPARE, COMMIT, ROLLBACK, RECOVER
    }

    /** The optional words after the xid, or after RECOVER. */
    public enum Option {
        JOIN("JOIN"), RESUME("RESUME"), SUSPEND("SUSPEND"), SUSPEND_FOR_MIGRATE(
                "SUSPEND FOR MIGRATE"), ONE_PHASE("ONE PHASE"), CONVERT_XID("CONVERT XID");

        private final String keywords;

        Option(String keywords) {
            this.keywords = keywords;
        }

        /** Whether the option follows {@code action}. */
        public boolean appliesTo(Action action) {
            switch (this) {
                case JOIN:
                case RESUME:
                    return action == Action.START || action == Action.BEGIN;
                case SUSPEND:
                case SUSPEND_FOR_MIGRATE:
                    return action == Action.END;
                case ONE_PHASE:
                    return action == Action.COMMIT;
                default:
                    return action == Action.RECOVER;
            }
        }

        @Override
        public String toString() {
            return keywords;
        }
    }

    private Action action;
    private TransactionId transactionId;
    private Option option;

    /** An XA statement; {@code transactionId} is null for {@link Action#RECOVER}. */
    public XaStatement(Action action, TransactionId transactionId) {
        this.action = Objects.requireNonNull(action, "action");
        this.transactionId = transactionId;
    }

    public Action getAction() {
        return action;
    }

    /**
     * Sets the action; the xid is kept.
     *
     * @throws IllegalArgumentException if the option does not apply to the action
     */
    public void setAction(Action action) {
        Objects.requireNonNull(action, "action");
        if (option != null && !option.appliesTo(action)) {
            throw new IllegalArgumentException(option + " does not apply to XA " + action);
        }
        this.action = action;
    }

    public XaStatement withAction(Action action) {
        setAction(action);
        return this;
    }

    /** The xid, or null for XA RECOVER. */
    public TransactionId getTransactionId() {
        return transactionId;
    }

    /** Sets the xid, which every action but RECOVER names. */
    public void setTransactionId(TransactionId transactionId) {
        this.transactionId = transactionId;
    }

    public XaStatement withTransactionId(TransactionId transactionId) {
        setTransactionId(transactionId);
        return this;
    }

    /** The option after the xid, or after RECOVER, or null. */
    public Option getOption() {
        return option;
    }

    /**
     * Sets the option; null removes it.
     *
     * @throws IllegalArgumentException if it does not apply to the action
     */
    public void setOption(Option option) {
        if (option != null && !option.appliesTo(action)) {
            throw new IllegalArgumentException(option + " does not apply to XA " + action);
        }
        this.option = option;
    }

    public XaStatement withOption(Option option) {
        setOption(option);
        return this;
    }

    public StringBuilder appendTo(StringBuilder builder) {
        builder.append("XA ").append(action);
        if (transactionId != null) {
            transactionId.appendTo(builder.append(' '));
        }
        if (option != null) {
            builder.append(' ').append(option);
        }
        return builder;
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
