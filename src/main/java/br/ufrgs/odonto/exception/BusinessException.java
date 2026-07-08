package br.ufrgs.odonto.exception;

public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;
    private final Object[] args;

    public BusinessException(String message) {
        super(message);
        this.errorCode = ErrorCode.ERROR_BUSINESS_RULE_VIOLATION;
        this.args = new Object[] { message };
    }

    public BusinessException(ErrorCode errorCode, Object... args) {
        super(errorCode.name());
        this.errorCode = errorCode;
        this.args = args;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public Object[] getArgs() {
        return args;
    }
}
