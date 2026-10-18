-- ==============================================================================
-- AgroDoc Database Schema
-- MySQL 8.0
-- Run this script once to create the agrodoc_db database and all tables.
-- ==============================================================================
-- Usage:
--   mysql -u root -p < database/schema.sql
-- Or paste into MySQL Workbench Query Editor and execute.
-- ==============================================================================

-- Create and select the database
CREATE DATABASE IF NOT EXISTS agrodoc_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE agrodoc_db;

-- ==============================================================================
-- TABLE: users
-- Stores registered user accounts.
-- Passwords are stored as BCrypt hashes — plaintext is never saved.
-- ==============================================================================
CREATE TABLE IF NOT EXISTS users (
    id            BIGINT          NOT NULL AUTO_INCREMENT,
    full_name     VARCHAR(100)    NOT NULL,
    email         VARCHAR(255)    NOT NULL,
    password_hash VARCHAR(255)    NOT NULL,
    is_active     BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP
                                  ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT uq_users_email UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==============================================================================
-- TABLE: disease_info
-- Master list of tomato diseases known to the system.
-- Seeded manually via seed_diseases.sql.
-- The disease_name column must match the label returned by the external AI API.
-- ==============================================================================
CREATE TABLE IF NOT EXISTS disease_info (
    id               BIGINT          NOT NULL AUTO_INCREMENT,
    disease_name     VARCHAR(100)    NOT NULL,
    scientific_name  VARCHAR(150)    NULL,
    description      TEXT            NOT NULL,
    symptoms         TEXT            NOT NULL,
    causes           TEXT            NOT NULL,
    prevention_tips  TEXT            NULL,
    severity_level   ENUM('LOW', 'MED', 'HIGH') NOT NULL DEFAULT 'MED',
    image_url        VARCHAR(500)    NULL,
    created_at       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT uq_disease_name UNIQUE (disease_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==============================================================================
-- TABLE: disease_remedies
-- Stores organic and chemical remedies linked to each disease.
-- One disease can have multiple remedies of each type.
-- ==============================================================================
CREATE TABLE IF NOT EXISTS disease_remedies (
    id            BIGINT        NOT NULL AUTO_INCREMENT,
    disease_id    BIGINT        NOT NULL,
    remedy_type   ENUM('ORGANIC', 'CHEMICAL') NOT NULL,
    remedy_name   VARCHAR(200)  NOT NULL,
    application   TEXT          NOT NULL,
    notes         TEXT          NULL,
    display_order INT           NOT NULL DEFAULT 1,

    PRIMARY KEY (id),
    CONSTRAINT fk_remedies_disease
        FOREIGN KEY (disease_id)
        REFERENCES disease_info (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==============================================================================
-- TABLE: scan_history
-- Records each disease detection scan performed by a user.
-- user_id ties the scan to a registered user (ownership enforcement in backend).
-- disease_id is nullable: if the AI returns an unrecognized label, no FK is set.
-- raw_api_result stores the full API response JSON for debugging / auditing.
-- ==============================================================================
CREATE TABLE IF NOT EXISTS scan_history (
    id               BIGINT          NOT NULL AUTO_INCREMENT,
    user_id          BIGINT          NOT NULL,
    disease_id       BIGINT          NULL,
    image_filename   VARCHAR(500)    NOT NULL,
    disease_label    VARCHAR(150)    NOT NULL,
    confidence_score DECIMAL(5, 2)   NULL,
    is_healthy       BOOLEAN         NOT NULL DEFAULT FALSE,
    raw_api_result   JSON            NULL,
    scanned_at       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT fk_scan_user
        FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_scan_disease
        FOREIGN KEY (disease_id)
        REFERENCES disease_info (id)
        ON DELETE SET NULL
        ON UPDATE CASCADE,

    -- Index for fast per-user history queries
    INDEX idx_scan_history_user_id (user_id),
    INDEX idx_scan_history_scanned_at (scanned_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==============================================================================
-- TABLE: user_feedback
-- Optional user ratings and comments on scan results.
-- Links to both the user and optionally a specific scan.
-- ==============================================================================
CREATE TABLE IF NOT EXISTS user_feedback (
    id           BIGINT    NOT NULL AUTO_INCREMENT,
    user_id      BIGINT    NOT NULL,
    scan_id      BIGINT    NULL,
    rating       TINYINT   NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comments     TEXT      NULL,
    submitted_at DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT fk_feedback_user
        FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_feedback_scan
        FOREIGN KEY (scan_id)
        REFERENCES scan_history (id)
        ON DELETE SET NULL
        ON UPDATE CASCADE,

    INDEX idx_feedback_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==============================================================================
-- Verification queries (uncomment to check after running)
-- ==============================================================================
-- SHOW TABLES;
-- DESCRIBE users;
-- DESCRIBE disease_info;
-- DESCRIBE disease_remedies;
-- DESCRIBE scan_history;
-- DESCRIBE user_feedback;
