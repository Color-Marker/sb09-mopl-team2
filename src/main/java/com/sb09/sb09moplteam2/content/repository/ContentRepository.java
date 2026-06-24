package com.sb09.sb09moplteam2.content.repository;

import com.sb09.sb09moplteam2.content.entity.Content;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContentRepository extends JpaRepository<Content, UUID> {

  Optional<Content> findByTypeAndExternalId(String type, String externalId);
}
