package com.sb09.sb09moplteam2.playlist.controller;

import com.sb09.sb09moplteam2.playlist.dto.data.PlaylistDto;
import com.sb09.sb09moplteam2.playlist.dto.request.PlaylistCreatedRequest;
import com.sb09.sb09moplteam2.playlist.dto.request.PlaylistUpdateRequest;
import com.sb09.sb09moplteam2.playlist.dto.response.CursorResponsePlaylistDto;
import com.sb09.sb09moplteam2.playlist.service.PlaylistService;
import com.sb09.sb09moplteam2.security.jwt.CustomUserDetails;
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
public class PlaylistController {

  private final PlaylistService playlistService;

  @PostMapping
  public ResponseEntity<PlaylistDto> create(
      @RequestBody PlaylistCreatedRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    PlaylistDto dto = playlistService.create(request, userDetails.getId());
    return ResponseEntity.status(HttpStatus.CREATED).body(dto);
  }

  @GetMapping
  public ResponseEntity<CursorResponsePlaylistDto> findAll(
      @RequestParam(required = false) String keywordLike,
      @RequestParam(required = false) UUID ownerIdEqual,
      @RequestParam(required = false) UUID subscriberIdEqual,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false) UUID idAfter,
      @RequestParam int limit,
      @RequestParam String sortDirection,
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

  @GetMapping("/{playlistId}")
  public ResponseEntity<PlaylistDto> findById(
      @PathVariable UUID playlistId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    UUID currentUserId = userDetails != null ? userDetails.getId() : null;
    return ResponseEntity.ok(playlistService.findById(playlistId, currentUserId));
  }

  @PatchMapping("/{playlistId}")
  public ResponseEntity<PlaylistDto> update(
      @PathVariable UUID playlistId,
      @RequestBody PlaylistUpdateRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(playlistService.update(playlistId, request, userDetails.getId()));
  }

  @DeleteMapping("/{playlistId}")
  public ResponseEntity<Void> delete(
      @PathVariable UUID playlistId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    playlistService.delete(playlistId, userDetails.getId());
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{playlistId}/contents/{contentId}")
  public ResponseEntity<Void> addContent(
      @PathVariable UUID playlistId,
      @PathVariable UUID contentId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    playlistService.addContent(playlistId, contentId, userDetails.getId());
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @DeleteMapping("/{playlistId}/contents/{contentId}")
  public ResponseEntity<Void> removeContent(
      @PathVariable UUID playlistId,
      @PathVariable UUID contentId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    playlistService.removeContent(playlistId, contentId, userDetails.getId());
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{playlistId}/subscription")
  public ResponseEntity<Void> subscribe(
      @PathVariable UUID playlistId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    playlistService.subscribe(playlistId, userDetails.getId());
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @DeleteMapping("/{playlistId}/subscription")
  public ResponseEntity<Void> unsubscribe(
      @PathVariable UUID playlistId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    playlistService.unsubscribe(playlistId, userDetails.getId());
    return ResponseEntity.noContent().build();
  }
}