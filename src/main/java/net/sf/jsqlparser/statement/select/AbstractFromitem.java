package net.sf.jsqlparser.statement.select;

import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

public abstract class AbstractFromitem extends ASTNodeAccessImpl implements FromItem {
    private Alias alias;
    private Pivot pivot;
    private UnPivot unPivot;
    private SampleClause sampleClause = null;

    @Override
    public Alias getAlias() {
        return alias;
    }

    @Override
    public void setAlias(Alias alias) {
        this.alias = alias;
    }

    @Override
    public Pivot getPivot() {
        return pivot;
    }

    @Override
    public void setPivot(Pivot pivot) {
        this.pivot = pivot;
    }

    @Override
    public UnPivot getUnPivot() {
        return unPivot;
    }

    @Override
    public void setUnPivot(UnPivot unpivot) {
        this.unPivot = unpivot;
    }

    @Override
    public SampleClause getSampleClause() {
        return sampleClause;
    }

    @Override
    public FromItem setSampleClause(SampleClause sampleClause) {
        this.sampleClause = sampleClause;
        return this;
    }

}
