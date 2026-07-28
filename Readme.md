# **2조벌조**

Sprint 9기 고급 프로젝트

[![codecov](https://codecov.io/gh/Color-Marker/sb09-mopl-team2/branch/main/graph/badge.svg)](https://codecov.io/gh/Color-Marker/sb09-mopl-team2)

## **팀원 구성**

- 김은애(팀장) https://github.com/Color-Marker
- 최재훈 https://github.com/cjhh0707
- 최건위 https://github.com/geoni-98
- 강현홍 https://github.com/Newbress
- 이용일 https://github.com/lyi980403-arch

---

## **프로젝트 소개**

* 모두의 플리 Spring 백엔드 시스템 구축
* 프로젝트 기간: 2026.06.22 \~ 2026.07.29

---

## **기술 스택**

* Backend: Spring Boot, Spring Security, Spring Data JPA
* Database: PostgreSQL
* 공통 Tool: Git \& Github, Discord
* Redis: aws ElasticCache
* Kafak: Confluent Cloud
* Opensearch: aws Opensearch
* 구글 및 카카오 계정 연동
* TBDM, The Sports DB API 연동

---

## **팀원별 구현 기능 상세**

### 김은애 (팀장)

* 알림 기능 구현 및 실시간 이벤트 전송 (SSE) 구현
* aws ecs 설정 및 alb, https 설정 진행
* 그 외 서버 관리를 통해 에러 발생 시 로그 확인 및 디버깅 요청

### 최재훈

* 사용자 관리 기능 및 스프링 security 구현
* 구글 및 카카오 계정 연동 구현
* 어드민 권한 및 일회성 비밀번호 발급 구현

### 최건위

* 프로필 관리 및 팔로우 기능 구현
* elastic search 및 open search 구현 및 연결

### 강현홍

* TBDM 및 The Sports DB api 연동을 통해 배치로 콘텐츠 데이터 자동 요청 및 저장 구현
* 리뷰 기능 및 플레이리스트 생성 삭제 수정 기능 구현
* 분산 환경 설정에 따른 분산 락 기능 구현

### 이용일

* 콘텐츠 실시간 같이 보기 채팅 구현 및 DM 시스템 구현 (웹소켓)
* DM 읽음 상태 업데이트 로직 구현 및 분산 환경 설정에 따른 채팅 시스템 구현

---

## **파일 구조**

```
src
 ┣ main
 ┃ ┣ java
 ┃ ┃ ┣ com
 ┃ ┃ ┃ ┣ sb09.sb09moplteam2
 ┃ ┃ ┃ ┃ ┣ auth
 ┃ ┃ ┃ ┃ ┃ ┣ batch
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ JwtSessionCleanupTasklet.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ PasswordResetTokenCleanupTasklet.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ SessionCleanupScheduler.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ WatchingSessionCleanupTasklet.java
 ┃ ┃ ┃ ┃ ┃ ┣ controller
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ api
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ AuthApi.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ AuthController.java
 ┃ ┃ ┃ ┃ ┃ ┣ dto
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ request
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ ResetPasswordRequest.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ response
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ TokenRefreshResult.java
 ┃ ┃ ┃ ┃ ┃ ┣ entity
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ JwtSession.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ PasswordResetToken.java
 ┃ ┃ ┃ ┃ ┃ ┣ repository
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ JwtSessionRepository.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ PasswordResetTokenRepository.java
 ┃ ┃ ┃ ┃ ┃ ┗ service
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ basic
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ BasicAuthService.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ LoggingMailService.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ SmtpMailService.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ AuthService.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ MailService.java
 ┃ ┃ ┃ ┃ ┣ batch
 ┃ ┃ ┃ ┃ ┃ ┣ config
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ GlobalBatchConfig.java
 ┃ ┃ ┃ ┃ ┃ ┣ listener
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ GlobalStepExceptionListener.java
 ┃ ┃ ┃ ┃ ┃ ┗ monitoring
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ BatchJobMetricsListener.java
 ┃ ┃ ┃ ┃ ┣ common
 ┃ ┃ ┃ ┃ ┃ ┗ SortDirection.java
 ┃ ┃ ┃ ┃ ┣ config
 ┃ ┃ ┃ ┃ ┃ ┣ admin
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ AdminInitializer.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ AdminProperties.java
 ┃ ┃ ┃ ┃ ┃ ┣ jwt
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ JwtChannelInterceptor.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ JwtProperties.java
 ┃ ┃ ┃ ┃ ┃ ┣ AsyncConfig.java
 ┃ ┃ ┃ ┃ ┃ ┣ JpaAuditingConfig.java
 ┃ ┃ ┃ ┃ ┃ ┣ KafkaConfig.java
 ┃ ┃ ┃ ┃ ┃ ┣ MDCLoggingInterceptor.java
 ┃ ┃ ┃ ┃ ┃ ┣ OpenSearchClientConfig.java
 ┃ ┃ ┃ ┃ ┃ ┣ PasswordEncoderConfig.java
 ┃ ┃ ┃ ┃ ┃ ┣ QuerydelConfig.java
 ┃ ┃ ┃ ┃ ┃ ┣ RedisConfig.java
 ┃ ┃ ┃ ┃ ┃ ┣ RestClientConfig.java
 ┃ ┃ ┃ ┃ ┃ ┣ S3Config.java
 ┃ ┃ ┃ ┃ ┃ ┣ SecurityConfig.java
 ┃ ┃ ┃ ┃ ┃ ┣ SessionCleanupBatchConfig.java
 ┃ ┃ ┃ ┃ ┃ ┣ SportBatchConfig.java
 ┃ ┃ ┃ ┃ ┃ ┣ StorageProperties.java
 ┃ ┃ ┃ ┃ ┃ ┣ TmdbBatchConfig.java
 ┃ ┃ ┃ ┃ ┃ ┣ WebMvcConfig.java
 ┃ ┃ ┃ ┃ ┃ ┣ WebSocketConfig.java
 ┃ ┃ ┃ ┃ ┃ ┗ WebSocketSecurityConfig.java
 ┃ ┃ ┃ ┃ ┣ content
 ┃ ┃ ┃ ┃ ┃ ┣ batch
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ sport
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ dto
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ SportsEventResponse.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ SportsPageResponse.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ SprotClient.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ SportProcessor.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ SportProperties.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ SportReader.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ SportSchedulter.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ SportWriter.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ tmdb
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ dto
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ TmdbEventResponse.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ TmdbPageResponse.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ TmdbClient.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ TmdbGenreMapper.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ TmdbMovieProcessor.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ TmdbMovieReader.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ TmdbMovieWriter.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ TmdbPagePartitioner.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ TmdbProperties.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ TmdbScheduler.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ TmdbSortByResolver.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ ContentAndTags.java
 ┃ ┃ ┃ ┃ ┃ ┣ controller
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ ContentController.java
 ┃ ┃ ┃ ┃ ┃ ┣ dto
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ data
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ ContentDto.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ request
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣  ContentCreateRequest.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ ContentUpdateRequest.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ response
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ CursorResponseContentDto.java
 ┃ ┃ ┃ ┃ ┃ ┣ entity
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ Content.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ ContentTag.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ ContentType.java
 ┃ ┃ ┃ ┃ ┃ ┣ mapper
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ ContentMapper.java
 ┃ ┃ ┃ ┃ ┃ ┣ repository
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ ContentRepository.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ ContentRepositoryCustom.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ ContentRepositoryCustomImpl.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ ContentTagRepository.java
 ┃ ┃ ┃ ┃ ┃ ┣ search
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ ContentDocument.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ ContentSearchInitializer.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ ContentSearchRepository.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ ContentSearchService.java
 ┃ ┃ ┃ ┃ ┃ ┗ service
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ ContentService.java
 ┃ ┃ ┃ ┃ ┣ dto
 ┃ ┃ ┃ ┃ ┃ ┣ ContentSummary.java
 ┃ ┃ ┃ ┃ ┃ ┣ CursorResponse.java
 ┃ ┃ ┃ ┃ ┃ ┗ UserSummary.java
 ┃ ┃ ┃ ┃ ┣ event
 ┃ ┃ ┃ ┃ ┃ ┣ kafka
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ KafkaProduceRequiredEventListener.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ NotificationRequiredTopicListener.java
 ┃ ┃ ┃ ┃ ┃ ┣ message
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ CreatedEvent.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ DmEvent.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ FollowedEvent.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ FollowUserWorkEvent.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ MessageCreatedEvent.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ NotificationCreatedEvent.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ NotificationDmEvent.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ NotificationRoleEvent.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ RoleUpdatedEvent.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ SubscribedPlaylistEvent.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ SubsPlaylistWorkEvent.java
 ┃ ┃ ┃ ┃ ┃ ┣ redis
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ RedisNotificationPublishEventListener.java 
 ┃ ┃ ┃ ┃ ┣ exception
 ┃ ┃ ┃ ┃ ┃ ┣ auth
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ AuthException.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ InvalidTokenException.java
 ┃ ┃ ┃ ┃ ┃ ┣ content
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ ContentNotFoundException.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ Duplicate\\\_Content.java
 ┃ ┃ ┃ ┃ ┃ ┣ follow
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ AlreadyFollowingException.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ FollowForbiddenException.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ FollowNotFoundException.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ SelfFollowNotAllowedException.java
 ┃ ┃ ┃ ┃ ┃ ┣ notification
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ NotificationException.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ NotificationForbiddenException.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ NotificationNotFoundException.java
 ┃ ┃ ┃ ┃ ┃ ┣ playlist
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ DuplicateSubscribeException.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ PlaylistForbiddenException.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ PlaylistNotFoundException.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ SubscribeNotFoundException.java
 ┃ ┃ ┃ ┃ ┃ ┣ profiles
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ UserForbiddenException.java
 ┃ ┃ ┃ ┃ ┃ ┣ review
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ DuplicateReviewException.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ ReviewForbiddenException.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ ReviewNotFoundException.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ ReviewException.java
 ┃ ┃ ┃ ┃ ┃ ┣ user
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ DuplicateEmailException.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ UserException.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ UserNotFoundException.java
 ┃ ┃ ┃ ┃ ┃ ┣ websocket
 ┃ ┃ ┃ ┃ ┃ ┣ ErrorCode.java
 ┃ ┃ ┃ ┃ ┃ ┣ ErrorResponse.java
 ┃ ┃ ┃ ┃ ┃ ┣ GlobalExceptionHandler.java
 ┃ ┃ ┃ ┃ ┃ ┗ MoplException.java
 ┃ ┃ ┃ ┃ ┣ follow
 ┃ ┃ ┃ ┃ ┃ ┣ dto
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ data
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ FollowDto.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ reqeust
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ FollowRequest.java
 ┃ ┃ ┃ ┃ ┃ ┣ controller
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ FollowController.java
 ┃ ┃ ┃ ┃ ┃ ┣ entity
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ Follow.java
 ┃ ┃ ┃ ┃ ┃ ┣ service
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ FollowService.java
 ┃ ┃ ┃ ┃ ┃ ┗ repository
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ FollowRepository.java
 ┃ ┃ ┃ ┃ ┣ notification
 ┃ ┃ ┃ ┃ ┃ ┣ controller
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ api
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ NotificationApi.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ NotificationController.java
 ┃ ┃ ┃ ┃ ┃ ┣ dto
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ data
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ NotificationDto.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ reqeust
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ NotificationListRequest.java
 ┃ ┃ ┃ ┃ ┃ ┣ entity
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ Notification.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ NotificationLevel.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ NotificationType.java
 ┃ ┃ ┃ ┃ ┃ ┣ mapper
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ CursorResponseNotificationMapper.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ NotificationMapper.java
 ┃ ┃ ┃ ┃ ┃ ┣ service
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ Basic
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ BasicNotificationService.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ NotificationService.java
 ┃ ┃ ┃ ┃ ┃ ┗ repository
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ NotificationRepository.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ NotificationRepositoryCustom.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ NotificationRepositoryImpl.java
 ┃ ┃ ┃ ┃ ┣ playlist
 ┃ ┃ ┃ ┃ ┃ ┣ dto
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ data
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ PlaylistDto.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ reqeust
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ PlaylistCreatedRequest.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ PlaylistUpdatedRequest.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ response
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ CursorResponsePlaylistDto.java
 ┃ ┃ ┃ ┃ ┃ ┣ controller
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ PlaylistController.java
 ┃ ┃ ┃ ┃ ┃ ┣ entity
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ Playlist.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ PlaylistItem.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ PlaylistSubscription.java
 ┃ ┃ ┃ ┃ ┃ ┣ mapper
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ PlaylistMapper.java
 ┃ ┃ ┃ ┃ ┃ ┣ service
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ PlaylistService.java
 ┃ ┃ ┃ ┃ ┃ ┗ repository
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ PlaylistItemRepository.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ PlaylistRepository.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ PlaylistRepositoryCustom.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ PlaylistRepositoryCustomImpl.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ PlaylistSubscriptionRepository.java
 ┃ ┃ ┃ ┃ ┣ profile
 ┃ ┃ ┃ ┃ ┃ ┣ dto
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ reqeust
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ ProfileUpdatedRequest.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ response
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ ProfileResponse.java
 ┃ ┃ ┃ ┃ ┃ ┗ service
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ ProfileService.java
 ┃ ┃ ┃ ┃ ┣ redis
 ┃ ┃ ┃ ┃ ┃ ┗ RedisSubscriber.java
 ┃ ┃ ┃ ┃ ┣ review
 ┃ ┃ ┃ ┃ ┃ ┣ dto
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ data
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ ReviewDto.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ reqeust
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ ReviewCreateRequest.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ ReviewUpdateRequest.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ response
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ CursorResponseReviewDto.java
 ┃ ┃ ┃ ┃ ┃ ┣ controller
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ ReviewController.java
 ┃ ┃ ┃ ┃ ┃ ┣ entity
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ Review.java
 ┃ ┃ ┃ ┃ ┃ ┣ mapper
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ ReviewMapper.java
 ┃ ┃ ┃ ┃ ┃ ┣ service
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ ReviewService.java
 ┃ ┃ ┃ ┃ ┃ ┗ repository
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ ReviewRepository.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ ReviewRepositoryCustom.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ ReviewRepositoryCustomImpl.java
 ┃ ┃ ┃ ┃ ┣ security
 ┃ ┃ ┃ ┃ ┃ ┣ jwt
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ CsrfCookieFilter.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ CustomAuthenticationProvier.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ CustomUserDetails.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ JwtAuthenticationFilter.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ JwtProvider.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ JwtSignInFilter.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ JwtSignOutHandler.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ RefreshTokenCookieFactory.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ SessionBlacklistService.java
 ┃ ┃ ┃ ┃ ┃ ┗ oauth
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ CustomOAuth2User.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ CustomOAuth2UserService.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ OAuth2SignInFailureHandler.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ OAuth2SignInSuccessHandler.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ RedisOAuth2AuthorizationRequestRepository.java
 ┃ ┃ ┃ ┃ ┣ sse
 ┃ ┃ ┃ ┃ ┃ ┣ SseController.java
 ┃ ┃ ┃ ┃ ┃ ┣ SseEmitterRepository.java
 ┃ ┃ ┃ ┃ ┃ ┣ SseMessage.java
 ┃ ┃ ┃ ┃ ┃ ┣ SseMessageRepository.java
 ┃ ┃ ┃ ┃ ┃ ┗ SseService.java
 ┃ ┃ ┃ ┃ ┣ storage
 ┃ ┃ ┃ ┃ ┃ ┣ FileStorageService.java
 ┃ ┃ ┃ ┃ ┃ ┣ LocalFileStorageService.java
 ┃ ┃ ┃ ┃ ┃ ┗ S3FileStorageService.java
 ┃ ┃ ┃ ┃ ┣ user
 ┃ ┃ ┃ ┃ ┃ ┣ controller
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ api
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ UserApi.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ UserController.java
 ┃ ┃ ┃ ┃ ┃ ┣ dto
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ data
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ UserDto.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ reqeust
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ ChangePasswordRequest.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ UserCreateRequest.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ UserLockUpdateRequest.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ UserRoleUpdateRequest.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ UserUpdateRequest.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ response
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ JwtDto.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ UserSearchCondition.java
 ┃ ┃ ┃ ┃ ┃ ┣ entity
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ Provider.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ Role.java
 ┃ ┃ ┃ ┃ ┃ ┃ ```java
src
 ┣ main
 ┃ ┣ java
 ┃ ┃ ┣ com
 ┃ ┃ ┃ ┣ sb09.sb09moplteam2
 ┃ ┃ ┃ ┃ ┣ auth
 ┃ ┃ ┃ ┃ ┃ ┣ batch
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ JwtSessionCleanupTasklet.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ PasswordResetTokenCleanupTasklet.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ SessionCleanupScheduler.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ WatchingSessionCleanupTasklet.java
 ┃ ┃ ┃ ┃ ┃ ┣ controller
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ api
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ AuthApi.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ AuthController.java
 ┃ ┃ ┃ ┃ ┃ ┣ dto
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ request
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ ResetPasswordRequest.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ response
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ TokenRefreshResult.java
 ┃ ┃ ┃ ┃ ┃ ┣ entity
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ JwtSession.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ PasswordResetToken.java
 ┃ ┃ ┃ ┃ ┃ ┣ repository
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ JwtSessionRepository.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ PasswordResetTokenRepository.java
 ┃ ┃ ┃ ┃ ┃ ┗ service
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ basic
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ BasicAuthService.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ LoggingMailService.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ SmtpMailService.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ AuthService.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ MailService.java
 ┃ ┃ ┃ ┃ ┣ batch
 ┃ ┃ ┃ ┃ ┃ ┣ config
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ GlobalBatchConfig.java
 ┃ ┃ ┃ ┃ ┃ ┣ listener
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ GlobalStepExceptionListener.java
 ┃ ┃ ┃ ┃ ┃ ┗ monitoring
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ BatchJobMetricsListener.java
 ┃ ┃ ┃ ┃ ┣ common
 ┃ ┃ ┃ ┃ ┃ ┗ SortDirection.java
 ┃ ┃ ┃ ┃ ┣ config
 ┃ ┃ ┃ ┃ ┃ ┣ admin
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ AdminInitializer.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ AdminProperties.java
 ┃ ┃ ┃ ┃ ┃ ┣ jwt
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ JwtChannelInterceptor.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ JwtProperties.java
 ┃ ┃ ┃ ┃ ┃ ┣ AsyncConfig.java
 ┃ ┃ ┃ ┃ ┃ ┣ JpaAuditingConfig.java
 ┃ ┃ ┃ ┃ ┃ ┣ KafkaConfig.java
 ┃ ┃ ┃ ┃ ┃ ┣ MDCLoggingInterceptor.java
 ┃ ┃ ┃ ┃ ┃ ┣ OpenSearchClientConfig.java
 ┃ ┃ ┃ ┃ ┃ ┣ PasswordEncoderConfig.java
 ┃ ┃ ┃ ┃ ┃ ┣ QuerydelConfig.java
 ┃ ┃ ┃ ┃ ┃ ┣ RedisConfig.java
 ┃ ┃ ┃ ┃ ┃ ┣ RestClientConfig.java
 ┃ ┃ ┃ ┃ ┃ ┣ S3Config.java
 ┃ ┃ ┃ ┃ ┃ ┣ SecurityConfig.java
 ┃ ┃ ┃ ┃ ┃ ┣ SessionCleanupBatchConfig.java
 ┃ ┃ ┃ ┃ ┃ ┣ SportBatchConfig.java
 ┃ ┃ ┃ ┃ ┃ ┣ StorageProperties.java
 ┃ ┃ ┃ ┃ ┃ ┣ TmdbBatchConfig.java
 ┃ ┃ ┃ ┃ ┃ ┣ WebMvcConfig.java
 ┃ ┃ ┃ ┃ ┃ ┣ WebSocketConfig.java
 ┃ ┃ ┃ ┃ ┃ ┗ WebSocketSecurityConfig.java
 ┃ ┃ ┃ ┃ ┣ content
 ┃ ┃ ┃ ┃ ┃ ┣ batch
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ sport
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ dto
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ SportsEventResponse.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ SportsPageResponse.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ SprotClient.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ SportProcessor.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ SportProperties.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ SportReader.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ SportSchedulter.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ SportWriter.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ tmdb
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ dto
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ TmdbEventResponse.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ TmdbPageResponse.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ TmdbClient.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ TmdbGenreMapper.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ TmdbMovieProcessor.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ TmdbMovieReader.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ TmdbMovieWriter.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ TmdbPagePartitioner.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ TmdbProperties.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ TmdbScheduler.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ TmdbSortByResolver.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ ContentAndTags.java
 ┃ ┃ ┃ ┃ ┃ ┣ controller
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ ContentController.java
 ┃ ┃ ┃ ┃ ┃ ┣ dto
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ data
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ ContentDto.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ request
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣  ContentCreateRequest.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ ContentUpdateRequest.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ response
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ CursorResponseContentDto.java
 ┃ ┃ ┃ ┃ ┃ ┣ entity
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ Content.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ ContentTag.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ ContentType.java
 ┃ ┃ ┃ ┃ ┃ ┣ mapper
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ ContentMapper.java
 ┃ ┃ ┃ ┃ ┃ ┣ repository
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ ContentRepository.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ ContentRepositoryCustom.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ ContentRepositoryCustomImpl.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ ContentTagRepository.java
 ┃ ┃ ┃ ┃ ┃ ┣ search
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ ContentDocument.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ ContentSearchInitializer.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ ContentSearchRepository.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ ContentSearchService.java
 ┃ ┃ ┃ ┃ ┃ ┗ service
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ ContentService.java
 ┃ ┃ ┃ ┃ ┣ dto
 ┃ ┃ ┃ ┃ ┃ ┣ ContentSummary.java
 ┃ ┃ ┃ ┃ ┃ ┣ CursorResponse.java
 ┃ ┃ ┃ ┃ ┃ ┗ UserSummary.java
 ┃ ┃ ┃ ┃ ┣ event
 ┃ ┃ ┃ ┃ ┃ ┣ kafka
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ KafkaProduceRequiredEventListener.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ NotificationRequiredTopicListener.java
 ┃ ┃ ┃ ┃ ┃ ┣ message
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ CreatedEvent.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ DmEvent.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ FollowedEvent.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ FollowUserWorkEvent.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ MessageCreatedEvent.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ NotificationCreatedEvent.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ NotificationDmEvent.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ NotificationRoleEvent.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ RoleUpdatedEvent.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ SubscribedPlaylistEvent.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ SubsPlaylistWorkEvent.java
 ┃ ┃ ┃ ┃ ┃ ┣ redis
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ RedisNotificationPublishEventListener.java 
 ┃ ┃ ┃ ┃ ┣ exception
 ┃ ┃ ┃ ┃ ┃ ┣ auth
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ AuthException.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ InvalidTokenException.java
 ┃ ┃ ┃ ┃ ┃ ┣ content
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ ContentNotFoundException.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ Duplicate\\\_Content.java
 ┃ ┃ ┃ ┃ ┃ ┣ follow
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ AlreadyFollowingException.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ FollowForbiddenException.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ FollowNotFoundException.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ SelfFollowNotAllowedException.java
 ┃ ┃ ┃ ┃ ┃ ┣ notification
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ NotificationException.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ NotificationForbiddenException.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ NotificationNotFoundException.java
 ┃ ┃ ┃ ┃ ┃ ┣ playlist
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ DuplicateSubscribeException.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ PlaylistForbiddenException.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ PlaylistNotFoundException.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ SubscribeNotFoundException.java
 ┃ ┃ ┃ ┃ ┃ ┣ profiles
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ UserForbiddenException.java
 ┃ ┃ ┃ ┃ ┃ ┣ review
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ DuplicateReviewException.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ ReviewForbiddenException.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ ReviewNotFoundException.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ ReviewException.java
 ┃ ┃ ┃ ┃ ┃ ┣ user
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ DuplicateEmailException.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ UserException.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ UserNotFoundException.java
 ┃ ┃ ┃ ┃ ┃ ┣ websocket
 ┃ ┃ ┃ ┃ ┃ ┣ ErrorCode.java
 ┃ ┃ ┃ ┃ ┃ ┣ ErrorResponse.java
 ┃ ┃ ┃ ┃ ┃ ┣ GlobalExceptionHandler.java
 ┃ ┃ ┃ ┃ ┃ ┗ MoplException.java
 ┃ ┃ ┃ ┃ ┣ follow
 ┃ ┃ ┃ ┃ ┃ ┣ dto
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ data
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ FollowDto.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ reqeust
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ FollowRequest.java
 ┃ ┃ ┃ ┃ ┃ ┣ controller
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ FollowController.java
 ┃ ┃ ┃ ┃ ┃ ┣ entity
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ Follow.java
 ┃ ┃ ┃ ┃ ┃ ┣ service
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ FollowService.java
 ┃ ┃ ┃ ┃ ┃ ┗ repository
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ FollowRepository.java
 ┃ ┃ ┃ ┃ ┣ notification
 ┃ ┃ ┃ ┃ ┃ ┣ controller
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ api
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ NotificationApi.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ NotificationController.java
 ┃ ┃ ┃ ┃ ┃ ┣ dto
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ data
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ NotificationDto.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ reqeust
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ NotificationListRequest.java
 ┃ ┃ ┃ ┃ ┃ ┣ entity
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ Notification.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ NotificationLevel.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ NotificationType.java
 ┃ ┃ ┃ ┃ ┃ ┣ mapper
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ CursorResponseNotificationMapper.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ NotificationMapper.java
 ┃ ┃ ┃ ┃ ┃ ┣ service
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ Basic
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ BasicNotificationService.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ NotificationService.java
 ┃ ┃ ┃ ┃ ┃ ┗ repository
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ NotificationRepository.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ NotificationRepositoryCustom.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ NotificationRepositoryImpl.java
 ┃ ┃ ┃ ┃ ┣ playlist
 ┃ ┃ ┃ ┃ ┃ ┣ dto
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ data
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ PlaylistDto.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ reqeust
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ PlaylistCreatedRequest.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ PlaylistUpdatedRequest.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ response
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ CursorResponsePlaylistDto.java
 ┃ ┃ ┃ ┃ ┃ ┣ controller
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ PlaylistController.java
 ┃ ┃ ┃ ┃ ┃ ┣ entity
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ Playlist.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ PlaylistItem.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ PlaylistSubscription.java
 ┃ ┃ ┃ ┃ ┃ ┣ mapper
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ PlaylistMapper.java
 ┃ ┃ ┃ ┃ ┃ ┣ service
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ PlaylistService.java
 ┃ ┃ ┃ ┃ ┃ ┗ repository
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ PlaylistItemRepository.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ PlaylistRepository.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ PlaylistRepositoryCustom.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ PlaylistRepositoryCustomImpl.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ PlaylistSubscriptionRepository.java
 ┃ ┃ ┃ ┃ ┣ profile
 ┃ ┃ ┃ ┃ ┃ ┣ dto
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ reqeust
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ ProfileUpdatedRequest.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ response
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ ProfileResponse.java
 ┃ ┃ ┃ ┃ ┃ ┗ service
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ ProfileService.java
 ┃ ┃ ┃ ┃ ┣ redis
 ┃ ┃ ┃ ┃ ┃ ┗ RedisSubscriber.java
 ┃ ┃ ┃ ┃ ┣ review
 ┃ ┃ ┃ ┃ ┃ ┣ dto
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ data
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ ReviewDto.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ reqeust
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ ReviewCreateRequest.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ ReviewUpdateRequest.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ response
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ CursorResponseReviewDto.java
 ┃ ┃ ┃ ┃ ┃ ┣ controller
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ ReviewController.java
 ┃ ┃ ┃ ┃ ┃ ┣ entity
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ Review.java
 ┃ ┃ ┃ ┃ ┃ ┣ mapper
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ ReviewMapper.java
 ┃ ┃ ┃ ┃ ┃ ┣ service
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ ReviewService.java
 ┃ ┃ ┃ ┃ ┃ ┗ repository
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ ReviewRepository.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ ReviewRepositoryCustom.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ ReviewRepositoryCustomImpl.java
 ┃ ┃ ┃ ┃ ┣ security
 ┃ ┃ ┃ ┃ ┃ ┣ jwt
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ CsrfCookieFilter.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ CustomAuthenticationProvier.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ CustomUserDetails.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ JwtAuthenticationFilter.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ JwtProvider.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ JwtSignInFilter.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ JwtSignOutHandler.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ RefreshTokenCookieFactory.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ SessionBlacklistService.java
 ┃ ┃ ┃ ┃ ┃ ┗ oauth
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ CustomOAuth2User.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ CustomOAuth2UserService.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ OAuth2SignInFailureHandler.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ OAuth2SignInSuccessHandler.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ RedisOAuth2AuthorizationRequestRepository.java
 ┃ ┃ ┃ ┃ ┣ sse
 ┃ ┃ ┃ ┃ ┃ ┣ SseController.java
 ┃ ┃ ┃ ┃ ┃ ┣ SseEmitterRepository.java
 ┃ ┃ ┃ ┃ ┃ ┣ SseMessage.java
 ┃ ┃ ┃ ┃ ┃ ┣ SseMessageRepository.java
 ┃ ┃ ┃ ┃ ┃ ┗ SseService.java
 ┃ ┃ ┃ ┃ ┣ storage
 ┃ ┃ ┃ ┃ ┃ ┣ FileStorageService.java
 ┃ ┃ ┃ ┃ ┃ ┣ LocalFileStorageService.java
 ┃ ┃ ┃ ┃ ┃ ┗ S3FileStorageService.java
 ┃ ┃ ┃ ┃ ┣ user
 ┃ ┃ ┃ ┃ ┃ ┣ controller
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ api
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ UserApi.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ UserController.java
 ┃ ┃ ┃ ┃ ┃ ┣ dto
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ data
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ UserDto.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ reqeust
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ ChangePasswordRequest.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ UserCreateRequest.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ UserLockUpdateRequest.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ UserRoleUpdateRequest.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ UserUpdateRequest.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ response
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ JwtDto.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ UserSearchCondition.java
 ┃ ┃ ┃ ┃ ┃ ┣ entity
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ Provider.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ Role.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ User.java
 ┃ ┃ ┃ ┃ ┃ ┣ mapper
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ UserMapper.java
 ┃ ┃ ┃ ┃ ┃ ┣ repository
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ custom
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ CustomUserRepository.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ CustomUserRepositoryImpl.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ UserRepository.java
 ┃ ┃ ┃ ┃ ┃ ┗ service
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ Basic
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ BasicUserService.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ UserService.java
 ┃ ┃ ┃ ┃ ┣ websocket
 ┃ ┃ ┃ ┃ ┃ ┣ controller
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ ConversationController.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ DirectMessageController.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ WatchingSessionChatController.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ WatchingSessionController.java
 ┃ ┃ ┃ ┃ ┃ ┣ dto
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ reqeust
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ ConversationCreateRequest.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ DirectMessageRequest.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ WatchingSessionChatRequest.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ response
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ DirectMessageResponse.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ WatchingSessionChatResponse.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ ConversationDto.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ DirectMessageDto.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ WatchingSessionDto.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ UserSearchCondition.java
 ┃ ┃ ┃ ┃ ┃ ┣ entity
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ Conversation.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ ConversationParticipant.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ ConversationType.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ DirectMessage.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ WatchingSession.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ WatchingSessionStatus.java
 ┃ ┃ ┃ ┃ ┃ ┣ event
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ WatchingSessionEvent.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ WatchingSessionEventListener.java
 ┃ ┃ ┃ ┃ ┃ ┣ interceptor
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ StompLoggingInterceptor.java
 ┃ ┃ ┃ ┃ ┃ ┣ mapper
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ ConversationMapper.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ DirectMessageMapper.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ WatchingSessionChatMapper.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ WatchingSessionMapper.java
 ┃ ┃ ┃ ┃ ┃ ┣ relay
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ StompBroadcastMessage.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ StompBroadcastRelay.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ StompBroadCastSubscriber.java
 ┃ ┃ ┃ ┃ ┃ ┣ repository
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ ConversationParticipantRepository.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ ConversationRepository.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ DirectMessageRepository.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ WatchingSessionRepository.java
 ┃ ┃ ┃ ┃ ┃ ┗ service
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ ConversationParticipantService.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ ConversationService.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ DirectMessageService.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ WatchingSessionChatService.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ WatchingSessionService.java
 ┃ ┃ ┃ ┃ ┗ Sb09MoplTeam2Application.java
 ┃ ┣ resources
 ┃ ┃ ┣ application.yml
 ┃ ┃ ┣ application-dev.yml
 ┃ ┃ ┣ application-prod.yml
 ┃ ┃ ┗ schema.sql
 ┣ test
 ┃ ┣ java
 ┃ ┃ ┣ com.sb09.sb09moplteam2
 ┃ ┃ ┃ ┣ auth
 ┃ ┃ ┃ ┃ ┣ batch
 ┃ ┃ ┃ ┃ ┃ ┣ SessionCleanupSchedulerTest.java 
 ┃ ┃ ┃ ┃ ┃ ┗ SessionCleanupTaskletTest.java
 ┃ ┃ ┃ ┃ ┣ controller
 ┃ ┃ ┃ ┃ ┃ ┗ AuthControllerTest.java
 ┃ ┃ ┃ ┃ ┣ repository
 ┃ ┃ ┃ ┃ ┃ ┣ JwtSessionRepositoryTest.java 
 ┃ ┃ ┃ ┃ ┃ ┗ PasswordResetTokenRepositoryTest.java
 ┃ ┃ ┃ ┃ ┗ service.basic
 ┃ ┃ ┃ ┃ ┃ ┗ BasicAuthServiceTest.java
 ┃ ┃ ┃ ┣ batch
 ┃ ┃ ┃ ┃ ┣ monitoring
 ┃ ┃ ┃ ┃ ┃ ┗ BatchJobMetricsListenerTest.java
 ┃ ┃ ┃ ┃ ┣ sport
 ┃ ┃ ┃ ┃ ┃ ┣ SportClientTest.java
 ┃ ┃ ┃ ┃ ┃ ┣ SportProcessorTest.java
 ┃ ┃ ┃ ┃ ┃ ┣ SportReaderTest.java
 ┃ ┃ ┃ ┃ ┃ ┣ SportSchedulerTest.java
 ┃ ┃ ┃ ┃ ┃ ┗ SportWriterTest.java
 ┃ ┃ ┃ ┃ ┣ tmdb
 ┃ ┃ ┃ ┃ ┃ ┣ TmdbClientTest.java
 ┃ ┃ ┃ ┃ ┃ ┣ TmdbMovieProcessorTest.java
 ┃ ┃ ┃ ┃ ┃ ┣ TmdbMovieReaderTest.java
 ┃ ┃ ┃ ┃ ┃ ┣ TmdbMovieWriterTest.java
 ┃ ┃ ┃ ┃ ┃ ┣ TmdbPagePartitionerTest.java
 ┃ ┃ ┃ ┃ ┃ ┗ TmdbSchedulterTest.java
 ┃ ┃ ┃ ┣ config
 ┃ ┃ ┃ ┃ ┣ admin
 ┃ ┃ ┃ ┃ ┃ ┗ AdminInitializerTest.java
 ┃ ┃ ┃ ┃ ┣ jwt
 ┃ ┃ ┃ ┃ ┃ ┗ JwtChannelInterceptorTest.java
 ┃ ┃ ┃ ┃ ┣ MockSearchTestConfig.java
 ┃ ┃ ┃ ┃ ┗ TestJpaConfig.java 
 ┃ ┃ ┃ ┣ content
 ┃ ┃ ┃ ┃ ┣ controller
 ┃ ┃ ┃ ┃ ┃ ┗ ContentControllerTest.java
 ┃ ┃ ┃ ┃ ┣ mapper
 ┃ ┃ ┃ ┃ ┃ ┗ ContentMapperTest.java
 ┃ ┃ ┃ ┃ ┣ repository
 ┃ ┃ ┃ ┃ ┃ ┗ ContentRepositoryTest.java
 ┃ ┃ ┃ ┃ ┣ search
 ┃ ┃ ┃ ┃ ┃ ┣ Initializer
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ ContentSearchInitializerTest.java
 ┃ ┃ ┃ ┃ ┃ ┗ service
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ ContentSearchServiceTest.java
 ┃ ┃ ┃ ┃ ┗ service
 ┃ ┃ ┃ ┃ ┃ ┗ ContentServiceTest.java
 ┃ ┃ ┃ ┣ follow
 ┃ ┃ ┃ ┃ ┣ controller
 ┃ ┃ ┃ ┃ ┃ ┗ FollowControllerTest.java 
 ┃ ┃ ┃ ┃ ┗ service
 ┃ ┃ ┃ ┃ ┃ ┗ FollowServiceTest.java
 ┃ ┃ ┃ ┣ notification
 ┃ ┃ ┃ ┃ ┣ controller
 ┃ ┃ ┃ ┃ ┃ ┗ NotificationControllerTest.java
 ┃ ┃ ┃ ┃ ┣ repository
 ┃ ┃ ┃ ┃ ┃ ┗ NotificationRepositoryImplTest.java
 ┃ ┃ ┃ ┃ ┗ service.Basic
 ┃ ┃ ┃ ┃ ┃ ┗ BasicNotificationServiceTest.java
 ┃ ┃ ┃ ┣ playlist
 ┃ ┃ ┃ ┃ ┣ controller
 ┃ ┃ ┃ ┃ ┃ ┗ PlaylistControllerTest.java
 ┃ ┃ ┃ ┃ ┗ mapper
 ┃ ┃ ┃ ┃ ┃ ┗ PlaylistMapperTest.java
 ┃ ┃ ┃ ┃ ┣ repository
 ┃ ┃ ┃ ┃ ┃ ┗ PlaylistRepositoryTest.java
 ┃ ┃ ┃ ┃ ┗ service.Basic
 ┃ ┃ ┃ ┃ ┃ ┗ PlaylistServiceTest.java
 ┃ ┃ ┃ ┣ review
 ┃ ┃ ┃ ┃ ┣ controller
 ┃ ┃ ┃ ┃ ┃ ┗ ReviewControllerTest.java
 ┃ ┃ ┃ ┃ ┗ mapper
 ┃ ┃ ┃ ┃ ┃ ┗ ReviewMapperTest.java
 ┃ ┃ ┃ ┃ ┣ repository
 ┃ ┃ ┃ ┃ ┃ ┗ ReviewRepositoryTest.java
 ┃ ┃ ┃ ┃ ┗ service.Basic
 ┃ ┃ ┃ ┃ ┃ ┗ ReviewServiceTest.java
 ┃ ┃ ┃ ┣ security
 ┃ ┃ ┃ ┃ ┣ jwt
 ┃ ┃ ┃ ┃ ┃ ┣ CsrfCookieFilterTest.java
 ┃ ┃ ┃ ┃ ┃ ┣ CustomAuthenticationProviderTest.java
 ┃ ┃ ┃ ┃ ┃ ┣ JwtAuthenticationFilterTest.java
 ┃ ┃ ┃ ┃ ┃ ┣ JwtProviderTest.java
 ┃ ┃ ┃ ┃ ┃ ┣ JwtSignInFilterTest.java
 ┃ ┃ ┃ ┃ ┃ ┣ JwtSignOutHandlerTest.java
 ┃ ┃ ┃ ┃ ┃ ┗ SessionBlacklistServiceTest.java
 ┃ ┃ ┃ ┃ ┗ oauth
 ┃ ┃ ┃ ┃ ┃ ┣ CustomOAuth2UserServiceTest.java
 ┃ ┃ ┃ ┃ ┃ ┣ OAuth2SignInFailureHandlerTest.java
 ┃ ┃ ┃ ┃ ┃ ┣ OAuth2SignInSuccessHandlerTest.java
 ┃ ┃ ┃ ┃ ┃ ┗ RedisOAuth2AuthorizationRequestRepositoryTest.java
 ┃ ┃ ┃ ┣ sse
 ┃ ┃ ┃ ┃ ┣ controller
 ┃ ┃ ┃ ┃ ┃ ┗ SseControllerTest.java
 ┃ ┃ ┃ ┃ ┣ repository
 ┃ ┃ ┃ ┃ ┃ ┣ SseEmitterRepositoryTest.java
 ┃ ┃ ┃ ┃ ┃ ┗ SseMessageRepositoryTest.java
 ┃ ┃ ┃ ┃ ┗ service.basic
 ┃ ┃ ┃ ┃ ┃ ┗ SseServiceTest.java
 ┃ ┃ ┃ ┣ user
 ┃ ┃ ┃ ┃ ┣ controller
 ┃ ┃ ┃ ┃ ┃ ┗ UserControllerTest.java
 ┃ ┃ ┃ ┃ ┣ mapper
 ┃ ┃ ┃ ┃ ┃ ┗ UserMapperTest.java
 ┃ ┃ ┃ ┃ ┣ repository.custom
 ┃ ┃ ┃ ┃ ┃ ┣ CustomUserRepositoryImplTest.java
 ┃ ┃ ┃ ┃ ┗ service.basic
 ┃ ┃ ┃ ┃ ┃ ┗ BasicUserServiceTest.java
 ┃ ┃ ┃ ┣ websocket
 ┃ ┃ ┃ ┃ ┣ controller
 ┃ ┃ ┃ ┃ ┃ ┣ ConversationControllerTest.java
 ┃ ┃ ┃ ┃ ┃ ┣ DirectMessageControllerTest.java
 ┃ ┃ ┃ ┃ ┃ ┣ WatchingSessionChatControllerTest.java
 ┃ ┃ ┃ ┃ ┃ ┗ WatchingSessionControllerTest.java
 ┃ ┃ ┃ ┃ ┣ event
 ┃ ┃ ┃ ┃ ┃ ┗ WatchingSessionEventListenerTest.java
 ┃ ┃ ┃ ┃ ┣ mapper
 ┃ ┃ ┃ ┃ ┃ ┣ ConversationMapperTest.java
 ┃ ┃ ┃ ┃ ┃ ┣ DirectMessageMapperTest.java
 ┃ ┃ ┃ ┃ ┃ ┣ WatchingSessionChatMapperTest.java
 ┃ ┃ ┃ ┃ ┃ ┗ WatchingSessionMapperTest.java
 ┃ ┃ ┃ ┃ ┣ relay
 ┃ ┃ ┃ ┃ ┃ ┣ StompBroadcastRelayTest.java
 ┃ ┃ ┃ ┃ ┃ ┗ StompBroadcastSubscriberTest.java
 ┃ ┃ ┃ ┃ ┣ repository
 ┃ ┃ ┃ ┃ ┃ ┣ ConversationParticipantRepositoryTest.java
 ┃ ┃ ┃ ┃ ┃ ┣ ConversationRepositoryTest.java
 ┃ ┃ ┃ ┃ ┃ ┣ DirectMessageRepositoryTest.java
 ┃ ┃ ┃ ┃ ┃ ┗ WatchingSessionRepositoryTest.java
 ┃ ┃ ┃ ┃ ┗ service
 ┃ ┃ ┃ ┃ ┃ ┣ ConversationParticipantServiceTest.java
 ┃ ┃ ┃ ┃ ┃ ┣ ConversationServiceTest.java
 ┃ ┃ ┃ ┃ ┃ ┣ DirectMessageServiceTest.java
 ┃ ┃ ┃ ┃ ┃ ┣ WatchingSessionCahtServiceTest.java
 ┃ ┃ ┃ ┃ ┃ ┗ WatchingSessionServiceTest.java
 ┃ ┃ ┃ ┗ Sb09MoplTeam2ApplicationTests.java
 ┃ ┗ resources
 ┃ ┃ ┗ application.yml
```

---

## **구현 홈페이지**

https://www.sb09-mopl-02-final-project.xyz/#/sign-in

---

## **프로젝트 회고록**

https://drive.google.com/file/d/1iCGXQNojcXSRJ-K8qkFTSBtSqWbmLEee/view?usp=drive\_link

