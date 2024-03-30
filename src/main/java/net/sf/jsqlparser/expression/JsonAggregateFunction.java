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

import java.util.List;
import java.util.Objects;
import net.sf.jsqlparser.statement.select.OrderByElement;

/**
 *
 * @author <a href="mailto:andreas@manticore-projects.com">Andreas Reichel</a>
 */

public class JsonAggregateFunction extends FilterOverImpl implements Expression {
    private JsonFunctionType functionType;
    
    private Expression expression = null;
    private final OrderByClause expressionOrderBy = new OrderByClause();
    
    private boolean usingKeyKeyword = false;
    private String key;
    private boolean usingValueKeyword = false;
    private Object value;
    
    private boolean usingFormatJson = false;
    
    private JsonAggregateOnNullType onNullType;
    private JsonAggregateUniqueKeysType uniqueKeysType;
    

    public JsonAggregateOnNullType getOnNullType() {
        return onNullType;
    }

    public void setOnNullType(JsonAggregateOnNullType onNullType) {
        this.onNullType = onNullType;
    }
    
    public JsonAggregateFunction withOnNullType(JsonAggregateOnNullType onNullType) {
        this.setOnNullType(onNullType);
        return this;
    }

    public JsonAggregateUniqueKeysType getUniqueKeysType() {
        return uniqueKeysType;
    }

    public void setUniqueKeysType(JsonAggregateUniqueKeysType uniqueKeysType) {
        this.uniqueKeysType = uniqueKeysType;
    }
    
    public JsonAggregateFunction withUniqueKeysType(JsonAggregateUniqueKeysType uniqueKeysType) {
        this.setUniqueKeysType(uniqueKeysType);
        return this;
    }

    public JsonFunctionType getType() {
        return functionType;
    }
    
    public void setType(JsonFunctionType type) {
        this.functionType = Objects.requireNonNull(type, "The Type of the JSON Aggregate Function must not be null");
    }
    
    public JsonAggregateFunction withType(JsonFunctionType type) {
        this.setType(type);
        return this;
    }

    public void setType(String typeName) {
        this.functionType = JsonFunctionType
          .valueOf( Objects.requireNonNull(typeName, "The Type of the JSON Aggregate Function must not be null").toUpperCase());
    }
    
    public JsonAggregateFunction withType(String typeName) {
        this.setType(typeName);
        return this;
    }

    public Expression getExpression() {
        return expression;
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
    }
    
    public JsonAggregateFunction withExpression(Expression expression) {
        this.setExpression(expression);
        return this;
    }

    public boolean isUsingKeyKeyword() {
        return usingKeyKeyword;
    }

    public void setUsingKeyKeyword(boolean usingKeyKeyword) {
        this.usingKeyKeyword = usingKeyKeyword;
    }
    
    public JsonAggregateFunction withUsingKeyKeyword(boolean usingKeyKeyword) {
        this.setUsingKeyKeyword(usingKeyKeyword);
        return this;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
    
    public JsonAggregateFunction withKey(String key) {
        this.setKey(key);
        return this;
    }

    public boolean isUsingValueKeyword() {
        return usingValueKeyword;
    }

    public void setUsingValueKeyword(boolean usingValueKeyword) {
        this.usingValueKeyword = usingValueKeyword;
    }
    
    public JsonAggregateFunction withUsingValueKeyword(boolean usingValueKeyword) {
        this.setUsingValueKeyword(usingValueKeyword);
        return this;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
    
    public JsonAggregateFunction withValue(Object value) {
        this.setValue(value);
        return this;
    }
    
    public boolean isUsingFormatJson() {
        return usingFormatJson;
    }

    public void setUsingFormatJson(boolean usingFormatJson) {
        this.usingFormatJson = usingFormatJson;
    }
    
    public JsonAggregateFunction withUsingFormatJson(boolean usingFormatJson) {
        this.setUsingFormatJson(usingFormatJson);
        return this;
    }
     
    public List<OrderByElement> getExpressionOrderByElements() {
        return expressionOrderBy.getOrderByElements();
    }

    public void setExpressionOrderByElements(List<OrderByElement> orderByElements) {
        expressionOrderBy.setOrderByElements(orderByElements);
    }
    
    public JsonAggregateFunction withExpressionOrderByElements(List<OrderByElement> orderByElements) {
        this.setExpressionOrderByElements(orderByElements);
        return this;
    }

    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }
    
    // avoid countless Builder --> String conversion
    @Override
    public StringBuilder append(StringBuilder builder) {
        switch (functionType) {
            case OBJECT:
                appendObject(builder);
                break;
            case ARRAY:
                appendArray(builder);
                break;
            default:
                // this should never happen really
                throw new UnsupportedOperationException("JSON Aggregate Function of the type " + functionType.name() + " has not been implemented yet.");
        }
        return builder;
    }

    public StringBuilder appendObject(StringBuilder builder) {
        builder.append("JSON_OBJECTAGG( ");
        keywordAppender(builder);
        if (usingFormatJson) {
            builder.append(" FORMAT JSON");
        }
        nullTypeAppender(builder);
        keysAppender(builder);
        super.append(builder);
        return builder;
    }

    public void nullTypeAppender(StringBuilder builder){
        if (onNullType!=null) {
            switch (onNullType) {
                case NULL:
                    builder.append(" NULL ON NULL");
                    break;
                case ABSENT:
                    builder.append(" ABSENT On NULL");
                    break;
                default:
                    // this should never happen
            }
        }
    }
    public void keywordAppender(StringBuilder builder){
        if (usingValueKeyword) {
            if (usingKeyKeyword) {
                builder.append("KEY ");
            }
            builder.append(key).append(" VALUE ").append(value);
        } else {
            builder.append(key).append(":").append(value);
        }

    }
    public void keysAppender(StringBuilder builder){

        if (uniqueKeysType!=null) {
            switch (uniqueKeysType) {
                case WITH:
                    builder.append(" WITH UNIQUE KEYS");
                    break;
                case WITHOUT:
                    builder.append(" WITHOUT UNIQUE KEYS");
                    break;
                default:
                    // this should never happen
            }
        }

        builder.append(" ) ");
    }
    
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPathComplexity"})
    public StringBuilder appendArray(StringBuilder builder) {
        builder.append("JSON_ARRAYAGG( ");
        builder.append(expression).append(" ");
        
        if (usingFormatJson) {
            builder.append("FORMAT JSON ");
        }
        
        expressionOrderBy.toStringOrderByElements(builder);
        
        if (onNullType!=null) {
            switch (onNullType) {
                case NULL:
                    builder.append(" NULL ON NULL ");
                    break;
                case ABSENT:
                    builder.append(" ABSENT On NULL ");
                    break;
                default:
                    // "ON NULL" was ommitted
            }
        }
        builder.append(") ");
        
        
        // FILTER( WHERE expression ) OVER windowNameOrSpecification
        super.append(builder);
        
        return builder;
    }

    @Override
    public String toString() {
       StringBuilder builder = new StringBuilder();
       return append(builder).toString();
    }
}
