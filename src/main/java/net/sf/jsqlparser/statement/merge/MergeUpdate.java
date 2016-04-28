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
package net.sf.jsqlparser.statement.merge;

import java.util.List;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;

/**
 *
 * @author toben
 */
public class MergeUpdate {

    private List<Column> columns = null;
    private List<Expression> values = null;
    private Expression whereCondition;
    private Expression deleteWhereCondition;

    public List<Column> getColumns() {
        return columns;
    }

    public void setColumns(List<Column> columns) {
        this.columns = columns;
    }

    public List<Expression> getValues() {
        return values;
    }

    public void setValues(List<Expression> values) {
        this.values = values;
    }

    public Expression getWhereCondition() {
        return whereCondition;
    }

    public void setWhereCondition(Expression whereCondition) {
        this.whereCondition = whereCondition;
    }

    public Expression getDeleteWhereCondition() {
        return deleteWhereCondition;
    }

    public void setDeleteWhereCondition(Expression deleteWhereCondition) {
        this.deleteWhereCondition = deleteWhereCondition;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(" WHEN MATCHED THEN UPDATE SET ");
        for (int i = 0; i < columns.size(); i++) {
			if (i != 0) {
				b.append(", ");
			}
            b.append(columns.get(i).toString()).append(" = ").append(values.get(i).toString());
        }
        if (whereCondition != null) {
            b.append(" WHERE ").append(whereCondition.toString());
        }
        if (deleteWhereCondition != null) {
            b.append(" DELETE WHERE ").append(deleteWhereCondition.toString());
        }
        return b.toString();
    }
}
