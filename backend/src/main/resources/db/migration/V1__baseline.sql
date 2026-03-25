-- ========================
-- users
-- ========================
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(128) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    nickname VARCHAR(64) NOT NULL,
    avatar_url VARCHAR(500),
    failed_login_count INT NOT NULL DEFAULT 0,
    locked_until TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- ========================
-- category_do
-- ========================
CREATE TABLE category_do (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(64) NOT NULL UNIQUE,
    sort_order INT NOT NULL DEFAULT 0,
    enabled TINYINT(1) NOT NULL DEFAULT 1
);

-- ========================
-- goods_do
-- ========================
CREATE TABLE goods_do (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    seller_id BIGINT NOT NULL,
    category_id BIGINT NULL,
    title VARCHAR(120) NOT NULL,
    description TEXT NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    condition_level VARCHAR(50) NOT NULL,
    campus_location VARCHAR(120) NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_goods_seller FOREIGN KEY (seller_id) REFERENCES users (id),
    CONSTRAINT fk_goods_category FOREIGN KEY (category_id) REFERENCES category_do (id)
);

-- 索引（重要）
CREATE INDEX idx_goods_status_category_created_at 
ON goods_do (status, category_id, created_at);

CREATE INDEX idx_goods_seller_created_at 
ON goods_do (seller_id, created_at);

CREATE INDEX idx_goods_seller_id 
ON goods_do (seller_id);

CREATE INDEX idx_goods_category_id 
ON goods_do (category_id);

-- ========================
-- goods_image_do
-- ========================
CREATE TABLE goods_image_do (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    goods_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,

    CONSTRAINT fk_goods_image_goods 
    FOREIGN KEY (goods_id) REFERENCES goods_do (id) ON DELETE CASCADE
);

CREATE INDEX idx_goods_image_goods_sort 
ON goods_image_do (goods_id, sort_order);

-- ========================
-- conversation_do
-- ========================
CREATE TABLE conversation_do (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    goods_id BIGINT NOT NULL,
    buyer_id BIGINT NOT NULL,
    seller_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_message_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_conversation_goods 
    FOREIGN KEY (goods_id) REFERENCES goods_do (id),

    CONSTRAINT fk_conversation_buyer 
    FOREIGN KEY (buyer_id) REFERENCES users (id),

    CONSTRAINT fk_conversation_seller 
    FOREIGN KEY (seller_id) REFERENCES users (id),

    CONSTRAINT uk_conversation_goods_buyer 
    UNIQUE (goods_id, buyer_id)
);

CREATE INDEX idx_conversation_buyer_seller_last_message_at 
ON conversation_do (buyer_id, seller_id, last_message_at);

CREATE INDEX idx_conversation_buyer_last_message_at 
ON conversation_do (buyer_id, last_message_at);

CREATE INDEX idx_conversation_seller_last_message_at 
ON conversation_do (seller_id, last_message_at);

-- ========================
-- messages
-- ========================
CREATE TABLE messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    conversation_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_messages_conversation 
    FOREIGN KEY (conversation_id) REFERENCES conversation_do (id) ON DELETE CASCADE,

    CONSTRAINT fk_messages_sender 
    FOREIGN KEY (sender_id) REFERENCES users (id)
);

CREATE INDEX idx_messages_conversation_created_at 
ON messages (conversation_id, created_at);