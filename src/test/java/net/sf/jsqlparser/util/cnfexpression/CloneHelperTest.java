/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2022 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.cnfexpression;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ParenthesedExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author tw
 */
public class CloneHelperTest {

    @Test
    public void testChangeBack() {
        MultipleExpression ors = transform(Arrays.asList("a>b", "5=a", "b=c", "a>c"));
        Expression expr = CloneHelper.changeBack(true, ors);
        assertThat(expr).isInstanceOf(ParenthesedExpressionList.class);
        assertThat(expr.toString()).isEqualTo("(a > b OR 5 = a OR b = c OR a > c)");
    }

    @Test
    public void testChangeBackOddNumberOfExpressions() {
        MultipleExpression ors = transform(Arrays.asList("a>b", "5=a", "b=c", "a>c", "e<f"));
        Expression expr = CloneHelper.changeBack(true, ors);
        assertThat(expr).isInstanceOf(ParenthesedExpressionList.class);
        assertThat(expr.toString()).isEqualTo("(a > b OR 5 = a OR b = c OR a > c OR e < f)");
    }

    private static MultipleExpression transform(List<String> expressions) {
        return new MultiOrExpression(
                expressions.stream()
                        .map(expr -> {
                            try {
                                return CCJSqlParserUtil.parseCondExpression(expr);
                            } catch (JSQLParserException ex) {
                                Logger.getLogger(CloneHelperTest.class.getName()).log(Level.SEVERE,
                                        null, ex);
                                return null;
                            }
                        })
                        .collect(toList()));
    }

}
