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

import java.util.Arrays;
import java.util.List;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.NamedExpressionList;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;

/**
 * A function as MAX,COUNT...
 */
public class Function extends ASTNodeAccessImpl implements Expression {

    private List<String> nameparts;
    private ExpressionList parameters;
    private NamedExpressionList namedParameters;
    private boolean allColumns = false;
    private boolean distinct = false;
    private boolean unique = false;
    private boolean isEscaped = false;
    private Expression attribute;
    private String attributeName;
    private List<OrderByElement> orderByElements;
    private KeepExpression keep = null;
    private boolean ignoreNulls = false;

    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }

    public String getName() {
        return nameparts == null ? null : String.join(".", nameparts);
    }
    
    public List<String> getMultipartName() {
        return nameparts;
    }

    public void setName(String string) {
        nameparts = Arrays.asList(string);
    }
    
    public Function withName(String name) {
        this.setName(name);
        return this;
    }
    
    public void setName(List<String> string) {
        nameparts = string;
    }

    public boolean isAllColumns() {
        return allColumns;
    }

    public void setAllColumns(boolean b) {
        allColumns = b;
    }

    public boolean isIgnoreNulls() {
        return ignoreNulls;
    }

    /**
     * This is at the moment only necessary for AnalyticExpression initialization and not for normal
     * functions. Therefore there is no deparsing for it for normal functions.
     *
     */
    public void setIgnoreNulls(boolean ignoreNulls) {
        this.ignoreNulls = ignoreNulls;
    }

    /**
     * true if the function is "distinct"
     *
     * @return true if the function is "distinct"
     */
    public boolean isDistinct() {
        return distinct;
    }

    public void setDistinct(boolean b) {
        distinct = b;
    }

    /**
     * true if the function is "unique"
     *
     * @return true if the function is "unique"
     */
    public boolean isUnique() {
        return unique;
    }

    public void setUnique(boolean b) {
        unique = b;
    }

    /**
     * The list of parameters of the function (if any, else null) If the parameter is "*",
     * allColumns is set to true
     *
     * @return the list of parameters of the function (if any, else null)
     */
    public ExpressionList getParameters() {
        return parameters;
    }

    public void setParameters(ExpressionList list) {
        parameters = list;
    }

    /**
     * the parameters might be named parameters, e.g. substring('foobar' from 2 for 3)
     *
     * @return the list of named parameters of the function (if any, else null)
     */
    public NamedExpressionList getNamedParameters() {
        return namedParameters;
    }

    public void setNamedParameters(NamedExpressionList list) {
        namedParameters = list;
    }

    /**
     * Return true if it's in the form "{fn function_body() }"
     *
     * @return true if it's java-escaped
     */
    public boolean isEscaped() {
        return isEscaped;
    }

    public void setEscaped(boolean isEscaped) {
        this.isEscaped = isEscaped;
    }

    public Expression getAttribute() {
        return attribute;
    }

    public void setAttribute(Expression attribute) {
        this.attribute = attribute;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public KeepExpression getKeep() {
        return keep;
    }

    public void setKeep(KeepExpression keep) {
        this.keep = keep;
    }

    @Override
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPathComplexity"})
    public String toString() {
        String params;

        if (parameters != null || namedParameters != null) {
            if (parameters != null) {
                StringBuilder b = new StringBuilder();
                b.append("(");
                if (isDistinct()) {
                    b.append("DISTINCT ");
                } else if (isUnique()) {
                    b.append("UNIQUE ");
                }
                if (isAllColumns()) {
                    b.append("ALL ");
                }
                b.append(PlainSelect.getStringList(parameters.getExpressions(), true, false));
                if (orderByElements != null) {
                    b.append(" ORDER BY ");
                    boolean comma = false;
                    for (OrderByElement orderByElement : orderByElements) {
                        if (comma) {
                            b.append(", ");
                        } else {
                            comma = true;
                        }
                        b.append(orderByElement);
                    }
                }
                b.append(")");
                params = b.toString();
            } else {
                params = namedParameters.toString();
            }
        } else {
            params = "()";
        }

        String ans = getName() + params;

        if (attribute != null) {
            ans += "." + attribute.toString();
        } else if (attributeName != null) {
            ans += "." + attributeName;
        }

        if (keep != null) {
            ans += " " + keep.toString();
        }

        if (isEscaped) {
            ans = "{fn " + ans + "}";
        }

        return ans;
    }

    public Function withAttribute(Expression attribute) {
        this.setAttribute(attribute);
        return this;
    }

    public Function withAttributeName(String attributeName) {
        this.setAttributeName(attributeName);
        return this;
    }

    public Function withKeep(KeepExpression keep) {
        this.setKeep(keep);
        return this;
    }

    public Function withIgnoreNulls(boolean ignoreNulls) {
        this.setIgnoreNulls(ignoreNulls);
        return this;
    }

    public Function withParameters(ExpressionList parameters) {
        this.setParameters(parameters);
        return this;
    }

    public Function withNamedParameters(NamedExpressionList namedParameters) {
        this.setNamedParameters(namedParameters);
        return this;
    }

    public Function withAllColumns(boolean allColumns) {
        this.setAllColumns(allColumns);
        return this;
    }

    public Function withDistinct(boolean distinct) {
        this.setDistinct(distinct);
        return this;
    }

    public Function withUnique(boolean unique) {
        this.setUnique(unique);
        return this;
    }

    public List<OrderByElement> getOrderByElements() {
        return orderByElements;
    }

    public void setOrderByElements(List<OrderByElement> orderByElements) {
        this.orderByElements = orderByElements;
    }

    public <E extends Expression> E getAttribute(Class<E> type) {
        return type.cast(getAttribute());
    }
}
