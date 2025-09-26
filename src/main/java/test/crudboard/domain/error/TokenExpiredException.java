package test.crudboard.domain.error;


import lombok.Getter;


@Getter
public class TokenExpiredException extends RuntimeException{

    private final ErrorCode errorCode;

    public TokenExpiredException(ErrorCode e){
        super(e.getMessage());
        this.errorCode = e;
    }
}
