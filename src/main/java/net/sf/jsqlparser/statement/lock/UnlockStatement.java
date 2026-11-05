/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.lock;

import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

/**
 * MySQL and MariaDB {@code UNLOCK TABLES}, which releases the table locks that
 * {@link LockStatement} took for the session.<br>
 * Example:<br>
 * UNLOCK TABLES<br>
 *
 * @see <a href="https://dev.mysql.com/doc/refman/8.4/en/lock-tables.html">MySQL LOCK TABLES and
 *      UNLOCK TABLES</a>
 */
public class UnlockStatement extends ASTNodeAccessImpl implements Statement {
    private boolean tablesKeyword = true;

    public UnlockStatement() {}

    public UnlockStatement(boolean tablesKeyword) {
        this.tablesKeyword = tablesKeyword;
    }

    /** Whether the statement is spelled UNLOCK TABLES rather than UNLOCK TABLE. */
    public boolean isTablesKeyword() {
        return tablesKeyword;
    }

    public void setTablesKeyword(boolean tablesKeyword) {
        this.tablesKeyword = tablesKeyword;
    }

    public StringBuilder appendTo(StringBuilder builder) {
        return builder.append(tablesKeyword ? "UNLOCK TABLES" : "UNLOCK TABLE");
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
