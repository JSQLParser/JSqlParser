/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.cursor;

import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

/**
 * PostgreSQL {@code CLOSE {name | ALL}}, which closes one cursor or every open cursor of the
 * session.<br>
 * Examples:<br>
 * CLOSE c<br>
 * CLOSE ALL<br>
 *
 * @see <a href="https://www.postgresql.org/docs/18/sql-close.html">PostgreSQL CLOSE</a>
 */
public class CloseCursorStatement extends ASTNodeAccessImpl implements Statement {
    private String cursorName;

    /** CLOSE ALL. */
    public CloseCursorStatement() {}

    public CloseCursorStatement(String cursorName) {
        this.cursorName = cursorName;
    }

    /** Returns null for CLOSE ALL. */
    public String getCursorName() {
        return cursorName;
    }

    /** A null name makes the statement CLOSE ALL. */
    public void setCursorName(String cursorName) {
        this.cursorName = cursorName;
    }

    /** Whether the statement is CLOSE ALL, which is when it has no cursor name. */
    public boolean isAll() {
        return cursorName == null;
    }

    @Override
    public StringBuilder appendTo(StringBuilder builder) {
        return builder.append("CLOSE ").append(isAll() ? "ALL" : cursorName);
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
