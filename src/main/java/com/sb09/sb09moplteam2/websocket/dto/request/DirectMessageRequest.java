package com.sb09.sb09moplteam2.websocket.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DirectMessageRequest(

    @NotBlank(message = "메시지 내용은 필수입니다.")
    @Size(max = 2000, message = "메시지는 2000자를 초과할 수 없습니다.")
    String content
) {
}
