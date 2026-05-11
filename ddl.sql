-- 推荐初始化入口（结构脚本）：ddl.sql
-- 执行顺序：ddl.sql -> dml.sql

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

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
  id BIGINT PRIMARY KEY COMMENT '角色ID',
  name VARCHAR(64) NOT NULL COMMENT '角色名称',
  code VARCHAR(64) NOT NULL UNIQUE COMMENT '角色编码',
  description VARCHAR(255) DEFAULT NULL COMMENT '角色描述',
  created_time DATETIME NOT NULL COMMENT '创建时间'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '角色表';

CREATE TABLE users (
  id BIGINT PRIMARY KEY COMMENT '用户ID',
  username VARCHAR(64) NOT NULL UNIQUE COMMENT '登录用户名',
  password_hash VARCHAR(128) NOT NULL COMMENT '登录密码哈希',
  display_name VARCHAR(64) NOT NULL COMMENT '显示名称',
  phone VARCHAR(32) DEFAULT NULL COMMENT '手机号',
  email VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
  role_id BIGINT NOT NULL COMMENT '角色ID',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态（1启用，0禁用）',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记（0否，1是）',
  created_time DATETIME NOT NULL COMMENT '创建时间',
  updated_time DATETIME NOT NULL COMMENT '更新时间',
  KEY idx_users_role (role_id),
  KEY idx_users_status (status),
  KEY idx_users_deleted (deleted),
  CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles(id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户表';

CREATE TABLE categories (
  id BIGINT PRIMARY KEY COMMENT '分类ID',
  parent_id BIGINT NOT NULL DEFAULT 0 COMMENT '父级分类ID（0表示一级分类）',
  name VARCHAR(128) NOT NULL COMMENT '分类名称',
  sort INT NOT NULL DEFAULT 0 COMMENT '排序值（升序）',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态（1启用，0禁用）',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记（0否，1是）',
  created_time DATETIME NOT NULL COMMENT '创建时间',
  updated_time DATETIME NOT NULL COMMENT '更新时间',
  KEY idx_categories_parent (parent_id),
  KEY idx_categories_status (status),
  KEY idx_categories_deleted (deleted)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '图书分类表';

CREATE TABLE books (
  id BIGINT PRIMARY KEY COMMENT '图书ID',
  category_id BIGINT NOT NULL COMMENT '分类ID',
  name VARCHAR(255) NOT NULL COMMENT '图书名称',
  author VARCHAR(128) DEFAULT NULL COMMENT '作者',
  isbn VARCHAR(64) DEFAULT NULL COMMENT 'ISBN编号',
  price DECIMAL(10, 2) NOT NULL COMMENT '销售单价',
  stock INT NOT NULL DEFAULT 0 COMMENT '库存数量',
  cover_url VARCHAR(255) DEFAULT NULL COMMENT '封面访问地址',
  description TEXT COMMENT '图书简介',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态（1上架，0下架）',
  sales INT NOT NULL DEFAULT 0 COMMENT '累计销量',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记（0否，1是）',
  created_time DATETIME NOT NULL COMMENT '创建时间',
  updated_time DATETIME NOT NULL COMMENT '更新时间',
  KEY idx_books_category (category_id),
  KEY idx_books_status (status),
  KEY idx_books_deleted (deleted),
  CONSTRAINT fk_books_category FOREIGN KEY (category_id) REFERENCES categories(id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '图书表';

CREATE TABLE cart_items (
  id BIGINT PRIMARY KEY COMMENT '购物车项ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  book_id BIGINT NOT NULL COMMENT '图书ID',
  quantity INT NOT NULL COMMENT '购买数量',
  selected TINYINT NOT NULL DEFAULT 1 COMMENT '是否勾选（1是，0否）',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记（0否，1是）',
  created_time DATETIME NOT NULL COMMENT '创建时间',
  updated_time DATETIME NOT NULL COMMENT '更新时间',
  KEY idx_cart_user (user_id),
  KEY idx_cart_book (book_id),
  KEY idx_cart_selected (selected),
  KEY idx_cart_deleted (deleted),
  UNIQUE KEY uk_cart_user_book_deleted (user_id, book_id, deleted),
  CONSTRAINT fk_cart_user FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_cart_book FOREIGN KEY (book_id) REFERENCES books(id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '购物车项表';

CREATE TABLE orders (
  id BIGINT PRIMARY KEY COMMENT '订单ID',
  order_no VARCHAR(64) NOT NULL UNIQUE COMMENT '订单号',
  user_id BIGINT NOT NULL COMMENT '下单用户ID',
  status VARCHAR(32) NOT NULL COMMENT '订单状态（CREATED/PAID/CANCELLED/CONFIRMED）',
  total_amount DECIMAL(10, 2) NOT NULL COMMENT '订单总金额',
  receiver_name VARCHAR(64) NOT NULL COMMENT '收货人姓名',
  receiver_phone VARCHAR(64) NOT NULL COMMENT '收货人电话',
  receiver_address VARCHAR(255) NOT NULL COMMENT '收货地址',
  remark VARCHAR(255) DEFAULT NULL COMMENT '订单备注',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记（0否，1是）',
  created_time DATETIME NOT NULL COMMENT '创建时间',
  updated_time DATETIME NOT NULL COMMENT '更新时间',
  KEY idx_orders_user (user_id),
  KEY idx_orders_status (status),
  KEY idx_orders_deleted (deleted),
  CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '订单表';

CREATE TABLE order_items (
  id BIGINT PRIMARY KEY COMMENT '订单明细ID',
  order_id BIGINT NOT NULL COMMENT '订单ID',
  book_id BIGINT NOT NULL COMMENT '图书ID',
  book_name VARCHAR(255) NOT NULL COMMENT '下单时图书名称快照',
  book_author VARCHAR(128) DEFAULT NULL COMMENT '下单时作者快照',
  cover_url VARCHAR(255) DEFAULT NULL COMMENT '下单时封面地址快照',
  quantity INT NOT NULL COMMENT '购买数量',
  price DECIMAL(10, 2) NOT NULL COMMENT '下单时单价',
  amount DECIMAL(10, 2) NOT NULL COMMENT '明细金额',
  KEY idx_order_items_order (order_id),
  KEY idx_order_items_book (book_id),
  CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id),
  CONSTRAINT fk_order_items_book FOREIGN KEY (book_id) REFERENCES books(id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '订单明细表';

CREATE TABLE payment_records (
  id BIGINT PRIMARY KEY COMMENT '支付记录ID',
  order_id BIGINT NOT NULL COMMENT '订单ID',
  pay_channel VARCHAR(64) NOT NULL COMMENT '支付渠道',
  pay_status VARCHAR(32) NOT NULL COMMENT '支付状态',
  transaction_no VARCHAR(128) DEFAULT NULL COMMENT '三方交易流水号',
  amount DECIMAL(10, 2) NOT NULL COMMENT '支付金额',
  callback_content TEXT COMMENT '支付回调原文',
  paid_time DATETIME DEFAULT NULL COMMENT '支付完成时间',
  created_time DATETIME NOT NULL COMMENT '创建时间',
  updated_time DATETIME NOT NULL COMMENT '更新时间',
  KEY idx_payment_order (order_id),
  KEY idx_payment_status (pay_status),
  CONSTRAINT fk_payment_order FOREIGN KEY (order_id) REFERENCES orders(id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '支付记录表';

CREATE TABLE file_resources (
  id BIGINT PRIMARY KEY COMMENT '文件资源ID',
  business_type VARCHAR(64) NOT NULL COMMENT '业务类型',
  original_name VARCHAR(255) NOT NULL COMMENT '原始文件名',
  stored_name VARCHAR(255) NOT NULL COMMENT '存储文件名',
  storage_path VARCHAR(255) NOT NULL COMMENT '存储路径',
  access_url VARCHAR(255) NOT NULL COMMENT '访问地址',
  content_type VARCHAR(128) DEFAULT NULL COMMENT 'MIME类型',
  size_bytes BIGINT NOT NULL COMMENT '文件大小（字节）',
  uploader_id BIGINT DEFAULT NULL COMMENT '上传人用户ID',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记（0否，1是）',
  created_time DATETIME NOT NULL COMMENT '创建时间',
  KEY idx_files_uploader (uploader_id),
  KEY idx_files_deleted (deleted),
  CONSTRAINT fk_file_uploader FOREIGN KEY (uploader_id) REFERENCES users(id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '文件资源表';

SET FOREIGN_KEY_CHECKS = 1;
