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
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.schema.Table;

/**
 * An {@code EXPLAIN} statement
 */
public class ExplainStatement extends ASTNodeAccessImpl implements Statement {
    private String keyword;
    private Statement statement;
    private List<Option> options = new ArrayList<>();
    private boolean parenthesizedOptions;
    private Table table;

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
        }
        return this;
    }

    public Statement getStatement() {
        return statement;
    }

    public ExplainStatement setStatement(Statement statement) {
        this.table = null;
        this.statement = statement;
        return this;
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
        StringBuilder builder = new StringBuilder(keyword);
        if (table != null) {
            builder.append(" ").append(table);
        } else {
            appendOptionsTo(builder);
            builder.append(" ");
            if (statement != null) {
                builder.append(statement);
            }
        }

        return builder.toString();
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

    public static class Option implements Serializable {

        private final OptionType type;
        private String value;

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

        public String formatOption() {
            return (type == OptionType.PLAN_FOR ? "PLAN FOR" : type.name()) + (value != null
                    ? " " + value
                    : "");
        }

        public Option withValue(String value) {
            this.setValue(value);
            return this;
        }
    }
}
