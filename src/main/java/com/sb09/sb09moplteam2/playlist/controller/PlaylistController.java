package com.sb09.sb09moplteam2.playlist.controller;

import com.sb09.sb09moplteam2.playlist.dto.data.PlaylistDto;
import com.sb09.sb09moplteam2.playlist.dto.request.PlaylistCreatedRequest;
import com.sb09.sb09moplteam2.playlist.dto.request.PlaylistUpdateRequest;
import com.sb09.sb09moplteam2.playlist.dto.response.CursorResponsePlaylistDto;
import com.sb09.sb09moplteam2.playlist.service.PlaylistService;
import com.sb09.sb09moplteam2.security.jwt.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/playlists")
@RequiredArgsConstructor
@Tag(name = "플레이리스트 관리")
public class PlaylistController {

  private final PlaylistService playlistService;

  @Operation(summary = "플레이리스트 생성")
  @PostMapping
  public ResponseEntity<PlaylistDto> create(
      @RequestBody PlaylistCreatedRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    PlaylistDto dto = playlistService.create(request, userDetails.getId());
    return ResponseEntity.status(HttpStatus.CREATED).body(dto);
  }

  @Operation(summary = "플레이리스트 목록 조회(커서 페이지네이)")
  @GetMapping
  public ResponseEntity<CursorResponsePlaylistDto> findAll(
      @Parameter(description = "검색 키워드") @RequestParam(required = false) String keywordLike,
      @Parameter(description = "소유자 ID") @RequestParam(required = false) UUID ownerIdEqual,
      @Parameter(description = "구독자 ID") @RequestParam(required = false) UUID subscriberIdEqual,
      @Parameter(description = "커서") @RequestParam(required = false) String cursor,
      @Parameter(description = "보조 커서") @RequestParam(required = false) UUID idAfter,
      @Parameter(description = "한 번에 가져올 개수") @RequestParam int limit,
      @Parameter(description = "정렬 방향", schema = @Schema(allowableValues = {"ASCENDING", "DESCENDING"}))
      @RequestParam String sortDirection,
      @Parameter(description = "정렬 기준", schema = @Schema(allowableValues = {"updatedAt", "subscribeCount"}))
      @RequestParam String sortBy,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    UUID currentUserId = userDetails != null ? userDetails.getId() : null;
    CursorResponsePlaylistDto response = playlistService.findAll(
        keywordLike, ownerIdEqual, subscriberIdEqual, cursor, idAfter,
        limit, sortDirection, sortBy, currentUserId
    );
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "플레이리스트 단건 조회")
  @GetMapping("/{playlistId}")
  public ResponseEntity<PlaylistDto> findById(
      @PathVariable UUID playlistId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    UUID currentUserId = userDetails != null ? userDetails.getId() : null;
    return ResponseEntity.ok(playlistService.findById(playlistId, currentUserId));
  }

  @Operation(summary = "플레이리스트 수정")
  @PatchMapping("/{playlistId}")
  public ResponseEntity<PlaylistDto> update(
      @PathVariable UUID playlistId,
      @RequestBody PlaylistUpdateRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(playlistService.update(playlistId, request, userDetails.getId()));
  }

  @Operation(summary = "플레이리스트 삭제")
  @DeleteMapping("/{playlistId}")
  public ResponseEntity<Void> delete(
      @PathVariable UUID playlistId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    playlistService.delete(playlistId, userDetails.getId());
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "플레이리스트에 콘텐츠 추가")
  @PostMapping("/{playlistId}/contents/{contentId}")
  public ResponseEntity<Void> addContent(
      @PathVariable UUID playlistId,
      @PathVariable UUID contentId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    playlistService.addContent(playlistId, contentId, userDetails.getId());
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @Operation(summary = "플레이리스트에 콘텐츠 삭제")
  @DeleteMapping("/{playlistId}/contents/{contentId}")
  public ResponseEntity<Void> removeContent(
      @PathVariable UUID playlistId,
      @PathVariable UUID contentId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    playlistService.removeContent(playlistId, contentId, userDetails.getId());
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "플레이리스트 구독")
  @PostMapping("/{playlistId}/subscription")
  public ResponseEntity<Void> subscribe(
      @PathVariable UUID playlistId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    playlistService.subscribe(playlistId, userDetails.getId());
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @Operation(summary = "플레이리스트 구독 취소")
  @DeleteMapping("/{playlistId}/subscription")
  public ResponseEntity<Void> unsubscribe(
      @PathVariable UUID playlistId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    playlistService.unsubscribe(playlistId, userDetails.getId());
    return ResponseEntity.noContent().build();
  }
}