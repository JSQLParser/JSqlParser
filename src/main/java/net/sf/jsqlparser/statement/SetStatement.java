/*
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2015 JSQLParser
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
 /*
 * Copyright (C) 2015 JSQLParser.
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
package net.sf.jsqlparser.statement;

import java.util.ArrayList;
import java.util.List;
import net.sf.jsqlparser.expression.Expression;

/**
 *
 * @author toben
 */
public final class SetStatement implements Statement {

    private final List<NameExpr> values = new ArrayList<>();

    public SetStatement(String name, Expression expression) {
        add(name, expression, true);
    }

    public void add(String name, Expression expression, boolean useEqual) {
        values.add(new NameExpr(name, expression, useEqual));
    }

    public boolean isUseEqual() {
        return values.get(0).useEqual;
    }

    public SetStatement setUseEqual(boolean useEqual) {
        values.get(0).useEqual = useEqual;
        return this;
    }

    public String getName() {
        return values.get(0).name;
    }

    public void setName(String name) {
        values.get(0).name = name;
    }

    public Expression getExpression() {
        return values.get(0).expression;
    }

    public void setExpression(Expression expression) {
        values.get(0).expression = expression;
    }

    private String toString(NameExpr ne) {
        return "SET " + ne.name + (ne.useEqual ? " = " : " ") + ne.expression.toString();
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("SET ");

        for (NameExpr ne : values) {
            if (b.length() != 4) {
                b.append(", ");
            }
            b.append(toString(ne));
        }

        return b.toString();
    }

    @Override
    public void accept(StatementVisitor statementVisitor) {
        statementVisitor.visit(this);
    }

    static class NameExpr {

        private String name;
        private Expression expression;
        private boolean useEqual;

        public NameExpr(String name, Expression expr, boolean useEqual) {
            this.name = name;
            this.expression = expr;
            this.useEqual = useEqual;
        }
    }
}
