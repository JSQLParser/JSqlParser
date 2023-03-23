package net.sf.jsqlparser.statement.select;

public class ParenthesedSelectBody implements SelectBody {
    private SelectBody selectBody;

    public SelectBody getSelectBody() {
        return selectBody;
    }

    public void setSelectBody(SelectBody selectBody) {
        this.selectBody = selectBody;
    }

    public ParenthesedSelectBody withSelectBody(SelectBody selectBody) {
        setSelectBody(selectBody);
        return this;
    }

    @Override
    public void accept(SelectVisitor selectVisitor) {
        selectVisitor.visit(this);
    }

    public String toString() {
        return "(" + selectBody + ")";
    }
}
