/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import java.io.Serializable;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

/**
 * The {@code {FIELDS | COLUMNS}} and {@code LINES} clauses that describe the text file of a MySQL
 * {@code LOAD DATA} or {@code SELECT ... INTO OUTFILE}:
 *
 * <pre>
 * [{FIELDS | COLUMNS} [TERMINATED BY 'string'] [[OPTIONALLY] ENCLOSED BY 'char'] [ESCAPED BY 'char']]
 * [LINES [STARTING BY 'string'] [TERMINATED BY 'string']]
 * </pre>
 *
 * Each clause has at least one subclause. MySQL accepts the subclauses of a clause in any order and
 * more than once; like the server, the model keeps the last value of each and prints them in the
 * order above. {@code OPTIONALLY} stays set once a subclause has written it.
 *
 * @see <a href="https://dev.mysql.com/doc/refman/8.4/en/load-data.html">LOAD DATA</a>
 */
public class MySqlFileFormat extends ASTNodeAccessImpl implements Serializable {

    /** The spelling of the fields clause. */
    public enum FieldsKeyword {
        FIELDS, COLUMNS
    }

    private FieldsKeyword fieldsKeyword;
    private StringValue fieldsTerminatedBy;
    private boolean fieldsOptionallyEnclosed;
    private StringValue fieldsEnclosedBy;
    private StringValue fieldsEscapedBy;
    private StringValue linesStartingBy;
    private StringValue linesTerminatedBy;

    /** The spelling of the fields clause; null prints FIELDS when a subclause is set. */
    public FieldsKeyword getFieldsKeyword() {
        return fieldsKeyword;
    }

    public void setFieldsKeyword(FieldsKeyword fieldsKeyword) {
        this.fieldsKeyword = fieldsKeyword;
    }

    public MySqlFileFormat withFieldsKeyword(FieldsKeyword fieldsKeyword) {
        setFieldsKeyword(fieldsKeyword);
        return this;
    }

    public StringValue getFieldsTerminatedBy() {
        return fieldsTerminatedBy;
    }

    public void setFieldsTerminatedBy(StringValue fieldsTerminatedBy) {
        this.fieldsTerminatedBy = fieldsTerminatedBy;
    }

    public MySqlFileFormat withFieldsTerminatedBy(StringValue fieldsTerminatedBy) {
        setFieldsTerminatedBy(fieldsTerminatedBy);
        return this;
    }

    public boolean isFieldsOptionallyEnclosed() {
        return fieldsOptionallyEnclosed;
    }

    public void setFieldsOptionallyEnclosed(boolean fieldsOptionallyEnclosed) {
        this.fieldsOptionallyEnclosed = fieldsOptionallyEnclosed;
    }

    public MySqlFileFormat withFieldsOptionallyEnclosed(boolean fieldsOptionallyEnclosed) {
        setFieldsOptionallyEnclosed(fieldsOptionallyEnclosed);
        return this;
    }

    public StringValue getFieldsEnclosedBy() {
        return fieldsEnclosedBy;
    }

    public void setFieldsEnclosedBy(StringValue fieldsEnclosedBy) {
        this.fieldsEnclosedBy = fieldsEnclosedBy;
    }

    public MySqlFileFormat withFieldsEnclosedBy(StringValue fieldsEnclosedBy) {
        setFieldsEnclosedBy(fieldsEnclosedBy);
        return this;
    }

    public StringValue getFieldsEscapedBy() {
        return fieldsEscapedBy;
    }

    public void setFieldsEscapedBy(StringValue fieldsEscapedBy) {
        this.fieldsEscapedBy = fieldsEscapedBy;
    }

    public MySqlFileFormat withFieldsEscapedBy(StringValue fieldsEscapedBy) {
        setFieldsEscapedBy(fieldsEscapedBy);
        return this;
    }

    public StringValue getLinesStartingBy() {
        return linesStartingBy;
    }

    public void setLinesStartingBy(StringValue linesStartingBy) {
        this.linesStartingBy = linesStartingBy;
    }

    public MySqlFileFormat withLinesStartingBy(StringValue linesStartingBy) {
        setLinesStartingBy(linesStartingBy);
        return this;
    }

    public StringValue getLinesTerminatedBy() {
        return linesTerminatedBy;
    }

    public void setLinesTerminatedBy(StringValue linesTerminatedBy) {
        this.linesTerminatedBy = linesTerminatedBy;
    }

    public MySqlFileFormat withLinesTerminatedBy(StringValue linesTerminatedBy) {
        setLinesTerminatedBy(linesTerminatedBy);
        return this;
    }

    /** Whether the format prints a fields clause: a spelling or a subclause is set. */
    public boolean hasFieldsClause() {
        return fieldsKeyword != null || fieldsTerminatedBy != null || fieldsEnclosedBy != null
                || fieldsEscapedBy != null;
    }

    /** Whether the format prints a lines clause: a subclause is set. */
    public boolean hasLinesClause() {
        return linesStartingBy != null || linesTerminatedBy != null;
    }

    /** Whether the format prints nothing. */
    public boolean isEmpty() {
        return !hasFieldsClause() && !hasLinesClause();
    }

    /** Appends the fields clause and then the lines clause, separated by a space. */
    public StringBuilder appendTo(StringBuilder builder) {
        if (hasFieldsClause()) {
            appendFieldsClause(builder);
            if (hasLinesClause()) {
                builder.append(' ');
            }
        }
        if (hasLinesClause()) {
            appendLinesClause(builder);
        }
        return builder;
    }

    private void appendFieldsClause(StringBuilder builder) {
        builder.append(fieldsKeyword != null ? fieldsKeyword : FieldsKeyword.FIELDS);
        if (fieldsTerminatedBy != null) {
            builder.append(" TERMINATED BY ").append(fieldsTerminatedBy);
        }
        if (fieldsEnclosedBy != null) {
            builder.append(fieldsOptionallyEnclosed ? " OPTIONALLY ENCLOSED BY " : " ENCLOSED BY ")
                    .append(fieldsEnclosedBy);
        }
        if (fieldsEscapedBy != null) {
            builder.append(" ESCAPED BY ").append(fieldsEscapedBy);
        }
    }

    private void appendLinesClause(StringBuilder builder) {
        builder.append("LINES");
        if (linesStartingBy != null) {
            builder.append(" STARTING BY ").append(linesStartingBy);
        }
        if (linesTerminatedBy != null) {
            builder.append(" TERMINATED BY ").append(linesTerminatedBy);
        }
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }
}
