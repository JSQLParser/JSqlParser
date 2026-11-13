/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.cursor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.select.Select;

/**
 * PostgreSQL {@code DECLARE name [BINARY] [ASENSITIVE | INSENSITIVE] [[NO] SCROLL] CURSOR
 * [{WITH | WITHOUT} HOLD] FOR query}, which defines a cursor over a SELECT, VALUES or TABLE query.
 * <br>
 * Example:<br>
 * DECLARE c SCROLL CURSOR WITH HOLD FOR SELECT * FROM t<br>
 * It is not the SQL Server {@link net.sf.jsqlparser.statement.DeclareStatement DECLARE @variable}.
 *
 * @see <a href="https://www.postgresql.org/docs/18/sql-declare.html">PostgreSQL DECLARE</a>
 */
public class DeclareCursorStatement extends ASTNodeAccessImpl implements Statement {
    private String cursorName;
    private final List<CursorOption> options = new ArrayList<>();
    private Holdability holdability;
    private Select query;

    public enum Holdability {
        WITH_HOLD("WITH HOLD"), WITHOUT_HOLD("WITHOUT HOLD");

        private final String keywords;

        Holdability(String keywords) {
            this.keywords = keywords;
        }

        public String getKeywords() {
            return keywords;
        }
    }

    public DeclareCursorStatement() {}

    public DeclareCursorStatement(String cursorName, Select query) {
        this.cursorName = cursorName;
        this.query = query;
    }

    public String getCursorName() {
        return cursorName;
    }

    public void setCursorName(String cursorName) {
        this.cursorName = cursorName;
    }

    /** The options between the name and CURSOR, in their order; the list is live. */
    public List<CursorOption> getOptions() {
        return options;
    }

    public void setOptions(List<CursorOption> options) {
        this.options.clear();
        if (options != null) {
            this.options.addAll(options);
        }
    }

    public DeclareCursorStatement addOptions(CursorOption... options) {
        this.options.addAll(Arrays.asList(options));
        return this;
    }

    /** Returns null when neither WITH HOLD nor WITHOUT HOLD is written. */
    public Holdability getHoldability() {
        return holdability;
    }

    public void setHoldability(Holdability holdability) {
        this.holdability = holdability;
    }

    public Select getQuery() {
        return query;
    }

    public void setQuery(Select query) {
        this.query = query;
    }

    @Override
    public StringBuilder appendTo(StringBuilder builder) {
        return appendTo(builder, builder::append);
    }

    /** Renders the query through the caller's select writer. */
    public StringBuilder appendTo(StringBuilder builder, Consumer<Select> queryPrinter) {
        builder.append("DECLARE ").append(cursorName);
        for (CursorOption option : options) {
            builder.append(' ').append(option.getKeyword());
        }
        builder.append(" CURSOR");
        if (holdability != null) {
            builder.append(' ').append(holdability.getKeywords());
        }
        builder.append(" FOR ");
        queryPrinter.accept(query);
        return builder;
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }
}
