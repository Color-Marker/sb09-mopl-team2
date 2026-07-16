package com.sb09.sb09moplteam2.content.search;

import com.sb09.sb09moplteam2.content.entity.Content;
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
@Document(indexName = "contents")
public class ContentDocument {

  @Id
  private String id;

  @Field(type = FieldType.Text, analyzer = "standard")
  private String title;

  @Field(type = FieldType.Text, analyzer = "standard")
  private String description;

  @Field(type = FieldType.Keyword)
  private String type;

  public static ContentDocument from(Content content) {
    return ContentDocument.builder()
        .id(content.getId().toString())
        .title(content.getTitle())
        .description(content.getDescription())
        .type(content.getType().name())
        .build();
  }
}