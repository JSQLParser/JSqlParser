package net.sf.jsqlparser.statement.select;

public class SampleClause {
    public enum SampleKeyword {
        SAMPLE, TABLESAMPLE;

        public static SampleKeyword from(String sampleKeyword) {
            return Enum.valueOf(SampleKeyword.class, sampleKeyword.toUpperCase());
        }
    }

    public enum SampleMethod {
        BERNOULLI, SYSTEM, BLOCK;

        public static SampleMethod from(String sampleMethod) {
            return Enum.valueOf(SampleMethod.class, sampleMethod.toUpperCase());
        }
    }

    private SampleKeyword keyword;
    private SampleMethod method;
    private Number percentageArgument;
    private Number repeatArgument;

    // Oracle Specific
    private Number seedArgument;

    public SampleClause(String keyword, String method, Number percentageArgument,
            Number repeatArgument, Number seedArgument) {
        this.keyword = SampleKeyword.from(keyword);
        this.method = method == null || method.length() == 0 ? null : SampleMethod.from(method);
        this.percentageArgument = percentageArgument;
        this.repeatArgument = repeatArgument;
        this.seedArgument = seedArgument;
    }

    public SampleClause() {
        this(SampleKeyword.TABLESAMPLE.toString(), null, null, null, null);
    }

    public SampleClause(String keyword) {
        this(keyword, null, null, null, null);
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
            builder.append(" (").append(percentageArgument).append(")");
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
}
