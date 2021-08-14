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
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.DDLStatement;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

public class Alter extends DDLStatement {

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
            alterExpressions = new ArrayList<>();
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

    public Alter withTable(Table table) {
        this.setTable(table);
        return this;
    }

    public Alter withUseOnly(boolean useOnly) {
        this.setUseOnly(useOnly);
        return this;
    }

    public Alter withAlterExpressions(List<AlterExpression> alterExpressions) {
        this.setAlterExpressions(alterExpressions);
        return this;
    }

    public Alter addAlterExpressions(AlterExpression... alterExpressions) {
        List<AlterExpression> collection = Optional.ofNullable(getAlterExpressions()).orElseGet(ArrayList::new);
        Collections.addAll(collection, alterExpressions);
        return this.withAlterExpressions(collection);
    }

    public Alter addAlterExpressions(Collection<? extends AlterExpression> alterExpressions) {
        List<AlterExpression> collection = Optional.ofNullable(getAlterExpressions()).orElseGet(ArrayList::new);
        collection.addAll(alterExpressions);
        return this.withAlterExpressions(collection);
    }

    @Override
    public StringBuilder appendTo(StringBuilder builder) {
        builder.append("ALTER TABLE ");
        if (useOnly) {
            builder.append("ONLY ");
        }
        builder.append(table.getFullyQualifiedName()).append(" ");

        Iterator<AlterExpression> altIter = alterExpressions.iterator();

        while (altIter.hasNext()) {
            builder.append(altIter.next().toString());

            // Need to append whitespace after each ADD or DROP statement
            // but not the last one
            if (altIter.hasNext()) {
                builder.append(", ");
            }
        }
        return builder;
    }
}
