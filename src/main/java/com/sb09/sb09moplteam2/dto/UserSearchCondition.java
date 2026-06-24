package com.sb09.sb09moplteam2.dto;

import com.sb09.sb09moplteam2.entity.Role;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserSearchCondition {

  private String emailLike;
  private Role roleEqual;
  private Boolean isLocked;
  private String cursor;
  private UUID idAfter;
  private int limit;
  private String sortDirection;
  private String sortBy;
}
