/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2025 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

import net.sf.jsqlparser.expression.Expression;

import java.io.Serializable;
import java.util.List;

public class WithFunctionDeclaration implements Serializable {
    private String functionName;
    private List<WithFunctionParameter> parameters;
    private String returnType;
    private Expression returnExpression;

    public WithFunctionDeclaration() {}

    public WithFunctionDeclaration(String functionName, List<WithFunctionParameter> parameters,
            String returnType, Expression returnExpression) {
        this.functionName = functionName;
        this.parameters = parameters;
        this.returnType = returnType;
        this.returnExpression = returnExpression;
    }

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public List<WithFunctionParameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<WithFunctionParameter> parameters) {
        this.parameters = parameters;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public Expression getReturnExpression() {
        return returnExpression;
    }

    public void setReturnExpression(Expression returnExpression) {
        this.returnExpression = returnExpression;
    }

    public WithFunctionDeclaration withFunctionName(String functionName) {
        this.setFunctionName(functionName);
        return this;
    }

    public WithFunctionDeclaration withParameters(List<WithFunctionParameter> parameters) {
        this.setParameters(parameters);
        return this;
    }

    public WithFunctionDeclaration withReturnType(String returnType) {
        this.setReturnType(returnType);
        return this;
    }

    public WithFunctionDeclaration withReturnExpression(Expression returnExpression) {
        this.setReturnExpression(returnExpression);
        return this;
    }

    public StringBuilder appendTo(StringBuilder builder) {
        builder
                .append("FUNCTION ")
                .append(functionName)
                .append("(");
        for (int i = 0; parameters != null && i < parameters.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            parameters.get(i).appendTo(builder);
        }
        return builder
                .append(") RETURNS ")
                .append(returnType)
                .append(" RETURN ")
                .append(returnExpression);
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }
}
