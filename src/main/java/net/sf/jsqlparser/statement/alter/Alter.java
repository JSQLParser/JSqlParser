/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.alter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

public class Alter implements Statement {

    private Table table;
    private boolean useOnly = false;

    private List<AlterExpression> alterExpressions;

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public boolean isUseOnly() {
        return useOnly;
    }

    public void setUseOnly(boolean useOnly) {
        this.useOnly = useOnly;
    }

    public void addAlterExpression(AlterExpression alterExpression) {
        if (alterExpressions == null) {
            alterExpressions = new ArrayList<AlterExpression>();
        }
        alterExpressions.add(alterExpression);
    }

    public List<AlterExpression> getAlterExpressions() {
        return alterExpressions;
    }

    public void setAlterExpressions(List<AlterExpression> alterExpressions) {
        this.alterExpressions = alterExpressions;
    }

    @Override
    public void accept(StatementVisitor statementVisitor) {
        statementVisitor.visit(this);
    }

    @Override
    public String toString() {

        StringBuilder b = new StringBuilder();
        b.append("ALTER TABLE ");
        if (useOnly) {
            b.append("ONLY ");
        }
        b.append(table.getFullyQualifiedName()).append(" ");

        Iterator<AlterExpression> altIter = alterExpressions.iterator();

        while (altIter.hasNext()) {
            b.append(altIter.next().toString());

            // Need to append whitespace after each ADD or DROP statement
            // but not the last one
            if (altIter.hasNext()) {
                b.append(", ");
            }
        }

        return b.toString();
    }

}
