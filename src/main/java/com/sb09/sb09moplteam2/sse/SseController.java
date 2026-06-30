package com.sb09.sb09moplteam2.sse;

import com.sb09.sb09moplteam2.security.CustomUserDetails;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/sse")
public class SseController {

  private final SseService sseService;

  @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter subscribe(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestParam(value = "LastEventId", required = false) UUID lastEventId
  ){
    UUID userId = userDetails.getId();
    return sseService.connect(userId, lastEventId);
  }
}
