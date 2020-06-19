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

import java.util.List;
import java.util.stream.Collectors;

/**
 * An {@code EXPLAIN} statement
 */
public class ExplainStatement implements Statement {

    private Select select;
    private List<Option> options;

    public ExplainStatement(Select select) {
        this.select = select;
    }

    public Select getStatement() {
        return select;
    }

    public void setStatement(Select select) {
        this.select = select;
    }

    public List<Option> getOptions() {
        return options;
    }

    public void setOptions(List<Option> options) {
        this.options = options;
    }

    @Override
    public String toString() {
        StringBuilder statementBuilder = new StringBuilder("EXPLAIN");
        if (options != null) {
            statementBuilder.append(" ");
            statementBuilder.append(options.stream().map(Option::formatOption).collect(Collectors.joining(" ")));
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
        ANALYZE,
        VERBOSE,
        COSTS,
        BUFFERS,
        FORMAT
    }

    public static class Option {

        private final OptionType option;
        private String value;

        public Option(OptionType option) {
            this.option = option;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String formatOption() {
            return option.name() + (value != null ? (" " + value) : "");
        }
    }
}
