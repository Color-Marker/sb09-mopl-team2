package com.sb09.sb09moplteam2.auth.service;

// 이후 실제 메일 발송 인프라 구성 예정
public interface MailService {

  void sendTemporaryPassword(String email, String temporaryPassword);
}