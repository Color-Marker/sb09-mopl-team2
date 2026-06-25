package com.sb09.sb09moplteam2.notification.dto.request;

import com.sb09.sb09moplteam2.common.SortDirection;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NotificationListdRequest {
  private String cursor;
  private UUID idAfter;
  @NotNull(message = "제한 범위는 필수입니다")
  @Min(value = 1, message = "최소 1개의 데이터 제한은 필수입니다.")
  private int limit;
  @NotNull(message = "정렬 방향은 필수입니다.")
  private SortDirection sortDirection;
  @NotNull(message = "정렬 기준은 필수입니다.")
  private String sortBy;
}
