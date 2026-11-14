/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

import java.io.Serializable;
import java.util.function.Consumer;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.UserVariable;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.statement.MySqlFileFormat;

public class MySqlSelectIntoClause extends ASTNodeAccessImpl implements Serializable {

    public enum Position {
        BEFORE_FROM, TRAILING
    }

    public enum Type {
        OUTFILE, DUMPFILE, VARIABLES
    }

    /**
     * The spelling of the fields clause, as {@link #getFieldsKeyword()} reports it from
     * {@link MySqlFileFormat.FieldsKeyword}.
     */
    public enum FieldsKeyword {
        FIELDS, COLUMNS
    }

    private Position position = Position.TRAILING;
    private Type type;
    private StringValue fileName;
    private ExpressionList<UserVariable> variables;
    private String characterSet;
    private boolean useCharsetKeyword;
    private MySqlFileFormat fileFormat;

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public MySqlSelectIntoClause withPosition(Position position) {
        this.setPosition(position);
        return this;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public ExpressionList<UserVariable> getVariables() {
        return variables;
    }

    public void setVariables(ExpressionList<UserVariable> variables) {
        this.variables = variables;
    }

    public StringValue getFileName() {
        return fileName;
    }

    public void setFileName(StringValue fileName) {
        this.fileName = fileName;
    }

    public String getCharacterSet() {
        return characterSet;
    }

    public void setCharacterSet(String characterSet) {
        this.characterSet = characterSet;
    }

    /** Whether the character set clause is spelled CHARSET rather than CHARACTER SET. */
    public boolean isUseCharsetKeyword() {
        return useCharsetKeyword;
    }

    public void setUseCharsetKeyword(boolean useCharsetKeyword) {
        this.useCharsetKeyword = useCharsetKeyword;
    }

    /**
     * The FIELDS and LINES clauses of INTO OUTFILE, shared with LOAD DATA. The getters and setters
     * of the separate subclauses below read and write this format.
     */
    public MySqlFileFormat getFileFormat() {
        return fileFormat;
    }

    public void setFileFormat(MySqlFileFormat fileFormat) {
        this.fileFormat = fileFormat;
    }

    public MySqlSelectIntoClause withFileFormat(MySqlFileFormat fileFormat) {
        setFileFormat(fileFormat);
        return this;
    }

    private MySqlFileFormat fileFormat() {
        if (fileFormat == null) {
            fileFormat = new MySqlFileFormat();
        }
        return fileFormat;
    }

    public FieldsKeyword getFieldsKeyword() {
        MySqlFileFormat.FieldsKeyword keyword =
                fileFormat != null ? fileFormat.getFieldsKeyword() : null;
        return keyword != null ? FieldsKeyword.valueOf(keyword.name()) : null;
    }

    public void setFieldsKeyword(FieldsKeyword fieldsKeyword) {
        fileFormat().setFieldsKeyword(fieldsKeyword != null
                ? MySqlFileFormat.FieldsKeyword.valueOf(fieldsKeyword.name())
                : null);
    }

    public StringValue getFieldsTerminatedBy() {
        return fileFormat != null ? fileFormat.getFieldsTerminatedBy() : null;
    }

    public void setFieldsTerminatedBy(StringValue fieldsTerminatedBy) {
        fileFormat().setFieldsTerminatedBy(fieldsTerminatedBy);
    }

    public boolean isFieldsOptionallyEnclosed() {
        return fileFormat != null && fileFormat.isFieldsOptionallyEnclosed();
    }

    public void setFieldsOptionallyEnclosed(boolean fieldsOptionallyEnclosed) {
        fileFormat().setFieldsOptionallyEnclosed(fieldsOptionallyEnclosed);
    }

    public StringValue getFieldsEnclosedBy() {
        return fileFormat != null ? fileFormat.getFieldsEnclosedBy() : null;
    }

    public void setFieldsEnclosedBy(StringValue fieldsEnclosedBy) {
        fileFormat().setFieldsEnclosedBy(fieldsEnclosedBy);
    }

    public StringValue getFieldsEscapedBy() {
        return fileFormat != null ? fileFormat.getFieldsEscapedBy() : null;
    }

    public void setFieldsEscapedBy(StringValue fieldsEscapedBy) {
        fileFormat().setFieldsEscapedBy(fieldsEscapedBy);
    }

    public StringValue getLinesStartingBy() {
        return fileFormat != null ? fileFormat.getLinesStartingBy() : null;
    }

    public void setLinesStartingBy(StringValue linesStartingBy) {
        fileFormat().setLinesStartingBy(linesStartingBy);
    }

    public StringValue getLinesTerminatedBy() {
        return fileFormat != null ? fileFormat.getLinesTerminatedBy() : null;
    }

    public void setLinesTerminatedBy(StringValue linesTerminatedBy) {
        fileFormat().setLinesTerminatedBy(linesTerminatedBy);
    }

    public boolean hasFieldsClause() {
        return fileFormat != null && fileFormat.hasFieldsClause();
    }

    public boolean hasLinesClause() {
        return fileFormat != null && fileFormat.hasLinesClause();
    }

    public StringBuilder appendTo(StringBuilder builder) {
        return appendTo(builder, expression -> builder.append(expression));
    }

    /** Shares INTO rendering while allowing deparsers to visit variable targets. */
    public StringBuilder appendTo(StringBuilder builder, Consumer<Expression> expressionRenderer) {
        builder.append("INTO ");
        if (type == Type.VARIABLES) {
            if (variables != null) {
                expressionRenderer.accept(variables);
            }
            return builder;
        }
        builder.append(type);
        if (fileName != null) {
            builder.append(" ").append(fileName);
        }
        if (characterSet != null) {
            builder.append(useCharsetKeyword ? " CHARSET " : " CHARACTER SET ")
                    .append(characterSet);
        }
        if (fileFormat != null && !fileFormat.isEmpty()) {
            fileFormat.appendTo(builder.append(" "));
        }
        return builder;
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }
}
