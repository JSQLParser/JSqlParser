/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseAnd;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseLeftShift;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseOr;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseRightShift;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseXor;
import net.sf.jsqlparser.expression.operators.arithmetic.Concat;
import net.sf.jsqlparser.expression.operators.arithmetic.Division;
import net.sf.jsqlparser.expression.operators.arithmetic.IntegerDivision;
import net.sf.jsqlparser.expression.operators.arithmetic.Modulo;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.expression.operators.arithmetic.Subtraction;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Iterator;

/**
 * A basic class for binary expressions, that is expressions having a left member and a right member
 * which are in turn expressions.
 */
public abstract class BinaryExpression extends ASTNodeAccessImpl implements Expression {

    private Expression leftExpression;
    private Expression rightExpression;

    public BinaryExpression() {}

    public BinaryExpression(Expression leftExpression, Expression rightExpression) {
        this.leftExpression = leftExpression;
        this.rightExpression = rightExpression;
    }

    public static Expression build(Class<? extends BinaryExpression> clz, Expression... expressions)
            throws NoSuchMethodException, InvocationTargetException, InstantiationException,
            IllegalAccessException {
        switch (expressions.length) {
            case 0:
                return new NullValue();
            case 1:
                return expressions[0];
            default:
                Iterator<Expression> it = Arrays.stream(expressions).iterator();

                Expression leftExpression = it.next();
                Expression rightExpression = it.next();
                BinaryExpression binaryExpression =
                        clz.getConstructor(Expression.class, Expression.class)
                                .newInstance(leftExpression, rightExpression);

                while (it.hasNext()) {
                    rightExpression = it.next();
                    binaryExpression = clz.getConstructor(Expression.class, Expression.class)
                            .newInstance(binaryExpression, rightExpression);
                }
                return binaryExpression;
        }
    }

    public static Expression add(Expression... expressions) {
        try {
            return build(Addition.class, expressions);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException
                | IllegalAccessException e) {
            // this should never happen, at least I don't see how
            throw new RuntimeException(e);
        }
    }

    public static Expression bitAnd(Expression... expressions) {
        try {
            return build(BitwiseAnd.class, expressions);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException
                | IllegalAccessException e) {
            // this should never happen, at least I don't see how
            throw new RuntimeException(e);
        }
    }

    public static Expression bitShiftLeft(Expression... expressions) {
        try {
            return build(BitwiseLeftShift.class, expressions);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException
                | IllegalAccessException e) {
            // this should never happen, at least I don't see how
            throw new RuntimeException(e);
        }
    }

    public static Expression multiply(Expression... expressions) {
        try {
            return build(Multiplication.class, expressions);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException
                | IllegalAccessException e) {
            // this should never happen, at least I don't see how
            throw new RuntimeException(e);
        }
    }

    public static Expression bitOr(Expression... expressions) {
        try {
            return build(BitwiseOr.class, expressions);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException
                | IllegalAccessException e) {
            // this should never happen, at least I don't see how
            throw new RuntimeException(e);
        }
    }

    public static Expression bitShiftRight(Expression... expressions) {
        try {
            return build(BitwiseRightShift.class, expressions);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException
                | IllegalAccessException e) {
            // this should never happen, at least I don't see how
            throw new RuntimeException(e);
        }
    }

    public static Expression bitXor(Expression... expressions) {
        try {
            return build(BitwiseXor.class, expressions);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException
                | IllegalAccessException e) {
            // this should never happen, at least I don't see how
            throw new RuntimeException(e);
        }
    }

    public static Expression concat(Expression... expressions) {
        try {
            return build(Concat.class, expressions);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException
                | IllegalAccessException e) {
            // this should never happen, at least I don't see how
            throw new RuntimeException(e);
        }
    }

    public static Expression divide(Expression... expressions) {
        try {
            return build(Division.class, expressions);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException
                | IllegalAccessException e) {
            // this should never happen, at least I don't see how
            throw new RuntimeException(e);
        }
    }

    public static Expression divideInt(Expression... expressions) {
        try {
            return build(IntegerDivision.class, expressions);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException
                | IllegalAccessException e) {
            // this should never happen, at least I don't see how
            throw new RuntimeException(e);
        }
    }

    public static Expression modulo(Expression... expressions) {
        try {
            return build(Modulo.class, expressions);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException
                | IllegalAccessException e) {
            // this should never happen, at least I don't see how
            throw new RuntimeException(e);
        }
    }

    public static Expression subtract(Expression... expressions) {
        try {
            return build(Subtraction.class, expressions);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException
                | IllegalAccessException e) {
            // this should never happen, at least I don't see how
            throw new RuntimeException(e);
        }
    }

    public Expression getLeftExpression() {
        return leftExpression;
    }

    public Expression getRightExpression() {
        return rightExpression;
    }

    public BinaryExpression withLeftExpression(Expression expression) {
        setLeftExpression(expression);
        return this;
    }

    public void setLeftExpression(Expression expression) {
        leftExpression = expression;
    }

    public BinaryExpression withRightExpression(Expression expression) {
        setRightExpression(expression);
        return this;
    }

    public void setRightExpression(Expression expression) {
        rightExpression = expression;
    }

    @Override
    public String toString() {
        return // (not ? "NOT " : "") +
        getLeftExpression() + " " + getStringExpression() + " " + getRightExpression();
    }

    public abstract String getStringExpression();

    public <E extends Expression> E getLeftExpression(Class<E> type) {
        return type.cast(getLeftExpression());
    }

    public <E extends Expression> E getRightExpression(Class<E> type) {
        return type.cast(getRightExpression());
    }
}
