package net.sf.jsqlparser.statement.piped;

import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.FromItemVisitor;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.LateralView;
import net.sf.jsqlparser.statement.select.Pivot;
import net.sf.jsqlparser.statement.select.SampleClause;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.UnPivot;
import net.sf.jsqlparser.statement.select.WithItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public class FromQuery extends Select {
    private boolean usingFromKeyword = true;
    private FromItem fromItem;
    private List<LateralView> lateralViews = null;
    private List<Join> joins = null;
    private final ArrayList<PipeOperator> pipeOperators = new ArrayList<>();

    public FromQuery(FromItem fromItem) {
        this.fromItem = fromItem;
    }

    public FromQuery(FromItem fromItem, boolean usingFromKeyword) {
        this.fromItem = fromItem;
        this.usingFromKeyword = usingFromKeyword;
    }

    public FromItem getFromItem() {
        return fromItem;
    }

    public FromQuery setFromItem(FromItem fromItem) {
        this.fromItem = fromItem;
        return this;
    }

    public FromQuery with(FromItem fromItem) {
        return this.setFromItem(fromItem);
    }

    public boolean isUsingFromKeyword() {
        return usingFromKeyword;
    }


    public List<LateralView> getLateralViews() {
        return lateralViews;
    }

    public FromQuery setLateralViews(List<LateralView> lateralViews) {
        this.lateralViews = lateralViews;
        return this;
    }

    public FromQuery addLateralViews(Collection<LateralView> lateralViews) {
        if (this.lateralViews == null) {
            this.lateralViews = new ArrayList<>(lateralViews);
        } else {
            this.lateralViews.addAll(lateralViews);
        }
        return this;
    }

    public FromQuery addLateralViews(LateralView... lateralViews) {
        return this.addLateralViews(Arrays.asList(lateralViews));
    }

    public List<Join> getJoins() {
        return joins;
    }

    public FromQuery setJoins(List<Join> joins) {
        this.joins = joins;
        return this;
    }

    public FromQuery addJoins(Collection<Join> joins) {
        if (this.joins == null) {
            this.joins = new ArrayList<>(joins);
        } else {
            this.joins.addAll(joins);
        }
        return this;
    }

    public FromQuery addJoins(Join... joins) {
        return addJoins(Arrays.asList(joins));
    }

    public FromQuery setUsingFromKeyword(boolean usingFromKeyword) {
        this.usingFromKeyword = usingFromKeyword;
        return this;
    }

    public FromQuery with(boolean usingFromKeyword) {
        return this.setUsingFromKeyword(usingFromKeyword);
    }

    public ArrayList<PipeOperator> getPipeOperators() {
        return pipeOperators;
    }

    public FromQuery add(PipeOperator operator) {
        pipeOperators.add(operator);
        return this;
    }

    public void add(int index, PipeOperator element) {
        pipeOperators.add(index, element);
    }

    public PipeOperator remove(int index) {
        return pipeOperators.remove(index);
    }

    public boolean remove(Object o) {
        return pipeOperators.remove(o);
    }

    public void clear() {
        pipeOperators.clear();
    }

    public boolean addAll(Collection<? extends PipeOperator> c) {
        return pipeOperators.addAll(c);
    }

    public boolean addAll(int index, Collection<? extends PipeOperator> c) {
        return pipeOperators.addAll(index, c);
    }

    public boolean removeAll(Collection<?> c) {
        return pipeOperators.removeAll(c);
    }

    public boolean retainAll(Collection<?> c) {
        return pipeOperators.retainAll(c);
    }

    public List<PipeOperator> subList(int fromIndex, int toIndex) {
        return pipeOperators.subList(fromIndex, toIndex);
    }

    public void forEach(Consumer<? super PipeOperator> action) {
        pipeOperators.forEach(action);
    }

    public Spliterator<PipeOperator> spliterator() {
        return pipeOperators.spliterator();
    }

    public boolean removeIf(Predicate<? super PipeOperator> filter) {
        return pipeOperators.removeIf(filter);
    }

    public void replaceAll(UnaryOperator<PipeOperator> operator) {
        pipeOperators.replaceAll(operator);
    }

    public FromQuery with(PipeOperator operator) {
        return this.add(operator);
    }

    public PipeOperator get(int index) {
        return pipeOperators.get(index);
    }

    public PipeOperator set(int index, PipeOperator element) {
        return pipeOperators.set(index, element);
    }

    public int size() {
        return pipeOperators.size();
    }

    public boolean isEmpty() {
        return pipeOperators.isEmpty();
    }

    public boolean contains(Object o) {
        return pipeOperators.contains(o);
    }

    public int indexOf(Object o) {
        return pipeOperators.indexOf(o);
    }

    public int lastIndexOf(Object o) {
        return pipeOperators.lastIndexOf(o);
    }

    public Object[] toArray() {
        return pipeOperators.toArray();
    }

    public <T> T[] toArray(T[] a) {
        return pipeOperators.toArray(a);
    }

    @Override
    public Alias getAlias() {
        return null;
    }

    @Override
    public void setAlias(Alias alias) {

    }

    @Override
    public Pivot getPivot() {
        return null;
    }

    @Override
    public void setPivot(Pivot pivot) {

    }

    @Override
    public UnPivot getUnPivot() {
        return null;
    }

    @Override
    public void setUnPivot(UnPivot unpivot) {

    }

    @Override
    public SampleClause getSampleClause() {
        return null;
    }

    @Override
    public FromItem setSampleClause(SampleClause sampleClause) {
        return null;
    }

    @Override
    public <T, S> T accept(ExpressionVisitor<T> expressionVisitor, S context) {
        return expressionVisitor.visit(this, context);
    }

    @Override
    public <T, S> T accept(FromItemVisitor<T> fromItemVisitor, S context) {
        return fromItemVisitor.visit(this, context);
    }

    @Override
    public <T, S> T accept(SelectVisitor<T> selectVisitor, S context) {
        return selectVisitor.visit(this, context);
    }

    public <T, S> T accept(FromQueryVisitor<T, S> fromQueryVisitor, S context) {
        return fromQueryVisitor.visit(this, context);
    }

    @Override
    public StringBuilder appendTo(StringBuilder builder) {
        if (withItemsList != null && !withItemsList.isEmpty()) {
            builder.append("WITH ");
            for (Iterator<WithItem<?>> iter = withItemsList.iterator(); iter.hasNext();) {
                WithItem withItem = iter.next();
                builder.append(withItem);
                if (iter.hasNext()) {
                    builder.append(",");
                }
                builder.append(" ");
            }
        }

        if (usingFromKeyword) {
            builder.append("FROM ");
        }
        builder.append(fromItem).append("\n");
        if (lateralViews != null) {
            for (LateralView lateralView : lateralViews) {
                builder.append(" ").append(lateralView);
            }
        }
        if (joins != null) {
            for (Join join : joins) {
                if (join.isSimple()) {
                    builder.append(", ").append(join);
                } else {
                    builder.append(" ").append(join);
                }
            }
        }
        for (PipeOperator operator : pipeOperators) {
            operator.appendTo(builder);
        }
        return builder;
    }
}
