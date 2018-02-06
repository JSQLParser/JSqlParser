/*
 * Copyright (C) 2017 JSQLParser.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package net.sf.jsqlparser.expression;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseAnd;
import net.sf.jsqlparser.expression.operators.arithmetic.Concat;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;

/**
 *
 * @author tw
 */
public class ExpressionPrecedenceTest {

    @Test
    public void testGetSign() throws JSQLParserException {
        Expression expr = CCJSqlParserUtil.parseExpression("1&2||3");
        assertTrue(expr instanceof Concat);
        assertTrue(((Concat) expr).getLeftExpression() instanceof BitwiseAnd);
        assertTrue(((Concat) expr).getRightExpression() instanceof LongValue);
    }
}
