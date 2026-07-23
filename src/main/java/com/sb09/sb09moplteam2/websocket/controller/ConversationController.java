package com.sb09.sb09moplteam2.websocket.controller;

import com.sb09.sb09moplteam2.dto.CursorResponse;
import com.sb09.sb09moplteam2.websocket.dto.ConversationDto;
import com.sb09.sb09moplteam2.websocket.dto.DirectMessageDto;
import com.sb09.sb09moplteam2.websocket.dto.request.ConversationCreateRequest;
import com.sb09.sb09moplteam2.websocket.service.ConversationService;
import com.sb09.sb09moplteam2.websocket.service.DirectMessageService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {

  private final ConversationService conversationService;
  private final DirectMessageService directMessageService;

  @PostMapping
  public ResponseEntity<ConversationDto> create(
      @RequestBody @Valid ConversationCreateRequest request,
      @AuthenticationPrincipal UUID myUserId
  ) {
    log.info("POST /api/conversations - withUserId: {}", request.withUserId());
    return ResponseEntity.ok(conversationService.createDirect(myUserId, request.withUserId()));
  }

  @GetMapping
  public ResponseEntity<CursorResponse<ConversationDto>> findAll(
      @RequestParam(required = false) String keywordLike,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false) UUID idAfter,
      @RequestParam @Min(1) @Max(100) Integer limit,
      @RequestParam @NotBlank String sortBy,
      @RequestParam @NotBlank String sortDirection,
      @AuthenticationPrincipal UUID myUserId
  ) {
    log.info("GET /api/conversations - myUserId: {}", myUserId);
    return ResponseEntity.ok(conversationService.findAll(
        myUserId, keywordLike, cursor, idAfter, limit, sortBy, sortDirection));
  }

  @GetMapping("/{conversationId}")
  public ResponseEntity<ConversationDto> findById(
      @PathVariable UUID conversationId,
      @AuthenticationPrincipal UUID myUserId
  ) {
    log.info("GET /api/conversations/{}", conversationId);
    return ResponseEntity.ok(conversationService.findById(conversationId, myUserId));
  }

  @GetMapping("/with")
  public ResponseEntity<ConversationDto> findWithUser(
      @RequestParam UUID userId,
      @AuthenticationPrincipal UUID myUserId
  ) {
    log.info("GET /api/conversations/with - userId: {}", userId);
    return ResponseEntity.ok(conversationService.findWithUser(myUserId, userId));
  }

  @GetMapping("/{conversationId}/direct-messages")
  public ResponseEntity<CursorResponse<DirectMessageDto>> findDms(
      @PathVariable UUID conversationId,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false) UUID idAfter,
      @RequestParam @Min(1) @Max(100) Integer limit,
      @RequestParam @NotBlank String sortBy,
      @RequestParam @NotBlank String sortDirection,
      @AuthenticationPrincipal UUID myUserId
  ) {
    log.info("GET /api/conversations/{}/direct-messages", conversationId);
    return ResponseEntity.ok(directMessageService.findAll(
        conversationId, myUserId, cursor, idAfter, limit, sortBy, sortDirection));
  }

  @PostMapping("/{conversationId}/read")
  public ResponseEntity<Void> read(
      @PathVariable UUID conversationId,
      @AuthenticationPrincipal UUID myUserId
  ) {
    log.info("POST /api/conversations/{}/read", conversationId);
    directMessageService.read(conversationId, myUserId);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/{conversationId}/direct-messages/{directMessageId}/read")
  public ResponseEntity<Void> readMessage(
      @PathVariable UUID conversationId,
      @PathVariable UUID directMessageId,
      @AuthenticationPrincipal UUID myUserId
  ) {
    log.info("POST /api/conversations/{}/direct-messages/{}/read", conversationId, directMessageId);
    directMessageService.read(conversationId, myUserId);
    return ResponseEntity.ok().build();
  }
}
