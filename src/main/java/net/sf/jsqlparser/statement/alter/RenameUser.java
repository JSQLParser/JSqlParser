/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.alter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.create.user.UserAccount;

/** MySQL {@code RENAME USER old TO new [, old TO new] ...} statement. */
public class RenameUser implements Statement {

    private final List<Rename> renames = new ArrayList<>();

    /** One {@code old TO new} pair. */
    public static class Rename implements Serializable {
        private UserAccount from;
        private UserAccount to;

        public Rename() {
            // empty constructor
        }

        public Rename(UserAccount from, UserAccount to) {
            this.from = from;
            this.to = to;
        }

        public UserAccount getFrom() {
            return from;
        }

        public void setFrom(UserAccount from) {
            this.from = from;
        }

        public UserAccount getTo() {
            return to;
        }

        public void setTo(UserAccount to) {
            this.to = to;
        }

        @Override
        public String toString() {
            return from + " TO " + to;
        }
    }

    /** Returns the renames in source order. */
    public List<Rename> getRenames() {
        return Collections.unmodifiableList(renames);
    }

    public void setRenames(Collection<? extends Rename> renames) {
        this.renames.clear();
        if (renames != null) {
            this.renames.addAll(renames);
        }
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }

    @Override
    public String toString() {
        return "RENAME USER "
                + renames.stream().map(Rename::toString).collect(Collectors.joining(", "));
    }

    public RenameUser withRenames(Collection<? extends Rename> renames) {
        setRenames(renames);
        return this;
    }

    public RenameUser addRename(UserAccount from, UserAccount to) {
        renames.add(new Rename(from, to));
        return this;
    }
}
