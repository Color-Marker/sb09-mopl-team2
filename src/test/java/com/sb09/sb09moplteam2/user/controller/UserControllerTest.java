package com.sb09.sb09moplteam2.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sb09.sb09moplteam2.exception.GlobalExceptionHandler;
import com.sb09.sb09moplteam2.exception.user.DuplicateEmailException;
import com.sb09.sb09moplteam2.user.dto.data.UserDto;
import com.sb09.sb09moplteam2.user.entity.Role;
import com.sb09.sb09moplteam2.user.service.UserService;
import com.sb09.sb09moplteam2.config.QueryDslConfig;
import com.sb09.sb09moplteam2.config.JpaAuditingConfig;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    controllers = UserController.class,
    excludeFilters = @Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = {QueryDslConfig.class, JpaAuditingConfig.class}
    )
)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private UserService userService;

  @Test
  void 회원가입_성공시_201과_사용자정보를_반환한다() throws Exception {
    UserDto response = new UserDto(
        UUID.randomUUID(),
        Instant.now(),
        "woody@mopl.io",
        "우디",
        null,
        Role.USER,
        false
    );

    given(userService.createUser(any())).willReturn(response);

    String body = """
                {
                  "name": "우디",
                  "email": "woody@mopl.io",
                  "password": "mopl1!@#$"
                }
                """;

    mockMvc.perform(post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.email").value("woody@mopl.io"))
        .andExpect(jsonPath("$.name").value("우디"));
  }

  @Test
  void 이메일이_중복되면_409를_반환한다() throws Exception {
    given(userService.createUser(any())).willThrow(DuplicateEmailException.withEmail("woody@mopl.io"));

    String body = """
                {
                  "name": "우디",
                  "email": "woody@mopl.io",
                  "password": "mopl1!@#$"
                }
                """;

    mockMvc.perform(post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isConflict());
  }

  @Test
  void 입력값이_유효하지_않으면_400을_반환한다() throws Exception {
    String body = """
                {
                  "name": "",
                  "email": "invalid-email",
                  "password": "1234567"
                }
                """;

    mockMvc.perform(post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isBadRequest());
  }
}