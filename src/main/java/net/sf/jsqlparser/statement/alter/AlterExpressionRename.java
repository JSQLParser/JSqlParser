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
 * Internal subclass for RENAME operations within ALTER TABLE. Handles RENAME COLUMN, RENAME TO
 * (table), RENAME INDEX/KEY/CONSTRAINT.
 */
public class AlterExpressionRename extends AlterExpression {

    @Override
    protected void appendBody(StringBuilder b) {
        switch (getOperation()) {
            case RENAME:
                toStringGeneral(b);
                break;
            case RENAME_TABLE:
            case RENAME_INDEX:
            case RENAME_KEY:
            case RENAME_CONSTRAINT:
                toStringRename(b);
                break;
        }
    }
}
