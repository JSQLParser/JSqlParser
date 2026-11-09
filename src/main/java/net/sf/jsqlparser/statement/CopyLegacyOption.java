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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.schema.Column;

/**
 * One option of the COPY option syntax PostgreSQL used before 9.0 and still accepts: a keyword
 * without parentheses or commas, such as {@code CSV}, {@code HEADER}, {@code DELIMITER AS ';'} or
 * {@code FORCE QUOTE *}. The options follow the file name and the optional {@code WITH} in any
 * order, as {@code copy_opt_item} in PostgreSQL's grammar.
 *
 * @see <a href="https://www.postgresql.org/docs/18/sql-copy.html">COPY, section Compatibility</a>
 */
public class CopyLegacyOption extends ASTNodeAccessImpl {
    private Kind kind;
    private boolean asKeyword;
    private StringValue value;
    private final List<Column> columns = new ArrayList<>();
    private boolean allColumns;

    /** The option keyword, and what it takes after it. */
    public enum Kind {
        /** BINARY, the binary format */
        BINARY("BINARY", Argument.NONE),
        /** FREEZE */
        FREEZE("FREEZE", Argument.NONE),
        /** DELIMITER [AS] 'string' */
        DELIMITER("DELIMITER", Argument.STRING_AFTER_OPTIONAL_AS),
        /** NULL [AS] 'string' */
        NULL("NULL", Argument.STRING_AFTER_OPTIONAL_AS),
        /** CSV, the CSV format */
        CSV("CSV", Argument.NONE),
        /** HEADER */
        HEADER("HEADER", Argument.NONE),
        /** QUOTE [AS] 'string' */
        QUOTE("QUOTE", Argument.STRING_AFTER_OPTIONAL_AS),
        /** ESCAPE [AS] 'string' */
        ESCAPE("ESCAPE", Argument.STRING_AFTER_OPTIONAL_AS),
        /** FORCE QUOTE {column, ... | *} */
        FORCE_QUOTE("FORCE QUOTE", Argument.COLUMNS),
        /** FORCE NOT NULL {column, ... | *} */
        FORCE_NOT_NULL("FORCE NOT NULL", Argument.COLUMNS),
        /** FORCE NULL {column, ... | *} */
        FORCE_NULL("FORCE NULL", Argument.COLUMNS),
        /** ENCODING 'string' */
        ENCODING("ENCODING", Argument.STRING);

        private final String keyword;
        private final Argument argument;

        Kind(String keyword, Argument argument) {
            this.keyword = keyword;
            this.argument = argument;
        }

        /** The keywords of the option, such as {@code FORCE NOT NULL}. */
        public String getKeyword() {
            return keyword;
        }

        /** Whether a string follows the keyword. */
        public boolean takesString() {
            return argument == Argument.STRING || argument == Argument.STRING_AFTER_OPTIONAL_AS;
        }

        /** Whether {@code AS} may precede the string. */
        public boolean allowsAsKeyword() {
            return argument == Argument.STRING_AFTER_OPTIONAL_AS;
        }

        /** Whether a column list or {@code *} follows the keyword. */
        public boolean takesColumns() {
            return argument == Argument.COLUMNS;
        }
    }

    private enum Argument {
        NONE, STRING, STRING_AFTER_OPTIONAL_AS, COLUMNS
    }

    public CopyLegacyOption(Kind kind) {
        setKind(kind);
    }

    public Kind getKind() {
        return kind;
    }

    public void setKind(Kind kind) {
        this.kind = Objects.requireNonNull(kind, "kind");
    }

    /** Whether {@code AS} precedes the string, as in {@code DELIMITER AS ','}. */
    public boolean isAsKeyword() {
        return asKeyword;
    }

    public void setAsKeyword(boolean asKeyword) {
        this.asKeyword = asKeyword;
    }

    public CopyLegacyOption withAsKeyword(boolean asKeyword) {
        setAsKeyword(asKeyword);
        return this;
    }

    /** The string of DELIMITER, NULL, QUOTE, ESCAPE or ENCODING; null for the other options. */
    public StringValue getValue() {
        return value;
    }

    public void setValue(StringValue value) {
        this.value = value;
    }

    public CopyLegacyOption withValue(StringValue value) {
        setValue(value);
        return this;
    }

    /** The columns of a FORCE option; empty for {@code *} and for the other options. */
    public List<Column> getColumns() {
        return columns;
    }

    public void setColumns(Collection<? extends Column> columns) {
        List<Column> replacement =
                columns == null ? Collections.emptyList() : new ArrayList<>(columns);
        this.columns.clear();
        this.columns.addAll(replacement);
    }

    public CopyLegacyOption addColumns(Column... columns) {
        Collections.addAll(this.columns, columns);
        return this;
    }

    public CopyLegacyOption withColumns(Collection<? extends Column> columns) {
        setColumns(columns);
        return this;
    }

    /** Whether a FORCE option applies to every column, written {@code *}. */
    public boolean isAllColumns() {
        return allColumns;
    }

    public void setAllColumns(boolean allColumns) {
        this.allColumns = allColumns;
    }

    public CopyLegacyOption withAllColumns(boolean allColumns) {
        setAllColumns(allColumns);
        return this;
    }

    @Override
    public StringBuilder appendTo(StringBuilder builder) {
        builder.append(kind.getKeyword());
        if (value != null) {
            builder.append(asKeyword ? " AS " : " ").append(value);
        }
        if (allColumns) {
            builder.append(" *");
        } else {
            for (int i = 0; i < columns.size(); i++) {
                builder.append(i == 0 ? " " : ", ").append(columns.get(i).getFullyQualifiedName());
            }
        }
        return builder;
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }
}
