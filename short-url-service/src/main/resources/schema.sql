-- MySQL schema for short URL service
-- This table stores the mapping between short codes and original URLs

CREATE TABLE IF NOT EXISTS short_urls (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    short_code VARCHAR(16) NOT NULL UNIQUE,
    original_url VARCHAR(2048) NOT NULL,
    business_id VARCHAR(64) NOT NULL,
    click_count BIGINT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_short_code (short_code),
    INDEX idx_business_id (business_id),
    INDEX idx_created_at (created_at)
);

-- Optional: Create a sequence table for high-performance ID generation if needed
-- This is an alternative approach for very high QPS requirements
/*
CREATE TABLE IF NOT EXISTS id_generator (
    table_name VARCHAR(64) PRIMARY KEY,
    next_id BIGINT NOT NULL,
    step INT DEFAULT 100
);

INSERT INTO id_generator (table_name, next_id, step) VALUES ('short_urls', 1, 100) ON DUPLICATE KEY UPDATE next_id = next_id;
*/