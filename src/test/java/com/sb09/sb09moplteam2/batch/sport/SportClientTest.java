package com.sb09.sb09moplteam2.batch.sport;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;

import com.sb09.sb09moplteam2.content.batch.sport.SportClient;
import com.sb09.sb09moplteam2.content.batch.sport.SportProperties;
import com.sb09.sb09moplteam2.content.batch.sport.dto.SportsEventResponse;
import com.sb09.sb09moplteam2.content.batch.sport.dto.SportsPageResponse;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"unchecked", "rawtypes"})
class SportClientTest {

  @Mock
  private RestClient restClient;

  @Mock
  private SportProperties sportProperties;

  @Mock
  private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

  @Mock
  private RestClient.RequestHeadersSpec requestHeadersSpec;

  @Mock
  private RestClient.ResponseSpec responseSpec;

  private SportClient sportClient;

  @BeforeEach
  void setUp() {
    sportClient = new SportClient(restClient, sportProperties);
  }

  @Test
  @DisplayName("다음 경기 목록을 정상적으로 조회한다")
  void fetchNextEvents_정상적으로_조회한다() {
    given(sportProperties.baseUrl()).willReturn("https://www.thesportsdb.com/api/v1/json");
    given(sportProperties.key()).willReturn("123");

    SportsEventResponse event = new SportsEventResponse(
        "1", "A vs B", "설명", "thumb.jpg", "2026-08-01", "축구");
    SportsPageResponse response = new SportsPageResponse(List.of(event));

    given(restClient.get()).willReturn(requestHeadersUriSpec);
    given(requestHeadersUriSpec.uri(anyString())).willReturn(requestHeadersSpec);
    given(requestHeadersSpec.retrieve()).willReturn(responseSpec);
    given(responseSpec.body(SportsPageResponse.class)).willReturn(response);

    List<SportsEventResponse> result = sportClient.fetchNextEvents("4328");

    assertThat(result).hasSize(1);
    assertThat(result.get(0).idEvent()).isEqualTo("1");
  }

  @Test
  @DisplayName("다음 경기 조회 시 올바른 URI를 사용한다")
  void fetchNextEvents_올바른_URI를_사용한다() {
    given(sportProperties.baseUrl()).willReturn("https://www.thesportsdb.com/api/v1/json");
    given(sportProperties.key()).willReturn("123");

    given(restClient.get()).willReturn(requestHeadersUriSpec);
    given(requestHeadersUriSpec.uri(anyString())).willReturn(requestHeadersSpec);
    given(requestHeadersSpec.retrieve()).willReturn(responseSpec);
    given(responseSpec.body(SportsPageResponse.class)).willReturn(new SportsPageResponse(List.of()));

    ArgumentCaptor<String> uriCaptor = ArgumentCaptor.forClass(String.class);

    sportClient.fetchNextEvents("4328");

    verify(requestHeadersUriSpec).uri(uriCaptor.capture());
    assertThat(uriCaptor.getValue())
        .isEqualTo("https://www.thesportsdb.com/api/v1/json/123/eventsnextleague.php?id=4328");
  }

  @Test
  @DisplayName("시즌 경기 목록을 정상적으로 조회한다")
  void fetchPastEvents_시즌_경기를_정상적으로_조회한다() {
    given(sportProperties.baseUrl()).willReturn("https://www.thesportsdb.com/api/v1/json");
    given(sportProperties.key()).willReturn("123");

    SportsEventResponse event = new SportsEventResponse(
        "2", "C vs D", "설명", "thumb.jpg", "2026-01-01", "축구");
    SportsPageResponse response = new SportsPageResponse(List.of(event));

    given(restClient.get()).willReturn(requestHeadersUriSpec);
    given(requestHeadersUriSpec.uri(anyString())).willReturn(requestHeadersSpec);
    given(requestHeadersSpec.retrieve()).willReturn(responseSpec);
    given(responseSpec.body(SportsPageResponse.class)).willReturn(response);

    List<SportsEventResponse> result = sportClient.fetchPastEvents("4328", "2025-2026");

    assertThat(result).hasSize(1);
    assertThat(result.get(0).idEvent()).isEqualTo("2");
  }

  @Test
  @DisplayName("시즌 경기 조회 시 올바른 URI를 사용한다")
  void fetchPastEvents_올바른_URI를_사용한다() {
    given(sportProperties.baseUrl()).willReturn("https://www.thesportsdb.com/api/v1/json");
    given(sportProperties.key()).willReturn("123");

    given(restClient.get()).willReturn(requestHeadersUriSpec);
    given(requestHeadersUriSpec.uri(anyString())).willReturn(requestHeadersSpec);
    given(requestHeadersSpec.retrieve()).willReturn(responseSpec);
    given(responseSpec.body(SportsPageResponse.class)).willReturn(new SportsPageResponse(List.of()));

    ArgumentCaptor<String> uriCaptor = ArgumentCaptor.forClass(String.class);

    sportClient.fetchPastEvents("4424", "2026");

    verify(requestHeadersUriSpec).uri(uriCaptor.capture());
    assertThat(uriCaptor.getValue())
        .isEqualTo("https://www.thesportsdb.com/api/v1/json/123/eventsseason.php?id=4424&season=2026");
  }

  @Test
  @DisplayName("응답이 null이면 빈 리스트를 반환한다")
  void fetchNextEvents_응답이_null이면_빈_리스트를_반환한다() {
    given(sportProperties.baseUrl()).willReturn("https://www.thesportsdb.com/api/v1/json");
    given(sportProperties.key()).willReturn("123");

    given(restClient.get()).willReturn(requestHeadersUriSpec);
    given(requestHeadersUriSpec.uri(anyString())).willReturn(requestHeadersSpec);
    given(requestHeadersSpec.retrieve()).willReturn(responseSpec);
    given(responseSpec.body(SportsPageResponse.class)).willReturn(null);

    List<SportsEventResponse> result = sportClient.fetchNextEvents("4328");

    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("응답의 events가 null이면 빈 리스트를 반환한다")
  void fetchNextEvents_events가_null이면_빈_리스트를_반환한다() {
    given(sportProperties.baseUrl()).willReturn("https://www.thesportsdb.com/api/v1/json");
    given(sportProperties.key()).willReturn("123");

    given(restClient.get()).willReturn(requestHeadersUriSpec);
    given(requestHeadersUriSpec.uri(anyString())).willReturn(requestHeadersSpec);
    given(requestHeadersSpec.retrieve()).willReturn(responseSpec);
    given(responseSpec.body(SportsPageResponse.class)).willReturn(new SportsPageResponse(null));

    List<SportsEventResponse> result = sportClient.fetchNextEvents("4328");

    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("API 호출 중 예외가 발생하면 빈 리스트를 반환한다")
  void fetchNextEvents_예외_발생시_빈_리스트를_반환한다() {
    given(sportProperties.baseUrl()).willReturn("https://www.thesportsdb.com/api/v1/json");
    given(sportProperties.key()).willReturn("123");

    given(restClient.get()).willReturn(requestHeadersUriSpec);
    given(requestHeadersUriSpec.uri(anyString())).willReturn(requestHeadersSpec);
    willThrow(new RuntimeException("네트워크 오류")).given(requestHeadersSpec).retrieve();

    List<SportsEventResponse> result = sportClient.fetchNextEvents("4328");

    assertThat(result).isEmpty();
  }
}