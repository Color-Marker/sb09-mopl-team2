package com.sb09.sb09moplteam2.auth.service.basic;

import com.sb09.sb09moplteam2.auth.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "mopl.mail.type", havingValue = "smtp")
public class SmtpMailService implements MailService {

  @Lazy
  private final JavaMailSender mailSender;

  @Override
  public void sendTemporaryPassword(String email, String temporaryPassword) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo(email);
    message.setSubject("[모두의 플리] 임시 비밀번호 안내");
    message.setText("임시 비밀번호: " + temporaryPassword
        + "\n\n로그인 후 반드시 비밀번호를 변경해주세요.\n임시 비밀번호는 3분간 유효합니다.");
    mailSender.send(message);
  }
}