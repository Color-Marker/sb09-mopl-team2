package com.sb09.sb09moplteam2.content.batch.sport;


import com.sb09.sb09moplteam2.content.batch.sport.dto.SportsEventResponse;
import com.sb09.sb09moplteam2.content.batch.sport.dto.SportsPageResponse;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
@Slf4j
public class SportClient {

  private final RestClient restClient;
  private final SportProperties sportProperties;

  private static final long MIN_INTERVAL_MILLIS = 2500;
  private final ReentrantLock rateLimitLock = new ReentrantLock();
  private volatile long lastRequestTime  = 0;

  public List<SportsEventResponse> fetchNextEvents(String leagueId) {
    return fetchEvents("/eventsnextleague.php?id=" + leagueId);
  }

  public List<SportsEventResponse> fetchPastEvents(String leagueId) {
    return fetchEvents("/eventspastleague.php?id=" + leagueId);
  }

  private List<SportsEventResponse> fetchEvents(String path) {
    awaitRateLimit();
    try {
      SportsPageResponse response = restClient.get()
          .uri(sportProperties.baseUrl() + "/" + sportProperties.key() + path)
          .retrieve()
          .body(SportsPageResponse.class);

      if (response == null || response.events() == null) {
        return List.of();
      }
      return response.events();
    } catch (Exception e) {
      log.error("TheSportsDB API 호출 실패: {}", path, e);
      return List.of();
    }
  }

  private void awaitRateLimit() {
    rateLimitLock.lock();
    try {
      long now = System.currentTimeMillis();
      long elapsed = now - lastRequestTime;
      if (elapsed < MIN_INTERVAL_MILLIS) {
        long waitTime = MIN_INTERVAL_MILLIS - elapsed;
        log.debug("The SportsDB 요청 속도 제한 대기 - {}ms", waitTime);
        try{
          Thread.sleep(waitTime);
        }catch(InterruptedException e){
          Thread.currentThread().interrupt();
        }
      }
      lastRequestTime = System.currentTimeMillis();
    } finally {
      rateLimitLock.unlock();
    }
  }
}