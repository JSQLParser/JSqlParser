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

import java.util.Objects;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

/**
 * PostgreSQL {@code FETCH [direction] [FROM | IN] cursor} and {@code MOVE}, which takes the same
 * arguments but returns no rows. The direction is an optional {@link FetchDirection} keyword
 * followed by a count or ALL where the keyword allows one.<br>
 * Examples:<br>
 * FETCH NEXT FROM c<br>
 * FETCH FORWARD ALL IN c<br>
 * MOVE ABSOLUTE -1 c<br>
 * FETCH 5 c<br>
 *
 * @see <a href="https://www.postgresql.org/docs/18/sql-fetch.html">PostgreSQL FETCH</a>
 * @see <a href="https://www.postgresql.org/docs/18/sql-move.html">PostgreSQL MOVE</a>
 */
public class FetchCursorStatement extends ASTNodeAccessImpl implements Statement {
    private Kind kind = Kind.FETCH;
    private FetchDirection direction;
    private Long count;
    private boolean all;
    private FromKeyword fromKeyword;
    private String cursorName;

    public enum Kind {
        FETCH, MOVE
    }

    /** The FROM or IN before the cursor name. */
    public enum FromKeyword {
        FROM, IN
    }

    public FetchCursorStatement() {}

    public FetchCursorStatement(Kind kind, String cursorName) {
        setKind(kind);
        this.cursorName = cursorName;
    }

    public Kind getKind() {
        return kind;
    }

    public void setKind(Kind kind) {
        this.kind = Objects.requireNonNull(kind);
    }

    /** Returns null when the statement has no direction keyword, as in FETCH 5 c or FETCH c. */
    public FetchDirection getDirection() {
        return direction;
    }

    public void setDirection(FetchDirection direction) {
        this.direction = direction;
    }

    /** The signed count of ABSOLUTE, RELATIVE, FORWARD, BACKWARD or of a bare count, or null. */
    public Long getCount() {
        return count;
    }

    /** Setting a count clears {@link #isAll()}. */
    public void setCount(Long count) {
        this.count = count;
        if (count != null) {
            all = false;
        }
    }

    /** Whether the direction is ALL, FORWARD ALL or BACKWARD ALL. */
    public boolean isAll() {
        return all;
    }

    /** Setting true clears {@link #getCount()}. */
    public void setAll(boolean all) {
        this.all = all;
        if (all) {
            count = null;
        }
    }

    /** Returns null when neither FROM nor IN precedes the cursor name. */
    public FromKeyword getFromKeyword() {
        return fromKeyword;
    }

    public void setFromKeyword(FromKeyword fromKeyword) {
        this.fromKeyword = fromKeyword;
    }

    public String getCursorName() {
        return cursorName;
    }

    public void setCursorName(String cursorName) {
        this.cursorName = cursorName;
    }

    @Override
    public StringBuilder appendTo(StringBuilder builder) {
        builder.append(kind.name());
        if (direction != null) {
            builder.append(' ').append(direction.name());
        }
        if (all) {
            builder.append(" ALL");
        } else if (count != null) {
            builder.append(' ').append(count);
        }
        if (fromKeyword != null) {
            builder.append(' ').append(fromKeyword.name());
        }
        return builder.append(' ').append(cursorName);
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
