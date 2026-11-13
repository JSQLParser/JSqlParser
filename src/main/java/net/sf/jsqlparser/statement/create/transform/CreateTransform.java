/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.transform;

import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.statement.RoutineReference;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.TransformSignature;

/**
 * PostgreSQL
 * {@code CREATE [OR REPLACE] TRANSFORM FOR type LANGUAGE lang (FROM SQL WITH FUNCTION f [(args)], TO SQL WITH FUNCTION g [(args)])}.
 * The transform is named by the {@link TransformSignature} that {@code DROP TRANSFORM} uses. It has
 * one or both of the functions, in either order.
 */
public class CreateTransform extends ASTNodeAccessImpl implements Statement {
    private boolean orReplace;
    private TransformSignature transform;
    private RoutineReference fromSqlFunction;
    private RoutineReference toSqlFunction;
    private boolean toSqlFirst;

    public boolean isOrReplace() {
        return orReplace;
    }

    public void setOrReplace(boolean orReplace) {
        this.orReplace = orReplace;
    }

    public CreateTransform withOrReplace(boolean orReplace) {
        setOrReplace(orReplace);
        return this;
    }

    public TransformSignature getTransform() {
        return transform;
    }

    public void setTransform(TransformSignature transform) {
        this.transform = transform;
    }

    public CreateTransform withTransform(TransformSignature transform) {
        setTransform(transform);
        return this;
    }

    /**
     * The function of {@code FROM SQL WITH FUNCTION}, which converts the type to the language.
     *
     * @return the function, or {@code null} without a {@code FROM SQL} item
     */
    public RoutineReference getFromSqlFunction() {
        return fromSqlFunction;
    }

    public void setFromSqlFunction(RoutineReference fromSqlFunction) {
        this.fromSqlFunction = fromSqlFunction;
    }

    public CreateTransform withFromSqlFunction(RoutineReference fromSqlFunction) {
        setFromSqlFunction(fromSqlFunction);
        return this;
    }

    /**
     * The function of {@code TO SQL WITH FUNCTION}, which converts the language's value to the
     * type.
     *
     * @return the function, or {@code null} without a {@code TO SQL} item
     */
    public RoutineReference getToSqlFunction() {
        return toSqlFunction;
    }

    public void setToSqlFunction(RoutineReference toSqlFunction) {
        this.toSqlFunction = toSqlFunction;
    }

    public CreateTransform withToSqlFunction(RoutineReference toSqlFunction) {
        setToSqlFunction(toSqlFunction);
        return this;
    }

    /**
     * Whether the {@code TO SQL} item is written before the {@code FROM SQL} item. It matters only
     * when the statement has both.
     *
     * @return whether {@code TO SQL} comes first
     */
    public boolean isToSqlFirst() {
        return toSqlFirst;
    }

    public void setToSqlFirst(boolean toSqlFirst) {
        this.toSqlFirst = toSqlFirst;
    }

    public CreateTransform withToSqlFirst(boolean toSqlFirst) {
        setToSqlFirst(toSqlFirst);
        return this;
    }

    public StringBuilder appendTo(StringBuilder builder) {
        builder.append("CREATE ");
        if (orReplace) {
            builder.append("OR REPLACE ");
        }
        builder.append("TRANSFORM ");
        transform.appendTo(builder).append(" (");
        String separator = "";
        if (toSqlFirst) {
            separator = appendItem(builder, separator, "TO", toSqlFunction);
        }
        separator = appendItem(builder, separator, "FROM", fromSqlFunction);
        if (!toSqlFirst) {
            appendItem(builder, separator, "TO", toSqlFunction);
        }
        return builder.append(')');
    }

    private static String appendItem(StringBuilder builder, String separator, String direction,
            RoutineReference function) {
        if (function == null) {
            return separator;
        }
        builder.append(separator).append(direction).append(" SQL WITH FUNCTION ")
                .append(function);
        return ", ";
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }
}
