package net.sf.jsqlparser.statement;

import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Column;

/**
 * @see https://docs.oracle.com/en/database/oracle/oracle-database/21/lnpls/RETURNING-INTO-clause.html#GUID-38F735B9-1100-45AF-AE71-18FB74A899BE
 */
public class ReturningClause {
    private String returnKeyword = "RETURNING";
    ExpressionList<Column> columns;
}
