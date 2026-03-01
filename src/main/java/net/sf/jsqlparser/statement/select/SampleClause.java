/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2023 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

public class SampleClause {
    private SampleKeyword keyword;
    private SampleMethod method;
    private Number percentageArgument;
    private String percentageUnit;
    private boolean argumentInBrackets = true;
    // ClickHouse specific
    private Number offsetArgument;
    private Number repeatArgument;
    // Oracle Specific
    private Number seedArgument;

    public SampleClause(String keyword, String method, Number percentageArgument,
            String percentageUnit,
            Number repeatArgument, Number seedArgument) {
        this(keyword, method, percentageArgument, percentageUnit, repeatArgument, seedArgument,
                true,
                null);
    }

    public SampleClause(String keyword, String method, Number percentageArgument,
            String percentageUnit,
            Number repeatArgument, Number seedArgument, boolean argumentInBrackets,
            Number offsetArgument) {
        this.keyword = SampleKeyword.from(keyword);
        this.method = method == null || method.length() == 0 ? null : SampleMethod.from(method);
        this.percentageArgument = percentageArgument;
        this.percentageUnit = percentageUnit;
        this.argumentInBrackets = argumentInBrackets;
        this.offsetArgument = offsetArgument;
        this.repeatArgument = repeatArgument;
        this.seedArgument = seedArgument;
    }

    public SampleClause() {
        this(SampleKeyword.TABLESAMPLE.toString(), null, null, null, null, null);
    }

    public SampleClause(String keyword) {
        this(keyword, null, null, null, null, null);
    }

    public SampleKeyword getKeyword() {
        return keyword;
    }

    public SampleClause setKeyword(SampleKeyword keyword) {
        this.keyword = keyword;
        return this;
    }

    public Number getPercentageArgument() {
        return percentageArgument;
    }

    public SampleClause setPercentageArgument(Number percentageArgument) {
        this.percentageArgument = percentageArgument;
        return this;
    }

    public Number getRepeatArgument() {
        return repeatArgument;
    }

    public String getPercentageUnit() {
        return percentageUnit;
    }

    public SampleClause setPercentageUnit(String percentageUnit) {
        this.percentageUnit = percentageUnit;
        return this;
    }

    public boolean isArgumentInBrackets() {
        return argumentInBrackets;
    }

    public SampleClause setArgumentInBrackets(boolean argumentInBrackets) {
        this.argumentInBrackets = argumentInBrackets;
        return this;
    }

    public Number getOffsetArgument() {
        return offsetArgument;
    }

    public SampleClause setOffsetArgument(Number offsetArgument) {
        this.offsetArgument = offsetArgument;
        return this;
    }

    public SampleClause setRepeatArgument(Number repeatArgument) {
        this.repeatArgument = repeatArgument;
        return this;
    }

    public Number getSeedArgument() {
        return seedArgument;
    }

    public SampleClause setSeedArgument(Number seedArgument) {
        this.seedArgument = seedArgument;
        return this;
    }

    public SampleMethod getMethod() {
        return method;
    }

    public SampleClause setMethod(SampleMethod method) {
        this.method = method;
        return this;
    }

    public SampleClause setMethod(String method) {
        this.method = method == null || method.length() == 0 ? null : SampleMethod.from(method);
        return this;
    }

    public StringBuilder appendTo(StringBuilder builder) {

        builder.append(" ").append(keyword);
        if (method != null) {
            builder.append(" ").append(method);
        }

        if (percentageArgument != null) {
            if (argumentInBrackets) {
                builder.append(" (").append(percentageArgument)
                        .append(percentageUnit != null ? " " + percentageUnit : "").append(")");
            } else {
                builder.append(" ").append(percentageArgument);
                if (percentageUnit != null) {
                    builder.append(" ").append(percentageUnit);
                }
            }
        }

        if (offsetArgument != null) {
            builder.append(" OFFSET ").append(offsetArgument);
        }

        if (repeatArgument != null) {
            builder.append(" REPEATABLE (").append(repeatArgument).append(")");
        }

        if (seedArgument != null) {
            builder.append(" SEED (").append(seedArgument).append(")");
        }

        return builder;
    }

    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }

    public enum SampleKeyword {
        SAMPLE("SAMPLE"), TABLESAMPLE("TABLESAMPLE"), USING_SAMPLE("USING SAMPLE");

        String keyword;

        SampleKeyword(String keyword) {
            this.keyword = keyword;
        }

        public static SampleKeyword from(String sampleKeyword) {
            return Enum.valueOf(SampleKeyword.class,
                    sampleKeyword.toUpperCase().replaceAll(" ", "_"));
        }

        @Override
        public String toString() {
            return keyword;
        }
    }

    public enum SampleMethod {
        BERNOULLI, SYSTEM, BLOCK;

        public static SampleMethod from(String sampleMethod) {
            return Enum.valueOf(SampleMethod.class,
                    sampleMethod.toUpperCase().replaceAll(" ", "_"));
        }
    }
}
