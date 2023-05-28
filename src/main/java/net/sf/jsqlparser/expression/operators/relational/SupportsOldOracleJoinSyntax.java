/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
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

    SupportsOldOracleJoinSyntax withOldOracleJoinSyntax(int oldOracleJoinSyntax);

    int getOraclePriorPosition();

    void setOraclePriorPosition(int priorPosition);

    SupportsOldOracleJoinSyntax withOraclePriorPosition(int priorPosition);
}
