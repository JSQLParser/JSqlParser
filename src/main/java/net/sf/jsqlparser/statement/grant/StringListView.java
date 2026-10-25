/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.grant;

import java.util.AbstractList;
import java.util.List;
import java.util.RandomAccess;
import java.util.function.Function;
import java.util.function.Supplier;

/** Mutable string view over a typed list, retained for the string-based GRANT/REVOKE API. */
final class StringListView<T> extends AbstractList<String> implements RandomAccess {
    private final Supplier<List<T>> items;
    private final Function<String, T> parse;

    StringListView(Supplier<List<T>> items, Function<String, T> parse) {
        this.items = items;
        this.parse = parse;
    }

    @Override
    public int size() {
        return items.get().size();
    }

    @Override
    public String get(int index) {
        return String.valueOf(items.get().get(index));
    }

    @Override
    public String set(int index, String value) {
        return String.valueOf(items.get().set(index, parse.apply(value)));
    }

    @Override
    public void add(int index, String value) {
        items.get().add(index, parse.apply(value));
    }

    @Override
    public String remove(int index) {
        return String.valueOf(items.get().remove(index));
    }
}
