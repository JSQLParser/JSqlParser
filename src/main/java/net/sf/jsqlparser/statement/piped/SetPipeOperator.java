/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2025 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.piped;

import net.sf.jsqlparser.statement.update.UpdateSet;

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

public class SetPipeOperator extends PipeOperator {
    private List<UpdateSet> updateSets;

    public SetPipeOperator(List<UpdateSet> updateSets) {
        this.updateSets = updateSets;
    }

    public List<UpdateSet> getUpdateSets() {
        return updateSets;
    }

    public SetPipeOperator setUpdateSets(List<UpdateSet> updateSets) {
        this.updateSets = updateSets;
        return this;
    }

    public int size() {
        return updateSets.size();
    }

    public void forEach(Consumer<? super UpdateSet> action) {
        updateSets.forEach(action);
    }

    public boolean remove(Object o) {
        return updateSets.remove(o);
    }

    public Spliterator<UpdateSet> spliterator() {
        return updateSets.spliterator();
    }

    public boolean addAll(Collection<? extends UpdateSet> c) {
        return updateSets.addAll(c);
    }

    public Stream<UpdateSet> parallelStream() {
        return updateSets.parallelStream();
    }

    public UpdateSet get(int index) {
        return updateSets.get(index);
    }

    public boolean containsAll(Collection<?> c) {
        return updateSets.containsAll(c);
    }

    public List<UpdateSet> subList(int fromIndex, int toIndex) {
        return updateSets.subList(fromIndex, toIndex);
    }

    public ListIterator<UpdateSet> listIterator() {
        return updateSets.listIterator();
    }

    public void sort(Comparator<? super UpdateSet> c) {
        updateSets.sort(c);
    }

    public <T> T[] toArray(T[] a) {
        return updateSets.toArray(a);
    }

    public ListIterator<UpdateSet> listIterator(int index) {
        return updateSets.listIterator(index);
    }

    public Stream<UpdateSet> stream() {
        return updateSets.stream();
    }

    public int lastIndexOf(Object o) {
        return updateSets.lastIndexOf(o);
    }

    public boolean add(UpdateSet updateSet) {
        return updateSets.add(updateSet);
    }

    public void clear() {
        updateSets.clear();
    }

    public Iterator<UpdateSet> iterator() {
        return updateSets.iterator();
    }

    public boolean retainAll(Collection<?> c) {
        return updateSets.retainAll(c);
    }

    public int indexOf(Object o) {
        return updateSets.indexOf(o);
    }

    public <T> T[] toArray(IntFunction<T[]> generator) {
        return updateSets.toArray(generator);
    }

    public boolean contains(Object o) {
        return updateSets.contains(o);
    }

    public Object[] toArray() {
        return updateSets.toArray();
    }

    public void replaceAll(UnaryOperator<UpdateSet> operator) {
        updateSets.replaceAll(operator);
    }

    public UpdateSet remove(int index) {
        return updateSets.remove(index);
    }

    public boolean addAll(int index, Collection<? extends UpdateSet> c) {
        return updateSets.addAll(index, c);
    }

    public boolean removeIf(Predicate<? super UpdateSet> filter) {
        return updateSets.removeIf(filter);
    }

    public void add(int index, UpdateSet element) {
        updateSets.add(index, element);
    }

    public boolean removeAll(Collection<?> c) {
        return updateSets.removeAll(c);
    }

    public UpdateSet set(int index, UpdateSet element) {
        return updateSets.set(index, element);
    }

    public boolean isEmpty() {
        return updateSets.isEmpty();
    }

    @Override
    public <T, S> T accept(PipeOperatorVisitor<T, S> visitor, S context) {
        return visitor.visit(this, context);
    }

    @Override
    public StringBuilder appendTo(StringBuilder builder) {
        builder.append("|> ").append("SET");
        int i = 0;
        for (UpdateSet updateSet : updateSets) {
            builder.append(i++ > 0 ? ", " : " ").append(updateSet);
        }
        builder.append("\n");
        return builder;
    }
}
