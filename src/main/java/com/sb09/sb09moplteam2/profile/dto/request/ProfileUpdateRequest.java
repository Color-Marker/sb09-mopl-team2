package com.sb09.sb09moplteam2.profile.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

public record ProfileUpdateRequest(
    @NotBlank(message = "이름은 필수 입력값입니다.")
    @Size(min = 2, max = 20, message = "이름은 2~20자 사이여야 합니다.")
    String name,

    @URL(message = "프로필 사진은 올바른 URL 형식이어야 합니다.")
    String profileImageUrl
) {}
