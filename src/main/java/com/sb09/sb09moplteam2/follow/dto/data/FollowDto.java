package com.sb09.sb09moplteam2.follow.dto.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FollowDto {

  private UUID id;
  private UUID followeeId;
  private UUID followerId;
}
