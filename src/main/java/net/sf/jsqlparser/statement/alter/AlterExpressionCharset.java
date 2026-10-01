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

import net.sf.jsqlparser.statement.create.table.Index;

/**
 * Internal subclass for character set and collation operations within ALTER TABLE. Handles CONVERT
 * TO CHARACTER SET, DEFAULT CHARACTER SET, CHARACTER SET, and COLLATE.
 */
public class AlterExpressionCharset extends AlterExpression {

    @Override
    public Index.Kind getConstraintKind() {
        return Index.Kind.OTHER;
    }

    @Override
    protected void appendBody(StringBuilder b) {
        toStringConvert(b);
    }
}
