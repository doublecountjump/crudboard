package test.crudboard.domain.error;


import lombok.Getter;

@Getter
public enum ErrorCode {

    JWT_TOKEN_IS_EXPIRED(401,"토큰이 만료되었습니다.");


    private int code;
    private String message;

    ErrorCode(int code, String message){
        this.code = code;
        this.message = message;
    }

}
