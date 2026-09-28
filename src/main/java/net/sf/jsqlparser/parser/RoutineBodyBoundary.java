/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.parser;

import static net.sf.jsqlparser.parser.CCJSqlParserConstants.*;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Finds the boundary of an opaque routine without interpreting its statements. In particular,
 * expression CASE and procedural END qualifiers must not close an enclosing BEGIN block.
 */
final class RoutineBodyBoundary {
    private enum Block {
        BEGIN, IF, CASE_STATEMENT, CASE_EXPRESSION, LOOP, WHILE, REPEAT
    }

    private final Deque<Block> blocks = new ArrayDeque<>();
    private int parentheses;
    private int previousKind = -1;
    private int statementKind = -1;
    private boolean hasParameters;
    private boolean standardReturns;
    private boolean returnTypeName;
    private boolean afterDot;
    private boolean declarations;
    private boolean bodyStarted;
    private boolean compoundBody;
    private boolean quotedBody;
    private boolean completed;
    private boolean handlerAction;
    private boolean statementStart = true;

    boolean endsAt(Token token) throws ParseException {
        if (token.kind == EOF) {
            if (!blocks.isEmpty()) {
                throw new ParseException("Unterminated routine block");
            }
            return true;
        }
        return token.kind == ST_SEMICOLON && parentheses == 0 && blocks.isEmpty()
                && !declarations && (hasParameters || quotedBody || completed);
    }

    boolean startsQuotedBody(Token token, Token next) {
        return parentheses == 0 && !compoundBody && !quotedBody && token.kind == K_AS
                && next.kind == S_CHAR_LITERAL
                && (!bodyStarted || statementKind == K_RETURN || statementKind == K_SET);
    }

    void quotedBody() {
        quotedBody = true;
        bodyStarted = true;
        declarations = false;
    }

    void accept(Token token, Token next) {
        int kind = token.kind;
        if (kind == OPENING_BRACKET) {
            parentheses++;
        } else if (kind == CLOSING_BRACKET) {
            if (--parentheses == 0 && !bodyStarted) {
                hasParameters = true;
            }
        } else if (parentheses == 0 && !quotedBody && !completed) {
            acceptTopLevel(token, next);
        }
        previousKind = kind;
        afterDot = ".".equals(token.image);
    }

    private void acceptTopLevel(Token token, Token next) {
        if (afterDot) {
            return; // Qualified type/column names may use otherwise significant keywords.
        }
        // END IF/CASE/LOOP/WHILE/REPEAT is one terminator, not another opener.
        if (previousKind == K_END && isEndQualifier(token)) {
            return;
        }
        if (!acceptRoutineHeader(token)) {
            acceptBodyToken(token, next);
        }
    }

    private boolean acceptRoutineHeader(Token token) {
        int kind = token.kind;
        if (kind == K_RETURNS && !bodyStarted) {
            standardReturns = true;
            returnTypeName = true;
            return true;
        }
        if (returnTypeName) {
            returnTypeName = keyword(token, "SETOF");
            return true;
        }
        if (!compoundBody && (kind == K_AS || kind == K_IS)
                && (!bodyStarted || statementKind == K_RETURN && !standardReturns
                        || statementKind == K_SET)) {
            declarations = true;
            bodyStarted = false;
            return true;
        }
        return false;
    }

    private void acceptBodyToken(Token token, Token next) {
        int kind = token.kind;
        if (acceptStatementBoundary(kind)) {
            return;
        }
        if (!(hasParameters || declarations || bodyStarted)) {
            return;
        }
        if (handlerAction && isSimpleStatementStart(kind)) {
            startStatement();
        }
        if (acceptBeginOrCase(kind) || declarations || acceptControlFlow(token)) {
            return;
        }
        acceptStatementToken(token, next);
    }

    private boolean acceptStatementBoundary(int kind) {
        if (kind == ST_SEMICOLON) {
            startStatement();
            return true;
        }
        if (kind == K_END) {
            if (!blocks.isEmpty()) {
                Block closed = blocks.pop();
                completed = blocks.isEmpty() && closed != Block.CASE_EXPRESSION;
            } else {
                // Retain legacy opaque declarations without a parameter list or BEGIN.
                completed = true;
            }
            statementStart = false;
            return true;
        }
        return false;
    }

    private boolean acceptBeginOrCase(int kind) {
        if (kind == K_BEGIN
                && (!bodyStarted || statementStart || statementKind == K_DECLARE)) {
            blocks.push(Block.BEGIN);
            declarations = false;
            compoundBody = true;
            bodyStarted = true;
            startStatement();
            return true;
        }
        if (kind == K_CASE) {
            boolean statementCase = !declarations && (statementStart || handlerAction);
            blocks.push(statementCase ? Block.CASE_STATEMENT : Block.CASE_EXPRESSION);
            if (statementCase) {
                bodyStarted = true;
                compoundBody = true;
                handlerAction = false;
            }
            statementStart = false;
            return true;
        }
        return false;
    }

    private boolean acceptControlFlow(Token token) {
        int kind = token.kind;
        Block block = controlBlock(token);
        if ((statementStart || handlerAction) && block != null) {
            blocks.push(block);
            bodyStarted = true;
            compoundBody = true;
            handlerAction = false;
            statementKind = kind;
            statementStart = block == Block.LOOP || block == Block.REPEAT;
            return true;
        }
        if (acceptOracleLoop(kind)) {
            return true;
        }
        if (isStatementBranch(kind)) {
            startStatement();
            return true;
        }
        return false;
    }

    private boolean acceptOracleLoop(int kind) {
        if (kind == K_LOOP && (statementKind == K_FOR || blocks.peek() == Block.WHILE)) {
            // Oracle FOR/WHILE ... LOOP shares the same END LOOP boundary.
            if (blocks.peek() != Block.WHILE) {
                blocks.push(Block.LOOP);
            }
            startStatement();
            return true;
        }
        return false;
    }

    private boolean isStatementBranch(int kind) {
        return (kind == K_THEN || kind == K_ELSE) && !blocks.isEmpty()
                && blocks.peek() != Block.CASE_EXPRESSION
                || kind == K_DO && blocks.peek() == Block.WHILE;
    }

    private void acceptStatementToken(Token token, Token next) {
        int kind = token.kind;
        if (isStatementPrefix(token, next)) {
            return;
        }
        if (!bodyStarted) {
            // SET may also be a PostgreSQL header option; AS remains recognizable above.
            if (!isSimpleStatementStart(kind)) {
                return;
            }
            bodyStarted = true;
        }
        if (statementStart) {
            statementKind = kind;
        }
        if (statementKind == K_DECLARE && keyword(token, "HANDLER")) {
            handlerAction = true;
        }
        statementStart = false;
    }

    private boolean isStatementPrefix(Token token, Token next) {
        // ATOMIC and a label precede the statement they qualify.
        return previousKind == K_BEGIN && keyword(token, "ATOMIC")
                || statementStart && (":".equals(next.image) || ":".equals(token.image));
    }

    private static boolean isEndQualifier(Token token) {
        return token.kind == K_CASE || controlBlock(token) != null;
    }

    private static Block controlBlock(Token token) {
        if (token.kind == K_IF) {
            return Block.IF;
        }
        if (token.kind == K_LOOP) {
            return Block.LOOP;
        }
        if (keyword(token, "WHILE")) {
            return Block.WHILE;
        }
        return keyword(token, "REPEAT") ? Block.REPEAT : null;
    }

    private void startStatement() {
        statementStart = true;
        statementKind = -1;
        handlerAction = false;
    }

    private static boolean keyword(Token token, String keyword) {
        return token.kind == S_IDENTIFIER && keyword.equalsIgnoreCase(token.image);
    }

    private static boolean isSimpleStatementStart(int kind) {
        return kind == K_RETURN || kind == K_SELECT || kind == K_INSERT || kind == K_UPDATE
                || kind == K_DELETE || kind == K_REPLACE || kind == K_SET || kind == K_CALL
                || kind == K_WITH || kind == K_DO;
    }
}
