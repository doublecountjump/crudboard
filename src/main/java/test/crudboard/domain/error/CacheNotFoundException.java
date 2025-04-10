package test.crudboard.domain.error;


import lombok.Getter;

@Getter
public class CacheNotFoundException extends RuntimeException{

    private final ErrorCode errorCode;

    public CacheNotFoundException(ErrorCode e){
        super(e.getMessage());
        this.errorCode = e;
    }
}
