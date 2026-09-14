/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.validation.validator;

import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.statement.comment.Comment;
import net.sf.jsqlparser.statement.comment.CommentTarget;
import net.sf.jsqlparser.util.validation.ValidationCapability;

public class CommentValidator extends AbstractValidator<Comment> {
    @Override
    public void validate(Comment comment) {
        for (ValidationCapability capability : getCapabilities()) {
            validateFeature(capability, Feature.comment);
            validateOptionalFeature(capability, comment.getTable(), Feature.commentOnTable);
            validateOptionalFeature(capability, comment.getColumn(), Feature.commentOnColumn);
            validateOptionalFeature(capability, comment.getView(), Feature.commentOnView);
            if (comment.getTarget() != null) {
                validateFeature(capability, targetFeature(comment.getTarget().getKind()));
            }
        }
    }

    private static Feature targetFeature(CommentTarget.Kind kind) {
        switch (kind) {
            case INDEX:
                return Feature.commentOnIndex;
            case SCHEMA:
                return Feature.commentOnSchema;
            case SEQUENCE:
                return Feature.commentOnSequence;
            case DOMAIN:
                return Feature.commentOnDomain;
            case TYPE:
                return Feature.commentOnType;
            case MATERIALIZED_VIEW:
                return Feature.commentOnMaterializedView;
            case FUNCTION:
                return Feature.commentOnFunction;
            case CONSTRAINT:
                return Feature.commentOnConstraint;
            default:
                throw new IllegalArgumentException("Unknown COMMENT target: " + kind);
        }
    }
}
