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
import java.util.function.Consumer;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

/** A row pattern, with explicit grouping and ordered alternatives preserved. */
public abstract class RowPattern extends ASTNodeAccessImpl {
    public abstract <T, S> T accept(RowPatternVisitor<T> visitor, S context);

    public abstract StringBuilder appendTo(StringBuilder builder, Consumer<Expression> expressions);

    /** Visits only SQL expressions embedded in the pattern, such as quantifier bounds. */
    public void forEachExpression(Consumer<Expression> expressions) {}

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        return appendTo(builder, expression -> builder.append(expression)).toString();
    }

    public static class Variable extends RowPattern {
        private String name;

        public Variable(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public Variable setName(String name) {
            this.name = name;
            return this;
        }

        @Override
        public <T, S> T accept(RowPatternVisitor<T> visitor, S context) {
            return visitor.visit(this, context);
        }

        @Override
        public StringBuilder appendTo(StringBuilder builder, Consumer<Expression> expressions) {
            return builder.append(name);
        }
    }

    public static class Empty extends RowPattern {
        @Override
        public <T, S> T accept(RowPatternVisitor<T> visitor, S context) {
            return visitor.visit(this, context);
        }

        @Override
        public StringBuilder appendTo(StringBuilder builder, Consumer<Expression> expressions) {
            return builder;
        }
    }

    public static class Anchor extends RowPattern {
        public enum Type {
            START, END
        }

        private Type type;

        public Anchor(Type type) {
            this.type = type;
        }

        public Type getType() {
            return type;
        }

        public Anchor setType(Type type) {
            this.type = type;
            return this;
        }

        @Override
        public <T, S> T accept(RowPatternVisitor<T> visitor, S context) {
            return visitor.visit(this, context);
        }

        @Override
        public StringBuilder appendTo(StringBuilder builder, Consumer<Expression> expressions) {
            return builder.append(type == Type.START ? '^' : '$');
        }
    }

    public static class Group extends RowPattern {
        private RowPattern pattern;

        public Group(RowPattern pattern) {
            this.pattern = pattern;
        }

        public RowPattern getPattern() {
            return pattern;
        }

        public Group setPattern(RowPattern pattern) {
            this.pattern = pattern;
            return this;
        }

        @Override
        public <T, S> T accept(RowPatternVisitor<T> visitor, S context) {
            return visitor.visit(this, context);
        }

        @Override
        public void forEachExpression(Consumer<Expression> expressions) {
            pattern.forEachExpression(expressions);
        }

        @Override
        public StringBuilder appendTo(StringBuilder builder, Consumer<Expression> expressions) {
            builder.append('(');
            pattern.appendTo(builder, expressions);
            return builder.append(')');
        }
    }

    public static class Operation extends RowPattern {
        public enum Type {
            SEQUENCE, ALTERNATION
        }

        private Type type;
        private List<RowPattern> patterns;

        public Operation(Type type, List<RowPattern> patterns) {
            this.type = type;
            this.patterns = new ArrayList<>(patterns);
        }

        public Type getType() {
            return type;
        }

        public Operation setType(Type type) {
            this.type = type;
            return this;
        }

        public List<RowPattern> getPatterns() {
            return patterns;
        }

        public Operation setPatterns(List<RowPattern> patterns) {
            this.patterns = patterns;
            return this;
        }

        @Override
        public <T, S> T accept(RowPatternVisitor<T> visitor, S context) {
            return visitor.visit(this, context);
        }

        @Override
        public void forEachExpression(Consumer<Expression> expressions) {
            patterns.forEach(pattern -> pattern.forEachExpression(expressions));
        }

        @Override
        public StringBuilder appendTo(StringBuilder builder, Consumer<Expression> expressions) {
            for (int i = 0; i < patterns.size(); i++) {
                if (i > 0) {
                    builder.append(type == Type.SEQUENCE ? " " : " | ");
                }
                RowPattern child = patterns.get(i);
                // Explicit parentheses keep the tree unambiguous across dialects, including
                // Snowflake's documented alternative-before-concatenation precedence.
                boolean brackets = type == Type.SEQUENCE && child instanceof Empty
                        || child instanceof Operation && ((Operation) child).type != type;
                if (brackets) {
                    builder.append('(');
                }
                child.appendTo(builder, expressions);
                if (brackets) {
                    builder.append(')');
                }
            }
            return builder;
        }
    }

    /** A permutation stays compact; it is never expanded into factorially many alternatives. */
    public static class Permute extends RowPattern {
        private List<RowPattern> patterns;

        public Permute(List<RowPattern> patterns) {
            this.patterns = new ArrayList<>(patterns);
        }

        public List<RowPattern> getPatterns() {
            return patterns;
        }

        public Permute setPatterns(List<RowPattern> patterns) {
            this.patterns = patterns;
            return this;
        }

        @Override
        public <T, S> T accept(RowPatternVisitor<T> visitor, S context) {
            return visitor.visit(this, context);
        }

        @Override
        public void forEachExpression(Consumer<Expression> expressions) {
            patterns.forEach(pattern -> pattern.forEachExpression(expressions));
        }

        @Override
        public StringBuilder appendTo(StringBuilder builder, Consumer<Expression> expressions) {
            builder.append("PERMUTE(");
            for (int i = 0; i < patterns.size(); i++) {
                if (i > 0) {
                    builder.append(", ");
                }
                patterns.get(i).appendTo(builder, expressions);
            }
            return builder.append(')');
        }
    }

    public static class Exclusion extends RowPattern {
        private RowPattern pattern;

        public Exclusion(RowPattern pattern) {
            this.pattern = pattern;
        }

        public RowPattern getPattern() {
            return pattern;
        }

        public Exclusion setPattern(RowPattern pattern) {
            this.pattern = pattern;
            return this;
        }

        @Override
        public <T, S> T accept(RowPatternVisitor<T> visitor, S context) {
            return visitor.visit(this, context);
        }

        @Override
        public void forEachExpression(Consumer<Expression> expressions) {
            pattern.forEachExpression(expressions);
        }

        @Override
        public StringBuilder appendTo(StringBuilder builder, Consumer<Expression> expressions) {
            builder.append("{- ");
            pattern.appendTo(builder, expressions);
            return builder.append(" -}");
        }
    }

    public static class Quantified extends RowPattern {
        public enum Type {
            ZERO_OR_MORE, ONE_OR_MORE, OPTIONAL, EXACT, RANGE
        }

        private RowPattern pattern;
        private Type type;
        private Expression lowerBound;
        private Expression upperBound;
        private boolean reluctant;

        public Quantified(RowPattern pattern, Type type) {
            this.pattern = pattern;
            this.type = type;
        }

        public RowPattern getPattern() {
            return pattern;
        }

        public Quantified setPattern(RowPattern pattern) {
            this.pattern = pattern;
            return this;
        }

        public Type getType() {
            return type;
        }

        public Quantified setType(Type type) {
            this.type = type;
            return this;
        }

        public Expression getLowerBound() {
            return lowerBound;
        }

        public Quantified setLowerBound(Expression bound) {
            lowerBound = bound;
            return this;
        }

        public Expression getUpperBound() {
            return upperBound;
        }

        public Quantified setUpperBound(Expression bound) {
            upperBound = bound;
            return this;
        }

        public boolean isReluctant() {
            return reluctant;
        }

        public Quantified setReluctant(boolean reluctant) {
            this.reluctant = reluctant;
            return this;
        }

        @Override
        public <T, S> T accept(RowPatternVisitor<T> visitor, S context) {
            return visitor.visit(this, context);
        }

        @Override
        public void forEachExpression(Consumer<Expression> expressions) {
            pattern.forEachExpression(expressions);
            if (lowerBound != null) {
                expressions.accept(lowerBound);
            }
            if (upperBound != null) {
                expressions.accept(upperBound);
            }
        }

        @Override
        public StringBuilder appendTo(StringBuilder builder, Consumer<Expression> expressions) {
            boolean brackets = !(pattern instanceof Variable || pattern instanceof Group
                    || pattern instanceof Permute || pattern instanceof Exclusion);
            if (brackets) {
                builder.append('(');
            }
            pattern.appendTo(builder, expressions);
            if (brackets) {
                builder.append(')');
            }
            switch (type) {
                case ZERO_OR_MORE:
                    builder.append('*');
                    break;
                case ONE_OR_MORE:
                    builder.append('+');
                    break;
                case OPTIONAL:
                    builder.append('?');
                    break;
                default:
                    builder.append('{');
                    if (lowerBound != null) {
                        expressions.accept(lowerBound);
                    }
                    if (type == Type.RANGE) {
                        builder.append(',');
                        if (upperBound != null) {
                            expressions.accept(upperBound);
                        }
                    }
                    builder.append('}');
            }
            if (reluctant) {
                builder.append('?');
            }
            return builder;
        }
    }
}
