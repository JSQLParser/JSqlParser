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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author <a href="mailto:andreas@manticore-projects.com">Andreas Reichel</a>
 */

public class UnsupportedStatement implements Statement {
    private List<String> declarations;

    public UnsupportedStatement(List<String> declarations) {
        this.declarations =
                Objects.requireNonNull(declarations, "The List of Tokens must not be null.");
    }

    public UnsupportedStatement(String upfront, List<String> declarations) {
        this.declarations = new ArrayList<>();
        this.declarations.add(upfront);
        this.declarations.addAll(
                Objects.requireNonNull(declarations, "The List of Tokens must not be null."));
    }

    @Override
    public <T> T accept(StatementVisitor<T> statementVisitor) {
        return statementVisitor.visit(this);
    }

    @SuppressWarnings({"PMD.MissingBreakInSwitch", "PMD.SwitchStmtsShouldHaveDefault",
            "PMD.CyclomaticComplexity"})
    public StringBuilder appendTo(StringBuilder builder) {
        int i = 0;
        for (String s : declarations) {
            if (i > 0) {
                builder.append(" ");
            }
            builder.append(s);
            i++;
        }
        return builder;
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }

    public boolean isEmpty() {
        return declarations.isEmpty();
    }
}
