package com.sb09.sb09moplteam2.content.repository;

import com.sb09.sb09moplteam2.content.entity.Content;
import com.sb09.sb09moplteam2.content.entity.ContentType;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContentRepository extends JpaRepository<Content, UUID>, ContentRepositoryCustom {

  Optional<Content> findByTypeAndExternalId(ContentType type, String externalId);

  @Query("SELECT c.externalId FROM Content c WHERE c.type = :type")
  Set<String> findAllExternalIdsByType(@Param("type") ContentType type);
}
