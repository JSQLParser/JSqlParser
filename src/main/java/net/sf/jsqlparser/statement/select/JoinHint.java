/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2023 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

import java.io.Serializable;
import java.util.Objects;

/**
 * SQL Server join hints precede JOIN; Doris distribution hints follow it in square brackets.
 *
 * @link <a href=
 *       "https://learn.microsoft.com/en-us/sql/t-sql/queries/hints-transact-sql-join?view=sql-server-ver16">Hints
 *       (Transact-SQL) - Join</a>
 */

public class JoinHint implements Serializable {
    public enum Position {
        BEFORE_JOIN, AFTER_JOIN
    }

    private final String keyword;
    private final Position position;

    public JoinHint(String keyword) {
        this(keyword, Position.BEFORE_JOIN);
    }

    public JoinHint(String keyword, Position position) {
        this.keyword = keyword;
        this.position = Objects.requireNonNull(position, "position");
    }

    public String getKeyword() {
        return keyword;
    }

    public Position getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return position == Position.AFTER_JOIN ? "[" + keyword + "]" : keyword;
    }
}
