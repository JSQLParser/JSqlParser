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

import net.sf.jsqlparser.statement.create.table.TableOption;

/**
 * Internal subclass for table-level option operations within ALTER TABLE. Handles ENGINE,
 * ALGORITHM, LOCK, KEY_BLOCK_SIZE, COMMENT, ENCRYPTION, AUTO_INCREMENT (SET_TABLE_OPTION),
 * DISCARD/IMPORT TABLESPACE, DISABLE/ENABLE KEYS.
 */
public class AlterExpressionTableOption extends AlterExpression {

    private TableOption structuredTableOption;

    public TableOption getStructuredTableOption() {
        return structuredTableOption;
    }

    /** Uses the same option model as CREATE TABLE, with legacy getters as live projections. */
    public void setStructuredTableOption(TableOption option) {
        structuredTableOption = option;
        setOperation(AlterOperation.SET_TABLE_OPTION);
        super.setTableOption(null);
        super.setUseEqual(false);
    }

    @Override
    public AlterOperation getOperation() {
        if (structuredTableOption != null) {
            switch (structuredTableOption.getKind()) {
                case ENGINE:
                    return AlterOperation.ENGINE;
                case KEY_BLOCK_SIZE:
                    return AlterOperation.KEY_BLOCK_SIZE;
                case COMMENT:
                    return structuredTableOption.isUseEquals()
                            ? AlterOperation.COMMENT_WITH_EQUAL_SIGN
                            : AlterOperation.COMMENT;
                default:
                    return AlterOperation.SET_TABLE_OPTION;
            }
        }
        return super.getOperation();
    }

    @Override
    public String getEngineOption() {
        return structuredTableOption != null
                && structuredTableOption.getKind() == TableOption.Kind.ENGINE
                        ? structuredTableOption.getValue()
                        : super.getEngineOption();
    }

    @Override
    public void setEngineOption(String value) {
        if (structuredTableOption != null
                && structuredTableOption.getKind() == TableOption.Kind.ENGINE) {
            structuredTableOption.setValue(value);
        }
        super.setEngineOption(value);
    }

    @Override
    public int getKeyBlockSize() {
        return structuredTableOption != null
                && structuredTableOption.getKind() == TableOption.Kind.KEY_BLOCK_SIZE
                        ? Integer.parseInt(structuredTableOption.getValue())
                        : super.getKeyBlockSize();
    }

    @Override
    public void setKeyBlockSize(int value) {
        if (structuredTableOption != null
                && structuredTableOption.getKind() == TableOption.Kind.KEY_BLOCK_SIZE) {
            structuredTableOption.setValue(Integer.toString(value));
        }
        super.setKeyBlockSize(value);
    }

    @Override
    public String getCommentText() {
        return structuredTableOption != null
                && structuredTableOption.getKind() == TableOption.Kind.COMMENT
                        ? structuredTableOption.getValue()
                        : super.getCommentText();
    }

    @Override
    public void setCommentText(String value) {
        if (structuredTableOption != null
                && structuredTableOption.getKind() == TableOption.Kind.COMMENT) {
            structuredTableOption.setValue(value);
        }
        super.setCommentText(value);
    }

    @Override
    public String getTableOption() {
        return structuredTableOption == null ? super.getTableOption()
                : structuredTableOption.toString();
    }

    @Override
    public void setTableOption(String option) {
        structuredTableOption = null;
        super.setTableOption(option);
    }

    @Override
    public boolean getUseEqual() {
        return structuredTableOption == null ? super.getUseEqual()
                : structuredTableOption.isUseEquals();
    }

    @Override
    public void setUseEqual(boolean useEqual) {
        if (structuredTableOption != null) {
            structuredTableOption.setUseEquals(useEqual);
        }
        super.setUseEqual(useEqual);
    }

    @Override
    protected void appendBody(StringBuilder b) {
        if (structuredTableOption != null) {
            b.append(structuredTableOption);
            return;
        }
        switch (getOperation()) {
            case SET_TABLE_OPTION:
                b.append(getTableOption());
                break;
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
