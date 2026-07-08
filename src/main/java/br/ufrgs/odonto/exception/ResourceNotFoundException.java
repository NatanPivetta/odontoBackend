package br.ufrgs.odonto.exception;

public class ResourceNotFoundException extends RuntimeException {

    private final ErrorCode errorCode;
    private final Object[] args;

    public ResourceNotFoundException(String message) {
        super(message);
        this.errorCode = ErrorCode.ERROR_RESOURCE_NOT_FOUND;
        this.args = new Object[] { message };
    }

    public ResourceNotFoundException(ErrorCode errorCode, Object... args) {
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
