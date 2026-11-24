package br.ufrgs.odonto.exception;

public enum ErrorCode {
    ERROR_ACCESS_DENIED("error.access.denied"),
    ERROR_AUTH_INVALID_CREDENTIALS("error.auth.invalid.credentials"),
    ERROR_BUSINESS_RULE_VIOLATION("error.business.rule.violation"),
    ERROR_INTERNAL_SERVER("error.internal.server"),
    ERROR_RESOURCE_NOT_FOUND("error.resource.not.found"),
    ERROR_VALIDATION("error.validation"),

    ERROR_ACTIVITY_NOT_FOUND("error.activity.not.found"),
    ERROR_ACTIVITY_ALTA_IMMUTABLE("error.activity.alta.immutable"),
    ERROR_ACTIVITY_ALTA_PROFESSOR_ONLY("error.activity.alta.professor.only"),
    ERROR_ACTIVITY_ALTA_REQUIRES_CONCLUIDA("error.activity.alta.requires.concluida"),
    ERROR_ACTIVITY_COMPLETION_DATE_FUTURE("error.activity.completion.date.future"),
    ERROR_ACTIVITY_TYPE_DESCRIPTION_REQUIRED("error.activity.type.description.required"),

    ERROR_EMAIL_ALREADY_REGISTERED("error.email.already.registered"),
    ERROR_CARD_NUMBER_ALREADY_REGISTERED("error.card.number.already.registered"),
    ERROR_VERIFICATION_CODE_NOT_FOUND("error.verification.code.not.found"),
    ERROR_VERIFICATION_CODE_USED("error.verification.code.used"),
    ERROR_VERIFICATION_CODE_EXPIRED("error.verification.code.expired"),
    ERROR_VERIFICATION_CODE_INVALID("error.verification.code.invalid"),

    ERROR_FEEDBACK_NOT_FOUND("error.feedback.not.found"),

    ERROR_STUDENT_ALREADY_ENROLLED("error.student.already.enrolled"),
    ERROR_STUDENT_NOT_ENROLLED("error.student.not.enrolled"),
    ERROR_STUDENT_WITHOUT_ACTIVE_TURMA("error.student.without.active.turma"),

    ERROR_TURMA_CLOSED_ENROLLMENT("error.turma.closed.enrollment"),
    ERROR_TURMA_NOT_FOUND("error.turma.not.found"),

    ERROR_USER_NOT_FOUND("error.user.not.found"),
    ERROR_USER_NOT_STUDENT("error.user.not.student"),
    ERROR_USER_NOT_PROFESSOR("error.user.not.professor");

    private final String messageKey;

    ErrorCode(String messageKey) {
        this.messageKey = messageKey;
    }

    public String getMessageKey() {
        return messageKey;
    }
}
