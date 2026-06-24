package com.sb09.sb09moplteam2.content.repository;

import com.sb09.sb09moplteam2.content.entity.ContentTag;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContentTagRepository extends JpaRepository<ContentTag, UUID> {

  List<ContentTag> findByContentId(UUID contentId);

  void deleteByContentId(UUID contentId);
}
