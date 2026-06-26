package com.sb09.sb09moplteam2.profile.controller.api;

import com.sb09.sb09moplteam2.profile.dto.data.ConversationDto;
import com.sb09.sb09moplteam2.profile.dto.request.ConversationCreateRequest;
import com.sb09.sb09moplteam2.profile.dto.response.CursorResponseConversationDto;
import com.sb09.sb09moplteam2.profile.dto.response.CursorResponseDirectMessageDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "다이렉트 메시지", description = "1:1 대화방 및 메시지 관리 API")
public interface ConversationApi {

  @Operation(summary = "대화 목록 조회", description = "내가 참여 중인 대화방 목록을 커서 기반으로 조회합니다.")
  @GetMapping("/api/conversations")
  ResponseEntity<CursorResponseConversationDto> getConversations(
      @RequestParam(value = "cursor", required = false) String cursor
  );

  @Operation(summary = "대화 생성", description = "특정 사용자와의 새로운 1:1 대화방을 생성합니다.")
  @PostMapping("/api/conversations")
  ResponseEntity<ConversationDto> createConversation(
      @RequestBody ConversationCreateRequest request
  );

  @Operation(summary = "특정 사용자와의 대화 조회", description = "해당 사용자와 이미 진행 중인 대화방이 있는지 조회합니다.")
  @GetMapping("/api/conversations/with")
  ResponseEntity<ConversationDto> getConversationWithUser(
      @RequestParam("userId") UUID userId
  );

  @Operation(summary = "DM 목록 조회", description = "특정 대화방의 메시지 내역을 조회합니다.")
  @GetMapping("/api/conversations/{conversationId}/direct-messages")
  ResponseEntity<CursorResponseDirectMessageDto> getDirectMessages(
      @PathVariable("conversationId") UUID conversationId,
      @RequestParam(value = "cursor", required = false) String cursor
  );

  @Operation(summary = "DM 읽음 처리", description = "특정 메시지를 읽음 상태로 변경합니다.")
  @PostMapping("/api/conversations/{conversationId}/direct-messages/{directMessageId}/read")
  ResponseEntity<Void> readDirectMessage(
      @PathVariable("conversationId") UUID conversationId,
      @PathVariable("directMessageId") UUID directMessageId
  );
}
