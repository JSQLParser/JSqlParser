/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Models the SQL Server (T-SQL) {@code OPTION (...)} query hint clause, which attaches a list of
 * {@link OptionHint}s to the end of a {@code SELECT}, {@code UPDATE}, {@code DELETE} or
 * {@code MERGE} statement, see
 * <a href="https://learn.microsoft.com/en-us/sql/t-sql/queries/hints-transact-sql-query">Hints
 * (Transact-SQL) - Query Hints</a>.
 */
public class OptionClause implements Serializable {

    private List<OptionHint> optionHints;

    public OptionClause() {
        super();
    }

    public OptionClause(List<OptionHint> optionHints) {
        this.optionHints = optionHints;
    }

    public List<OptionHint> getOptionHints() {
        return optionHints;
    }

    public void setOptionHints(List<OptionHint> optionHints) {
        this.optionHints = optionHints;
    }

    public OptionClause withOptionHints(List<OptionHint> optionHints) {
        setOptionHints(optionHints);
        return this;
    }

    public OptionClause addOptionHint(OptionHint optionHint) {
        if (optionHints == null) {
            optionHints = new ArrayList<>();
        }
        optionHints.add(optionHint);
        return this;
    }

    @Override
    public String toString() {
        return " OPTION (" + Select.getStringList(optionHints, true, false) + ")";
    }
}
