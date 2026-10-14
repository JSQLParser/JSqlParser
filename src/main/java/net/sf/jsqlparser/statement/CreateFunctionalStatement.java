/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2020 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.sf.jsqlparser.statement.create.function.FunctionReturnType;
import net.sf.jsqlparser.statement.create.table.TableElement;

/**
 * A base for the declaration of function like statements
 */
public abstract class CreateFunctionalStatement implements Statement {

    private String kind;
    private boolean orReplace = false;

    public enum Operation {
        CREATE, ALTER, CREATE_OR_ALTER
    }

    private Operation operation = Operation.CREATE;
    private MySqlDefiner definer;
    private FunctionReturnType returnType;
    private List<String> routineBodyParts;

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    /**
     * The MySQL {@code DEFINER} account, rendered after {@code CREATE [OR REPLACE]}.
     *
     * @return the definer, or {@code null}
     */
    public MySqlDefiner getDefiner() {
        return definer;
    }

    public void setDefiner(MySqlDefiner definer) {
        this.definer = definer;
    }

    public FunctionReturnType getReturnType() {
        return returnType;
    }

    public void setReturnType(FunctionReturnType returnType) {
        this.returnType = returnType;
    }

    public List<String> getRoutineBodyParts() {
        return routineBodyParts;
    }

    public void setRoutineBodyParts(List<String> parts) {
        routineBodyParts = parts;
    }


    private List<String> functionDeclarationParts;

    protected CreateFunctionalStatement(String kind) {
        this.kind = kind;
    }

    protected CreateFunctionalStatement(String kind, List<String> functionDeclarationParts) {
        this(false, kind, functionDeclarationParts);
    }

    protected CreateFunctionalStatement(boolean orReplace, String kind,
            List<String> functionDeclarationParts) {
        this.orReplace = orReplace;
        this.kind = kind;
        this.functionDeclarationParts = functionDeclarationParts;
    }

    /**
     * @return the declaration parts after {@code CREATE FUNCTION|PROCEDURE}. For a SQL Server
     *         function with a structured {@link #getReturnType()}, these are the name and parameter
     *         tokens before RETURNS; {@link #getRoutineBodyParts()} holds the remaining tokens.
     */
    public List<String> getFunctionDeclarationParts() {
        return functionDeclarationParts;
    }

    public void setFunctionDeclarationParts(List<String> functionDeclarationParts) {
        this.functionDeclarationParts = functionDeclarationParts;
    }

    /**
     * @return the kind of functional statement
     */
    public String getKind() {
        return kind;
    }

    public void setOrReplace(boolean orReplace) {
        this.orReplace = orReplace;
    }

    /**
     * @return a whitespace appended String with the declaration parts with some minimal formatting.
     */
    public String formatDeclaration() {
        StringBuilder builder = new StringBuilder();
        return appendDeclarationTo(builder, builder::append).toString();
    }

    private StringBuilder appendDeclarationTo(StringBuilder builder,
            Consumer<TableElement> printer) {
        appendTokens(builder, functionDeclarationParts);
        if (returnType != null) {
            builder.append(' ');
            returnType.appendTo(builder, printer);
            if (routineBodyParts != null && !routineBodyParts.isEmpty()) {
                builder.append(' ');
                appendTokens(builder, routineBodyParts);
            }
        }
        return builder;
    }

    private static void appendTokens(StringBuilder builder, List<String> tokens) {
        if (tokens == null) {
            return;
        }
        for (int i = 0; i < tokens.size(); i++) {
            if (i > 0 && !";".equals(tokens.get(i))) {
                builder.append(' ');
            }
            builder.append(tokens.get(i));
        }
    }

    public StringBuilder appendTo(StringBuilder builder, Consumer<TableElement> printer) {
        builder.append(operation.name().replace('_', ' ')).append(' ');
        if (orReplace && operation == Operation.CREATE) {
            builder.append("OR REPLACE ");
        }
        if (definer != null) {
            builder.append("DEFINER = ").append(definer).append(' ');
        }
        builder.append(kind).append(' ');
        return appendDeclarationTo(builder, printer);
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        return appendTo(builder, builder::append).toString();
    }

    public CreateFunctionalStatement withFunctionDeclarationParts(
            List<String> functionDeclarationParts) {
        this.setFunctionDeclarationParts(functionDeclarationParts);
        return this;
    }

    public CreateFunctionalStatement addFunctionDeclarationParts(
            String... functionDeclarationParts) {
        List<String> collection =
                Optional.ofNullable(getFunctionDeclarationParts()).orElseGet(ArrayList::new);
        Collections.addAll(collection, functionDeclarationParts);
        return this.withFunctionDeclarationParts(collection);
    }

    public CreateFunctionalStatement addFunctionDeclarationParts(
            Collection<String> functionDeclarationParts) {
        List<String> collection =
                Optional.ofNullable(getFunctionDeclarationParts()).orElseGet(ArrayList::new);
        collection.addAll(functionDeclarationParts);
        return this.withFunctionDeclarationParts(collection);
    }
}
