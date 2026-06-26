package com.sb09.sb09moplteam2.profile.repository;

import com.sb09.sb09moplteam2.profile.entity.WatchingSession;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WatchingSessionRepository extends JpaRepository<WatchingSession, UUID> {

  Optional<WatchingSession> findFirstByWatcherIdOrderByCreatedAtDesc(UUID watcherId);

  Slice<WatchingSession> findByWatcherIdAndIdLessThanOrderByIdDesc(
      UUID watcherId,
      UUID cursor,
      Pageable pageable
  );

  void deleteByWatcherId(UUID watcherId);
}
