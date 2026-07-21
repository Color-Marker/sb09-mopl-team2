package com.sb09.sb09moplteam2.batch.sport;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.sb09.sb09moplteam2.content.batch.sport.SportClient;
import com.sb09.sb09moplteam2.content.batch.sport.SportReader;
import com.sb09.sb09moplteam2.content.batch.sport.dto.SportsEventResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SportReaderTest {

  @Mock
  private SportClient sportClient;

  @Test
  void 첫번째_리그의_다음_경기를_먼저_반환한다() {

    SportsEventResponse next = new SportsEventResponse(
        "1", "A vs B", "설명", "thumb.jpg", "2026-08-01", "축구");
    given(sportClient.fetchNextEvents("4328")).willReturn(List.of(next));

    SportReader reader = new SportReader(sportClient);

    SportsEventResponse result = reader.read();

    assertThat(result).isEqualTo(next);
  }

  @Test
  void 한_리그에_여러_경기가_있으면_순차적으로_반환한다() {
    SportsEventResponse e1 = new SportsEventResponse(
        "1", "A vs B", "설명1", "thumb.jpg", "2026-08-01", "축구");
    SportsEventResponse e2 = new SportsEventResponse(
        "2", "C vs D", "설명2", "thumb.jpg", "2026-08-02", "축구");
    given(sportClient.fetchNextEvents("4328")).willReturn(List.of(e1, e2));

    SportReader reader = new SportReader(sportClient);

    assertThat(reader.read()).isEqualTo(e1);
    assertThat(reader.read()).isEqualTo(e2);
  }

  @Test
  void 다음_경기_버퍼를_모두_읽으면_시즌_경기를_읽는다() {
    // given
    SportsEventResponse next = new SportsEventResponse(
        "1", "A vs B", "설명", "thumb.jpg", "2026-08-01", "축구");
    SportsEventResponse past = new SportsEventResponse(
        "2", "C vs D", "설명", "thumb.jpg", "2026-01-01", "축구");
    given(sportClient.fetchNextEvents("4328")).willReturn(List.of(next));
    given(sportClient.fetchPastEvents("4328", "2025-2026")).willReturn(List.of(past));

    SportReader reader = new SportReader(sportClient);

    reader.read(); // next 소비
    SportsEventResponse result = reader.read();

    assertThat(result).isEqualTo(past);
  }

  @Test
  void 첫번째_리그가_비어있으면_다음_리그로_넘어간다() {
    // given
    given(sportClient.fetchNextEvents("4328")).willReturn(List.of());
    given(sportClient.fetchPastEvents("4328", "2025-2026")).willReturn(List.of());
    SportsEventResponse next = new SportsEventResponse(
        "3", "E vs F", "설명", "thumb.jpg", "2026-08-03", "축구");
    given(sportClient.fetchNextEvents("4387")).willReturn(List.of(next));

    SportReader reader = new SportReader(sportClient);

    SportsEventResponse result = reader.read();

    assertThat(result).isEqualTo(next);
  }

  @Test
  void 모든_리그를_다_읽으면_null을_반환한다() {
    given(sportClient.fetchNextEvents("4328")).willReturn(List.of());
    given(sportClient.fetchPastEvents("4328", "2025-2026")).willReturn(List.of());
    given(sportClient.fetchNextEvents("4387")).willReturn(List.of());
    given(sportClient.fetchPastEvents("4387", "2025-2026")).willReturn(List.of());
    given(sportClient.fetchNextEvents("4424")).willReturn(List.of());
    given(sportClient.fetchPastEvents("4424", "2026")).willReturn(List.of());
    given(sportClient.fetchNextEvents("4429")).willReturn(List.of());
    given(sportClient.fetchPastEvents("4429", "2026")).willReturn(List.of());
    given(sportClient.fetchNextEvents("4830")).willReturn(List.of());
    given(sportClient.fetchPastEvents("4830", "2026")).willReturn(List.of());

    SportReader reader = new SportReader(sportClient);

    SportsEventResponse result = reader.read();

    assertThat(result).isNull();
  }
}