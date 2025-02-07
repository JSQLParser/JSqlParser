package net.sf.jsqlparser.statement.piped;

public interface FromQueryVisitor<T, S> {
    T visit(FromQuery fromQuery, S context);
}
