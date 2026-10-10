/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
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

public class ForeignKeyConstraint extends NamedConstraint implements KeyColumnSource {
    private List<KeyElement> columns;
    private String indexName;
    private ForeignKeyReference reference = new ForeignKeyReference();

    public ForeignKeyConstraint() {
        setType("FOREIGN KEY");
    }

    @Override
    public ConstraintKind getKind() {
        return ConstraintKind.FOREIGN_KEY;
    }

    @Override
    public List<KeyElement> getColumns() {
        return columns;
    }

    @Override
    public void setColumns(List<KeyElement> columns) {
        this.columns = columns;
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String value) {
        indexName = value;
    }

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

    public ForeignKeyConstraint withReferentialAction(Type type, Action action) {
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
        appendConstraintPrefixTo(b);
        b.append(getType());
        if (indexName != null) {
            b.append(' ').append(indexName);
        }
        if (columns != null) {
            b.append(' ');
            appendColumnsTo(b, expressionPrinter);
        }
        b.append(' ').append(reference);
        appendConstraintSuffixTo(b);
        appendConstraintAttributesTo(b);
    }

    public ForeignKeyConstraint withTable(Table table) {
        this.setTable(table);
        return this;
    }

    public ForeignKeyConstraint withReferencedColumnNames(List<String> referencedColumnNames) {
        this.setReferencedColumnNames(referencedColumnNames);
        return this;
    }

    public ForeignKeyConstraint withReference(ForeignKeyReference reference) {
        setReference(reference);
        return this;
    }

    public ForeignKeyConstraint withMatchType(ForeignKeyReference.MatchType matchType) {
        setMatchType(matchType);
        return this;
    }

    public ForeignKeyConstraint withOnDeleteReferenceOption(String onDeleteReferenceOption) {
        this.setOnDeleteReferenceOption(onDeleteReferenceOption);
        return this;
    }

    public ForeignKeyConstraint withOnUpdateReferenceOption(String onUpdateReferenceOption) {
        this.setOnUpdateReferenceOption(onUpdateReferenceOption);
        return this;
    }

    public ForeignKeyConstraint addReferencedColumnNames(String... referencedColumnNames) {
        reference.addReferencedColumnNames(referencedColumnNames);
        return this;
    }

    public ForeignKeyConstraint addReferencedColumnNames(Collection<String> referencedColumnNames) {
        reference.addReferencedColumnNames(referencedColumnNames);
        return this;
    }

    @Override
    public ForeignKeyConstraint withType(String type) {
        return (ForeignKeyConstraint) super.withType(type);
    }

    @Override
    public ForeignKeyConstraint withUseConstraintKeyword(boolean useConstraintKeyword) {
        return (ForeignKeyConstraint) super.withUseConstraintKeyword(useConstraintKeyword);
    }

    @Override
    public ForeignKeyConstraint withName(List<String> name) {
        return (ForeignKeyConstraint) super.withName(name);
    }

    @Override
    public ForeignKeyConstraint withName(String name) {
        return (ForeignKeyConstraint) super.withName(name);
    }

    public ForeignKeyConstraint withIndexName(String value) {
        setIndexName(value);
        return this;
    }

    public ForeignKeyConstraint withColumnsNames(List<String> value) {
        setColumnsNames(value);
        return this;
    }

    public ForeignKeyConstraint withColumns(List<KeyElement> value) {
        setColumns(value);
        return this;
    }

    public ForeignKeyConstraint addColumns(KeyElement... values) {
        return addColumns(java.util.Arrays.asList(values));
    }

    public ForeignKeyConstraint addColumns(Collection<? extends KeyElement> values) {
        if (columns == null) {
            columns = new java.util.ArrayList<>();
        }
        columns.addAll(values);
        return this;
    }
}
