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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.statement.select.PlainSelect;

/** A structured option following a column data type. */
public class ColumnOption implements Serializable {

    public enum Kind {
        SERIAL_DEFAULT_VALUE, REFERENCE, IDENTITY, CONSTRAINT, DEFAULT, NULLABILITY, COLLATE, COMMENT, ON_UPDATE, GENERATED, AUTO_INCREMENT, VISIBILITY, STORAGE, COMPRESSION, FOREIGN_OPTIONS, SRID, COLUMN_FORMAT, ENGINE_ATTRIBUTE, SECONDARY_ENGINE_ATTRIBUTE, OTHER
    }

    /** PostgreSQL storage strategies and MySQL column storage locations. */
    public enum Storage {
        PLAIN, EXTERNAL, EXTENDED, MAIN, DEFAULT, DISK, MEMORY
    }

    /** MySQL {@code COLUMN_FORMAT} values. */
    public enum ColumnFormat {
        FIXED, DYNAMIC, DEFAULT
    }

    /**
     * Inline key spellings whose legacy tokens are split on spaces. KEY is MySQL's column-level
     * synonym for PRIMARY KEY.
     */
    private static final List<String> KEY_SPELLINGS =
            Arrays.asList("PRIMARY KEY", "UNIQUE KEY", "KEY");

    private List<ForeignDataOption> foreignOptions;
    private Long srid;
    private ColumnFormat columnFormat;
    private StringValue engineAttribute;
    private boolean useEquals;

    public List<ForeignDataOption> getForeignOptions() {
        return foreignOptions;
    }

    public void setForeignOptions(List<ForeignDataOption> options) {
        foreignOptions = options;
        kind = Kind.FOREIGN_OPTIONS;
        tokens = null;
    }

    public static ColumnOption foreignOptions(List<ForeignDataOption> options) {
        ColumnOption option = new ColumnOption();
        option.setForeignOptions(options);
        return option;
    }

    private Storage storage;
    private String compression;

    public static ColumnOption storage(Storage storage) {
        ColumnOption option = new ColumnOption();
        option.kind = Kind.STORAGE;
        option.setStorage(storage);
        return option;
    }

    public Storage getStorage() {
        return storage;
    }

    public void setStorage(Storage storage) {
        this.storage = Objects.requireNonNull(storage, "storage");
    }

    public static ColumnOption compression(String compression) {
        ColumnOption option = new ColumnOption();
        option.kind = Kind.COMPRESSION;
        option.setCompression(compression);
        return option;
    }

    public String getCompression() {
        return compression;
    }

    public void setCompression(String compression) {
        this.compression = Objects.requireNonNull(compression, "compression");
    }

    /** Creates a MySQL {@code SRID} option for a spatial column. */
    public static ColumnOption srid(long srid) {
        ColumnOption option = new ColumnOption();
        option.kind = Kind.SRID;
        option.setSrid(srid);
        return option;
    }

    /** Returns the spatial reference system identifier of an SRID option, or null. */
    public Long getSrid() {
        return srid;
    }

    public void setSrid(long srid) {
        this.srid = srid;
    }

    /** Creates a MySQL {@code COLUMN_FORMAT} option. */
    public static ColumnOption columnFormat(ColumnFormat columnFormat) {
        ColumnOption option = new ColumnOption();
        option.kind = Kind.COLUMN_FORMAT;
        option.setColumnFormat(columnFormat);
        return option;
    }

    public ColumnFormat getColumnFormat() {
        return columnFormat;
    }

    public void setColumnFormat(ColumnFormat columnFormat) {
        this.columnFormat = Objects.requireNonNull(columnFormat, "columnFormat");
    }

    /** Creates a MySQL {@code ENGINE_ATTRIBUTE} option with a JSON string literal. */
    public static ColumnOption engineAttribute(StringValue attribute) {
        ColumnOption option = new ColumnOption();
        option.kind = Kind.ENGINE_ATTRIBUTE;
        option.setEngineAttribute(attribute);
        return option;
    }

    /** Creates a MySQL {@code SECONDARY_ENGINE_ATTRIBUTE} option with a JSON string literal. */
    public static ColumnOption secondaryEngineAttribute(StringValue attribute) {
        ColumnOption option = new ColumnOption();
        option.kind = Kind.SECONDARY_ENGINE_ATTRIBUTE;
        option.setEngineAttribute(attribute);
        return option;
    }

    /** Returns the literal of an ENGINE_ATTRIBUTE or SECONDARY_ENGINE_ATTRIBUTE option, or null. */
    public StringValue getEngineAttribute() {
        return engineAttribute;
    }

    public void setEngineAttribute(StringValue attribute) {
        engineAttribute = Objects.requireNonNull(attribute, "attribute");
    }

    /** Whether an engine attribute is written with the optional equals sign. */
    public boolean isUseEquals() {
        return useEquals;
    }

    public void setUseEquals(boolean useEquals) {
        this.useEquals = useEquals;
    }

    private boolean isEngineAttribute() {
        return kind == Kind.ENGINE_ATTRIBUTE || kind == Kind.SECONDARY_ENGINE_ATTRIBUTE;
    }

    private Kind kind = Kind.OTHER;
    private List<String> tokens;
    private ForeignKeyReference foreignKeyReference;
    private IdentityDefinition identityDefinition;
    private NamedConstraint constraint;
    private Expression defaultExpression;
    private Boolean nullable;
    private Boolean visible;
    private String collation;
    private StringValue comment;
    private Expression onUpdateExpression;
    private GeneratedColumnDefinition generatedDefinition;

    public static ColumnOption nullability(boolean nullable) {
        ColumnOption option = new ColumnOption();
        option.kind = Kind.NULLABILITY;
        option.nullable = nullable;
        return option;
    }

    public Boolean getNullable() {
        return nullable;
    }

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    public static ColumnOption visibility(boolean visible) {
        ColumnOption option = new ColumnOption();
        option.kind = Kind.VISIBILITY;
        option.visible = visible;
        return option;
    }

    public Boolean getVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public static ColumnOption autoIncrement() {
        ColumnOption option = new ColumnOption();
        option.kind = Kind.AUTO_INCREMENT;
        return option;
    }

    public static ColumnOption collate(String collation) {
        ColumnOption option = new ColumnOption();
        option.kind = Kind.COLLATE;
        option.setCollation(collation);
        return option;
    }

    public String getCollation() {
        return collation;
    }

    public void setCollation(String collation) {
        this.collation = Objects.requireNonNull(collation, "collation");
    }

    public static ColumnOption comment(StringValue comment) {
        ColumnOption option = new ColumnOption();
        option.kind = Kind.COMMENT;
        option.setComment(comment);
        return option;
    }

    public StringValue getComment() {
        return comment;
    }

    public void setComment(StringValue comment) {
        this.comment = Objects.requireNonNull(comment, "comment");
    }

    public static ColumnOption onUpdate(Expression expression) {
        ColumnOption option = new ColumnOption();
        option.kind = Kind.ON_UPDATE;
        option.setOnUpdateExpression(expression);
        return option;
    }

    public Expression getOnUpdateExpression() {
        return onUpdateExpression;
    }

    public void setOnUpdateExpression(Expression expression) {
        onUpdateExpression = Objects.requireNonNull(expression, "expression");
    }

    public static ColumnOption generated(GeneratedColumnDefinition definition) {
        ColumnOption option = new ColumnOption();
        option.kind = Kind.GENERATED;
        option.setGeneratedDefinition(definition);
        return option;
    }

    public GeneratedColumnDefinition getGeneratedDefinition() {
        return generatedDefinition;
    }

    public void setGeneratedDefinition(GeneratedColumnDefinition definition) {
        generatedDefinition = Objects.requireNonNull(definition, "definition");
    }

    /** Visits expressions of the selected option kind, including comment and attribute literals. */
    public void visitExpressions(Consumer<Expression> visitor) {
        if (kind == Kind.FOREIGN_OPTIONS) {
            ForeignDataOption.visitExpressions(foreignOptions, visitor);
        } else if (kind == Kind.DEFAULT) {
            visitor.accept(defaultExpression);
        } else if (kind == Kind.COMMENT) {
            visitor.accept(comment);
        } else if (isEngineAttribute()) {
            visitor.accept(engineAttribute);
        } else if (kind == Kind.ON_UPDATE) {
            visitor.accept(onUpdateExpression);
        } else if (kind == Kind.GENERATED) {
            visitor.accept(generatedDefinition.getExpression());
        }
    }

    /** Creates a DEFAULT option. Use a NullValue expression for SQL NULL. */
    public static ColumnOption defaultValue(Expression expression) {
        ColumnOption option = new ColumnOption();
        option.kind = Kind.DEFAULT;
        option.setDefaultExpression(expression);
        return option;
    }

    public Expression getDefaultExpression() {
        return defaultExpression;
    }

    /** Replaces the expression of a DEFAULT option created by {@link #defaultValue(Expression)}. */
    public void setDefaultExpression(Expression expression) {
        defaultExpression = Objects.requireNonNull(expression, "defaultExpression");
    }

    public static ColumnOption identity(IdentityDefinition definition) {
        ColumnOption option = new ColumnOption();
        option.kind = Kind.IDENTITY;
        option.identityDefinition = definition;
        return option;
    }

    public static ColumnOption constraint(NamedConstraint constraint) {
        ColumnOption option = new ColumnOption();
        option.kind = Kind.CONSTRAINT;
        option.constraint = constraint;
        return option;
    }

    public IdentityDefinition getIdentityDefinition() {
        return identityDefinition;
    }

    public NamedConstraint getConstraint() {
        return constraint;
    }

    /**
     * Returns the constraint kind explicitly represented by this option, including REFERENCES and
     * NOT NULL. Ordinary DEFAULT options remain column options; an explicit DefaultConstraint node
     * has kind DEFAULT. Returns OTHER for NULL, unrelated or raw options. No implicit constraints,
     * such as NOT NULL implied by a primary key, or catalogue objects are inferred.
     */
    public ConstraintKind getConstraintKind() {
        switch (kind) {
            case CONSTRAINT:
                return constraint != null && constraint.getKind() != null ? constraint.getKind()
                        : ConstraintKind.OTHER;
            case REFERENCE:
                return foreignKeyReference == null ? ConstraintKind.OTHER
                        : ConstraintKind.FOREIGN_KEY;
            case NULLABILITY:
                return Boolean.FALSE.equals(nullable) ? ConstraintKind.NOT_NULL
                        : ConstraintKind.OTHER;
            default:
                return ConstraintKind.OTHER;
        }
    }

    /** Creates an opaque option with its own copy of the supplied tokens, without parsing them. */
    public static ColumnOption raw(List<String> tokens) {
        ColumnOption option = new ColumnOption();
        option.setTokens(tokens);
        return option;
    }

    public static ColumnOption raw(String... tokens) {
        return raw(Arrays.asList(tokens));
    }

    public static ColumnOption serialDefaultValue() {
        ColumnOption option = raw("SERIAL", "DEFAULT", "VALUE");
        option.kind = Kind.SERIAL_DEFAULT_VALUE;
        return option;
    }

    public static ColumnOption reference(ForeignKeyReference reference) {
        ColumnOption option = new ColumnOption();
        option.kind = Kind.REFERENCE;
        option.foreignKeyReference = reference;
        return option;
    }

    public Kind getKind() {
        return kind;
    }

    /**
     * Returns an unmodifiable, detached token snapshot for both raw and structured options. An
     * unset raw option returns null; an explicitly empty raw option returns an empty snapshot. Use
     * {@link #setTokens(List)} to edit an opaque option, or the typed setters and mutable child
     * nodes to edit structured options. Later changes to the option do not change this snapshot.
     */
    public List<String> getTokens() {
        switch (kind) {
            case DEFAULT:
                return Collections.unmodifiableList(
                        Arrays.asList("DEFAULT", String.valueOf(defaultExpression)));
            case NULLABILITY:
                return nullable ? Collections.singletonList("NULL")
                        : Collections.unmodifiableList(Arrays.asList("NOT", "NULL"));
            case COLLATE:
                return Collections.unmodifiableList(Arrays.asList("COLLATE", collation));
            case COMMENT:
                return Collections.unmodifiableList(Arrays.asList("COMMENT", comment.toString()));
            case ON_UPDATE:
                return Collections.unmodifiableList(
                        Arrays.asList("ON", "UPDATE", onUpdateExpression.toString()));
            case GENERATED:
                return generatedDefinition.getTokens();
            case CONSTRAINT:
                return constraintTokens();
            case SRID:
                return Collections.unmodifiableList(Arrays.asList("SRID", String.valueOf(srid)));
            case COLUMN_FORMAT:
                return Collections.unmodifiableList(
                        Arrays.asList("COLUMN_FORMAT", columnFormat.name()));
            case ENGINE_ATTRIBUTE:
            case SECONDARY_ENGINE_ATTRIBUTE:
                return Collections.unmodifiableList(useEquals
                        ? Arrays.asList(kind.name(), "=", engineAttribute.toString())
                        : Arrays.asList(kind.name(), engineAttribute.toString()));
            case OTHER:
            case SERIAL_DEFAULT_VALUE:
                return tokens == null ? null
                        : Collections.unmodifiableList(new ArrayList<>(tokens));
            default:
                return Collections.singletonList(toString());
        }
    }

    private List<String> constraintTokens() {
        String sql = constraint.toString();
        if (KEY_SPELLINGS.contains(sql)) {
            return Collections.unmodifiableList(Arrays.asList(sql.split(" ")));
        }
        return Collections.singletonList(sql);
    }

    /**
     * Replaces the tokens of an opaque {@link Kind#OTHER} option with a copy of the supplied list.
     * Null unsets the tokens; an empty list retains an explicitly empty option. Tokens are never
     * parsed or classified. To replace a structured option, including SERIAL DEFAULT VALUE, with
     * raw text, replace its node in {@link ColumnDefinition#getColumnOptions()} using
     * {@link #raw(List)} instead.
     *
     * @throws IllegalStateException if this is a structured option
     */
    public void setTokens(List<String> tokens) {
        if (kind != Kind.OTHER) {
            throw new IllegalStateException(
                    "Only opaque options accept tokens; use typed setters or replace the option");
        }
        this.tokens = tokens == null ? null : new ArrayList<>(tokens);
    }

    public ForeignKeyReference getForeignKeyReference() {
        return foreignKeyReference;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        appendTo(builder, builder::append);
        return builder.toString();
    }

    /** Appends the option using the supplied printer for structured expressions. */
    public void appendTo(StringBuilder builder, Consumer<Expression> expressionPrinter) {
        switch (kind) {
            case FOREIGN_OPTIONS:
                ForeignDataOption.appendOptionsTo(builder, foreignOptions, expressionPrinter);
                break;
            case STORAGE:
                builder.append("STORAGE ").append(storage);
                break;
            case COMPRESSION:
                builder.append("COMPRESSION ").append(compression);
                break;
            case SRID:
                builder.append("SRID ").append(srid);
                break;
            case COLUMN_FORMAT:
                builder.append("COLUMN_FORMAT ").append(columnFormat);
                break;
            case ENGINE_ATTRIBUTE:
            case SECONDARY_ENGINE_ATTRIBUTE:
                builder.append(kind).append(useEquals ? " = " : " ");
                expressionPrinter.accept(engineAttribute);
                break;
            case DEFAULT:
                builder.append("DEFAULT ");
                expressionPrinter.accept(defaultExpression);
                break;
            case NULLABILITY:
                builder.append(nullable ? "NULL" : "NOT NULL");
                break;
            case VISIBILITY:
                builder.append(visible ? "VISIBLE" : "INVISIBLE");
                break;
            case AUTO_INCREMENT:
                builder.append("AUTO_INCREMENT");
                break;
            case COLLATE:
                builder.append("COLLATE ").append(collation);
                break;
            case COMMENT:
                builder.append("COMMENT ");
                expressionPrinter.accept(comment);
                break;
            case ON_UPDATE:
                builder.append("ON UPDATE ");
                expressionPrinter.accept(onUpdateExpression);
                break;
            case GENERATED:
                generatedDefinition.appendTo(builder, expressionPrinter);
                break;
            case REFERENCE:
                builder.append(foreignKeyReference);
                break;
            case IDENTITY:
                builder.append(identityDefinition);
                break;
            case CONSTRAINT:
                constraint.appendTo(builder, expressionPrinter);
                break;
            default:
                builder.append(PlainSelect.getStringList(tokens, false, false));
                break;
        }
    }
}
