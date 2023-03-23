package net.sf.jsqlparser.statement.select;

public class ParenthesedJoin extends Join {
    private Join join;

    public Join getJoin() {
        return join;
    }

    public void setJoin(Join join) {
        this.join = join;
    }

    public ParenthesedJoin withJoin(Join join) {
        setJoin(join);
        return this;
    }

    @Override
    public String toString() {
        return "(" + super.toString() + ")";
    }
}
