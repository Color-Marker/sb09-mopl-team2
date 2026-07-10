package com.sb09.sb09moplteam2.content.batch.sport;

import com.sb09.sb09moplteam2.content.batch.sport.dto.SportsEventResponse;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;

@Slf4j
@RequiredArgsConstructor
public class SportReader implements ItemReader<SportsEventResponse> {

  private final SportClient sportClient;

  private static final List<String> LEAGUE_IDS = List.of("4328", "4387", "4424", "4429");

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

    // 다음 리그 또는 타입으로 이동
    while (leagueIndex < LEAGUE_IDS.size()) {
      String leagueId = LEAGUE_IDS.get(leagueIndex);

      if (!fetchedNext) {
        buffer = new ArrayList<>(sportClient.fetchNextEvents(leagueId));
        fetchedNext = true;
        index = 0;
        if (!buffer.isEmpty()) return buffer.get(index++);
      }

      if (!fetchedPast) {
        buffer = new ArrayList<>(sportClient.fetchPastEvents(leagueId));
        fetchedPast = true;
        index = 0;
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