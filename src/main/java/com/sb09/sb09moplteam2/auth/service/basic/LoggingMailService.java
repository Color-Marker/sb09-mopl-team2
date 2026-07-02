package com.sb09.sb09moplteam2.auth.service.basic;

import com.sb09.sb09moplteam2.auth.service.MailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class LoggingMailService implements MailService {

  @Override
  public void sendTemporaryPassword(String email, String temporaryPassword) {
    log.info("[임시 비밀번호 발송] to={}, tempPassword={}", email, temporaryPassword);
  }
}