/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.validation.validator;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.RowPatternFunction;
import net.sf.jsqlparser.expression.UserVariable;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.schema.MultiPartName;
import net.sf.jsqlparser.statement.select.MatchRecognize;
import net.sf.jsqlparser.statement.select.RowPattern;
import net.sf.jsqlparser.statement.select.RowPatternVisitorAdapter;
import net.sf.jsqlparser.util.validation.ValidationCapability;
import net.sf.jsqlparser.util.validation.ValidationException;

/**
 * Static dialect checks; schema binding, function eligibility and runtime checks remain with the
 * DB.
 */
public class MatchRecognizeValidator extends AbstractValidator<MatchRecognize> {
    @Override
    public void validate(MatchRecognize match) {
        String dialect = context().getAsString(Feature.dialect);
        boolean bigQuery = Dialect.BIGQUERY.name().equals(dialect);
        boolean oracle = Dialect.ORACLE.name().equals(dialect);
        boolean snowflake = Dialect.SNOWFLAKE.name().equals(dialect);
        if (!bigQuery && !oracle && !snowflake) {
            return;
        }
        if (bigQuery) {
            if (match.getOrderByElements().isEmpty() || match.getMeasures().isEmpty()) {
                error("BigQuery MATCH_RECOGNIZE requires ORDER BY and MEASURES");
            }
            if (match.getRowsPerMatch() != null || match.getSkipVariable() != null) {
                error("BigQuery does not support ROWS PER MATCH or SKIP TO a variable");
            }
        } else if (match.getOptions() != null) {
            error("MATCH_RECOGNIZE OPTIONS is specific to BigQuery");
        }
        if (!oracle && !match.getSubsets().isEmpty()) {
            error("SUBSET is specific to Oracle");
        }
        if (match.getEmptyMatchMode() != null
                && match.getRowsPerMatch() != MatchRecognize.RowsPerMatch.ALL) {
            error("Empty match options require ALL ROWS PER MATCH");
        }
        match.getMeasures().forEach(measure -> {
            if (measure.getAlias() == null || oracle && !measure.getAlias().isUseAs()) {
                error("A measure requires an alias (with AS in Oracle)");
            }
        });

        Set<String> used = new HashSet<>();
        match.getPattern().accept(new RowPatternVisitorAdapter<Void>() {
            @Override
            public <S> Void visit(RowPattern.Variable variable, S context) {
                used.add(normalize(variable.getName(), bigQuery));
                return null;
            }
        }, null);
        Set<String> defined = new HashSet<>();
        for (MatchRecognize.Definition definition : match.getDefinitions()) {
            String name = normalize(definition.getName(), bigQuery);
            if (!defined.add(name)) {
                error("Duplicate row pattern variable: " + definition.getName());
            }
            if ((bigQuery || oracle) && !used.contains(name)) {
                error("Unused row pattern variable: " + definition.getName());
            }
        }
        if (bigQuery) {
            for (String name : used) {
                if (!defined.contains(name)) {
                    error("Undefined row pattern variable: " + name);
                }
            }
        }
        Set<String> subsetNames = new HashSet<>();
        for (MatchRecognize.Subset subset : match.getSubsets()) {
            String name = normalize(subset.getName(), false);
            if (used.contains(name) || !subsetNames.add(name)) {
                error("Duplicate SUBSET variable: " + subset.getName());
            }
            for (String variable : subset.getVariables()) {
                if (!used.contains(normalize(variable, false))) {
                    error("SUBSET requires a primary pattern variable: " + variable);
                }
            }
        }
        if (match.getSkipVariable() != null
                && !used.contains(normalize(match.getSkipVariable(), bigQuery))
                && !subsetNames.contains(normalize(match.getSkipVariable(), false))) {
            error("Unknown AFTER MATCH SKIP variable: " + match.getSkipVariable());
        }
        match.getPattern().accept(new RowPatternVisitorAdapter<Void>() {
            @Override
            public <S> Void visit(RowPattern.Quantified quantified, S context) {
                checkBound(quantified.getLowerBound(), bigQuery);
                checkBound(quantified.getUpperBound(), bigQuery);
                if (quantified.getLowerBound() instanceof LongValue
                        && quantified.getUpperBound() instanceof LongValue
                        && ((LongValue) quantified.getLowerBound()).getBigIntegerValue()
                                .compareTo(((LongValue) quantified.getUpperBound())
                                        .getBigIntegerValue()) > 0) {
                    error("Row pattern upper bound is smaller than its lower bound");
                }
                if (bigQuery && quantified.getType() == RowPattern.Quantified.Type.EXACT
                        && quantified.isReluctant()) {
                    error("BigQuery does not support reluctant fixed quantifiers");
                }
                if (oracle) {
                    Expression maximum = quantified.getType() == RowPattern.Quantified.Type.EXACT
                            ? quantified.getLowerBound()
                            : quantified.getUpperBound();
                    if (maximum instanceof LongValue
                            && ((LongValue) maximum).getBigIntegerValue().signum() == 0) {
                        error("Oracle row pattern quantifier maximum must be positive");
                    }
                }
                return super.visit(quantified, context);
            }

            @Override
            public <S> Void visit(RowPattern.Operation operation, S context) {
                if (oracle && operation.getType() == RowPattern.Operation.Type.ALTERNATION
                        && operation.getPatterns().stream()
                                .anyMatch(RowPattern.Empty.class::isInstance)) {
                    error("Oracle requires a pattern on each side of an alternative");
                }
                return super.visit(operation, context);
            }

            @Override
            public <S> Void visit(RowPattern.Exclusion exclusion, S context) {
                if (bigQuery) {
                    error("BigQuery does not support row pattern exclusion");
                }
                if (match.getEmptyMatchMode() == MatchRecognize.EmptyMatchMode.WITH_UNMATCHED) {
                    error("Row pattern exclusion cannot be combined with WITH UNMATCHED ROWS");
                }
                return super.visit(exclusion, context);
            }

            @Override
            public <S> Void visit(RowPattern.Permute permute, S context) {
                if (bigQuery) {
                    error("BigQuery does not support PERMUTE");
                }
                return super.visit(permute, context);
            }
        }, null);
        match.getMeasures()
                .forEach(measure -> checkFunctions(measure.getExpression(), bigQuery, false));
        match.getDefinitions()
                .forEach(definition -> checkFunctions(definition.getExpression(), bigQuery, true));
    }

    private void checkFunctions(Expression expression, boolean bigQuery, boolean definition) {
        expression.accept(new ExpressionVisitorAdapter<Void>() {
            @Override
            public <S> Void visit(RowPatternFunction function, S context) {
                if (bigQuery) {
                    error("BigQuery does not support RUNNING or FINAL function prefixes");
                }
                if (definition && function
                        .getEvaluationMode() == RowPatternFunction.EvaluationMode.FINAL) {
                    error("FINAL is only allowed in MATCH_RECOGNIZE MEASURES");
                }
                return function.getFunction().accept(this, context);
            }
        }, null);
    }

    private static String normalize(String name, boolean bigQuery) {
        String unquoted = MultiPartName.unquote(name);
        return bigQuery || name.equals(unquoted) ? unquoted.toUpperCase(Locale.ROOT) : unquoted;
    }

    private void checkBound(Expression bound, boolean bigQuery) {
        if (bound == null) {
            return;
        }
        if (bound instanceof LongValue) {
            BigInteger value = ((LongValue) bound).getBigIntegerValue();
            if (value.signum() < 0 || bigQuery && value.compareTo(BigInteger.valueOf(10000)) > 0) {
                error(bigQuery ? "BigQuery row pattern bounds must be between 0 and 10000"
                        : "Row pattern bounds must be nonnegative");
            }
        } else if (!bigQuery || !(bound instanceof JdbcParameter)
                && !(bound instanceof UserVariable && !((UserVariable) bound).isDoubleAdd())) {
            error(bigQuery ? "A row pattern bound must be an integer literal or query parameter"
                    : "A row pattern bound must be an integer literal");
        }
    }

    private void error(String message) {
        for (ValidationCapability capability : getCapabilities()) {
            putError(capability, new ValidationException(message));
        }
    }
}
