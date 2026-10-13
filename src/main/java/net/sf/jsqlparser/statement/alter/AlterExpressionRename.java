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

import java.util.Collections;
import java.util.List;

import net.sf.jsqlparser.statement.create.table.ConstraintKind;

/**
 * Internal subclass for RENAME operations within ALTER TABLE. Handles RENAME COLUMN, RENAME TO
 * (table), RENAME INDEX/KEY/CONSTRAINT.
 */
public class AlterExpressionRename extends AlterExpression implements ColumnRenameAction {

    @Override
    public ColumnRenameAction getColumnRename() {
        return getOperation() == AlterOperation.RENAME ? this : null;
    }

    @Override
    public String getSourceName() {
        return getColumnOldName();
    }

    @Override
    public void setSourceName(String name) {
        setColumnOldName(name);
    }

    @Override
    public String getTargetName() {
        return getColumnName();
    }

    @Override
    public void setTargetName(String name) {
        setColumnName(name);
    }

    @Override
    public List<ColumnIdentityAction> getColumnIdentityActions() {
        return Collections.emptyList();
    }

    @Override
    protected boolean usesColumnActionBody() {
        return false;
    }

    @Override
    public boolean hasActiveColumnDefinitions() {
        return getOperation() == AlterOperation.RENAME && hasColumnDefinitionPayload();
    }

    @Override
    public boolean hasActiveTableDefinition() {
        return false;
    }

    @Override
    public ConstraintKind getConstraintKind() {
        return ConstraintKind.OTHER;
    }

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
