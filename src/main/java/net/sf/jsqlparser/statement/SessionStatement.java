/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2025 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

public class SessionStatement implements Statement {
    public enum Action {
        START, APPLY, DROP, SHOW, DESCRIBE;

        public static Action from(String flag) {
            return Enum.valueOf(Action.class, flag.toUpperCase());
        }
    }

    final private Action action;
    final private String id;

    public SessionStatement(Action action, String id) {
        this.action = action;
        this.id = id;
    }

    public SessionStatement(String action, String id) {
        this(Action.from(action), id);
    }

    public SessionStatement(String action) {
        this(action, null);
    }


    public Action getAction() {
        return action;
    }

    public String getId() {
        return id;
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }

    @Override
    public void accept(StatementVisitor<?> statementVisitor) {
        Statement.super.accept(statementVisitor);
    }

    @Override
    public String toString() {
        return "SESSION " + action + " " + (id != null ? id : "") + ";";
    }
}
