-- 推荐初始化入口（数据脚本）：dml.sql
-- 执行前请先完成 ddl.sql

SET NAMES utf8mb4;

INSERT INTO roles (id, name, code, description, created_time) VALUES
(1, '管理员', 'ADMIN', '系统管理员', NOW()),
(2, '普通用户', 'CUSTOMER', '商城用户', NOW());

INSERT INTO users (id, username, password_hash, display_name, phone, email, role_id, status, deleted, created_time, updated_time) VALUES
(1001, 'admin', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', '系统管理员', '13800000000', 'admin@bookstore.local', 1, 1, 0, NOW(), NOW()),
(1002, 'demo', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', '演示用户', '13900000000', 'demo@bookstore.local', 2, 1, 0, NOW(), NOW()),
(1003, 'lisi', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', '李四', '13700000000', 'lisi@bookstore.local', 2, 1, 0, NOW(), NOW());

INSERT INTO categories (id, parent_id, name, sort, status, deleted, created_time, updated_time) VALUES
(2001, 0, '文学', 1, 1, 0, NOW(), NOW()),
(2002, 2001, '小说', 1, 1, 0, NOW(), NOW()),
(2003, 0, '技术', 2, 1, 0, NOW(), NOW()),
(2004, 2003, 'Java', 1, 1, 0, NOW(), NOW());

INSERT INTO books (id, category_id, name, author, isbn, price, stock, cover_url, description, status, sales, deleted, created_time, updated_time) VALUES
(3001, 2002, '活着', '余华', '9787506365437', 39.90, 100, '/book_img/book1.jpg', '经典文学作品', 1, 12, 0, NOW(), NOW()),
(3002, 2004, 'Spring Boot 实战示例', '开源作者', '9787111544937', 69.00, 80, '/book_img/book1.jpg', '演示用技术图书', 1, 8, 0, NOW(), NOW()),
(3003, 2004, 'Java 并发编程实战', 'Brian Goetz', '9787111213826', 88.00, 50, '/book_img/book1.jpg', '高并发编程入门演示图书', 1, 5, 0, NOW(), NOW());

INSERT INTO cart_items (id, user_id, book_id, quantity, selected, deleted, created_time, updated_time) VALUES
(4001, 1002, 3001, 1, 1, 0, NOW(), NOW()),
(4002, 1002, 3002, 2, 1, 0, NOW(), NOW());

INSERT INTO orders (id, order_no, user_id, status, total_amount, receiver_name, receiver_phone, receiver_address, remark, deleted, created_time, updated_time) VALUES
(5001, 'OD202604120001', 1002, 'PAID', 177.90, '演示用户', '13900000000', '广东省深圳市南山区科苑路 1 号', '初始化演示订单', 0, NOW(), NOW());

INSERT INTO order_items (id, order_id, book_id, book_name, book_author, cover_url, quantity, price, amount) VALUES
(6001, 5001, 3001, '活着', '余华', '/book_img/book1.jpg', 1, 39.90, 39.90),
(6002, 5001, 3002, 'Spring Boot 实战示例', '开源作者', '/book_img/book1.jpg', 2, 69.00, 138.00);

INSERT INTO payment_records (id, order_id, pay_channel, pay_status, transaction_no, amount, callback_content, paid_time, created_time, updated_time) VALUES
(7001, 5001, 'ALIPAY', 'SUCCESS', 'TRADE202604120001', 177.90, '{"tradeStatus":"SUCCESS","source":"bootstrap"}', NOW(), NOW(), NOW());

INSERT INTO file_resources (id, business_type, original_name, stored_name, storage_path, access_url, content_type, size_bytes, uploader_id, deleted, created_time) VALUES
(8001, 'BOOK_COVER', 'book1.jpg', 'book1-demo.jpg', '/book_img/book1.jpg', '/book_img/book1.jpg', 'image/jpeg', 1024, 1001, 0, NOW());
