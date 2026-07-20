package com.sb09.sb09moplteam2.content.search;

import com.sb09.sb09moplteam2.content.entity.Content;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
// createIndex = false: 리포지토리 빈 생성 시 ES 접속(인덱스 자동 생성)을 막아 ES 미가용 환경에서도 앱이 기동되게 함.
// 인덱스 생성은 ContentSearchInitializer가 기동 완료 후 담당.
@Document(indexName = "contents", createIndex = false)
public class ContentDocument {

  @Id
  private String id;

  @Field(type = FieldType.Text, analyzer = "standard")
  private String title;

  @Field(type = FieldType.Text, analyzer = "standard")
  private String description;

  @Field(type = FieldType.Keyword)
  private String type;

  @Field(type = FieldType.Keyword)
  private List<String> tags;

  public static ContentDocument from(Content content, List<String> tags) {
    return ContentDocument.builder()
        .id(content.getId().toString())
        .title(content.getTitle())
        .description(content.getDescription())
        .type(content.getType().name())
        .tags(tags)
        .build();
  }
}