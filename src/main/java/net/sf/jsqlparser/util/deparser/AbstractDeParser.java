/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2020 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.deparser;

import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.statement.update.UpdateSet;

import java.util.List;

/**
 * A base for a Statement DeParser
 *
 * @param <S> the type of statement this DeParser supports
 */
abstract class AbstractDeParser<S> {
    protected StringBuilder buffer;

    protected AbstractDeParser(StringBuilder buffer) {
        this.buffer = buffer;
    }

    public static void deparseUpdateSets(List<UpdateSet> updateSets, StringBuilder buffer,
            ExpressionVisitor visitor) {
        ExpressionListDeParser expressionListDeParser =
                new ExpressionListDeParser(visitor, buffer);
        int j = 0;
        if (updateSets != null) {
            for (UpdateSet updateSet : updateSets) {
                if (j++ > 0) {
                    buffer.append(", ");
                }
                expressionListDeParser.deParse(updateSet.getColumns());
                buffer.append(" = ");
                expressionListDeParser.deParse(updateSet.getValues());
            }
        }
    }

    public StringBuilder getBuffer() {
        return buffer;
    }

    public void setBuffer(StringBuilder buffer) {
        this.buffer = buffer;
    }

    /**
     * DeParses the given statement into the buffer
     *
     * @param statement the statement to deparse
     */
    abstract void deParse(S statement);
}
