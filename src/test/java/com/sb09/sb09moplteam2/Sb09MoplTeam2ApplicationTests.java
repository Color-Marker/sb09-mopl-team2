package com.sb09.sb09moplteam2;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("테스트 환경에 Redis/Kafka 브로커 없음. 전체 컨텍스트 로딩 테스트는 스킵")
class Sb09MoplTeam2ApplicationTests {

  @Test
  void contextLoads() {
  }

}
