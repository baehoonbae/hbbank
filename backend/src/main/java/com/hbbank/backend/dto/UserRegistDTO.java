package com.hbbank.backend.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserRegistDTO {
    @NotBlank(message = "이름은 필수입니다")
    private final String name;
    
    @NotNull(message = "생년월일은 필수입니다")
    @Past(message = "생년월일은 과거 날짜여야 합니다")
    private final LocalDate birth;
    
    @NotBlank(message = "아이디는 필수입니다")
    @Size(min = 4, max = 20)
    @Pattern(regexp = "^[a-zA-Z0-9]+$")
    private final String username;
    
    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min = 8, max = 100, message = "비밀번호는 8자 이상이어야 합니다")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$", 
            message = "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다")
    private final String password;
    
    @NotBlank(message = "주소는 필수입니다")
    private final String address;
    
    @NotBlank(message = "전화번호는 필수입니다")
    @Pattern(regexp = "^01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}$")
    private final String phone;
    
    @NotBlank(message = "이메일은 필수입니다")
    @Email
    private final String email;
}
