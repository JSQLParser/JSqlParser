/*
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2017 JSQLParser
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
package net.sf.jsqlparser.expression.mysql;

import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.select.WithItem;

/**
 * @author sam
 */
public class SelectVisitorUsingSqlCalcFoundRowsFactory {
    public static SelectVisitor create(final MySqlSqlCalcFoundRowRef ref) {
        return new SelectVisitor() {
            @Override
            public void visit(PlainSelect plainSelect) {
                ref.sqlCalcFoundRows = plainSelect.getMySqlSqlCalcFoundRows();
            }

            @Override
            public void visit(SetOperationList setOpList) {

            }

            @Override
            public void visit(WithItem withItem) {

            }
        };
    }
}
