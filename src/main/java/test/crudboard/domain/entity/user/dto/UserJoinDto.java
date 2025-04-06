package test.crudboard.domain.entity.user.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserJoinDto {
    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이어야 합니다")
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다")
    private final String password;

    @NotBlank(message = "필수 항목입니다.")
    private String nickname;
}
