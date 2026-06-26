package com.sb09.sb09moplteam2.websocket.controller;

import com.sb09.sb09moplteam2.dto.CursorResponse;
import com.sb09.sb09moplteam2.websocket.dto.ConversationDto;
import com.sb09.sb09moplteam2.websocket.dto.DirectMessageDto;
import com.sb09.sb09moplteam2.websocket.dto.request.ConversationCreateRequest;
import com.sb09.sb09moplteam2.websocket.service.ConversationService;
import com.sb09.sb09moplteam2.websocket.service.DirectMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {

  private final ConversationService conversationService;
  private final DirectMessageService directMessageService;

  // POST /api/conversations
  // 대화 생성 (이미 있으면 기존 대화 반환)
  @PostMapping
  public ResponseEntity<ConversationDto> create(
      @RequestBody ConversationCreateRequest request,
      @AuthenticationPrincipal UUID myUserId
  ) {
    log.info("POST /api/conversations - withUserId: {}", request.withUserId());
    return ResponseEntity.ok(conversationService.createDirect(myUserId, request.withUserId()));
  }

  // GET /api/conversations
  // 내 대화 목록 조회 (커서 페이지네이션)
  @GetMapping
  public ResponseEntity<CursorResponse<ConversationDto>> findAll(
      @RequestParam(required = false) String keywordLike,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false) UUID idAfter,
      @RequestParam Integer limit,
      @RequestParam String sortBy,
      @RequestParam String sortDirection,
      @AuthenticationPrincipal UUID myUserId
  ) {
    log.info("GET /api/conversations - myUserId: {}", myUserId);
    return ResponseEntity.ok(conversationService.findAll(
        myUserId, cursor, idAfter, limit, sortBy, sortDirection));
  }

  // GET /api/conversations/{conversationId}
  // 대화 단건 조회
  @GetMapping("/{conversationId}")
  public ResponseEntity<ConversationDto> findById(
      @PathVariable UUID conversationId,
      @AuthenticationPrincipal UUID myUserId
  ) {
    log.info("GET /api/conversations/{}", conversationId);
    return ResponseEntity.ok(conversationService.findById(conversationId, myUserId));
  }

  // GET /api/conversations/with?userId=
  // 특정 유저와의 대화 조회
  @GetMapping("/with")
  public ResponseEntity<ConversationDto> findWithUser(
      @RequestParam UUID userId,
      @AuthenticationPrincipal UUID myUserId
  ) {
    log.info("GET /api/conversations/with - userId: {}", userId);
    return ResponseEntity.ok(conversationService.findWithUser(myUserId, userId));
  }

  // GET /api/conversations/{conversationId}/direct-messages
  // DM 목록 조회 (커서 페이지네이션)
  @GetMapping("/{conversationId}/direct-messages")
  public ResponseEntity<CursorResponse<DirectMessageDto>> findDms(
      @PathVariable UUID conversationId,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false) UUID idAfter,
      @RequestParam Integer limit,
      @RequestParam String sortBy,
      @RequestParam String sortDirection,
      @AuthenticationPrincipal UUID myUserId
  ) {
    log.info("GET /api/conversations/{}/direct-messages", conversationId);
    return ResponseEntity.ok(directMessageService.findAll(
        conversationId, myUserId, cursor, idAfter, limit, sortBy, sortDirection));
  }

  // POST /api/conversations/{conversationId}/direct-messages/{directMessageId}/read
  // DM 읽음 처리
  @PostMapping("/{conversationId}/direct-messages/{directMessageId}/read")
  public ResponseEntity<Void> read(
      @PathVariable UUID conversationId,
      @PathVariable UUID directMessageId,
      @AuthenticationPrincipal UUID myUserId
  ) {
    log.info("POST /api/conversations/{}/direct-messages/{}/read", conversationId, directMessageId);
    directMessageService.read(conversationId, myUserId);
    return ResponseEntity.ok().build();
  }
}
