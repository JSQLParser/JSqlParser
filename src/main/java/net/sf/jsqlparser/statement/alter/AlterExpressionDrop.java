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


import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.create.table.ConstraintKind;

/**
 * Internal subclass for DROP operations within ALTER TABLE. Handles DROP column, DROP CONSTRAINT,
 * DROP INDEX/KEY, DROP PRIMARY KEY, DROP UNIQUE, DROP FOREIGN KEY, and DROP PARTITION.
 */
public class AlterExpressionDrop extends AlterExpression {
    @Override
    public boolean hasActiveTableDefinition() {
        return false;
    }

    @Override
    public ConstraintKind getConstraintKind() {
        if (getOperation() == null) {
            return ConstraintKind.OTHER;
        }
        switch (getOperation()) {
            case DROP_PRIMARY_KEY:
                return ConstraintKind.PRIMARY_KEY;
            case DROP_UNIQUE:
                return ConstraintKind.UNIQUE;
            case DROP_FOREIGN_KEY:
                return ConstraintKind.FOREIGN_KEY;
            case DROP_CHECK:
                return ConstraintKind.CHECK;
            default:
                return ConstraintKind.OTHER;
        }
    }

    @Override
    protected void appendBody(StringBuilder b) {
        if (isDropSpecialOperation()) {
            toStringDropSpecial(b);
            return;
        }
        switch (getOperation()) {
            case DROP_PARTITION:
                b.append("DROP PARTITION ")
                        .append(PlainSelect.getStringList(getPartitions()));
                break;
            default:
                toStringDropDefault(b);
                break;
        }
    }

    private void toStringDropDefault(StringBuilder b) {
        b.append("DROP ");
        if (getColumnName() == null && getPkColumns() != null && !getPkColumns().isEmpty()) {
            // Oracle Multi Column Drop
            b.append("(").append(PlainSelect.getStringList(getPkColumns())).append(')');
        } else if (getConstraintName() != null) {
            b.append("CONSTRAINT ");
            if (isUsingIfExists()) {
                b.append("IF EXISTS ");
            }
            b.append(getConstraintName());
        } else if (getColumnName() != null) {
            if (hasColumn()) {
                b.append("COLUMN ");
            }
            if (isUsingIfExists()) {
                b.append("IF EXISTS ");
            }
            b.append(getColumnName());
        } else if (getIndex() != null) {
            b.append(getIndex());
        }
    }
}
