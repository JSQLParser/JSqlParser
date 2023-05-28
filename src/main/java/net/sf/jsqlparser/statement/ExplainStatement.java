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

import net.sf.jsqlparser.statement.select.Select;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

/**
 * An {@code EXPLAIN} statement
 */
public class ExplainStatement implements Statement {

    private Select select;

    private LinkedHashMap<OptionType, Option> options;

    public ExplainStatement() {
        // empty constructor
    }

    public ExplainStatement(Select select) {
        this.select = select;
    }

    public Select getStatement() {
        return select;
    }

    public void setStatement(Select select) {
        this.select = select;
    }

    public LinkedHashMap<OptionType, Option> getOptions() {
        return options == null ? null : new LinkedHashMap<>(options);
    }

    public void addOption(Option option) {
        if (options == null) {
            options = new LinkedHashMap<>();
        }
        options.put(option.getType(), option);
    }

    /**
     * Returns the first option that matches this optionType
     * @param optionType the option type to retrieve an Option for
     * @return an option of that type, or null. In case of duplicate options, the first found option will be returned.
     */
    public Option getOption(OptionType optionType) {
        if (options == null) {
            return null;
        }
        return options.get(optionType);
    }

    @Override
    public String toString() {
        StringBuilder statementBuilder = new StringBuilder("EXPLAIN");
        if (options != null) {
            statementBuilder.append(" ");
            statementBuilder.append(options.values().stream().map(Option::formatOption).collect(Collectors.joining(" ")));
        }
        statementBuilder.append(" ");
        statementBuilder.append(select.toString());
        return statementBuilder.toString();
    }

    @Override
    public void accept(StatementVisitor statementVisitor) {
        statementVisitor.visit(this);
    }

    public enum OptionType {

        ANALYZE, VERBOSE, COSTS, BUFFERS, FORMAT
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
            return type.name() + (value != null ? " " + value : "");
        }

        public Option withValue(String value) {
            this.setValue(value);
            return this;
        }
    }
}
