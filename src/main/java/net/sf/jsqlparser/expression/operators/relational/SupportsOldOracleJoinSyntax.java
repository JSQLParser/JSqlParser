/*
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2013 JSQLParser
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
package net.sf.jsqlparser.expression.operators.relational;

public interface SupportsOldOracleJoinSyntax {

	int NO_ORACLE_JOIN = 0;
	int ORACLE_JOIN_RIGHT = 1;
	int ORACLE_JOIN_LEFT = 2;
	int NO_ORACLE_PRIOR = 0;
	int ORACLE_PRIOR_START = 1;
	int ORACLE_PRIOR_END = 2;

	int getOldOracleJoinSyntax();

	void setOldOracleJoinSyntax(int oldOracleJoinSyntax);

	int getOraclePriorPosition();

	void setOraclePriorPosition(int priorPosition);
}
