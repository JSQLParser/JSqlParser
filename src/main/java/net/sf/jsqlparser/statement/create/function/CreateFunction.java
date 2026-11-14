/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2020 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.function;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sf.jsqlparser.statement.CreateFunctionalStatement;
import net.sf.jsqlparser.statement.RoutineCharacteristic;

/**
 * A {@code CREATE FUNCTION} statement. A MySQL loadable function
 * ({@code CREATE [AGGREGATE] FUNCTION name RETURNS type SONAME 'library'}) is held as a
 * {@link LoadableFunction}, which then is the source of the declaration parts.
 */
public class CreateFunction extends CreateFunctionalStatement {

    private boolean aggregate;
    private LoadableFunction loadableFunction;

    public CreateFunction() {
        super("FUNCTION");
    }

    public CreateFunction(List<String> functionDeclarationParts) {
        this(false, functionDeclarationParts);
    }

    public CreateFunction(boolean orReplace, List<String> functionDeclarationParts) {
        super(orReplace, "FUNCTION", functionDeclarationParts);
    }

    /**
     * @return whether the function is declared with {@code AGGREGATE}, as a MySQL loadable
     *         aggregate function is
     */
    public boolean isAggregate() {
        return aggregate;
    }

    public void setAggregate(boolean aggregate) {
        this.aggregate = aggregate;
    }

    public CreateFunction withAggregate(boolean aggregate) {
        setAggregate(aggregate);
        return this;
    }

    /**
     * @return the definition of a MySQL loadable function, or {@code null} for any other function
     */
    public LoadableFunction getLoadableFunction() {
        return loadableFunction;
    }

    /**
     * Makes this statement a MySQL loadable function. The definition replaces the declaration parts
     * and the characteristics; {@link #getFunctionDeclarationParts()} is derived from it.
     *
     * @param loadableFunction the definition, or {@code null} to remove it
     */
    public void setLoadableFunction(LoadableFunction loadableFunction) {
        if (loadableFunction != null) {
            super.setFunctionDeclarationParts(null, Collections.emptyList());
        }
        this.loadableFunction = loadableFunction;
    }

    public CreateFunction withLoadableFunction(LoadableFunction loadableFunction) {
        setLoadableFunction(loadableFunction);
        return this;
    }

    @Override
    protected String getKindKeywords() {
        return aggregate ? "AGGREGATE " + getKind() : getKind();
    }

    /**
     * @return the declaration parts; for a loadable function, a snapshot of
     *         {@link #getLoadableFunction()}
     */
    @Override
    public List<String> getFunctionDeclarationParts() {
        return loadableFunction != null ? loadableFunction.toDeclarationParts()
                : super.getFunctionDeclarationParts();
    }

    /** Replaces the declaration parts, which also removes a loadable function definition. */
    @Override
    public void setFunctionDeclarationParts(List<String> functionDeclarationParts,
            List<RoutineCharacteristic> characteristics) {
        loadableFunction = null;
        super.setFunctionDeclarationParts(functionDeclarationParts, characteristics);
    }

    @Override
    public CreateFunction withFunctionDeclarationParts(List<String> functionDeclarationParts) {
        return (CreateFunction) super.withFunctionDeclarationParts(functionDeclarationParts);
    }

    @Override
    public CreateFunction addFunctionDeclarationParts(String... functionDeclarationParts) {
        return (CreateFunction) super.addFunctionDeclarationParts(functionDeclarationParts);
    }

    @Override
    public CreateFunction addFunctionDeclarationParts(Collection<String> functionDeclarationParts) {
        return (CreateFunction) super.addFunctionDeclarationParts(functionDeclarationParts);
    }

}
