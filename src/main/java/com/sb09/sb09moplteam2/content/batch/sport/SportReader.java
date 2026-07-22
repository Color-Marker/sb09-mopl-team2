package com.sb09.sb09moplteam2.content.batch.sport;

import com.sb09.sb09moplteam2.content.batch.sport.dto.SportsEventResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;

@Slf4j
@RequiredArgsConstructor
public class SportReader implements ItemReader<SportsEventResponse> {

  private final SportClient sportClient;

  private static final List<String> LEAGUE_IDS = List.of("4328", "4387", "4424", "4429","4830");

  private static final Map<String, String> LEAGUE_SEASONS = Map.of(
      "4328", "2025-2026",  // English Premier League
      "4387", "2025-2026",      // NBA
      "4424", "2026",           // MLB
      "4429", "2026",           // FIFA World Cup
      "4830", "2026"                // KBO League
  );

  private List<SportsEventResponse> buffer = new ArrayList<>();
  private int index = 0;
  private int leagueIndex = 0;
  private boolean fetchedNext = false;
  private boolean fetchedPast = false;

  @Override
  public SportsEventResponse read() {
    if (index < buffer.size()) {
      return buffer.get(index++);
    }

    while (leagueIndex < LEAGUE_IDS.size()) {
      String leagueId = LEAGUE_IDS.get(leagueIndex);

      if (!fetchedNext) {
        buffer = new ArrayList<>(sportClient.fetchNextEvents(leagueId));
        fetchedNext = true;
        index = 0;
        log.info("리그 {} 다음 경기 조회 완료 - {}건", leagueId, buffer.size());
        if (!buffer.isEmpty()) return buffer.get(index++);
      }

      if (!fetchedPast) {
        String season = LEAGUE_SEASONS.get(leagueId);
        buffer = new ArrayList<>(sportClient.fetchPastEvents(leagueId, season));
        fetchedPast = true;
        index = 0;
        log.info("리그 {} 시즌 {} 과거 경기 조회 완료 - {}건", leagueId, season, buffer.size());
        if (!buffer.isEmpty()) return buffer.get(index++);
      }

      // 다음 리그로
      leagueIndex++;
      fetchedNext = false;
      fetchedPast = false;
    }

    return null;
  }
}