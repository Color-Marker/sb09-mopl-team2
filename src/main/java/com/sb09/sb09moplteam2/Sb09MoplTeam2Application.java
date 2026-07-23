package com.sb09.sb09moplteam2;

import com.sb09.sb09moplteam2.content.batch.sport.SportProperties;
import com.sb09.sb09moplteam2.content.batch.tmdb.TmdbProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableRetry
@SpringBootApplication(exclude = { ElasticsearchDataAutoConfiguration.class })
@EnableScheduling
@EnableConfigurationProperties({TmdbProperties.class, SportProperties.class})
public class Sb09MoplTeam2Application {

  public static void main(String[] args) {
    SpringApplication.run(Sb09MoplTeam2Application.class, args);
  }

}
