-- users
CREATE TABLE users
(
    id uuid PRIMARY KEY,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone,
    name varchar(30) NOT NULL,
    email varchar(50) UNIQUE NOT NULL,
    password varchar(100),
    profile_image_url varchar(255),
    role varchar(20) default 'USER' NOT NULL,
    is_locked boolean default false NOT NULL,
    provider varchar(20) default 'LOCAL' NOT NULL,
    provider_id varchar(255)
);

ALTER TABLE users
    ADD CONSTRAINT chk_user_role
        CHECK (role IN ('USER', 'ADMIN'));

-- follows
CREATE TABLE follows
(
    id uuid PRIMARY KEY,
    to_user_id UUID NOT NULL,
    from_user_id UUID not null,
    created_at timestamp with time zone NOT NULL
);

-- follows -> users
ALTER TABLE follows
    ADD CONSTRAINT fk_follows_to_user
        FOREIGN KEY (to_user_id)
            REFERENCES users (id)
            ON DELETE CASCADE;

ALTER TABLE follows
    ADD CONSTRAINT fk_follows_from_user
        FOREIGN KEY (from_user_id)
            REFERENCES users (id)
            ON DELETE CASCADE;

-- 팔로우 관계는 유일해야 함
ALTER TABLE follows
    ADD CONSTRAINT unique_follow
        UNIQUE (from_user_id, to_user_id);

-- 스스로 팔로우하는 거 방지
ALTER TABLE follows
    ADD CONSTRAINT check_not_self_follow
        CHECK ( from_user_id <> to_user_id);

-- jwt_sessions
CREATE TABLE jwt_sessions
(
    id uuid PRIMARY KEY,
    user_id uuid NOT NULL,
    refresh_token varchar(1024) NOT NULL,
    expiration_time timestamp with time zone NOT NULL,
    revoked boolean default false NOT NULL,
    created_at timestamp with time zone NOT NULL
);

-- jwt_sessions -> users
ALTER TABLE jwt_sessions
    ADD CONSTRAINT fk_jwt_user_id
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE;

-- pasword_reset_tokens
CREATE TABLE password_reset_tokens
(
    id uuid PRIMARY KEY,
    user_id uuid NOT NULL,
    temp_password varchar(255) NOT NULL,
    expiry_date timestamp with time zone NOT NULL,
    used boolean default false NOT NULL,
    created_at timestamp with time zone NOT NULL
);

-- password_reset_tokens -> users
ALTER TABLE password_reset_tokens
    ADD CONSTRAINT fk_password_user_id
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE;

-- contents
CREATE TABLE contents
(
    id uuid PRIMARY KEY,
    type varchar(20) not null,
    external_id varchar(1024) not null,
    title varchar(50) not null,
    description text,
    thumbnail_url varchar(255),
    release_date date,
    status varchar(10),
    created_at timestamp with time zone,
    updated_at timestamp with time zone,
    average_rating double precision,
    review_count int,
    watcher_count int
);

ALTER TABLE contents
    ADD CONSTRAINT chk_content_status
        CHECK (status IN ('UPCOMING', 'RELEASE'));

-- content_tags
CREATE TABLE content_tags
(
    id uuid primary key,
    tag varchar(50) not null,
    content_id uuid not null
);

-- content_tags -> contents
ALTER TABLE content_tags
    ADD CONSTRAINT fk_content_tags_id
        FOREIGN KEY (content_id)
            REFERENCES contents (id)
            ON DELETE CASCADE;

-- reviews
CREATE TABLE reviews
(
    id uuid primary key,
    rating double precision not null,
    text text not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone,
    content_id uuid not null,
    user_id uuid not null
);

-- reviews -> contents
ALTER TABLE reviews
    ADD CONSTRAINT fk_reviews_content_id
        FOREIGN KEY (content_id)
            REFERENCES contents (id)
            ON DELETE CASCADE;

-- reviews -> users
ALTER TABLE reviews
    ADD CONSTRAINT fk_reviews_user_id
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE;

-- playlists
CREATE TABLE playlists
(
    id uuid primary key,
    title varchar(100) not null,
    description text not null,
    subscriber_count int not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone,
    owner_id uuid not null
);

-- playlists -> users
ALTER TABLE playlists
    ADD CONSTRAINT fk_playlists_owner_id
        FOREIGN KEY (owner_id)
            REFERENCES users (id)
            ON DELETE CASCADE;

-- playlist_item
CREATE TABLE playlist_items
(
    id uuid primary key,
    order_index int not null,
    created_at timestamp with time zone not null,
    playlist_id uuid not null,
    content_id uuid not null
);

-- playlist_items -> playlists
ALTER TABLE playlist_items
    ADD CONSTRAINT fk_playlist_items_playlist_id
        FOREIGN KEY (playlist_id)
            REFERENCES playlists (id)
            ON DELETE CASCADE;

-- playlist_items -> contents
ALTER TABLE playlist_items
    ADD CONSTRAINT fk_playlist_items_content_id
        FOREIGN KEY (content_id)
            REFERENCES contents (id)
            ON DELETE CASCADE;

CREATE TABLE playlist_subscriptions
(
    id uuid primary key,
    created_at timestamp with time zone not null,
    playlist_id uuid not null,
    subscriber_id uuid not null
);

-- playlist_subscriptions -> playlists
ALTER TABLE playlist_subscriptions
    ADD CONSTRAINT fk_playlist_subs_playlist_id
        FOREIGN KEY (playlist_id)
            REFERENCES playlists (id)
            ON DELETE CASCADE;

-- playlist_subscriptions -> users
ALTER TABLE playlist_subscriptions
    ADD CONSTRAINT fk_playlist_subs_subscriber_id
        FOREIGN KEY (subscriber_id)
            REFERENCES users (id)
            ON DELETE CASCADE;

-- watching_sessions
CREATE TABLE watching_sessions
(
    id uuid primary key,
    user_id uuid not null,
    content_id uuid not null,
    status varchar(10),
    started_at timestamp with time zone,
    ended_at timestamp with time zone
);

-- watching_sessions -> users
ALTER TABLE watching_sessions
    ADD CONSTRAINT fk_sessions_user_id
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE set null;

-- watching_sessions -> contents
ALTER TABLE watching_sessions
    ADD CONSTRAINT fk_sessions_content_id
        FOREIGN KEY (content_id)
            REFERENCES contents (id)
            ON DELETE set null;

-- conversations
CREATE TABLE conversations
(
    id uuid primary key,
    type varchar(10),
    name varchar(100),
    created_at timestamp with time zone not null
);

-- conversation_participants
CREATE TABLE conversations_participants
(
    id uuid primary key,
    user_id uuid not null,
    conversation_id uuid not null,
    joined_at timestamp with time zone,
    last_read_at timestamp with time zone
);

-- conversation_participants -> users
ALTER TABLE conversations_participants
    ADD CONSTRAINT fk_participants_user_id
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE;

-- conversation_participants -> conversations
ALTER TABLE conversations_participants
    ADD CONSTRAINT fk_participants_conversation_id
        FOREIGN KEY (conversation_id)
            REFERENCES conversations (id)
            ON DELETE CASCADE;

-- direct_messages
CREATE TABLE direct_messages
(
    id uuid primary key,
    conversation_id uuid not null,
    sender_id uuid not null,
    content text,
    created_at timestamp with time zone not null
);

-- direct_messages -> conversations
ALTER TABLE direct_messages
    ADD CONSTRAINT fk_dm_conversation_id
        FOREIGN KEY (conversation_id)
            REFERENCES conversations (id)
            ON DELETE CASCADE;

-- direct_messages -> users
ALTER TABLE direct_messages
    ADD CONSTRAINT fk_dm_sender_id
        FOREIGN KEY (sender_id)
            REFERENCES users (id)
            ON DELETE CASCADE;

-- notifications
CREATE TABLE notifications
(
    id uuid primary key,
    receiver_id uuid not null,
    message_id uuid,
    created_at timestamp with time zone not null,
    type varchar(20) not null,
    title varchar(255) not null,
    content text,
    level varchar(20)
);

ALTER TABLE notifications
    ADD CONSTRAINT chk_notification_type
        CHECK (type IN ('DM', 'NOTIFICATION'));

ALTER TABLE notifications
    ADD CONSTRAINT chk_notification_level
        CHECK (level IN ('INFO', 'WARNING', 'ERROR'));

-- notifications -> users
ALTER TABLE notifications
    ADD CONSTRAINT fk_notification_receiver_id
        FOREIGN KEY (receiver_id)
            REFERENCES users (id)
            ON DELETE CASCADE;

-- notifications -> direct_messages
ALTER TABLE notifications
    ADD CONSTRAINT fk_notification_message_id
        FOREIGN KEY (message_id)
            REFERENCES direct_messages (id)
            ON DELETE set null;
