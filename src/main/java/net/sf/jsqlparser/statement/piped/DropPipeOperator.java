package net.sf.jsqlparser.statement.piped;

import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Column;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public class DropPipeOperator extends PipeOperator {
    private ExpressionList<Column> columns;

    public DropPipeOperator(ExpressionList<Column> columns) {
        this.columns = columns;
    }

    public ExpressionList<Column> getColumns() {
        return columns;
    }

    public DropPipeOperator setColumns(ExpressionList<Column> columns) {
        this.columns = columns;
        return this;
    }

    public boolean containsAll(Collection<? extends Column> c) {
        return columns.containsAll(c);
    }

    public Column get(int index) {
        return columns.get(index);
    }

    public ExpressionList<?> addExpression(Column expression) {
        return columns.addExpression(expression);
    }

    public ExpressionList<?> addExpressions(Column... expressions) {
        return columns.addExpressions(expressions);
    }

    public ExpressionList<?> addExpressions(Collection<Column> expressions) {
        return columns.addExpressions(expressions);
    }

    public ExpressionList<?> withExpressions(Column... expressions) {
        return columns.withExpressions(expressions);
    }

    public ExpressionList<?> withExpressions(Collection<Column> expressions) {
        return columns.withExpressions(expressions);
    }

    public <K, S> K accept(ExpressionVisitor<K> expressionVisitor, S context) {
        return columns.accept(expressionVisitor, context);
    }

    public void trimToSize() {
        columns.trimToSize();
    }

    public boolean addAll(int index, Collection<? extends Column> c) {
        return columns.addAll(index, c);
    }

    public boolean retainAll(Collection<? extends Column> c) {
        return columns.retainAll(c);
    }

    public Stream<Column> parallelStream() {
        return columns.parallelStream();
    }

    public boolean addAll(Collection<? extends Column> c) {
        return columns.addAll(c);
    }

    public int indexOf(Column o) {
        return columns.indexOf(o);
    }

    public <T> void accept(ExpressionVisitor<T> expressionVisitor) {
        columns.accept(expressionVisitor);
    }

    public void forEach(Consumer<? super Column> action) {
        columns.forEach(action);
    }

    public int lastIndexOf(Column o) {
        return columns.lastIndexOf(o);
    }

    public Stream<Column> stream() {
        return columns.stream();
    }

    public Spliterator<Column> spliterator() {
        return columns.spliterator();
    }

    public Column set(int index, Column element) {
        return columns.set(index, element);
    }

    public void sort(Comparator<? super Column> c) {
        columns.sort(c);
    }

    public void ensureCapacity(int minCapacity) {
        columns.ensureCapacity(minCapacity);
    }

    public boolean remove(Column o) {
        return columns.remove(o);
    }

    public Object[] toArray() {
        return columns.toArray();
    }

    public Iterator<Column> iterator() {
        return columns.iterator();
    }

    public <T> T[] toArray(IntFunction<T[]> generator) {
        return columns.toArray(generator);
    }

    public boolean add(Column column) {
        return columns.add(column);
    }

    public ListIterator<Column> listIterator(int index) {
        return columns.listIterator(index);
    }

    public void replaceAll(UnaryOperator<Column> operator) {
        columns.replaceAll(operator);
    }

    public List<Column> subList(int fromIndex, int toIndex) {
        return columns.subList(fromIndex, toIndex);
    }

    public boolean removeAll(Collection<Column> c) {
        return columns.removeAll(c);
    }

    public boolean isEmpty() {
        return columns.isEmpty();
    }

    public void clear() {
        columns.clear();
    }

    public boolean contains(Column o) {
        return columns.contains(o);
    }

    public Column remove(int index) {
        return columns.remove(index);
    }

    public boolean removeIf(Predicate<? super Column> filter) {
        return columns.removeIf(filter);
    }

    public <T> T[] toArray(T[] a) {
        return columns.toArray(a);
    }

    public void add(int index, Column element) {
        columns.add(index, element);
    }

    public int size() {
        return columns.size();
    }

    public ListIterator<Column> listIterator() {
        return columns.listIterator();
    }

    @Override
    public <T, S> T accept(PipeOperatorVisitor<T> visitor, S context) {
        return visitor.visit(this, context);
    }

    @Override
    public StringBuilder appendTo(StringBuilder builder) {
        builder.append("|> ").append("DROP ");
        columns.appendTo(builder).append("\n");
        return builder;
    }
}
