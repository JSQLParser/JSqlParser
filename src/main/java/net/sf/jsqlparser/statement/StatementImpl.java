package net.sf.jsqlparser.statement;

/**
 *
 * @author <a href="mailto:andreas@manticore-projects.com">Andreas Reichel</a>
 */
public abstract class StatementImpl implements Statement {

    public boolean isQuery() { return false; }

    public boolean isDML() { return false; }

    public boolean isDDL() { return false; }

    public boolean isBlock() { return false; }

    public boolean isUnparsed() { return false; }

    public abstract StatementType getStatementType();

    public abstract StringBuilder appendTo(StringBuilder builder);

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }
}
