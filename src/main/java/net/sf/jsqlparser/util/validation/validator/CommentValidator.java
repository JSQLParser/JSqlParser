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
            case TRIGGER:
                return Feature.commentOnTrigger;
            case POLICY:
                return Feature.commentOnPolicy;
            case RULE:
                return Feature.commentOnRule;
            case PROCEDURE:
                return Feature.commentOnProcedure;
            case ROUTINE:
                return Feature.commentOnRoutine;
            case AGGREGATE:
                return Feature.commentOnAggregate;
            case CAST:
                return Feature.commentOnCast;
            case COLLATION:
                return Feature.commentOnCollation;
            case FOREIGN_TABLE:
                return Feature.commentOnForeignTable;
            case STATISTICS:
                return Feature.commentOnStatistics;
            case DATABASE:
                return Feature.commentOnDatabase;
            case ROLE:
                return Feature.commentOnRole;
            case TABLESPACE:
                return Feature.commentOnTablespace;
            case ACCESS_METHOD:
                return Feature.commentOnAccessMethod;
            case EXTENSION:
                return Feature.commentOnExtension;
            case FOREIGN_DATA_WRAPPER:
                return Feature.commentOnForeignDataWrapper;
            case LANGUAGE:
                return Feature.commentOnLanguage;
            case SERVER:
                return Feature.commentOnServer;
            case EVENT_TRIGGER:
                return Feature.commentOnEventTrigger;
            case PUBLICATION:
                return Feature.commentOnPublication;
            case SUBSCRIPTION:
                return Feature.commentOnSubscription;
            default:
                throw new IllegalArgumentException("Unknown COMMENT target: " + kind);
        }
    }
}
