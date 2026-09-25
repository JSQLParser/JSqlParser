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
        SERIAL_DEFAULT_VALUE, REFERENCE, IDENTITY, CONSTRAINT, DEFAULT, NULLABILITY, COLLATE, COMMENT, ON_UPDATE, GENERATED, AUTO_INCREMENT, VISIBILITY, STORAGE, COMPRESSION, FOREIGN_OPTIONS, OTHER
    }

    /** PostgreSQL storage strategies and MySQL column storage locations. */
    public enum Storage {
        PLAIN, EXTERNAL, EXTENDED, MAIN, DEFAULT, DISK, MEMORY
    }

    private List<ForeignDataOption> foreignOptions;

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

    private Kind kind = Kind.OTHER;
    private List<String> tokens;
    private ForeignKeyReference foreignKeyReference;
    private IdentityDefinition identityDefinition;
    private Index constraint;
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

    /** Visits expressions of the selected option kind, including comment literals. */
    public void visitExpressions(Consumer<Expression> visitor) {
        if (kind == Kind.FOREIGN_OPTIONS) {
            ForeignDataOption.visitExpressions(foreignOptions, visitor);
        } else if (kind == Kind.DEFAULT) {
            visitor.accept(defaultExpression);
        } else if (kind == Kind.COMMENT) {
            visitor.accept(comment);
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

    public static ColumnOption constraint(Index constraint) {
        ColumnOption option = new ColumnOption();
        option.kind = Kind.CONSTRAINT;
        option.constraint = constraint;
        return option;
    }

    public IdentityDefinition getIdentityDefinition() {
        return identityDefinition;
    }

    public Index getConstraint() {
        return constraint;
    }

    public static ColumnOption raw(List<String> tokens) {
        ColumnOption option = new ColumnOption();
        option.tokens = tokens;
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

    public List<String> getTokens() {
        switch (kind) {
            case DEFAULT:
                return Arrays.asList("DEFAULT", String.valueOf(defaultExpression));
            case NULLABILITY:
                return nullable ? Collections.singletonList("NULL") : Arrays.asList("NOT", "NULL");
            case COLLATE:
                return Arrays.asList("COLLATE", collation);
            case COMMENT:
                return Arrays.asList("COMMENT", comment.toString());
            case ON_UPDATE:
                return Arrays.asList("ON", "UPDATE", onUpdateExpression.toString());
            case GENERATED:
                return generatedDefinition.getTokens();
            case CONSTRAINT:
                if ("PRIMARY KEY".equals(constraint.toString())) {
                    return Arrays.asList("PRIMARY", "KEY");
                }
                return Collections.singletonList(toString());
            case OTHER:
            case SERIAL_DEFAULT_VALUE:
                return tokens;
            default:
                return Collections.singletonList(toString());
        }
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
