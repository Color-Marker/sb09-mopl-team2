package com.sb09.sb09moplteam2.content.batch.tmdb;

import java.util.List;
import java.util.Map;

public class TmdbGenreMapper {

  private static final Map<Integer, String> GENRE_MAP = Map.ofEntries(
      // 영화 장르
      Map.entry(28, "액션"),
      Map.entry(12, "모험"),
      Map.entry(16, "애니메이션"),
      Map.entry(35, "코미디"),
      Map.entry(80, "범죄"),
      Map.entry(99, "다큐멘터리"),
      Map.entry(18, "드라마"),
      Map.entry(10751, "가족"),
      Map.entry(14, "판타지"),
      Map.entry(36, "역사"),
      Map.entry(27, "공포"),
      Map.entry(10402, "음악"),
      Map.entry(9648, "미스터리"),
      Map.entry(10749, "로맨스"),
      Map.entry(878, "SF"),
      Map.entry(10770, "TV 영화"),
      Map.entry(53, "스릴러"),
      Map.entry(10752, "전쟁"),
      Map.entry(37, "서부"),

      // TV 전용 장르 (영화와 ID가 다른 것들)
      Map.entry(10759, "액션&어드벤처"),
      Map.entry(10762, "키즈"),
      Map.entry(10763, "뉴스"),
      Map.entry(10764, "리얼리티"),
      Map.entry(10765, "SF&판타지"),
      Map.entry(10766, "소프"),
      Map.entry(10767, "토크"),
      Map.entry(10768, "전쟁&정치")
  );

  private TmdbGenreMapper() {}

  public static List<String> toTagNames(List<Integer> genreIds) {
    if( genreIds == null || genreIds.isEmpty()) {
      return List.of();
    }
    return genreIds.stream()
        .map(GENRE_MAP::get)
        .distinct()
        .toList();
  }

}
