package com.sb09.sb09moplteam2;

import com.sb09.sb09moplteam2.content.batch.Sports.SportsProperties;
import com.sb09.sb09moplteam2.content.batch.Tmdb.TmdbProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties({TmdbProperties.class, SportsProperties.class})
public class Sb09MoplTeam2Application {

  public static void main(String[] args) {
    SpringApplication.run(Sb09MoplTeam2Application.class, args);
  }

}
