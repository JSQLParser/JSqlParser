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

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Recognizes the BigQuery PATTERN lexical region even during JavaCC lookahead. The required initial
 * ORDER BY (or optional PARTITION BY) distinguishes a clause from an ordinary function named
 * MATCH_RECOGNIZE. Quoted tokens never change this state.
 */
final class RowPatternTokenContext implements CCJSqlParserConstants {
    private final Deque<Frame> frames = new ArrayDeque<>();
    private int depth;
    private boolean pendingClause;

    private static final class Frame {
        private final int depth;
        private int phase;
        private int patternDepth = -1;
        private boolean pendingPattern;
        private boolean patternComplete;

        private Frame(int depth) {
            this.depth = depth;
        }
    }

    boolean isIdle() {
        return frames.isEmpty() && !pendingClause;
    }

    boolean isInPattern() {
        return !frames.isEmpty() && frames.peek().patternDepth >= 0;
    }

    void accept(Token token) {
        if (token.kind == OPENING_BRACKET) {
            depth++;
            if (pendingClause) {
                frames.push(new Frame(depth));
                pendingClause = false;
                return;
            }
            if (!frames.isEmpty() && frames.peek().pendingPattern) {
                frames.peek().patternDepth = depth;
                frames.peek().pendingPattern = false;
            }
        } else if (token.kind == CLOSING_BRACKET) {
            if (!frames.isEmpty()) {
                Frame frame = frames.peek();
                if (frame.patternDepth == depth) {
                    frame.patternDepth = -1;
                    frame.patternComplete = true;
                }
                if (frame.depth == depth) {
                    frames.pop();
                }
            }
            depth--;
        } else if (!frames.isEmpty() && depth == frames.peek().depth) {
            Frame frame = frames.peek();
            if (frame.phase == 0) {
                frame.phase = token.kind == K_ORDER || token.kind == K_PARTITION ? 1 : -1;
            } else if (frame.phase == 1) {
                frame.phase = token.kind == K_BY ? 2 : -1;
            } else if (frame.phase == 2 && !frame.patternComplete) {
                frame.pendingPattern = token.kind == S_IDENTIFIER
                        && "PATTERN".equalsIgnoreCase(token.image);
            }
        }
        pendingClause = token.kind == S_IDENTIFIER
                && "MATCH_RECOGNIZE".equalsIgnoreCase(token.image);
    }
}
