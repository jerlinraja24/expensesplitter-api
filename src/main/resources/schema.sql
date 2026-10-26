CREATE DATABASE IF NOT EXISTS expense_splitter;
USE expense_splitter;

CREATE TABLE IF NOT EXISTS users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(60) NOT NULL,
  email VARCHAR(254) NOT NULL UNIQUE,
  password_hash VARCHAR(100) NOT NULL,
  created_at DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS sessions (
  token VARCHAR(100) PRIMARY KEY,
  user_id BIGINT NOT NULL,
  expires_at DATETIME NOT NULL,
  CONSTRAINT fk_sessions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS trips (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  name VARCHAR(100) NOT NULL,
  start_date DATE NULL,
  end_date DATE NULL,
  created_at DATETIME NOT NULL,
  INDEX idx_trips_user (user_id),
  CONSTRAINT fk_trips_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS trip_members (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  trip_id BIGINT NOT NULL,
  name VARCHAR(60) NOT NULL,
  money_given DECIMAL(12,2) NOT NULL DEFAULT 0 CHECK (money_given >= 0),
  created_at DATETIME NOT NULL,
  UNIQUE KEY uq_member_name (trip_id, name),
  CONSTRAINT fk_members_trip FOREIGN KEY (trip_id) REFERENCES trips(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS expenses (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  trip_id BIGINT NOT NULL,
  expense_date DATE NOT NULL,
  category VARCHAR(40) NOT NULL,
  amount DECIMAL(12,2) NOT NULL CHECK (amount > 0),
  description VARCHAR(200) NULL,
  created_at DATETIME NOT NULL,
  INDEX idx_expenses_trip_date (trip_id, expense_date),
  CONSTRAINT fk_expenses_trip FOREIGN KEY (trip_id) REFERENCES trips(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS expense_members (
  expense_id BIGINT NOT NULL,
  member_id BIGINT NOT NULL,
  share_amount DECIMAL(12,2) NOT NULL,
  PRIMARY KEY (expense_id, member_id),
  CONSTRAINT fk_em_expense FOREIGN KEY (expense_id) REFERENCES expenses(id) ON DELETE CASCADE,
  CONSTRAINT fk_em_member FOREIGN KEY (member_id) REFERENCES trip_members(id) ON DELETE CASCADE
);
