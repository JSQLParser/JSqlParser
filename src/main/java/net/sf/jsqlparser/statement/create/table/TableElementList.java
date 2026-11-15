/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.table;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;

/** A mutable, filtered view preserving the other declarations in a table definition. */
public final class TableElementList<E extends TableElement> extends AbstractList<E>
        implements Serializable {
    private final List<TableElement> elements;
    private final Class<E> type;

    public TableElementList(List<TableElement> elements, Class<E> type) {
        this.elements = elements;
        this.type = type;
    }

    @Override
    public int size() {
        int count = 0;
        for (TableElement element : elements) {
            if (type.isInstance(element)) {
                count++;
            }
        }
        return count;
    }

    @Override
    public Iterator<E> iterator() {
        return new Iterator<E>() {
            private int cursor;
            private int last = -1;

            @Override
            public boolean hasNext() {
                while (cursor < elements.size() && !type.isInstance(elements.get(cursor))) {
                    cursor++;
                }
                return cursor < elements.size();
            }

            @Override
            public E next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                last = cursor++;
                return type.cast(elements.get(last));
            }

            @Override
            public void remove() {
                if (last < 0) {
                    throw new IllegalStateException();
                }
                elements.remove(last);
                cursor--;
                last = -1;
                modCount++;
            }
        };
    }

    private int elementIndex(int index, boolean insertion) {
        if (index < 0) {
            throw new IndexOutOfBoundsException(Integer.toString(index));
        }
        int count = 0;
        int end = elements.size();
        for (int i = 0; i < elements.size(); i++) {
            if (type.isInstance(elements.get(i))) {
                if (count++ == index) {
                    return i;
                }
                end = i + 1;
            }
        }
        if (insertion && index == count) {
            return end;
        }
        throw new IndexOutOfBoundsException(Integer.toString(index));
    }

    @Override
    public E get(int index) {
        return type.cast(elements.get(elementIndex(index, false)));
    }

    @Override
    public E set(int index, E element) {
        return type.cast(elements.set(elementIndex(index, false), Objects.requireNonNull(element)));
    }

    @Override
    public void add(int index, E element) {
        elements.add(elementIndex(index, true), Objects.requireNonNull(element));
        modCount++;
    }

    @Override
    public E remove(int index) {
        E removed = type.cast(elements.remove(elementIndex(index, false)));
        modCount++;
        return removed;
    }

    static <E extends TableElement> void replace(List<TableElement> elements, Class<E> type,
            List<E> replacements) {
        // The replacement may itself be a view of elements.
        Iterator<E> replacement = (replacements == null ? Collections.<E>emptyList()
                : new ArrayList<>(replacements)).iterator();
        ListIterator<TableElement> iterator = elements.listIterator();
        while (iterator.hasNext()) {
            if (type.isInstance(iterator.next())) {
                if (replacement.hasNext()) {
                    iterator.set(replacement.next());
                } else {
                    iterator.remove();
                }
            }
        }
        replacement.forEachRemaining(iterator::add);
    }
}
