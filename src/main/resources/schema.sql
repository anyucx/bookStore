DROP TABLE IF EXISTS file_resources;
DROP TABLE IF EXISTS payment_records;
DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS cart_items;
DROP TABLE IF EXISTS books;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS roles;

CREATE TABLE roles (
  id BIGINT PRIMARY KEY,
  name VARCHAR(64) NOT NULL,
  code VARCHAR(64) NOT NULL UNIQUE,
  description VARCHAR(255),
  created_time DATETIME NOT NULL
);

CREATE TABLE users (
  id BIGINT PRIMARY KEY,
  username VARCHAR(64) NOT NULL UNIQUE,
  password_hash VARCHAR(128) NOT NULL,
  display_name VARCHAR(64) NOT NULL,
  phone VARCHAR(32),
  email VARCHAR(128),
  role_id BIGINT NOT NULL,
  status TINYINT NOT NULL DEFAULT 1,
  deleted TINYINT NOT NULL DEFAULT 0,
  created_time DATETIME NOT NULL,
  updated_time DATETIME NOT NULL,
  KEY idx_users_role (role_id),
  KEY idx_users_deleted (deleted),
  CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles(id)
);

CREATE TABLE categories (
  id BIGINT PRIMARY KEY,
  parent_id BIGINT NOT NULL DEFAULT 0,
  name VARCHAR(128) NOT NULL,
  sort INT NOT NULL DEFAULT 0,
  status TINYINT NOT NULL DEFAULT 1,
  deleted TINYINT NOT NULL DEFAULT 0,
  created_time DATETIME NOT NULL,
  updated_time DATETIME NOT NULL,
  KEY idx_categories_parent (parent_id),
  KEY idx_categories_deleted (deleted)
);

CREATE TABLE books (
  id BIGINT PRIMARY KEY,
  category_id BIGINT NOT NULL,
  name VARCHAR(255) NOT NULL,
  author VARCHAR(128),
  isbn VARCHAR(64),
  price DECIMAL(10,2) NOT NULL,
  stock INT NOT NULL DEFAULT 0,
  cover_url VARCHAR(255),
  description TEXT,
  status TINYINT NOT NULL DEFAULT 1,
  sales INT NOT NULL DEFAULT 0,
  deleted TINYINT NOT NULL DEFAULT 0,
  created_time DATETIME NOT NULL,
  updated_time DATETIME NOT NULL,
  KEY idx_books_category (category_id),
  KEY idx_books_deleted (deleted),
  CONSTRAINT fk_books_category FOREIGN KEY (category_id) REFERENCES categories(id)
);

CREATE TABLE cart_items (
  id BIGINT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  book_id BIGINT NOT NULL,
  quantity INT NOT NULL,
  selected TINYINT NOT NULL DEFAULT 1,
  deleted TINYINT NOT NULL DEFAULT 0,
  created_time DATETIME NOT NULL,
  updated_time DATETIME NOT NULL,
  KEY idx_cart_user (user_id),
  KEY idx_cart_deleted (deleted),
  CONSTRAINT fk_cart_user FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_cart_book FOREIGN KEY (book_id) REFERENCES books(id)
);

CREATE TABLE orders (
  id BIGINT PRIMARY KEY,
  order_no VARCHAR(64) NOT NULL UNIQUE,
  user_id BIGINT NOT NULL,
  status VARCHAR(32) NOT NULL,
  total_amount DECIMAL(10,2) NOT NULL,
  receiver_name VARCHAR(64) NOT NULL,
  receiver_phone VARCHAR(64) NOT NULL,
  receiver_address VARCHAR(255) NOT NULL,
  remark VARCHAR(255),
  deleted TINYINT NOT NULL DEFAULT 0,
  created_time DATETIME NOT NULL,
  updated_time DATETIME NOT NULL,
  KEY idx_orders_user (user_id),
  KEY idx_orders_status (status),
  KEY idx_orders_deleted (deleted),
  CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE order_items (
  id BIGINT PRIMARY KEY,
  order_id BIGINT NOT NULL,
  book_id BIGINT NOT NULL,
  book_name VARCHAR(255) NOT NULL,
  book_author VARCHAR(128),
  cover_url VARCHAR(255),
  quantity INT NOT NULL,
  price DECIMAL(10,2) NOT NULL,
  amount DECIMAL(10,2) NOT NULL,
  KEY idx_order_items_order (order_id),
  CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id),
  CONSTRAINT fk_order_items_book FOREIGN KEY (book_id) REFERENCES books(id)
);

CREATE TABLE payment_records (
  id BIGINT PRIMARY KEY,
  order_id BIGINT NOT NULL,
  pay_channel VARCHAR(64) NOT NULL,
  pay_status VARCHAR(32) NOT NULL,
  transaction_no VARCHAR(128),
  amount DECIMAL(10,2) NOT NULL,
  callback_content TEXT,
  paid_time DATETIME,
  created_time DATETIME NOT NULL,
  updated_time DATETIME NOT NULL,
  KEY idx_payment_order (order_id),
  CONSTRAINT fk_payment_order FOREIGN KEY (order_id) REFERENCES orders(id)
);

CREATE TABLE file_resources (
  id BIGINT PRIMARY KEY,
  business_type VARCHAR(64) NOT NULL,
  original_name VARCHAR(255) NOT NULL,
  stored_name VARCHAR(255) NOT NULL,
  storage_path VARCHAR(255) NOT NULL,
  access_url VARCHAR(255) NOT NULL,
  content_type VARCHAR(128),
  size_bytes BIGINT NOT NULL,
  uploader_id BIGINT,
  deleted TINYINT NOT NULL DEFAULT 0,
  created_time DATETIME NOT NULL,
  KEY idx_files_deleted (deleted),
  CONSTRAINT fk_file_uploader FOREIGN KEY (uploader_id) REFERENCES users(id)
);
