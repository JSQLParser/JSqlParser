/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.table;

import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.ReferentialAction;
import net.sf.jsqlparser.statement.ReferentialAction.Action;
import net.sf.jsqlparser.statement.ReferentialAction.Type;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import net.sf.jsqlparser.expression.Expression;

public class ForeignKeyIndex extends NamedConstraint {

    private ForeignKeyReference reference = new ForeignKeyReference();

    /** Returns the mutable reference shared by the structured and legacy accessors. */
    public ForeignKeyReference getReference() {
        return reference;
    }

    /**
     * Replaces the reference. A null value detaches its current table, referenced columns and
     * referential actions from reference-only options such as MATCH, preserving the legacy setter
     * contract without restoring stale values from before structured edits.
     */
    public void setReference(ForeignKeyReference reference) {
        if (reference == null) {
            ForeignKeyReference detached = new ForeignKeyReference()
                    .withTable(this.reference.getTable())
                    .withReferencedColumnNames(this.reference.getReferencedColumnNames());
            detached.getReferentialActions().addAll(this.reference.getReferentialActions());
            this.reference = detached;
        } else {
            this.reference = reference;
        }
    }

    public ForeignKeyReference.MatchType getMatchType() {
        return reference.getMatchType();
    }

    public void setMatchType(ForeignKeyReference.MatchType matchType) {
        getReference().setMatchType(matchType);
    }

    public Table getTable() {
        return reference.getTable();
    }

    public void setTable(Table table) {
        reference.setTable(table);
    }

    public List<String> getReferencedColumnNames() {
        return reference.getReferencedColumnNames();
    }

    public void setReferencedColumnNames(List<String> referencedColumnNames) {
        reference.setReferencedColumnNames(referencedColumnNames);
    }

    /**
     * @param type
     * @param action
     */
    public void setReferentialAction(Type type, Action action) {
        reference.setReferentialAction(type, action);
    }

    public ForeignKeyIndex withReferentialAction(Type type, Action action) {
        setReferentialAction(type, action);
        return this;
    }

    /**
     * @param type
     */
    public void removeReferentialAction(Type type) {
        reference.removeReferentialAction(type);
    }

    /**
     * @param type
     * @return
     */
    public ReferentialAction getReferentialAction(Type type) {
        return reference.getReferentialAction(type);
    }

    @Deprecated
    public String getOnDeleteReferenceOption() {
        ReferentialAction a = getReferentialAction(Type.DELETE);
        return a == null ? null : a.getAction().getAction();
    }

    @Deprecated
    public void setOnDeleteReferenceOption(String onDeleteReferenceOption) {
        if (onDeleteReferenceOption == null) {
            removeReferentialAction(Type.DELETE);
        } else {
            setReferentialAction(Type.DELETE, Action.from(onDeleteReferenceOption));
        }
    }

    @Deprecated
    public String getOnUpdateReferenceOption() {
        ReferentialAction a = getReferentialAction(Type.UPDATE);
        return a == null ? null : a.getAction().getAction();
    }

    @Deprecated
    public void setOnUpdateReferenceOption(String onUpdateReferenceOption) {
        if (onUpdateReferenceOption == null) {
            removeReferentialAction(Type.UPDATE);
        } else {
            setReferentialAction(Type.UPDATE, Action.from(onUpdateReferenceOption));
        }
    }

    @Override
    public void appendTo(StringBuilder b, Consumer<Expression> expressionPrinter) {
        super.appendTo(b, expressionPrinter);
        b.append(' ').append(reference);
        appendConstraintSuffixTo(b);
        appendConstraintAttributesTo(b);
    }

    public ForeignKeyIndex withTable(Table table) {
        this.setTable(table);
        return this;
    }

    public ForeignKeyIndex withReferencedColumnNames(List<String> referencedColumnNames) {
        this.setReferencedColumnNames(referencedColumnNames);
        return this;
    }

    public ForeignKeyIndex withReference(ForeignKeyReference reference) {
        setReference(reference);
        return this;
    }

    public ForeignKeyIndex withMatchType(ForeignKeyReference.MatchType matchType) {
        setMatchType(matchType);
        return this;
    }

    public ForeignKeyIndex withOnDeleteReferenceOption(String onDeleteReferenceOption) {
        this.setOnDeleteReferenceOption(onDeleteReferenceOption);
        return this;
    }

    public ForeignKeyIndex withOnUpdateReferenceOption(String onUpdateReferenceOption) {
        this.setOnUpdateReferenceOption(onUpdateReferenceOption);
        return this;
    }

    public ForeignKeyIndex addReferencedColumnNames(String... referencedColumnNames) {
        reference.addReferencedColumnNames(referencedColumnNames);
        return this;
    }

    public ForeignKeyIndex addReferencedColumnNames(Collection<String> referencedColumnNames) {
        reference.addReferencedColumnNames(referencedColumnNames);
        return this;
    }

    @Override
    public ForeignKeyIndex withType(String type) {
        return (ForeignKeyIndex) super.withType(type);
    }

    @Override
    public ForeignKeyIndex withUsing(String using) {
        return (ForeignKeyIndex) super.withUsing(using);
    }

    @Override
    public ForeignKeyIndex withIndexName(String indexName) {
        return (ForeignKeyIndex) super.withIndexName(indexName);
    }

    @Override
    public ForeignKeyIndex withUseConstraintKeyword(boolean useConstraintKeyword) {
        return (ForeignKeyIndex) super.withUseConstraintKeyword(useConstraintKeyword);
    }

    @Override
    public ForeignKeyIndex withName(List<String> name) {
        return (ForeignKeyIndex) super.withName(name);
    }

    @Override
    public ForeignKeyIndex withName(String name) {
        return (ForeignKeyIndex) super.withName(name);
    }

    @Override
    public ForeignKeyIndex withColumnsNames(List<String> list) {
        return (ForeignKeyIndex) super.withColumnsNames(list);
    }

    @Override
    public ForeignKeyIndex withColumns(List<ColumnParams> columns) {
        return (ForeignKeyIndex) super.withColumns(columns);
    }

    @Override
    public ForeignKeyIndex addColumns(ColumnParams... functionDeclarationParts) {
        return (ForeignKeyIndex) super.addColumns(functionDeclarationParts);
    }

    @Override
    public ForeignKeyIndex addColumns(Collection<? extends ColumnParams> functionDeclarationParts) {
        return (ForeignKeyIndex) super.addColumns(functionDeclarationParts);
    }

    @Override
    public ForeignKeyIndex withIndexSpec(List<String> idxSpec) {
        return (ForeignKeyIndex) super.withIndexSpec(idxSpec);
    }

    @Override
    public ForeignKeyIndex withIndexKeyword(String indexKeyword) {
        return (ForeignKeyIndex) super.withIndexKeyword(indexKeyword);
    }
}
