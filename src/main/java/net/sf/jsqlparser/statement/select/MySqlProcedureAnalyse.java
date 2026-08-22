/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

import java.io.Serializable;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

/**
 * Models the legacy MySQL {@code PROCEDURE ANALYSE()} clause.
 *
 * @see <a href="https://dev.mysql.com/doc/refman/5.7/en/procedure-analyse.html">MySQL 5.7 Reference
 *      Manual</a>
 */
public class MySqlProcedureAnalyse extends ASTNodeAccessImpl implements Serializable {

    private LongValue maxElements;
    private LongValue maxMemory;

    public MySqlProcedureAnalyse() {}

    public MySqlProcedureAnalyse(LongValue maxElements, LongValue maxMemory) {
        this.maxElements = maxElements;
        this.maxMemory = maxMemory;
    }

    public LongValue getMaxElements() {
        return maxElements;
    }

    public void setMaxElements(LongValue maxElements) {
        this.maxElements = maxElements;
    }

    public MySqlProcedureAnalyse withMaxElements(LongValue maxElements) {
        setMaxElements(maxElements);
        return this;
    }

    public LongValue getMaxMemory() {
        return maxMemory;
    }

    public void setMaxMemory(LongValue maxMemory) {
        this.maxMemory = maxMemory;
    }

    public MySqlProcedureAnalyse withMaxMemory(LongValue maxMemory) {
        setMaxMemory(maxMemory);
        return this;
    }

    public StringBuilder appendTo(StringBuilder builder) {
        builder.append(" PROCEDURE ANALYSE(");
        if (maxElements != null) {
            builder.append(maxElements);
            if (maxMemory != null) {
                builder.append(", ").append(maxMemory);
            }
        }
        return builder.append(")");
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }
}
