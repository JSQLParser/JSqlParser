/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2021 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */

package net.sf.jsqlparser.statement;

import java.util.Objects;
import java.util.function.Consumer;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.create.table.Index;

/**
 * @author <a href="mailto:andreas@manticore-projects.com">Andreas Reichel</a>
 * @see <a href=
 *      "https://docs.oracle.com/cd/B19306_01/server.102/b14200/statements_9018.htm">Purge</a>
 * @see <a href="https://dev.mysql.com/doc/refman/8.4/en/purge-binary-logs.html">MySQL PURGE BINARY
 *      LOGS</a>
 */

public class PurgeStatement extends ASTNodeAccessImpl implements Statement {
    private final PurgeObjectType purgeObjectType;
    private final Object object;
    private String userName;
    private LogBound logBound;

    /** How {@code PURGE BINARY LOGS} gives the first log it keeps. */
    public enum LogBound {
        /** {@code TO 'log_name'}: the logs before the named one */
        TO,
        /** {@code BEFORE datetime_expr}: the logs older than a point in time */
        BEFORE
    }

    public PurgeStatement(Table table) {
        this.purgeObjectType = PurgeObjectType.TABLE;
        this.object = Objects.requireNonNull(table,
                "The TABLE of the PURGE TABLE statement must not be null.");
    }

    public PurgeStatement(Index index) {
        this.purgeObjectType = PurgeObjectType.INDEX;
        this.object = Objects.requireNonNull(index,
                "The INDEX of the PURGE INDEX statement must not be null.");
    }

    public PurgeStatement(PurgeObjectType purgeObjectType) {
        this.purgeObjectType = purgeObjectType;
        this.object = null;
    }

    public PurgeStatement(PurgeObjectType purgeObjectType, String tableSpaceName, String userName) {
        this.purgeObjectType = purgeObjectType;
        this.object = Objects.requireNonNull(tableSpaceName,
                "The TABLESPACE NAME of the PURGE TABLESPACE statement must not be null.");
        this.userName = userName;
    }

    /**
     * MySQL {@code PURGE {BINARY | MASTER} LOGS {TO 'log_name' | BEFORE expr}}; for {@code TO} the
     * expression is the string literal of the log name.
     */
    public PurgeStatement(PurgeObjectType purgeObjectType, LogBound logBound,
            Expression logExpression) {
        if (purgeObjectType != PurgeObjectType.BINARY_LOGS
                && purgeObjectType != PurgeObjectType.MASTER_LOGS) {
            throw new IllegalArgumentException(
                    "Only BINARY LOGS and MASTER LOGS take TO or BEFORE: " + purgeObjectType);
        }
        this.purgeObjectType = purgeObjectType;
        this.logBound = Objects.requireNonNull(logBound,
                "The TO or BEFORE of the PURGE LOGS statement must not be null.");
        this.object = Objects.requireNonNull(logExpression,
                "The log name or point in time of the PURGE LOGS statement must not be null.");
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }

    public StringBuilder appendTo(StringBuilder builder) {
        return appendTo(builder, builder::append);
    }

    @SuppressWarnings({"PMD.MissingBreakInSwitch", "PMD.SwitchStmtsShouldHaveDefault",
            "PMD.CyclomaticComplexity"})
    public StringBuilder appendTo(StringBuilder builder, Consumer<Expression> expressions) {
        builder.append("PURGE ");
        if (logBound != null) {
            builder.append(purgeObjectType == PurgeObjectType.BINARY_LOGS ? "BINARY" : "MASTER")
                    .append(" LOGS ").append(logBound).append(' ');
            expressions.accept((Expression) object);
            return builder;
        }

        switch (purgeObjectType) {
            case RECYCLEBIN:
            case DBA_RECYCLEBIN:
                builder.append(purgeObjectType);
                break;
            case TABLE:
            case INDEX:
                builder.append(purgeObjectType);
                if (object != null) {
                    builder.append(" ").append(object);
                }
                break;
            case TABLESPACE:
                builder.append(purgeObjectType);
                if (object != null) {
                    builder.append(" ").append(object);
                }
                if (userName != null && userName.length() > 0) {
                    builder.append(" USER ").append(userName);
                }
                break;
        }
        return builder;
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public PurgeObjectType getPurgeObjectType() {
        return purgeObjectType;
    }

    public Object getObject() {
        return object;
    }

    /** TO or BEFORE of {@code PURGE BINARY LOGS}, null for the other object types. */
    public LogBound getLogBound() {
        return logBound;
    }

    /**
     * The log name literal of {@code TO} or the expression of {@code BEFORE}, null for the other
     * object types. It is the same object as {@link #getObject()}.
     */
    public Expression getLogExpression() {
        return logBound != null ? (Expression) object : null;
    }
}
