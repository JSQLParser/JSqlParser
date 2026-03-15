/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.alter;

/**
 * Internal subclass for character set and collation operations within ALTER TABLE. Handles CONVERT
 * TO CHARACTER SET, DEFAULT CHARACTER SET, CHARACTER SET, and COLLATE.
 */
public class AlterExpressionCharset extends AlterExpression {

    @Override
    protected void appendBody(StringBuilder b) {
        toStringConvert(b);
    }
}
