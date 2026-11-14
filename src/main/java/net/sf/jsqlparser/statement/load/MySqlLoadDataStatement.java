/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.load;

import java.util.List;
import java.util.function.Consumer;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.MySqlFileFormat;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.update.UpdateSet;

/**
 * MySQL's {@code LOAD DATA} and {@code LOAD XML}:
 *
 * <pre>
 * LOAD {DATA | XML} [LOW_PRIORITY | CONCURRENT] [LOCAL] INFILE 'file_name' [REPLACE | IGNORE]
 *     INTO TABLE tbl_name [PARTITION (partition_name [, partition_name] ...)]
 *     [CHARACTER SET charset_name] [ROWS IDENTIFIED BY '&lt;tagname&gt;']
 *     [{FIELDS | COLUMNS} ...] [LINES ...] [IGNORE number {LINES | ROWS}]
 *     [(col_name_or_user_var [, col_name_or_user_var] ...)]
 *     [SET col_name = {expr | DEFAULT} [, col_name = {expr | DEFAULT}] ...]
 * </pre>
 *
 * Both forms share one grammar in MySQL, so every clause is available to both. The FIELDS and LINES
 * clauses are the {@link MySqlFileFormat} of {@code SELECT ... INTO OUTFILE}. BigQuery's
 * {@code LOAD DATA INTO} is {@link LoadDataStatement}.
 *
 * @see <a href="https://dev.mysql.com/doc/refman/8.4/en/load-data.html">LOAD DATA</a>
 * @see <a href="https://dev.mysql.com/doc/refman/8.4/en/load-xml.html">LOAD XML</a>
 */
public class MySqlLoadDataStatement extends ASTNodeAccessImpl implements Statement {

    /** The word after LOAD: a delimited text file or an XML file. */
    public enum Format {
        DATA, XML
    }

    /** When the rows are written while other sessions use the table. */
    public enum LockOption {
        LOW_PRIORITY, CONCURRENT
    }

    /** The word of {@code IGNORE number {LINES | ROWS}}. */
    public enum IgnoreUnit {
        LINES, ROWS
    }

    private Format format = Format.DATA;
    private LockOption lockOption;
    private boolean local;
    private StringValue fileName;
    private CreateTable.DuplicateHandling duplicateHandling;
    private Table table;
    private List<String> partitions;
    private String characterSet;
    private boolean useCharsetKeyword;
    private StringValue rowsIdentifiedBy;
    private MySqlFileFormat fileFormat;
    private Long ignoreCount;
    private IgnoreUnit ignoreUnit = IgnoreUnit.LINES;
    private ExpressionList<Expression> columns;
    private List<UpdateSet> updateSets;

    public Format getFormat() {
        return format;
    }

    public void setFormat(Format format) {
        this.format = format;
    }

    public MySqlLoadDataStatement withFormat(Format format) {
        setFormat(format);
        return this;
    }

    public LockOption getLockOption() {
        return lockOption;
    }

    public void setLockOption(LockOption lockOption) {
        this.lockOption = lockOption;
    }

    public MySqlLoadDataStatement withLockOption(LockOption lockOption) {
        setLockOption(lockOption);
        return this;
    }

    /** Whether the client reads the file ({@code LOCAL}) rather than the server. */
    public boolean isLocal() {
        return local;
    }

    public void setLocal(boolean local) {
        this.local = local;
    }

    public MySqlLoadDataStatement withLocal(boolean local) {
        setLocal(local);
        return this;
    }

    public StringValue getFileName() {
        return fileName;
    }

    public void setFileName(StringValue fileName) {
        this.fileName = fileName;
    }

    public MySqlLoadDataStatement withFileName(StringValue fileName) {
        setFileName(fileName);
        return this;
    }

    /** {@code REPLACE} or {@code IGNORE} for rows that duplicate a unique key, or null. */
    public CreateTable.DuplicateHandling getDuplicateHandling() {
        return duplicateHandling;
    }

    public void setDuplicateHandling(CreateTable.DuplicateHandling duplicateHandling) {
        this.duplicateHandling = duplicateHandling;
    }

    public MySqlLoadDataStatement withDuplicateHandling(
            CreateTable.DuplicateHandling duplicateHandling) {
        setDuplicateHandling(duplicateHandling);
        return this;
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public MySqlLoadDataStatement withTable(Table table) {
        setTable(table);
        return this;
    }

    /** The names of {@code PARTITION (p0, p1)}, or null without the clause. */
    public List<String> getPartitions() {
        return partitions;
    }

    public void setPartitions(List<String> partitions) {
        this.partitions = partitions;
    }

    public MySqlLoadDataStatement withPartitions(List<String> partitions) {
        setPartitions(partitions);
        return this;
    }

    /** The character set name as written, a quoted name keeping its quotes. */
    public String getCharacterSet() {
        return characterSet;
    }

    public void setCharacterSet(String characterSet) {
        this.characterSet = characterSet;
    }

    public MySqlLoadDataStatement withCharacterSet(String characterSet) {
        setCharacterSet(characterSet);
        return this;
    }

    /** Whether the character set clause is spelled CHARSET rather than CHARACTER SET. */
    public boolean isUseCharsetKeyword() {
        return useCharsetKeyword;
    }

    public void setUseCharsetKeyword(boolean useCharsetKeyword) {
        this.useCharsetKeyword = useCharsetKeyword;
    }

    public MySqlLoadDataStatement withUseCharsetKeyword(boolean useCharsetKeyword) {
        setUseCharsetKeyword(useCharsetKeyword);
        return this;
    }

    /** The XML tag of {@code ROWS IDENTIFIED BY '<tagname>'}, or null. */
    public StringValue getRowsIdentifiedBy() {
        return rowsIdentifiedBy;
    }

    public void setRowsIdentifiedBy(StringValue rowsIdentifiedBy) {
        this.rowsIdentifiedBy = rowsIdentifiedBy;
    }

    public MySqlLoadDataStatement withRowsIdentifiedBy(StringValue rowsIdentifiedBy) {
        setRowsIdentifiedBy(rowsIdentifiedBy);
        return this;
    }

    /** The FIELDS and LINES clauses, or null without either. */
    public MySqlFileFormat getFileFormat() {
        return fileFormat;
    }

    public void setFileFormat(MySqlFileFormat fileFormat) {
        this.fileFormat = fileFormat;
    }

    public MySqlLoadDataStatement withFileFormat(MySqlFileFormat fileFormat) {
        setFileFormat(fileFormat);
        return this;
    }

    /** The number of {@code IGNORE number {LINES | ROWS}}, or null without the clause. */
    public Long getIgnoreCount() {
        return ignoreCount;
    }

    public void setIgnoreCount(Long ignoreCount) {
        this.ignoreCount = ignoreCount;
    }

    public MySqlLoadDataStatement withIgnoreCount(Long ignoreCount) {
        setIgnoreCount(ignoreCount);
        return this;
    }

    public IgnoreUnit getIgnoreUnit() {
        return ignoreUnit;
    }

    public void setIgnoreUnit(IgnoreUnit ignoreUnit) {
        this.ignoreUnit = ignoreUnit;
    }

    public MySqlLoadDataStatement withIgnoreUnit(IgnoreUnit ignoreUnit) {
        setIgnoreUnit(ignoreUnit);
        return this;
    }

    /**
     * The columns and user variables the input fields go to: {@code Column} and
     * {@code UserVariable} elements. Null without a list, empty for {@code ()}.
     */
    public ExpressionList<Expression> getColumns() {
        return columns;
    }

    public void setColumns(ExpressionList<Expression> columns) {
        this.columns = columns;
    }

    public MySqlLoadDataStatement withColumns(ExpressionList<Expression> columns) {
        setColumns(columns);
        return this;
    }

    /** The assignments of the SET clause, each one column and one value, or null. */
    public List<UpdateSet> getUpdateSets() {
        return updateSets;
    }

    public void setUpdateSets(List<UpdateSet> updateSets) {
        this.updateSets = updateSets;
    }

    public MySqlLoadDataStatement withUpdateSets(List<UpdateSet> updateSets) {
        setUpdateSets(updateSets);
        return this;
    }

    public StringBuilder appendTo(StringBuilder builder) {
        return appendTo(builder, builder::append);
    }

    /** Renders the column list and the SET clause through the caller's expression visitor. */
    public StringBuilder appendTo(StringBuilder builder, Consumer<Expression> expressionRenderer) {
        appendSource(builder);
        appendTarget(builder);
        appendFileOptions(builder);
        if (columns != null) {
            builder.append(" (");
            appendList(builder, columns, expressionRenderer);
            builder.append(")");
        }
        if (updateSets != null && !updateSets.isEmpty()) {
            builder.append(" SET ");
            for (int i = 0; i < updateSets.size(); i++) {
                if (i > 0) {
                    builder.append(", ");
                }
                appendList(builder, updateSets.get(i).getColumns(), expressionRenderer);
                builder.append(" = ");
                appendList(builder, updateSets.get(i).getValues(), expressionRenderer);
            }
        }
        return builder;
    }

    private void appendSource(StringBuilder builder) {
        builder.append("LOAD ").append(format);
        if (lockOption != null) {
            builder.append(' ').append(lockOption);
        }
        if (local) {
            builder.append(" LOCAL");
        }
        builder.append(" INFILE ").append(fileName);
        if (duplicateHandling != null) {
            builder.append(' ').append(duplicateHandling);
        }
    }

    private void appendTarget(StringBuilder builder) {
        builder.append(" INTO TABLE ").append(table);
        if (partitions != null) {
            builder.append(" PARTITION (").append(String.join(", ", partitions)).append(")");
        }
        if (characterSet != null) {
            builder.append(useCharsetKeyword ? " CHARSET " : " CHARACTER SET ")
                    .append(characterSet);
        }
    }

    private void appendFileOptions(StringBuilder builder) {
        if (rowsIdentifiedBy != null) {
            builder.append(" ROWS IDENTIFIED BY ").append(rowsIdentifiedBy);
        }
        if (fileFormat != null && !fileFormat.isEmpty()) {
            fileFormat.appendTo(builder.append(' '));
        }
        if (ignoreCount != null) {
            builder.append(" IGNORE ").append(ignoreCount).append(' ').append(ignoreUnit);
        }
    }

    private static void appendList(StringBuilder builder, List<? extends Expression> expressions,
            Consumer<Expression> expressionRenderer) {
        for (int i = 0; i < expressions.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            expressionRenderer.accept(expressions.get(i));
        }
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }
}
