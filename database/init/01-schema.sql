-- SweetColors - Creación de bases de datos y tablas (MySQL 8)
-- Una base de datos por microservicio.

-- auth_db (auth-service)
CREATE DATABASE IF NOT EXISTS auth_db
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE auth_db;

CREATE TABLE IF NOT EXISTS users (
  id          BIGINT       NOT NULL AUTO_INCREMENT,
  email       VARCHAR(100) NOT NULL,
  password    VARCHAR(255) NOT NULL,
  full_name   VARCHAR(100) NOT NULL,
  role        ENUM('CLIENTE','ADMIN') NOT NULL DEFAULT 'CLIENTE',
  enabled     BOOLEAN      NOT NULL DEFAULT TRUE,
  created_at  DATETIME(6)  NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_users_email (email)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS refresh_tokens (
  id          BIGINT       NOT NULL AUTO_INCREMENT,
  token_hash  VARCHAR(512) NOT NULL,
  user_id     BIGINT       NOT NULL,
  expires_at  DATETIME(6)  NOT NULL,
  revoked     BOOLEAN      NOT NULL DEFAULT FALSE,
  created_at  DATETIME(6)  NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_refresh_token_hash (token_hash),
  CONSTRAINT fk_refresh_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB;

-- catalog_db (catalog-service)
CREATE DATABASE IF NOT EXISTS catalog_db
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE catalog_db;

CREATE TABLE IF NOT EXISTS products (
  id          BIGINT         NOT NULL AUTO_INCREMENT,
  name        VARCHAR(150)   NOT NULL,
  price       DECIMAL(10,2)  NOT NULL,
  stock       INT            NOT NULL DEFAULT 0,
  category    ENUM('DESAYUNO','DECORACION') NOT NULL,
  created_at  DATETIME(6)    NOT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB;

-- wishlist_db (wishlist-service)
CREATE DATABASE IF NOT EXISTS wishlist_db
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE wishlist_db;

CREATE TABLE IF NOT EXISTS wishlist_items (
  id            BIGINT       NOT NULL AUTO_INCREMENT,
  user_id       BIGINT       NOT NULL,
  product_id    BIGINT       NOT NULL,
  product_name  VARCHAR(150) NOT NULL,
  quantity      INT          NOT NULL DEFAULT 1,
  created_at    DATETIME(6)  NOT NULL,
  updated_at    DATETIME(6)  NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_product (user_id, product_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS wishlist_events (
  id            BIGINT       NOT NULL AUTO_INCREMENT,
  user_id       BIGINT       NOT NULL,
  product_id    BIGINT       NOT NULL,
  product_name  VARCHAR(150) NOT NULL,
  event_type    ENUM('ADDED','REMOVED','UPDATED','OUT_OF_STOCK') NOT NULL,
  created_at    DATETIME(6)  NOT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB;

-- history_db (history-service)
CREATE DATABASE IF NOT EXISTS history_db
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE history_db;

CREATE TABLE IF NOT EXISTS history_events (
  id            BIGINT       NOT NULL AUTO_INCREMENT,
  source_id     BIGINT       NOT NULL,
  user_id       BIGINT       NOT NULL,
  product_id    BIGINT       NOT NULL,
  product_name  VARCHAR(150) NOT NULL,
  event_type    ENUM('ADDED','REMOVED','UPDATED','OUT_OF_STOCK') NOT NULL,
  created_at    DATETIME(6)  NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_history_source (source_id)
) ENGINE=InnoDB;
