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

import java.util.ArrayList;
import java.util.Objects;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

/**
 *
 * @author <a href="mailto:andreas@manticore-projects.com">Andreas Reichel</a>
 */

public class JsonFunction extends ASTNodeAccessImpl implements Expression {
  private JsonFunctionType functionType;
  private final ArrayList<JsonKeyValuePair> keyValuePairs = new ArrayList<>();
  private final ArrayList<JsonFunctionExpression> expressions = new ArrayList<>();
  private JsonAggregateOnNullType onNullType;
  private JsonAggregateUniqueKeysType uniqueKeysType;

  public ArrayList<JsonKeyValuePair> getKeyValuePairs() {
    return keyValuePairs;
  }

  public ArrayList<JsonFunctionExpression> getExpressions() {
    return expressions;
  }

  public JsonKeyValuePair getKeyValuePair(int i) {
    return keyValuePairs.get(i);
  }

  public JsonFunctionExpression getExpression(int i) {
    return expressions.get(i);
  }

  public boolean add(JsonKeyValuePair keyValuePair) {
    return keyValuePairs.add(keyValuePair);
  }

  public void add(int i, JsonKeyValuePair keyValuePair) {
    keyValuePairs.add(i, keyValuePair);
  }

  public boolean add(JsonFunctionExpression expression) {
    return expressions.add(expression);
  }

  public void add(int i, JsonFunctionExpression expression) {
    expressions.add(i, expression);
  }

  public boolean isEmpty() {
    return keyValuePairs.isEmpty();
  }

  public JsonAggregateOnNullType getOnNullType() {
    return onNullType;
  }

  public void setOnNullType(JsonAggregateOnNullType onNullType) {
    this.onNullType = onNullType;
  }

  public JsonFunction withOnNullType(JsonAggregateOnNullType onNullType) {
    this.setOnNullType(onNullType);
    return this;
  }

  public JsonAggregateUniqueKeysType getUniqueKeysType() {
    return uniqueKeysType;
  }

  public void setUniqueKeysType(JsonAggregateUniqueKeysType uniqueKeysType) {
    this.uniqueKeysType = uniqueKeysType;
  }

  public JsonFunction withUniqueKeysType(JsonAggregateUniqueKeysType uniqueKeysType) {
    this.setUniqueKeysType(uniqueKeysType);
    return this;
  }

  public JsonFunctionType getType() {
    return functionType;
  }

  public void setType(JsonFunctionType type) {
    this.functionType =
        Objects.requireNonNull(type, "The Type of the JSON Aggregate Function must not be null");
  }

  public JsonFunction withType(JsonFunctionType type) {
    this.setType(type);
    return this;
  }

  public void setType(String typeName) {
    this.functionType = JsonFunctionType.valueOf(
        Objects.requireNonNull(typeName, "The Type of the JSON Aggregate Function must not be null")
            .toUpperCase());
  }

  public JsonFunction withType(String typeName) {
    this.setType(typeName);
    return this;
  }

  @Override
  public void accept(ExpressionVisitor expressionVisitor) {
    expressionVisitor.visit(this);
  }

  // avoid countless Builder --> String conversion
  public StringBuilder append(StringBuilder builder) {
    switch (functionType) {
      case OBJECT:
        appendObject(builder);
        break;
      case POSTGRES_OBJECT:
        appendPostgresObject(builder);
        break;
      case MYSQL_OBJECT:
        appendMySqlObject(builder);
        break;
      case ARRAY:
        appendArray(builder);
        break;
      default:
        // this should never happen really
    }
    return builder;
  }

  @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPathComplexity"})
  public StringBuilder appendObject(StringBuilder builder) {
    builder.append("JSON_OBJECT( ");
    int i = 0;
    for (JsonKeyValuePair keyValuePair : keyValuePairs) {
      if (i > 0) {
        builder.append(", ");
      }
      if (keyValuePair.isUsingValueKeyword()) {
        if (keyValuePair.isUsingKeyKeyword()) {
          builder.append("KEY ");
        }
        builder.append(keyValuePair.getKey()).append(" VALUE ").append(keyValuePair.getValue());
      } else {
        builder.append(keyValuePair.getKey()).append(":").append(keyValuePair.getValue());
      }

      if (keyValuePair.isUsingFormatJson()) {
        builder.append(" FORMAT JSON");
      }
      i++;
    }

    if (onNullType != null) {
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

    if (uniqueKeysType != null) {
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

    return builder;
  }


  @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPathComplexity"})
  public StringBuilder appendPostgresObject(StringBuilder builder) {
    builder.append("JSON_OBJECT( ");
    for (JsonKeyValuePair keyValuePair : keyValuePairs) {
      builder.append(keyValuePair.getKey());
      if (keyValuePair.getValue()!=null) {
        builder.append(", ").append(keyValuePair.getValue());
      }
    }
    builder.append(" ) ");

    return builder;
  }

  public StringBuilder appendMySqlObject(StringBuilder builder) {
    builder.append("JSON_OBJECT( ");
    int i=0;
    for (JsonKeyValuePair keyValuePair : keyValuePairs) {
      if (i>0) {
        builder.append(", ");
      }
      builder.append(keyValuePair.getKey());
      builder.append(", ").append(keyValuePair.getValue());
      i++;
    }
    builder.append(" ) ");

    return builder;
  }

  @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPathComplexity"})
  public StringBuilder appendArray(StringBuilder builder) {
    builder.append("JSON_ARRAY( ");
    int i = 0;

    for (JsonFunctionExpression expr : expressions) {
      if (i > 0) {
        builder.append(", ");
      }
      expr.append(builder);
      i++;
    }

    if (onNullType != null) {
      switch (onNullType) {
        case NULL:
          builder.append(" NULL ON NULL ");
          break;
        case ABSENT:
          builder.append(" ABSENT ON NULL ");
          break;
        default:
          // "ON NULL" was omitted
      }
    }
    builder.append(") ");

    return builder;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    return append(builder).toString();
  }
}
