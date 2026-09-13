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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Locale;
import net.sf.jsqlparser.schema.MultiPartName;
import java.util.function.Consumer;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

/** A relation transformed by MATCH_RECOGNIZE. Input and result aliases are independent. */
public class MatchRecognize extends AbstractFromitem {
    public enum SkipMode {
        PAST_LAST_ROW, TO_NEXT_ROW, TO_FIRST, TO_LAST, TO_VARIABLE
    }

    public enum RowsPerMatch {
        ONE, ALL
    }

    public enum EmptyMatchMode {
        SHOW, OMIT, WITH_UNMATCHED
    }

    private RowsPerMatch rowsPerMatch;
    private EmptyMatchMode emptyMatchMode;
    private String skipVariable;
    private List<Subset> subsets = new ArrayList<>();
    private FromItem input;
    private ExpressionList<Expression> partitionBy = new ExpressionList<>();
    private List<OrderByElement> orderByElements = new ArrayList<>();
    private List<SelectItem<?>> measures = new ArrayList<>();
    private SkipMode skipMode;
    private RowPattern pattern;
    private List<Definition> definitions = new ArrayList<>();
    private Options options;

    public MatchRecognize(FromItem input) {
        this.input = input;
    }

    public FromItem getInput() {
        return input;
    }

    public MatchRecognize setInput(FromItem input) {
        this.input = input;
        return this;
    }

    public ExpressionList<Expression> getPartitionBy() {
        return partitionBy;
    }

    public MatchRecognize setPartitionBy(ExpressionList<Expression> partitionBy) {
        this.partitionBy = partitionBy;
        return this;
    }

    public List<OrderByElement> getOrderByElements() {
        return orderByElements;
    }

    public MatchRecognize setOrderByElements(List<OrderByElement> elements) {
        orderByElements = elements;
        return this;
    }

    public List<SelectItem<?>> getMeasures() {
        return measures;
    }

    public MatchRecognize setMeasures(List<SelectItem<?>> measures) {
        this.measures = measures;
        return this;
    }

    public SkipMode getSkipMode() {
        return skipMode;
    }

    public MatchRecognize setSkipMode(SkipMode mode) {
        skipMode = mode;
        return this;
    }

    public RowsPerMatch getRowsPerMatch() {
        return rowsPerMatch;
    }

    public MatchRecognize setRowsPerMatch(RowsPerMatch value) {
        rowsPerMatch = value;
        return this;
    }

    public EmptyMatchMode getEmptyMatchMode() {
        return emptyMatchMode;
    }

    public MatchRecognize setEmptyMatchMode(EmptyMatchMode value) {
        emptyMatchMode = value;
        return this;
    }

    public String getSkipVariable() {
        return skipVariable;
    }

    public MatchRecognize setSkipVariable(String value) {
        skipVariable = value;
        return this;
    }

    public List<Subset> getSubsets() {
        return subsets;
    }

    public MatchRecognize setSubsets(List<Subset> value) {
        subsets = value;
        return this;
    }

    public RowPattern getPattern() {
        return pattern;
    }

    public MatchRecognize setPattern(RowPattern pattern) {
        this.pattern = pattern;
        return this;
    }

    public List<Definition> getDefinitions() {
        return definitions;
    }

    public MatchRecognize setDefinitions(List<Definition> definitions) {
        this.definitions = definitions;
        return this;
    }

    public Options getOptions() {
        return options;
    }

    public MatchRecognize setOptions(Options options) {
        this.options = options;
        return this;
    }

    @Override
    public <T, S> T accept(FromItemVisitor<T> visitor, S context) {
        return visitor.visit(this, context);
    }

    /** Case-insensitive pattern names for syntactic table discovery, including SUBSET names. */
    public Set<String> getPatternVariableNames() {
        Set<String> names = new HashSet<>();
        if (pattern != null) {
            pattern.accept(new RowPatternVisitorAdapter<Void>() {
                @Override
                public <S> Void visit(RowPattern.Variable variable, S context) {
                    names.add(normalizeVariableName(variable.getName()));
                    return null;
                }
            }, null);
        }
        subsets.forEach(subset -> names.add(normalizeVariableName(subset.getName())));
        return names;
    }

    public static String normalizeVariableName(String name) {
        return name == null ? null : MultiPartName.unquote(name).toLowerCase(Locale.ROOT);
    }

    /** Visits expression roots once; normal expression visitors traverse their descendants. */
    public void forEachExpression(Consumer<Expression> expressions) {
        partitionBy.forEach(expressions);
        orderByElements.forEach(order -> expressions.accept(order.getExpression()));
        forEachPatternExpression(expressions);
    }

    /** Expression roots where pattern variables, rather than input aliases, are in scope. */
    public void forEachPatternExpression(Consumer<Expression> expressions) {
        measures.forEach(measure -> expressions.accept(measure.getExpression()));
        pattern.forEachExpression(expressions);
        definitions.forEach(definition -> expressions.accept(definition.getExpression()));
    }

    /** Shared clause rendering, retaining the caller's source and expression customization. */
    public StringBuilder appendTo(StringBuilder builder, Consumer<FromItem> sources,
            Consumer<Expression> expressions, Consumer<OrderByElement> ordering,
            Consumer<Pivot> pivots, Consumer<UnPivot> unpivots) {
        sources.accept(input);
        builder.append(" MATCH_RECOGNIZE (");
        if (!partitionBy.isEmpty()) {
            builder.append("PARTITION BY ");
            appendList(builder, partitionBy, expressions);
            builder.append(' ');
        }
        if (!orderByElements.isEmpty()) {
            builder.append("ORDER BY ");
            appendList(builder, orderByElements, ordering);
            builder.append(' ');
        }
        if (!measures.isEmpty()) {
            builder.append("MEASURES ");
            appendList(builder, measures, item -> {
                expressions.accept(item.getExpression());
                if (item.getAlias() != null) {
                    builder.append(item.getAlias());
                }
            });
            builder.append(' ');
        }
        if (rowsPerMatch != null) {
            builder.append(rowsPerMatch == RowsPerMatch.ONE ? "ONE ROW PER MATCH "
                    : "ALL ROWS PER MATCH ");
        }
        if (emptyMatchMode != null) {
            switch (emptyMatchMode) {
                case SHOW:
                    builder.append("SHOW EMPTY MATCHES ");
                    break;
                case OMIT:
                    builder.append("OMIT EMPTY MATCHES ");
                    break;
                default:
                    builder.append("WITH UNMATCHED ROWS ");
            }
        }
        if (skipMode != null) {
            builder.append("AFTER MATCH SKIP ");
            switch (skipMode) {
                case PAST_LAST_ROW:
                    builder.append("PAST LAST ROW");
                    break;
                case TO_NEXT_ROW:
                    builder.append("TO NEXT ROW");
                    break;
                case TO_FIRST:
                    builder.append("TO FIRST ").append(skipVariable);
                    break;
                case TO_LAST:
                    builder.append("TO LAST ").append(skipVariable);
                    break;
                default:
                    builder.append("TO ").append(skipVariable);
            }
            builder.append(' ');
        }
        builder.append("PATTERN (");
        pattern.appendTo(builder, expressions);
        builder.append(')');
        if (!subsets.isEmpty()) {
            builder.append(" SUBSET ");
            appendList(builder, subsets, subset -> {
                builder.append(subset.getName()).append(" = (");
                appendList(builder, subset.getVariables(), name -> builder.append(name));
                builder.append(')');
            });
        }
        builder.append(" DEFINE ");
        appendList(builder, definitions, definition -> {
            builder.append(definition.getName()).append(" AS ");
            expressions.accept(definition.getExpression());
        });
        if (options != null) {
            builder.append(" OPTIONS (");
            if (options.getUseLongestMatch() != null) {
                builder.append("use_longest_match = ")
                        .append(options.getUseLongestMatch() ? "TRUE" : "FALSE");
            }
            builder.append(')');
        }
        builder.append(')');
        if (getPivot() != null) {
            pivots.accept(getPivot());
        }
        if (getUnPivot() != null) {
            unpivots.accept(getUnPivot());
        }
        if (getAlias() != null) {
            builder.append(getAlias());
        }
        if (getSampleClause() != null) {
            builder.append(getSampleClause());
        }
        return builder;
    }

    private static <E> void appendList(StringBuilder builder, List<E> elements,
            Consumer<E> append) {
        for (int i = 0; i < elements.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            append.accept(elements.get(i));
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        return appendTo(builder, source -> builder.append(source),
                expression -> builder.append(expression), order -> builder.append(order),
                pivot -> builder.append(" ").append(pivot),
                unpivot -> builder.append(" ").append(unpivot))
                .toString();
    }

    public static class Subset extends ASTNodeAccessImpl {
        private String name;
        private List<String> variables;

        public Subset(String name, List<String> variables) {
            this.name = name;
            this.variables = variables;
        }

        public String getName() {
            return name;
        }

        public Subset setName(String name) {
            this.name = name;
            return this;
        }

        public List<String> getVariables() {
            return variables;
        }

        public Subset setVariables(List<String> variables) {
            this.variables = variables;
            return this;
        }
    }

    public static class Definition extends ASTNodeAccessImpl {
        private String name;
        private Expression expression;

        public Definition(String name, Expression expression) {
            this.name = name;
            this.expression = expression;
        }

        public String getName() {
            return name;
        }

        public Definition setName(String name) {
            this.name = name;
            return this;
        }

        public Expression getExpression() {
            return expression;
        }

        public Definition setExpression(Expression expression) {
            this.expression = expression;
            return this;
        }
    }

    /** The presence of this object distinguishes an empty OPTIONS clause from an omitted one. */
    public static class Options extends ASTNodeAccessImpl {
        private Boolean useLongestMatch;

        public Boolean getUseLongestMatch() {
            return useLongestMatch;
        }

        public Options setUseLongestMatch(Boolean value) {
            useLongestMatch = value;
            return this;
        }
    }
}
