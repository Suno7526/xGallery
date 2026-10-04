CREATE TABLE IF NOT EXISTS user_account (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS x_user_profile (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_account_id BIGINT NOT NULL,
    screen_name VARCHAR(100) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_x_user_profile_user FOREIGN KEY (user_account_id) REFERENCES user_account(id),
    CONSTRAINT uk_x_user_profile UNIQUE (user_account_id, screen_name)
);

CREATE TABLE IF NOT EXISTS gallery_post (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    x_user_profile_id BIGINT NOT NULL,
    tweet_id VARCHAR(50) NOT NULL,
    author VARCHAR(100) NOT NULL,
    post_text TEXT NOT NULL,
    image_url VARCHAR(2048),
    tweet_created_at VARCHAR(50),
    cached_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_gallery_post_x_user FOREIGN KEY (x_user_profile_id) REFERENCES x_user_profile(id),
    CONSTRAINT uk_gallery_post_tweet UNIQUE (x_user_profile_id, tweet_id)
);
