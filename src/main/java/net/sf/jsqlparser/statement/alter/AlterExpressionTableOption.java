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
 * Internal subclass for table-level option operations within ALTER TABLE. Handles ENGINE,
 * ALGORITHM, LOCK, KEY_BLOCK_SIZE, COMMENT, ENCRYPTION, AUTO_INCREMENT (SET_TABLE_OPTION),
 * DISCARD/IMPORT TABLESPACE, DISABLE/ENABLE KEYS.
 */
public class AlterExpressionTableOption extends AlterExpression {

    @Override
    protected void appendBody(StringBuilder b) {
        switch (getOperation()) {
            case COMMENT:
                b.append("COMMENT ");
                b.append(getCommentText());
                break;
            case COMMENT_WITH_EQUAL_SIGN:
                b.append("COMMENT = ");
                b.append(getCommentText());
                break;
            default:
                toStringSimpleKeyword(b);
                break;
        }
    }
}
