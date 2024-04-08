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
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.select.Select;

import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CastExpression extends ASTNodeAccessImpl implements Expression {
    private final static Pattern PATTERN =
            Pattern.compile("(^[a-z0-9_]*){1}", Pattern.CASE_INSENSITIVE);
    public String keyword;

    private Expression leftExpression;
    private ColDataType colDataType = null;
    private ArrayList<ColumnDefinition> columnDefinitions = new ArrayList<>();

    private boolean isImplicitCast = false;

    // BigQuery specific FORMAT clause:
    // https://cloud.google.com/bigquery/docs/reference/standard-sql/conversion_functions#cast_as_date
    private String format = null;

    public CastExpression(String keyword, Expression leftExpression, String dataType) {
        this.keyword = keyword;
        this.leftExpression = leftExpression;
        this.colDataType = new ColDataType(dataType);
    }

    // Implicit Cast
    public CastExpression(ColDataType colDataType, String value) {
        this.keyword = null;
        this.isImplicitCast = true;
        this.colDataType = colDataType;
        this.leftExpression = new StringValue(value);
    }

    public CastExpression(ColDataType colDataType, Long value) {
        this.keyword = null;
        this.isImplicitCast = true;
        this.colDataType = colDataType;
        this.leftExpression = new LongValue(value);
    }

    public CastExpression(ColDataType colDataType, Double value) {
        this.keyword = null;
        this.isImplicitCast = true;
        this.colDataType = colDataType;
        this.leftExpression = new DoubleValue(value);
    }

    public CastExpression(Expression leftExpression, String dataType) {
        this.keyword = null;
        this.leftExpression = leftExpression;
        this.colDataType = new ColDataType(dataType);
    }



    public CastExpression(String keyword) {
        this.keyword = keyword;
    }

    public CastExpression() {
        this("CAST");
    }

    public ColDataType getColDataType() {
        return colDataType;
    }

    public ArrayList<ColumnDefinition> getColumnDefinitions() {
        return columnDefinitions;
    }

    public void setColDataType(ColDataType colDataType) {
        this.colDataType = colDataType;
    }

    public void addColumnDefinition(ColumnDefinition columnDefinition) {
        this.columnDefinitions.add(columnDefinition);
    }

    public Expression getLeftExpression() {
        return leftExpression;
    }

    public void setLeftExpression(Expression expression) {
        leftExpression = expression;
    }

    public boolean isImplicitCast() {
        return isImplicitCast;
    }

    public CastExpression setImplicitCast(boolean implicitCast) {
        isImplicitCast = implicitCast;
        return this;
    }

    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }

    @Deprecated
    public boolean isUseCastKeyword() {
        return keyword != null && !keyword.isEmpty();
    }

    @Deprecated
    public void setUseCastKeyword(boolean useCastKeyword) {
        if (useCastKeyword) {
            if (keyword == null || keyword.isEmpty()) {
                keyword = "CAST";
            }
        } else {
            keyword = null;
        }
    }

    public String getFormat() {
        return format;
    }

    public CastExpression setFormat(String format) {
        this.format = format;
        return this;
    }

    @Override
    public String toString() {
        String formatStr = format != null && !format.isEmpty()
                ? " FORMAT " + format
                : "";
        if (isImplicitCast) {
            return colDataType + " " + leftExpression;
        } else if (keyword != null && !keyword.isEmpty()) {
            return columnDefinitions.size() > 1
                    ? keyword + "(" + leftExpression + " AS ROW("
                            + Select.getStringList(columnDefinitions) + ")" + formatStr + ")"
                    : keyword + "(" + leftExpression + " AS " + colDataType.toString() + formatStr
                            + ")";
        } else {
            return leftExpression + "::" + colDataType.toString();
        }
    }

    public CastExpression withType(ColDataType type) {
        this.setColDataType(type);
        return this;
    }

    public CastExpression withUseCastKeyword(boolean useCastKeyword) {
        this.setUseCastKeyword(useCastKeyword);
        return this;
    }

    public CastExpression withLeftExpression(Expression leftExpression) {
        this.setLeftExpression(leftExpression);
        return this;
    }

    public <E extends Expression> E getLeftExpression(Class<E> type) {
        return type.cast(getLeftExpression());
    }

    public enum DataType {
        ARRAY, BIT, BITSTRING, BLOB, BYTEA, BINARY, VARBINARY, BYTES, BOOLEAN, BOOL, ENUM, INTERVAL, LIST, MAP, STRUCT, TINYINT, INT1, SMALLINT, INT2, SHORT, INTEGER, INT4, INT, SIGNED, BIGINT, INT8, LONG, HUGEINT, UTINYINT, USMALLINT, UINTEGER, UBIGINT, UHUGEINT, DECIMAL, NUMBER, NUMERIC, REAL, FLOAT4, FLOAT, DOUBLE, DOUBLE_PRECISION, FLOAT8, FLOAT64, UUID, VARCHAR, NVARCHAR, CHAR, NCHAR, BPCHAR, STRING, TEXT, CLOB, DATE, TIME, TIME_WITHOUT_TIME_ZONE, TIMETZ, TIME_WITH_TIME_ZONE, TIMESTAMP_NS, TIMESTAMP, TIMESTAMP_WITHOUT_TIME_ZONE, DATETIME, TIMESTAMP_MS, TIMESTAMP_S, TIMESTAMPTZ, TIMESTAMP_WITH_TIME_ZONE, UNKNOWN;

        public static DataType from(String typeStr) {
            Matcher matcher = PATTERN.matcher(typeStr.trim().replaceAll("\\s+", "_").toUpperCase());
            if (matcher.find()) {
                try {
                    return Enum.valueOf(DataType.class, matcher.group(0));
                } catch (Exception ex) {
                    Logger.getLogger(CastExpression.class.getName()).log(Level.WARNING,
                            "Type " + typeStr + " unknown", ex);
                    return DataType.UNKNOWN;
                }
            } else {
                Logger.getLogger(CastExpression.class.getName()).log(Level.WARNING,
                        "Type " + typeStr + " unknown");
                return DataType.UNKNOWN;
            }
        }
    }

    public boolean isOf(DataType... types) {
        return Set.of(types).contains(DataType.from(colDataType.getDataType()));
    }

    public boolean isTime() {
        return isOf(DataType.TIME, DataType.TIME_WITH_TIME_ZONE, DataType.TIME_WITHOUT_TIME_ZONE);
    }

    public boolean isTimeStamp() {
        return isOf(DataType.TIMESTAMP_NS, DataType.TIMESTAMP, DataType.TIMESTAMP_WITHOUT_TIME_ZONE,
                DataType.DATETIME, DataType.TIMESTAMP_MS, DataType.TIMESTAMP_S,
                DataType.TIMESTAMPTZ, DataType.TIMESTAMP_WITH_TIME_ZONE);
    }

    public boolean isDate() {
        return isOf(DataType.DATE);
    }

    public boolean isBLOB() {
        return isOf(DataType.BLOB, DataType.BYTEA, DataType.BINARY, DataType.VARBINARY,
                DataType.BYTES);
    }

    public boolean isFloat() {
        return isOf(DataType.REAL, DataType.FLOAT4, DataType.FLOAT, DataType.DOUBLE,
                DataType.DOUBLE_PRECISION, DataType.FLOAT8);
    }

    public boolean isInteger() {
        return isOf(DataType.TINYINT, DataType.INT1, DataType.SMALLINT, DataType.INT2,
                DataType.SHORT, DataType.INTEGER, DataType.INT4, DataType.INT, DataType.SIGNED,
                DataType.BIGINT, DataType.INT8, DataType.LONG, DataType.HUGEINT, DataType.UTINYINT,
                DataType.USMALLINT, DataType.UINTEGER, DataType.UBIGINT, DataType.UHUGEINT);
    }

    public boolean isDecimal() {
        return isOf(DataType.DECIMAL, DataType.NUMBER, DataType.NUMERIC);
    }

    public boolean isText() {
        return isOf(DataType.VARCHAR, DataType.NVARCHAR, DataType.CHAR, DataType.NCHAR,
                DataType.BPCHAR, DataType.STRING, DataType.TEXT, DataType.CLOB);
    }
}
