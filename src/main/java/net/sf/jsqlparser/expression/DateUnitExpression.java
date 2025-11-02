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

import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

import java.util.Objects;

public class DateUnitExpression extends ASTNodeAccessImpl implements Expression {

    private final DateUnit type;

    public DateUnitExpression(DateUnit type) {
        this.type = Objects.requireNonNull(type);
    }

    public DateUnitExpression(String DateUnitStr) {
        this.type = Objects.requireNonNull(DateUnit.from(DateUnitStr));
    }

    public DateUnit getType() {
        return type;
    }


    @Override
    public <T, S> T accept(ExpressionVisitor<T> expressionVisitor, S context) {
        return expressionVisitor.visit(this, context);
    }

    @Override
    public String toString() {
        return type.toString();
    }

    public enum DateUnit {
        CENTURY, DECADE, YEAR, QUARTER, MONTH, WEEK, DAY, HOUR, MINUTE, SECOND, MILLISECOND, MICROSECOND, NANOSECOND;

        public static DateUnit from(String UnitStr) {
            return Enum.valueOf(DateUnit.class, UnitStr.toUpperCase());
        }
    }
}
