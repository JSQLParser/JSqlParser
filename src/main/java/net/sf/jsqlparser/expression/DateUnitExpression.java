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

import java.util.Locale;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

import java.util.Objects;

public class DateUnitExpression extends ASTNodeAccessImpl implements Expression {

    private final DateUnit type;

    private final String text;

    public DateUnitExpression(DateUnit type) {
        this.type = Objects.requireNonNull(type);
        this.text = type.toString();
    }

    public DateUnitExpression(String DateUnitStr) {
        this.type = Objects.requireNonNull(DateUnit.from(DateUnitStr));
        this.text = DateUnitStr;
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
        return text;
    }

    public enum DateUnit {
        CENTURY, DECADE, YEAR, QUARTER, MONTH, WEEK, DAY, HOUR, MINUTE, SECOND, MILLISECOND, MICROSECOND, NANOSECOND, SQL_TSI_FRAC_SECOND, SQL_TSI_SECOND, SQL_TSI_MINUTE, SQL_TSI_HOUR, SQL_TSI_DAY, SQL_TSI_WEEK, SQL_TSI_MONTH, SQL_TSI_QUARTER, SQL_TSI_YEAR;

        /** Returns an ODBC interval keyword, or null when the text is not one. */
        public static DateUnit fromOdbcInterval(String text) {
            if (text == null || !text.toUpperCase(Locale.ROOT).startsWith("SQL_TSI_")) {
                return null;
            }
            try {
                return from(text);
            } catch (IllegalArgumentException exception) {
                return null;
            }
        }

        public static DateUnit from(String UnitStr) {
            return Enum.valueOf(DateUnit.class, UnitStr.toUpperCase(Locale.ROOT));
        }
    }
}
