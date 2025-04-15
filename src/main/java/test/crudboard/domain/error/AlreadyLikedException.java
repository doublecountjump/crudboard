package test.crudboard.domain.error;



public class AlreadyLikedException extends RuntimeException {

    private final ErrorCode errorCode;

    public AlreadyLikedException(ErrorCode e) {
        super(e.getMessage());
        this.errorCode = e;
    }
}