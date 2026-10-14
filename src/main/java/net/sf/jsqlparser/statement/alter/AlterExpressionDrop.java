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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sf.jsqlparser.statement.CascadeBehavior;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.create.table.ConstraintKind;

/**
 * Internal subclass for DROP operations within ALTER TABLE. Handles DROP column, DROP CONSTRAINT,
 * DROP INDEX/KEY, DROP PRIMARY KEY, DROP UNIQUE, DROP FOREIGN KEY, and DROP PARTITION.
 */
public class AlterExpressionDrop extends AlterExpression {
    private CascadeBehavior behavior;

    /** The explicit CASCADE or RESTRICT clause; null when omitted. */
    public CascadeBehavior getBehavior() {
        return behavior;
    }

    public void setBehavior(CascadeBehavior behavior) {
        this.behavior = behavior;
    }

    public AlterExpressionDrop withBehavior(CascadeBehavior behavior) {
        setBehavior(behavior);
        return this;
    }

    /**
     * Returns the raw parameters, such as Oracle's {@code INVALIDATE} or
     * {@code CASCADE CONSTRAINTS}. With a {@link #getBehavior() behavior}, this is an unmodifiable
     * snapshot that ends with its keyword; use {@link #addParameters(String...)} to append raw
     * parameters.
     */
    @Override
    public List<String> getParameters() {
        List<String> parameters = super.getParameters();
        if (behavior == null) {
            return parameters;
        }
        List<String> tokens = parameters == null ? new ArrayList<>() : new ArrayList<>(parameters);
        tokens.add(behavior.name());
        return Collections.unmodifiableList(tokens);
    }

    @Override
    protected void appendCommonTail(StringBuilder b) {
        super.appendCommonTail(b);
        if (behavior != null) {
            b.append(' ').append(behavior);
        }
    }

    @Override
    public List<ColumnIdentityAction> getColumnIdentityActions() {
        return getColumnIdentityActionsFromDefinitions();
    }

    @Override
    protected boolean usesColumnActionBody() {
        return false;
    }

    @Override
    public boolean hasActiveColumnDefinitions() {
        return hasColumnDefinitionPayload() && !isDropSpecialOperation()
                && getOperation() != AlterOperation.DROP_PARTITION;
    }

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
                toStringGeneral(b);
                break;
        }
    }

}
