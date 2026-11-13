/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.UserVariable;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.schema.Table;

/**
 * An {@code EXPLAIN} statement. It explains a statement, describes a table, or, in MySQL, explains
 * the statement another connection runs.
 *
 * @see <a href="https://dev.mysql.com/doc/refman/8.4/en/explain.html">MySQL EXPLAIN</a>
 */
public class ExplainStatement extends ASTNodeAccessImpl implements Statement {
    private String keyword;
    private Statement statement;
    private List<Option> options = new ArrayList<>();
    private boolean parenthesizedOptions;
    private Table table;
    private Expression columnPattern;
    private UserVariable intoVariable;
    private String schemaName;
    private boolean schemaKeyword;
    private Expression connectionId;

    public ExplainStatement(String keyword) {
        this.keyword = keyword;
    }

    public ExplainStatement() {
        this("EXPLAIN");
    }

    public ExplainStatement(String keyword, Table table) {
        this.keyword = keyword;
        this.table = table;
    }

    public ExplainStatement(String keyword, Statement statement, List<Option> optionList) {
        this.keyword = keyword;
        setStatement(statement);

        setOptionList(optionList);
    }

    public ExplainStatement(Statement statement) {
        this("EXPLAIN", statement, null);
    }

    public Table getTable() {
        return table;
    }

    public ExplainStatement setTable(Table table) {
        this.table = table;
        if (table != null) {
            this.statement = null;
            this.connectionId = null;
        }
        return this;
    }

    public Statement getStatement() {
        return statement;
    }

    public ExplainStatement setStatement(Statement statement) {
        this.table = null;
        this.statement = statement;
        if (statement != null) {
            this.connectionId = null;
        }
        return this;
    }

    /**
     * The MySQL column name ({@link net.sf.jsqlparser.schema.Column}) or wildcard string that
     * limits the columns of {@code EXPLAIN table} to those it matches, or null.
     */
    public Expression getColumnPattern() {
        return columnPattern;
    }

    public void setColumnPattern(Expression columnPattern) {
        this.columnPattern = columnPattern;
    }

    public ExplainStatement withColumnPattern(Expression columnPattern) {
        setColumnPattern(columnPattern);
        return this;
    }

    /** The MySQL {@code INTO @variable} that receives the plan, or null. */
    public UserVariable getIntoVariable() {
        return intoVariable;
    }

    public void setIntoVariable(UserVariable intoVariable) {
        this.intoVariable = intoVariable;
    }

    public ExplainStatement withIntoVariable(UserVariable intoVariable) {
        setIntoVariable(intoVariable);
        return this;
    }

    /** The schema of MySQL's {@code FOR {SCHEMA | DATABASE} name}, or null. */
    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public ExplainStatement withSchemaName(String schemaName) {
        setSchemaName(schemaName);
        return this;
    }

    /** Whether the schema clause is spelled {@code FOR SCHEMA} rather than {@code FOR DATABASE}. */
    public boolean isSchemaKeyword() {
        return schemaKeyword;
    }

    public void setSchemaKeyword(boolean schemaKeyword) {
        this.schemaKeyword = schemaKeyword;
    }

    public ExplainStatement withSchemaKeyword(boolean schemaKeyword) {
        setSchemaKeyword(schemaKeyword);
        return this;
    }

    /**
     * The connection of MySQL's {@code FOR CONNECTION id}, a
     * {@link net.sf.jsqlparser.expression.LongValue} or
     * {@link net.sf.jsqlparser.expression.HexValue}, or null.
     */
    public Expression getConnectionId() {
        return connectionId;
    }

    public ExplainStatement setConnectionId(Expression connectionId) {
        this.connectionId = connectionId;
        if (connectionId != null) {
            this.table = null;
            this.statement = null;
        }
        return this;
    }

    /** What the statement explains, derived from the table, connection and statement it holds. */
    public TargetKind getTargetKind() {
        if (table != null) {
            return TargetKind.TABLE;
        }
        return connectionId != null ? TargetKind.CONNECTION : TargetKind.STATEMENT;
    }

    /**
     * The output format of the {@code FORMAT} option, or null without one or for a name no dialect
     * defines.
     */
    public Format getFormat() {
        Option option = getOption(OptionType.FORMAT);
        return option == null ? null : Format.find(option.getValue());
    }

    public LinkedHashMap<OptionType, Option> getOptions() {
        if (options.isEmpty()) {
            return null;
        }
        LinkedHashMap<OptionType, Option> result = new LinkedHashMap<>();
        for (Option option : options) {
            result.put(option.getType(), option);
        }
        return result;
    }

    /** Ordered options, including repetitions; the returned list is a defensive copy. */
    public List<Option> getOptionList() {
        return new ArrayList<>(options);
    }

    public void setOptionList(List<Option> optionList) {
        options = optionList == null ? new ArrayList<>() : new ArrayList<>(optionList);
    }

    public boolean isParenthesizedOptions() {
        return parenthesizedOptions;
    }

    public ExplainStatement setParenthesizedOptions(boolean parenthesizedOptions) {
        this.parenthesizedOptions = parenthesizedOptions;
        return this;
    }

    /** Adds an option, or replaces the last existing option of the same type. */
    public void addOption(Option option) {
        for (int i = options.size() - 1; i >= 0; i--) {
            if (options.get(i).getType() == option.getType()) {
                options.set(i, option);
                return;
            }
        }
        options.add(option);
    }

    /**
     * Returns the last option that matches this optionType.
     *
     * @param optionType the option type to retrieve an Option for
     * @return an option of that type, or null. In case of duplicate options, the last found option
     *         will be returned.
     */
    public Option getOption(OptionType optionType) {
        for (int i = options.size() - 1; i >= 0; i--) {
            if (options.get(i).getType() == optionType) {
                return options.get(i);
            }
        }
        return null;
    }

    public String getKeyword() {
        return keyword;
    }

    public ExplainStatement setKeyword(String keyword) {
        this.keyword = keyword;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        return appendTo(builder, builder::append, builder::append).toString();
    }

    /** Shared by SQL rendering and statement deparsers. */
    public StringBuilder appendTo(StringBuilder builder, Consumer<Statement> statementPrinter,
            Consumer<Expression> expressionPrinter) {
        builder.append(keyword);
        if (table != null) {
            builder.append(" ").append(table);
            if (columnPattern != null) {
                builder.append(" ");
                expressionPrinter.accept(columnPattern);
            }
            return builder;
        }
        appendOptionsTo(builder);
        if (intoVariable != null) {
            builder.append(" INTO ");
            expressionPrinter.accept(intoVariable);
        }
        builder.append(" ");
        if (connectionId != null) {
            builder.append("FOR CONNECTION ");
            expressionPrinter.accept(connectionId);
            return builder;
        }
        if (schemaName != null) {
            builder.append("FOR ").append(schemaKeyword ? "SCHEMA " : "DATABASE ")
                    .append(schemaName).append(" ");
        }
        if (statement != null) {
            statementPrinter.accept(statement);
        }
        return builder;
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }

    /** Shared by SQL rendering and statement deparsers; includes the leading separator. */
    public StringBuilder appendOptionsTo(StringBuilder builder) {
        if (!options.isEmpty()) {
            builder.append(parenthesizedOptions ? " (" : " ");
            for (int i = 0; i < options.size(); i++) {
                if (i > 0) {
                    builder.append(parenthesizedOptions ? ", " : " ");
                }
                builder.append(options.get(i).formatOption());
            }
            if (parenthesizedOptions) {
                builder.append(")");
            }
        }
        return builder;
    }

    public enum OptionType {
        ANALYZE, VERBOSE, COSTS, BUFFERS, FORMAT, PLAN, PLAN_FOR, TIMING, SUMMARY, SETTINGS, WAL, GENERIC_PLAN, SERIALIZE, MEMORY;

        public static OptionType from(String type) {
            return Enum.valueOf(OptionType.class, type.toUpperCase(Locale.ROOT));
        }
    }

    /** The thing an {@code EXPLAIN} statement explains. */
    public enum TargetKind {
        /** A statement, such as {@code EXPLAIN SELECT ...}. */
        STATEMENT,
        /** The columns of a table, such as {@code EXPLAIN t}. */
        TABLE,
        /** The statement a MySQL connection runs: {@code EXPLAIN FOR CONNECTION id}. */
        CONNECTION
    }

    /**
     * Output formats: {@code TEXT}, {@code XML}, {@code JSON} and {@code YAML} of PostgreSQL, and
     * {@code TRADITIONAL}, {@code JSON} and {@code TREE} of MySQL.
     */
    public enum Format {
        TEXT, XML, JSON, YAML, TRADITIONAL, TREE;

        public static Format from(String format) {
            return Enum.valueOf(Format.class, format.toUpperCase(Locale.ROOT));
        }

        /** The format of this name in any case, or null for another name. */
        public static Format find(String format) {
            for (Format value : values()) {
                if (value.name().equalsIgnoreCase(format)) {
                    return value;
                }
            }
            return null;
        }
    }

    public static class Option implements Serializable {

        private final OptionType type;
        private String value;
        private boolean useEqual;

        public Option(OptionType type) {
            this.type = type;
        }

        public OptionType getType() {
            return type;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        /** Whether the value follows an equals sign, as in MySQL's {@code FORMAT = JSON}. */
        public boolean isUseEqual() {
            return useEqual;
        }

        public void setUseEqual(boolean useEqual) {
            this.useEqual = useEqual;
        }

        public String formatOption() {
            return (type == OptionType.PLAN_FOR ? "PLAN FOR" : type.name()) + (value != null
                    ? (useEqual ? " = " : " ") + value
                    : "");
        }

        public Option withValue(String value) {
            this.setValue(value);
            return this;
        }

        public Option withUseEqual(boolean useEqual) {
            this.setUseEqual(useEqual);
            return this;
        }
    }
}
