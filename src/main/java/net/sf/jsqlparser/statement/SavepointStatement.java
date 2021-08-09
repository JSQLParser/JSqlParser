/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2021 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */

package net.sf.jsqlparser.statement;

import java.util.Objects;

/**
 *
 * @author <a href="mailto:andreas@manticore-projects.com">Andreas Reichel</a>
 */
public class SavepointStatement implements Statement {
    private String savepointName;

    public String getSavepointName() {
        return savepointName;
    }

    public void setSavepointName(String savepointName) {
        this.savepointName = Objects.requireNonNull(savepointName, "The Savepoint Name must not be NULL.");
    }

    public SavepointStatement(String savepointName) {
        this.savepointName = Objects.requireNonNull(savepointName, "The Savepoint Name must not be NULL.");
    }

    @Override
    public String toString() {
        return "SAVEPOINT " + savepointName;
    }

    @Override
    public void accept(StatementVisitor statementVisitor) {
         statementVisitor.visit(this);
    }
}
