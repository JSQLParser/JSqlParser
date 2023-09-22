package net.sf.jsqlparser.statement.select;

/**
 * Hints (Transact-SQL) - Join
 *
 * @link <a href=
 *       "https://learn.microsoft.com/en-us/sql/t-sql/queries/hints-transact-sql-join?view=sql-server-ver16">Hints
 *       (Transact-SQL) - Join</a>
 */

public class JoinHint {
    private final String keyword;

    public JoinHint(String keyword) {
        this.keyword = keyword;
    }

    @Override
    public String toString() {
        return keyword;
    }
}
