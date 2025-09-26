package test.crudboard.domain.error;


import lombok.Getter;

@Getter
public enum ErrorCode {

    JWT_TOKEN_HAS_EXPIRED(401,"토큰이 만료되었습니다."),
    NO_DATA_EXISTS_IN_CACHE(404, "캐시에 데이터가 존재하지 않습니다."),
    INSUFFICIENT_DATA_IN_CACHE(422, "캐시의 데이터가 부족합니다."),
    USER_HAS_ALREADY_LIKED_POST(409, "이미 추천한 게시글입니다.");

    private int code;
    private String message;

    ErrorCode(int code, String message){
        this.code = code;
        this.message = message;
    }

}
