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

import java.nio.charset.StandardCharsets;

public class HexValue extends ASTNodeAccessImpl implements Expression {

    private String value;

    public HexValue() {
        // empty constructor
    }

    public HexValue(final String value) {
        String val = value;
        this.value = val;
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    @Override
    public <T, S> T accept(ExpressionVisitor<T> expressionVisitor, S context) {
        return expressionVisitor.visit(this, context);
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public HexValue withValue(String value) {
        this.setValue(value);
        return this;
    }

    @Override
    public String toString() {
        return value;
    }

    public String getDigits() {
        return value.toUpperCase().startsWith("0X")
                ? value.substring(2)
                : value.substring(2, value.length() - 1);
    }

    public Long getLong() {
        return Long.parseLong(
                getDigits(), 16);
    }

    public LongValue getLongValue() {
        return new LongValue(getLong());
    }

    // `X'C3BC'` --> `'ü'`
    public StringValue getStringValue() {
        return new StringValue(
                new String(hexStringToByteArray(getDigits()), StandardCharsets.UTF_8));
    }

    // `X'C3BC'` --> `\xC3\xBC`
    public StringValue getBlob() {
        StringBuilder builder = new StringBuilder();
        String digits = getDigits();
        int len = digits.length();
        for (int i = 0; i < len; i += 2) {
            builder.append("\\x").append(digits.charAt(i)).append(digits.charAt(i + 1));
        }
        return new StringValue(builder.toString());
    }
}
